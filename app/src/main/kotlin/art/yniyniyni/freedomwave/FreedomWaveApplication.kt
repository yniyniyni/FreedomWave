package art.yniyniyni.freedomwave

import android.app.Application
import art.yniyniyni.freedomwave.di.allModules
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class FreedomWaveApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initAndroidContext(applicationContext)
        startKoin {
            androidContext(this@FreedomWaveApplication)
            modules(allModules())
        }
    }
}
