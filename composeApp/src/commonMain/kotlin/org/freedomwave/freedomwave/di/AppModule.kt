package org.freedomwave.di

import org.freedomwave.data.api.buildHttpClient
import org.freedomwave.data.api.buildPlainHttpClient
import org.freedomwave.data.api.service.AuthService
import org.freedomwave.data.api.service.BandwidthService
import org.freedomwave.data.api.service.DashboardService
import org.freedomwave.data.api.service.HostService
import org.freedomwave.data.api.service.HwidService
import org.freedomwave.data.api.service.InfraBillingService
import org.freedomwave.data.api.service.ConfigProfileService
import org.freedomwave.data.api.service.NodeService
import org.freedomwave.data.api.service.SquadService
import org.freedomwave.data.api.service.SubHistoryService
import org.freedomwave.data.api.service.SubPageConfigService
import org.freedomwave.data.api.service.TemplateService
import org.freedomwave.data.api.service.UserService
import org.freedomwave.data.repository.AuthRepository
import org.freedomwave.data.repository.BandwidthRepository
import org.freedomwave.data.repository.DashboardRepository
import org.freedomwave.data.repository.HostRepository
import org.freedomwave.data.repository.HwidRepository
import org.freedomwave.data.repository.InfraBillingRepository
import org.freedomwave.data.repository.ConfigProfileRepository
import org.freedomwave.data.repository.NodeRepository
import org.freedomwave.data.repository.SquadRepository
import org.freedomwave.data.repository.SubHistoryRepository
import org.freedomwave.data.repository.SubPageConfigRepository
import org.freedomwave.data.repository.TemplateRepository
import org.freedomwave.data.repository.UserRepository
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import org.freedomwave.data.store.AppPreferences
import org.freedomwave.data.store.SecretStore
import org.freedomwave.data.store.createDataStore
import org.freedomwave.ui.feature.bandwidth.BandwidthViewModel
import org.freedomwave.ui.feature.dashboard.DashboardViewModel
import org.freedomwave.ui.feature.hosts.HostFormViewModel
import org.freedomwave.ui.feature.hosts.HostsViewModel
import org.freedomwave.ui.feature.infrabilling.InfraBillingViewModel
import org.freedomwave.ui.feature.login.LoginViewModel
import org.freedomwave.ui.feature.nodes.NodeFormViewModel
import org.freedomwave.ui.feature.nodes.NodesViewModel
import org.freedomwave.ui.feature.settings.SettingsViewModel
import org.freedomwave.ui.feature.squads.ExternalSquadEditViewModel
import org.freedomwave.ui.feature.squads.InternalSquadEditViewModel
import org.freedomwave.ui.feature.squads.SquadsViewModel
import org.freedomwave.ui.feature.users.UserDetailViewModel
import org.freedomwave.ui.feature.users.UserTrafficViewModel
import org.freedomwave.ui.feature.users.UsersViewModel
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
    viewModel { (userUuid: String) -> UserTrafficViewModel(get(), userUuid) }
}

fun allModules() = listOf(prefsModule, networkModule, repositoryModule, viewModelModule)
