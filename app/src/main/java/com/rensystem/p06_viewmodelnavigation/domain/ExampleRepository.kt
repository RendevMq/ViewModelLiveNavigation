package com.rensystem.p06_viewmodelnavigation.domain

interface ExampleRepository {
    suspend fun getAllList() : List<String>
}