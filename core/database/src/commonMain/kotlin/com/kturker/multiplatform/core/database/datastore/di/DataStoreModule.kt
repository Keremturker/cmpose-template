package com.kturker.multiplatform.core.database.datastore.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.kturker.multiplatform.core.database.datastore.getPlatformDataStore
import org.koin.core.annotation.Single

@Single
fun provideDataStore(): DataStore<Preferences> = getPlatformDataStore()
