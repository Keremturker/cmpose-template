package com.kturker.multiplatform.core.database.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import org.koin.mp.KoinPlatform.getKoin

fun createAndroidDataStore(context: Context): DataStore<Preferences> = createDataStore(
    producePath = { context.filesDir.resolve(DATA_STORE_FILE_NAME).absolutePath }
)

private lateinit var dataStore: DataStore<Preferences>

actual fun getPlatformDataStore(): DataStore<Preferences> {
    if (::dataStore.isInitialized.not()) {
        val context: Context = getKoin().get()
        dataStore = createAndroidDataStore(context)
    }
    return dataStore
}
