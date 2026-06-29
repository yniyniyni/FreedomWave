package org.freedomwave.ui.feature.infrabilling

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import org.freedomwave.data.repository.InfraBillingRepository
import org.freedomwave.domain.model.AvailableNode
import org.freedomwave.domain.model.BillRecord
import org.freedomwave.domain.model.BillingNode
import org.freedomwave.domain.model.BillingStats
import org.freedomwave.domain.model.InfraProvider
import freedomwave.composeapp.generated.resources.Res
import freedomwave.composeapp.generated.resources.infra_amount_invalid
import freedomwave.composeapp.generated.resources.infra_node_required
import freedomwave.composeapp.generated.resources.infra_provider_name_required
import freedomwave.composeapp.generated.resources.infra_provider_required
import org.freedomwave.ui.l10n.UiText
import org.freedomwave.ui.l10n.toUiText
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

/** Which modal form is currently open. */
enum class BillingDialog { ADD_PROVIDER, EDIT_PROVIDER, ADD_NODE, UPDATE_DATE, ADD_PAYMENT }

/** A pending destructive action awaiting confirmation. */
data class PendingDelete(val kind: Kind, val uuid: String, val label: String) {
    enum class Kind { PROVIDER, NODE, RECORD }
}

data class InfraBillingUiState(
    val activeTab: Int = 0, // 0 = Nodes, 1 = History, 2 = Providers
    val stats: BillingStats? = null,
    val billingNodes: List<BillingNode> = emptyList(),
    val availableNodes: List<AvailableNode> = emptyList(),
    val providers: List<InfraProvider> = emptyList(),
    val history: List<BillRecord> = emptyList(),
    val isLoading: Boolean = false,
    val error: UiText? = null,
    val actionError: UiText? = null,

    // Modal form state (shared across dialogs).
    val dialog: BillingDialog? = null,
    val dialogIsLoading: Boolean = false,
    val dialogError: UiText? = null,
    val formName: String = "",
    val formFavicon: String = "",
    val formLogin: String = "",
    val formAmount: String = "",
    val formProviderUuid: String? = null,
    val formNodeUuid: String? = null,
    val formDateMillis: Long = 0L,
    val editingProviderUuid: String? = null,
    val editingBillingNodeUuid: String? = null,

    val pendingDelete: PendingDelete? = null,
)

class InfraBillingViewModel(private val repo: InfraBillingRepository) : ViewModel() {

    private val _state = MutableStateFlow(InfraBillingUiState())
    val state: StateFlow<InfraBillingUiState> = _state.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            runCatching {
                coroutineScope {
                    val nodes     = async { repo.getBillingNodes() }
                    val providers = async { repo.getProviders() }
                    val history   = async { repo.getHistory() }
                    Triple(nodes.await(), providers.await(), history.await())
                }
            }.onSuccess { (nodesRes, providersRes, historyRes) ->
                val bundle = nodesRes.getOrNull()
                _state.update { s ->
                    s.copy(
                        isLoading      = false,
                        stats          = bundle?.stats ?: s.stats,
                        billingNodes   = bundle?.nodes ?: s.billingNodes,
                        availableNodes = bundle?.available ?: s.availableNodes,
                        providers      = providersRes.getOrDefault(s.providers),
                        history        = historyRes.getOrDefault(s.history),
                        error = (nodesRes.exceptionOrNull()
                            ?: providersRes.exceptionOrNull()
                            ?: historyRes.exceptionOrNull())?.toUiText(),
                    )
                }
            }.onFailure { e ->
                _state.update { it.copy(isLoading = false, error = e.toUiText()) }
            }
        }
    }

    fun setTab(index: Int) = _state.update { it.copy(activeTab = index) }
    fun clearActionError() = _state.update { it.copy(actionError = null) }

    // Dialog open/close

    /** Opens the add dialog appropriate for the active tab. */
    fun openAddDialog() {
        val now = Clock.System.now().toEpochMilliseconds()
        _state.update { s ->
            when (s.activeTab) {
                2 -> s.copy(
                    dialog = BillingDialog.ADD_PROVIDER, dialogError = null,
                    editingProviderUuid = null,
                    formName = "", formFavicon = "", formLogin = "",
                )
                1 -> s.copy(
                    dialog = BillingDialog.ADD_PAYMENT, dialogError = null,
                    formProviderUuid = s.providers.firstOrNull()?.uuid,
                    formAmount = "", formDateMillis = now,
                )
                else -> s.copy(
                    dialog = BillingDialog.ADD_NODE, dialogError = null,
                    formProviderUuid = s.providers.firstOrNull()?.uuid,
                    formNodeUuid = s.availableNodes.firstOrNull()?.uuid,
                    formDateMillis = now,
                )
            }
        }
    }

    fun openEditProvider(provider: InfraProvider) = _state.update {
        it.copy(
            dialog = BillingDialog.EDIT_PROVIDER, dialogError = null,
            editingProviderUuid = provider.uuid,
            formName = provider.name,
            formFavicon = provider.faviconLink.orEmpty(),
            formLogin = provider.loginUrl.orEmpty(),
        )
    }

    fun openUpdateDate(node: BillingNode) {
        val millis = org.freedomwave.util.parseInstant(node.nextBillingAt)?.toEpochMilliseconds()
            ?: Clock.System.now().toEpochMilliseconds()
        _state.update {
            it.copy(
                dialog = BillingDialog.UPDATE_DATE, dialogError = null,
                editingBillingNodeUuid = node.uuid, formDateMillis = millis,
            )
        }
    }

    fun dismissDialog() = _state.update { it.copy(dialog = null, dialogError = null, dialogIsLoading = false) }

    // Form field setters

    fun onName(v: String)        = _state.update { it.copy(formName = v, dialogError = null) }
    fun onFavicon(v: String)     = _state.update { it.copy(formFavicon = v) }
    fun onLogin(v: String)       = _state.update { it.copy(formLogin = v) }
    fun onAmount(v: String)      = _state.update { it.copy(formAmount = v, dialogError = null) }
    fun onProvider(uuid: String) = _state.update { it.copy(formProviderUuid = uuid, dialogError = null) }
    fun onNode(uuid: String)     = _state.update { it.copy(formNodeUuid = uuid, dialogError = null) }
    fun onDate(millis: Long)     = _state.update { it.copy(formDateMillis = millis) }

    // Submit

    fun submitDialog() {
        val s = _state.value
        when (s.dialog) {
            BillingDialog.ADD_PROVIDER, BillingDialog.EDIT_PROVIDER -> submitProvider()
            BillingDialog.ADD_NODE    -> submitBillingNode()
            BillingDialog.UPDATE_DATE -> submitUpdateDate()
            BillingDialog.ADD_PAYMENT -> submitPayment()
            null -> Unit
        }
    }

    private fun submitProvider() {
        val s = _state.value
        if (s.formName.isBlank()) {
            _state.update { it.copy(dialogError = UiText.Res(Res.string.infra_provider_name_required)) }; return
        }
        val favicon = s.formFavicon.trim().ifBlank { null }
        val login = s.formLogin.trim().ifBlank { null }
        runAction {
            if (s.editingProviderUuid != null)
                repo.updateProvider(s.editingProviderUuid, s.formName.trim(), favicon, login)
            else
                repo.createProvider(s.formName.trim(), favicon, login)
        }
    }

    private fun submitBillingNode() {
        val s = _state.value
        val provider = s.formProviderUuid
        val node = s.formNodeUuid
        if (provider == null) { _state.update { it.copy(dialogError = UiText.Res(Res.string.infra_provider_required)) }; return }
        if (node == null) { _state.update { it.copy(dialogError = UiText.Res(Res.string.infra_node_required)) }; return }
        runAction { repo.createBillingNode(provider, node, isoOf(s.formDateMillis)) }
    }

    private fun submitUpdateDate() {
        val s = _state.value
        val uuid = s.editingBillingNodeUuid ?: return
        runAction { repo.updateBillingNodeDate(uuid, isoOf(s.formDateMillis)) }
    }

    private fun submitPayment() {
        val s = _state.value
        val provider = s.formProviderUuid
        if (provider == null) { _state.update { it.copy(dialogError = UiText.Res(Res.string.infra_provider_required)) }; return }
        val amount = s.formAmount.trim().replace(',', '.').toDoubleOrNull()
        if (amount == null || amount < 0) { _state.update { it.copy(dialogError = UiText.Res(Res.string.infra_amount_invalid)) }; return }
        runAction { repo.createHistory(provider, amount, isoOf(s.formDateMillis)) }
    }

    /** Runs a repo mutation, then reloads on success; surfaces errors in-dialog. */
    private fun runAction(block: suspend () -> Result<Unit>) {
        viewModelScope.launch {
            _state.update { it.copy(dialogIsLoading = true, dialogError = null) }
            block()
                .onSuccess {
                    _state.update { it.copy(dialog = null, dialogIsLoading = false) }
                    load()
                }
                .onFailure { e -> _state.update { it.copy(dialogIsLoading = false, dialogError = e.toUiText()) } }
        }
    }

    // Delete

    fun requestDelete(pending: PendingDelete) = _state.update { it.copy(pendingDelete = pending) }
    fun dismissDelete() = _state.update { it.copy(pendingDelete = null) }

    fun confirmDelete() {
        val pending = _state.value.pendingDelete ?: return
        viewModelScope.launch {
            _state.update { it.copy(pendingDelete = null) }
            val result = when (pending.kind) {
                PendingDelete.Kind.PROVIDER -> repo.deleteProvider(pending.uuid)
                PendingDelete.Kind.NODE     -> repo.deleteBillingNode(pending.uuid)
                PendingDelete.Kind.RECORD   -> repo.deleteHistory(pending.uuid)
            }
            result
                .onSuccess { load() }
                .onFailure { e -> _state.update { it.copy(actionError = e.toUiText()) } }
        }
    }

    private fun isoOf(millis: Long): String =
        kotlinx.datetime.Instant.fromEpochMilliseconds(millis).toString()
}
