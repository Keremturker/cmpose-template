package com.kturker.multiplatform.core.update.di

import com.kturker.multiplatform.core.update.UpdateCheckStore
import com.kturker.multiplatform.core.update.getPlatformUpdateCheckStore
import org.koin.core.annotation.Single

@Single
fun provideUpdateCheckStore(): UpdateCheckStore = getPlatformUpdateCheckStore()
