package com.lifeos.app.feature.selfbelief.domain

import kotlinx.coroutines.flow.Flow

interface SelfBeliefRepository {
    fun observeAll(): Flow<List<SelfBeliefReflection>>
    suspend fun getById(id: String): SelfBeliefReflection?
    suspend fun upsert(reflection: SelfBeliefReflection)
    suspend fun delete(id: String)
    suspend fun allStrengths(): List<String>
}
