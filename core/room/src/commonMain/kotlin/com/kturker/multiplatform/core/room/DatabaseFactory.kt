package com.kturker.multiplatform.core.room

import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

expect fun getPlatformDatabaseBuilder(): RoomDatabase.Builder<AppDatabase>

// The same SQLite on Android and iOS (the bundled driver); queries run on Dispatchers.IO
fun buildAppDatabase(builder: RoomDatabase.Builder<AppDatabase>): AppDatabase = builder
    .setDriver(BundledSQLiteDriver())
    .setQueryCoroutineContext(Dispatchers.IO)
    .build()

internal const val DATABASE_FILE_NAME = "app.db"
