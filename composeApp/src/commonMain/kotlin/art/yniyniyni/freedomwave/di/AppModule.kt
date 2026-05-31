package art.yniyniyni.freedomwave.di

import art.yniyniyni.freedomwave.data.api.buildHttpClient
import art.yniyniyni.freedomwave.data.api.service.AuthService
import art.yniyniyni.freedomwave.data.api.service.DashboardService
import art.yniyniyni.freedomwave.data.api.service.HostService
import art.yniyniyni.freedomwave.data.api.service.NodeService
import art.yniyniyni.freedomwave.data.api.service.UserService
import art.yniyniyni.freedomwave.data.repository.AuthRepository
import art.yniyniyni.freedomwave.data.repository.DashboardRepository
import art.yniyniyni.freedomwave.data.repository.HostRepository
import art.yniyniyni.freedomwave.data.repository.NodeRepository
import art.yniyniyni.freedomwave.data.repository.UserRepository
import art.yniyniyni.freedomwave.data.store.AppPreferences
import art.yniyniyni.freedomwave.data.store.createDataStore
import art.yniyniyni.freedomwave.ui.feature.dashboard.DashboardViewModel
import art.yniyniyni.freedomwave.ui.feature.hosts.HostsViewModel
import art.yniyniyni.freedomwave.ui.feature.login.LoginViewModel
import art.yniyniyni.freedomwave.ui.feature.nodes.NodesViewModel
import art.yniyniyni.freedomwave.ui.feature.users.UsersViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val prefsModule = module {
    single { AppPreferences(createDataStore()) }
}

val networkModule = module {
    single { buildHttpClient(get()) }
    single { AuthService(get()) }
    single { DashboardService(get()) }
    single { UserService(get()) }
    single { NodeService(get()) }
    single { HostService(get()) }
}

val repositoryModule = module {
    single { AuthRepository(get(), get()) }
    single { DashboardRepository(get(), get()) }
    single { UserRepository(get(), get()) }
    single { NodeRepository(get(), get()) }
    single { HostRepository(get(), get()) }
}

val viewModelModule = module {
    viewModel { LoginViewModel(get()) }
    viewModel { DashboardViewModel(get()) }
    viewModel { UsersViewModel(get()) }
    viewModel { NodesViewModel(get()) }
    viewModel { HostsViewModel(get()) }
}

fun allModules() = listOf(prefsModule, networkModule, repositoryModule, viewModelModule)
