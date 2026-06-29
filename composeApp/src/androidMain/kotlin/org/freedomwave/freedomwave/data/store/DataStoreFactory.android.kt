package org.freedomwave.data.store

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import org.freedomwave.androidAppContext
import okio.Path.Companion.toPath
import java.io.File

actual fun createDataStore(): DataStore<Preferences> =
    PreferenceDataStoreFactory.createWithPath(
        produceFile = {
            File(androidAppContext.filesDir, "settings.preferences_pb")
                .absolutePath.toPath()
        }
    )
