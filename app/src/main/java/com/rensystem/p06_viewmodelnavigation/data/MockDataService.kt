package com.rensystem.p06_viewmodelnavigation.data

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MockDataService @Inject constructor(){
    fun getMockData(): List<String> {
        return listOf(
            "Mock Data 15",
            "Mock Data 33",
            "Mock Data 2",
            "Mock Data 6",
            "Mock Data 12"
        )
    }
}