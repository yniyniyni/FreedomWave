package art.yniyniyni.freedomwave.data.store

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences

// TODO Phase 1: implement using NSFileManager + PreferenceDataStoreFactory.createWithPath
actual fun createDataStore(): DataStore<Preferences> {
    throw NotImplementedError("iOS DataStore not implemented yet")
}
