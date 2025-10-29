package com.rensystem.p06_viewmodelnavigation.core.di

import com.rensystem.p06_viewmodelnavigation.data.ExampleRepositoryImpl
import com.rensystem.p06_viewmodelnavigation.data.MockDataService
import com.rensystem.p06_viewmodelnavigation.domain.ExampleRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ExampleModule {
    @Provides
    @Singleton
    fun provideExampleRepository (
        mockSevice: MockDataService
    ) : ExampleRepository {
        return ExampleRepositoryImpl(mockSevice)
    }
}
