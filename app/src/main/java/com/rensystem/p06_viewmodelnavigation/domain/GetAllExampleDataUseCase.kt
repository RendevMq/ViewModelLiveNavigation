package com.rensystem.p06_viewmodelnavigation.domain

import javax.inject.Inject

class GetAllExampleDataUseCase @Inject constructor(
    private val repository: ExampleRepository
) {
    suspend operator fun invoke(): List<String> {
        return repository.getAllList().sortedBy {
            it.substringAfterLast(" ").toInt()
        }
    }
}