package com.kturker.multiplatform.core.room

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import org.koin.mp.KoinPlatform.getKoin

actual fun getPlatformDatabaseBuilder(): RoomDatabase.Builder<AppDatabase> {
    val context: Context = getKoin().get<Context>().applicationContext
    return Room.databaseBuilder<AppDatabase>(
        context = context,
        name = context.getDatabasePath(DATABASE_FILE_NAME).absolutePath
    )
}
