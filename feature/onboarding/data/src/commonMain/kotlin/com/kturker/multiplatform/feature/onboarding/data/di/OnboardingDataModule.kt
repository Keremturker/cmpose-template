package com.kturker.multiplatform.feature.onboarding.data.di

import com.kturker.multiplatform.core.network.di.NetworkModule
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module

@Module(includes = [NetworkModule::class])
@ComponentScan("com.kturker.multiplatform.feature.onboarding.data")
class OnboardingDataModule
