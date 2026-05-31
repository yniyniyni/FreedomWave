package art.yniyniyni.freedomwave

import android.content.Context

internal lateinit var androidAppContext: Context
    private set

fun initAndroidContext(context: Context) {
    androidAppContext = context.applicationContext
}
