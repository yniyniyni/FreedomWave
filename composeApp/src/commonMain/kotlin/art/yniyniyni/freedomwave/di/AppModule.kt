package art.yniyniyni.freedomwave.di

import art.yniyniyni.freedomwave.data.api.buildHttpClient
import art.yniyniyni.freedomwave.data.api.buildPlainHttpClient
import art.yniyniyni.freedomwave.data.api.service.AuthService
import art.yniyniyni.freedomwave.data.api.service.BandwidthService
import art.yniyniyni.freedomwave.data.api.service.DashboardService
import art.yniyniyni.freedomwave.data.api.service.HostService
import art.yniyniyni.freedomwave.data.api.service.HwidService
import art.yniyniyni.freedomwave.data.api.service.InfraBillingService
import art.yniyniyni.freedomwave.data.api.service.ConfigProfileService
import art.yniyniyni.freedomwave.data.api.service.NodeService
import art.yniyniyni.freedomwave.data.api.service.SquadService
import art.yniyniyni.freedomwave.data.api.service.SubHistoryService
import art.yniyniyni.freedomwave.data.api.service.SubPageConfigService
import art.yniyniyni.freedomwave.data.api.service.TemplateService
import art.yniyniyni.freedomwave.data.api.service.UserService
import art.yniyniyni.freedomwave.data.repository.AuthRepository
import art.yniyniyni.freedomwave.data.repository.BandwidthRepository
import art.yniyniyni.freedomwave.data.repository.DashboardRepository
import art.yniyniyni.freedomwave.data.repository.HostRepository
import art.yniyniyni.freedomwave.data.repository.HwidRepository
import art.yniyniyni.freedomwave.data.repository.InfraBillingRepository
import art.yniyniyni.freedomwave.data.repository.ConfigProfileRepository
import art.yniyniyni.freedomwave.data.repository.NodeRepository
import art.yniyniyni.freedomwave.data.repository.SquadRepository
import art.yniyniyni.freedomwave.data.repository.SubHistoryRepository
import art.yniyniyni.freedomwave.data.repository.SubPageConfigRepository
import art.yniyniyni.freedomwave.data.repository.TemplateRepository
import art.yniyniyni.freedomwave.data.repository.UserRepository
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import art.yniyniyni.freedomwave.data.store.AppPreferences
import art.yniyniyni.freedomwave.data.store.SecretStore
import art.yniyniyni.freedomwave.data.store.createDataStore
import art.yniyniyni.freedomwave.ui.feature.bandwidth.BandwidthViewModel
import art.yniyniyni.freedomwave.ui.feature.dashboard.DashboardViewModel
import art.yniyniyni.freedomwave.ui.feature.hosts.HostFormViewModel
import art.yniyniyni.freedomwave.ui.feature.hosts.HostsViewModel
import art.yniyniyni.freedomwave.ui.feature.infrabilling.InfraBillingViewModel
import art.yniyniyni.freedomwave.ui.feature.login.LoginViewModel
import art.yniyniyni.freedomwave.ui.feature.nodes.NodeFormViewModel
import art.yniyniyni.freedomwave.ui.feature.nodes.NodesViewModel
import art.yniyniyni.freedomwave.ui.feature.settings.SettingsViewModel
import art.yniyniyni.freedomwave.ui.feature.squads.ExternalSquadEditViewModel
import art.yniyniyni.freedomwave.ui.feature.squads.InternalSquadEditViewModel
import art.yniyniyni.freedomwave.ui.feature.squads.SquadsViewModel
import art.yniyniyni.freedomwave.ui.feature.users.UserDetailViewModel
import art.yniyniyni.freedomwave.ui.feature.users.UsersViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module

val prefsModule = module {
    single<DataStore<Preferences>> { createDataStore() }
    single { SecretStore() }
    single { AppPreferences(get(), get()) }
}

val networkModule = module {
    // Authenticated client (panel API)
    single { buildHttpClient(get()) }
    // Plain client (no auth — third-party calls like ipwho.is)
    single(qualifier = org.koin.core.qualifier.named("plain")) { buildPlainHttpClient() }

    // Plain (no-auth) client: verification sets the bearer explicitly per request.
    single { AuthService(get(qualifier = org.koin.core.qualifier.named("plain"))) }
    single { BandwidthService(get(), get()) }
    single { DashboardService(get(), get()) }
    single { UserService(get(), get()) }
    single { NodeService(get(), get()) }
    single { ConfigProfileService(get(), get()) }
    single { HostService(get(), get()) }
    single { SquadService(get(), get()) }
    single { InfraBillingService(get(), get()) }
    single { HwidService(get(), get()) }
    single { TemplateService(get(), get()) }
    single { SubPageConfigService(get(), get()) }
    single { SubHistoryService(
        panelClient = get(),
        plainClient = get(qualifier = org.koin.core.qualifier.named("plain")),
        prefs = get()
    ) }
}

val repositoryModule = module {
    single { AuthRepository(get(), get(), get()) }
    single { BandwidthRepository(get(), get()) }
    single { DashboardRepository(get(), get(), get()) }
    single { UserRepository(get(), get()) }
    single { NodeRepository(get(), get()) }
    single { ConfigProfileRepository(get(), get()) }
    single { HostRepository(get(), get()) }
    single { SquadRepository(get(), get()) }
    single { InfraBillingRepository(get(), get()) }
    single { HwidRepository(get(), get()) }
    single { SubHistoryRepository(get(), get()) }
    single { TemplateRepository(get(), get()) }
    single { SubPageConfigRepository(get(), get()) }
}

val viewModelModule = module {
    viewModel { BandwidthViewModel(get()) }
    viewModel { LoginViewModel(get()) }
    viewModel { DashboardViewModel(get()) }
    viewModel { UsersViewModel(get(), get(), get()) }
    viewModel { NodesViewModel(get()) }
    viewModel { (uuid: String?) -> NodeFormViewModel(uuid, get(), get()) }
    viewModel { HostsViewModel(get()) }
    viewModel { (uuid: String?) -> HostFormViewModel(uuid, get(), get(), get(), get()) }
    viewModel { SettingsViewModel(get(), get()) }
    viewModel { SquadsViewModel(get()) }
    viewModel { InfraBillingViewModel(get()) }
    viewModel { (uuid: String) -> InternalSquadEditViewModel(uuid, get(), get(), get()) }
    viewModel { (uuid: String) -> ExternalSquadEditViewModel(uuid, get(), get(), get(), get()) }
    viewModel { (userUuid: String) ->
        UserDetailViewModel(
            userUuid             = userUuid,
            userRepository       = get(),
            hwidRepository       = get(),
            subHistoryRepository = get(),
        )
    }
}

fun allModules() = listOf(prefsModule, networkModule, repositoryModule, viewModelModule)
