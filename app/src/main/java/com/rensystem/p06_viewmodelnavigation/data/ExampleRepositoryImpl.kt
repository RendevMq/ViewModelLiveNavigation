package com.rensystem.p06_viewmodelnavigation.data

import com.rensystem.p06_viewmodelnavigation.domain.ExampleRepository
import kotlinx.coroutines.delay
import javax.inject.Inject

class ExampleRepositoryImpl @Inject constructor(
    private val apiService: MockDataService
) : ExampleRepository {
    override suspend fun getAllList(): List<String> {
        delay(2000)
        return apiService.getMockData()
    }
}