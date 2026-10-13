package com.kturker.multiplatform.core.review.di

import com.kturker.multiplatform.core.review.ReviewCounterStore
import com.kturker.multiplatform.core.review.getPlatformReviewCounterStore
import org.koin.core.annotation.Single

@Single
fun provideReviewCounterStore(): ReviewCounterStore = getPlatformReviewCounterStore()
