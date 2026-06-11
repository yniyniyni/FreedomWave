package art.yniyniyni.freedomwave.di

import art.yniyniyni.freedomwave.data.api.buildHttpClient
import art.yniyniyni.freedomwave.data.api.buildPlainHttpClient
import art.yniyniyni.freedomwave.data.api.service.AuthService
import art.yniyniyni.freedomwave.data.api.service.BandwidthService
import art.yniyniyni.freedomwave.data.api.service.DashboardService
import art.yniyniyni.freedomwave.data.api.service.HostService
import art.yniyniyni.freedomwave.data.api.service.HwidService
import art.yniyniyni.freedomwave.data.api.service.NodeService
import art.yniyniyni.freedomwave.data.api.service.SquadService
import art.yniyniyni.freedomwave.data.api.service.SubHistoryService
import art.yniyniyni.freedomwave.data.api.service.UserService
import art.yniyniyni.freedomwave.data.repository.AuthRepository
import art.yniyniyni.freedomwave.data.repository.BandwidthRepository
import art.yniyniyni.freedomwave.data.repository.DashboardRepository
import art.yniyniyni.freedomwave.data.repository.HostRepository
import art.yniyniyni.freedomwave.data.repository.HwidRepository
import art.yniyniyni.freedomwave.data.repository.NodeRepository
import art.yniyniyni.freedomwave.data.repository.SquadRepository
import art.yniyniyni.freedomwave.data.repository.SubHistoryRepository
import art.yniyniyni.freedomwave.data.repository.UserRepository
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import art.yniyniyni.freedomwave.data.store.AppPreferences
import art.yniyniyni.freedomwave.data.store.SecretStore
import art.yniyniyni.freedomwave.data.store.createDataStore
import art.yniyniyni.freedomwave.ui.feature.bandwidth.BandwidthViewModel
import art.yniyniyni.freedomwave.ui.feature.dashboard.DashboardViewModel
import art.yniyniyni.freedomwave.ui.feature.hosts.HostsViewModel
import art.yniyniyni.freedomwave.ui.feature.login.LoginViewModel
import art.yniyniyni.freedomwave.ui.feature.nodes.NodesViewModel
import art.yniyniyni.freedomwave.ui.feature.settings.SettingsViewModel
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

    single { AuthService(get()) }
    single { BandwidthService(get()) }
    single { DashboardService(get()) }
    single { UserService(get()) }
    single { NodeService(get()) }
    single { HostService(get()) }
    single { SquadService(get()) }
    single { HwidService(get()) }
    single { SubHistoryService(
        panelClient = get(),
        plainClient = get(qualifier = org.koin.core.qualifier.named("plain"))
    ) }
}

val repositoryModule = module {
    single { AuthRepository(get(), get(), get()) }
    single { BandwidthRepository(get(), get()) }
    single { DashboardRepository(get(), get(), get()) }
    single { UserRepository(get(), get()) }
    single { NodeRepository(get(), get()) }
    single { HostRepository(get(), get()) }
    single { SquadRepository(get(), get()) }
    single { HwidRepository(get(), get()) }
    single { SubHistoryRepository(get(), get()) }
}

val viewModelModule = module {
    viewModel { BandwidthViewModel(get()) }
    viewModel { LoginViewModel(get()) }
    viewModel { DashboardViewModel(get()) }
    viewModel { UsersViewModel(get(), get(), get()) }
    viewModel { NodesViewModel(get()) }
    viewModel { HostsViewModel(get()) }
    viewModel { SettingsViewModel(get(), get()) }
    viewModel { SquadsViewModel(get()) }
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
