package org.freedomwave.ui.feature.hosts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import org.freedomwave.data.repository.ConfigProfileRepository
import org.freedomwave.data.repository.HostRepository
import org.freedomwave.data.repository.NodeRepository
import org.freedomwave.data.repository.TemplateRepository
import org.freedomwave.domain.model.ConfigProfile
import org.freedomwave.domain.model.Node
import org.freedomwave.domain.model.XrayTemplate
import org.freedomwave.ui.l10n.UiText
import org.freedomwave.ui.l10n.toUiText
import freedomwave.composeapp.generated.resources.Res
import freedomwave.composeapp.generated.resources.hosts_form_address_error
import freedomwave.composeapp.generated.resources.hosts_form_port_error
import freedomwave.composeapp.generated.resources.hosts_form_remark_error
import freedomwave.composeapp.generated.resources.hosts_form_server_desc_error
import freedomwave.composeapp.generated.resources.hosts_form_tag_error
import freedomwave.composeapp.generated.resources.hosts_form_vless_route_error
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HostFormUiState(
    val isEdit: Boolean = false,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val actionError: UiText? = null,

    val profiles: List<ConfigProfile> = emptyList(),
    val nodes: List<Node> = emptyList(),
    val xrayTemplates: List<XrayTemplate> = emptyList(),

    // Text fields
    val remark: String = "",
    val address: String = "",
    val port: String = "",
    val tags: String = "",
    val sni: String = "",
    val host: String = "",
    val path: String = "",
    val serverDescription: String = "",
    val vlessRouteId: String = "",

    val selectedProfileUuid: String? = null,
    val selectedInboundUuid: String? = null,

    val securityLayer: String = "DEFAULT",
    val alpn: String? = null,
    val fingerprint: String? = null,

    // Boolean toggles
    val isDisabled: Boolean = false,
    val isHidden: Boolean = false,
    val overrideSniFromAddress: Boolean = false,
    val keepSniBlank: Boolean = false,
    val pinnedPeerCertSha256: String = "",
    val verifyPeerCertByName: String = "",
    val mihomoIpVersion: String? = null,
    val shuffleHost: Boolean = false,
    val mihomoX25519: Boolean = false,

    val selectedNodeUuids: Set<String> = emptySet(),
    val xrayTemplateUuid: String? = null,
) {
    val remarkError: UiText?
        get() = if (remark.isNotBlank() && !remarkValid(remark)) UiText.Res(Res.string.hosts_form_remark_error) else null

    val addressError: UiText?
        get() = if (address.isNotBlank() && !addressValid(address)) UiText.Res(Res.string.hosts_form_address_error) else null

    val portError: UiText?
        get() = if (port.isNotBlank() && portOrNull(port) == null) UiText.Res(Res.string.hosts_form_port_error) else null

    val tagError: UiText?
        get() = if (tags.isNotBlank() && !tagsValid(tags)) UiText.Res(Res.string.hosts_form_tag_error) else null

    val vlessRouteIdError: UiText?
        get() = if (vlessRouteId.isNotBlank() && !vlessRouteIdValid(vlessRouteId)) UiText.Res(Res.string.hosts_form_vless_route_error) else null

    val serverDescriptionError: UiText?
        get() = if (serverDescription.isNotBlank() && !serverDescriptionValid(serverDescription)) UiText.Res(Res.string.hosts_form_server_desc_error) else null

    val selectedInboundTag: String?
        get() = profiles.flatMap { it.inbounds }.find { it.uuid == selectedInboundUuid }?.tag

    val canSave: Boolean
        get() = remarkValid(remark) && addressValid(address) && portOrNull(port) != null &&
            tagsValid(tags) && vlessRouteIdValid(vlessRouteId) &&
            serverDescriptionValid(serverDescription) &&
            selectedProfileUuid != null && selectedInboundUuid != null && !isSaving
}

class HostFormViewModel(
    private val hostUuid: String?,
    private val hostRepo: HostRepository,
    private val configRepo: ConfigProfileRepository,
    private val nodeRepo: NodeRepository,
    private val templateRepo: TemplateRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(HostFormUiState(isEdit = hostUuid != null, isLoading = true))
    val state: StateFlow<HostFormUiState> = _state.asStateFlow()

    init { load() }

    private fun load() {
        viewModelScope.launch {
            val profiles = configRepo.getProfiles().getOrDefault(emptyList())
            val nodes = nodeRepo.getNodes().getOrDefault(emptyList())
            val templates = templateRepo.getXrayTemplates().getOrDefault(emptyList())
            _state.update { it.copy(profiles = profiles, nodes = nodes, xrayTemplates = templates) }

            if (hostUuid != null) {
                hostRepo.getHost(hostUuid).onSuccess { h ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            remark = h.remark,
                            address = h.address,
                            port = h.port.toString(),
                            tags = formatHostTags(h.tags),
                            sni = h.sni ?: "",
                            host = h.host ?: "",
                            path = h.path ?: "",
                            serverDescription = h.serverDescription ?: "",
                            vlessRouteId = h.vlessRouteId?.toString() ?: "",
                            selectedProfileUuid = h.configProfileUuid,
                            selectedInboundUuid = h.configProfileInboundUuid,
                            securityLayer = h.securityLayer,
                            alpn = h.alpn,
                            fingerprint = h.fingerprint,
                            isDisabled = h.isDisabled,
                            isHidden = h.isHidden,
                            overrideSniFromAddress = h.overrideSniFromAddress,
                            keepSniBlank = h.keepSniBlank,
                            pinnedPeerCertSha256 = h.pinnedPeerCertSha256 ?: "",
                            verifyPeerCertByName = h.verifyPeerCertByName ?: "",
                            mihomoIpVersion = h.mihomoIpVersion,
                            shuffleHost = h.shuffleHost,
                            mihomoX25519 = h.mihomoX25519,
                            selectedNodeUuids = h.nodes.toSet(),
                            xrayTemplateUuid = h.xrayJsonTemplateUuid,
                        )
                    }
                }.onFailure { e ->
                    _state.update { it.copy(isLoading = false, actionError = e.toUiText()) }
                }
            } else {
                _state.update { it.copy(isLoading = false, port = "443", securityLayer = "DEFAULT") }
            }
        }
    }

    fun onRemark(v: String)            = _state.update { it.copy(remark = v, actionError = null) }
    fun onAddress(v: String)           = _state.update { it.copy(address = v, actionError = null) }
    fun onPort(v: String)              = _state.update { it.copy(port = v.filter { c -> c.isDigit() }) }
    fun onTags(v: String)              = _state.update { it.copy(tags = v) }
    fun onSni(v: String)               = _state.update { it.copy(sni = v) }
    fun onHost(v: String)              = _state.update { it.copy(host = v) }
    fun onPath(v: String)              = _state.update { it.copy(path = v) }
    fun onServerDescription(v: String) = _state.update { it.copy(serverDescription = v) }
    fun onVlessRouteId(v: String)      = _state.update { it.copy(vlessRouteId = v.filter { c -> c.isDigit() }) }

    fun setDisabled(v: Boolean)            = _state.update { it.copy(isDisabled = v) }
    fun setHidden(v: Boolean)              = _state.update { it.copy(isHidden = v) }
    fun setOverrideSni(v: Boolean)         = _state.update { it.copy(overrideSniFromAddress = v) }
    fun setKeepSniBlank(v: Boolean)        = _state.update { it.copy(keepSniBlank = v) }
    fun onPinnedPeerCertSha256(v: String)  = _state.update { it.copy(pinnedPeerCertSha256 = v) }
    fun onVerifyPeerCertByName(v: String)  = _state.update { it.copy(verifyPeerCertByName = v) }
    fun setMihomoIpVersion(v: String?)     = _state.update { it.copy(mihomoIpVersion = v) }
    fun setShuffleHost(v: Boolean)         = _state.update { it.copy(shuffleHost = v) }
    fun setMihomo(v: Boolean)              = _state.update { it.copy(mihomoX25519 = v) }

    fun setSecurityLayer(v: String)        = _state.update { it.copy(securityLayer = v) }
    fun setAlpn(v: String?)                = _state.update { it.copy(alpn = v) }
    fun setFingerprint(v: String?)         = _state.update { it.copy(fingerprint = v) }
    fun selectXrayTemplate(v: String?)     = _state.update { it.copy(xrayTemplateUuid = v) }

    fun selectInbound(profileUuid: String, inboundUuid: String) =
        _state.update { it.copy(selectedProfileUuid = profileUuid, selectedInboundUuid = inboundUuid) }

    fun toggleNode(uuid: String) = _state.update { s ->
        s.copy(selectedNodeUuids = if (uuid in s.selectedNodeUuids) s.selectedNodeUuids - uuid else s.selectedNodeUuids + uuid)
    }

    fun clearError() = _state.update { it.copy(actionError = null) }

    fun submit(onSaved: () -> Unit) {
        val s = _state.value
        if (!s.canSave) return
        val input = HostFormInput(
            configProfileUuid = s.selectedProfileUuid!!,
            configProfileInboundUuid = s.selectedInboundUuid!!,
            remark = s.remark,
            address = s.address,
            port = portOrNull(s.port)!!,
            path = s.path,
            sni = s.sni,
            host = s.host,
            alpn = s.alpn,
            fingerprint = s.fingerprint,
            securityLayer = s.securityLayer,
            serverDescription = serverDescriptionOrNull(s.serverDescription),
            tags = parseHostTags(s.tags),
            isDisabled = s.isDisabled,
            isHidden = s.isHidden,
            overrideSniFromAddress = s.overrideSniFromAddress,
            keepSniBlank = s.keepSniBlank,
            pinnedPeerCertSha256 = s.pinnedPeerCertSha256,
            verifyPeerCertByName = s.verifyPeerCertByName,
            mihomoIpVersion = s.mihomoIpVersion,
            vlessRouteId = vlessRouteIdOrNull(s.vlessRouteId),
            shuffleHost = s.shuffleHost,
            mihomoX25519 = s.mihomoX25519,
            nodes = s.selectedNodeUuids.toList(),
            xrayJsonTemplateUuid = s.xrayTemplateUuid,
        )
        _state.update { it.copy(isSaving = true, actionError = null) }
        viewModelScope.launch {
            val result = if (hostUuid == null) hostRepo.createHost(buildCreateRequest(input))
            else hostRepo.updateHost(buildUpdateRequest(hostUuid, input))
            result.onSuccess {
                _state.update { it.copy(isSaving = false) }
                onSaved()
            }.onFailure { e ->
                _state.update { it.copy(isSaving = false, actionError = e.toUiText()) }
            }
        }
    }
}
