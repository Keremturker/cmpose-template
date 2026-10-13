package com.kturker.multiplatform.core.room.di

import com.kturker.multiplatform.core.room.AppDatabase
import com.kturker.multiplatform.core.room.buildAppDatabase
import com.kturker.multiplatform.core.room.getPlatformDatabaseBuilder
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
@ComponentScan("com.kturker.multiplatform.core.room")
class RoomModule

// One database for the whole app, opened on first use
@Single
fun provideAppDatabase(): AppDatabase = buildAppDatabase(getPlatformDatabaseBuilder())
