package com.lifeos.app.feature.reflection.domain

import kotlinx.coroutines.flow.Flow

interface ReflectionRepository {
    fun observeAll(): Flow<List<ReflectionEntry>>
    fun observeByType(type: ReflectionTemplateType): Flow<List<ReflectionEntry>>
    suspend fun getById(id: String): ReflectionEntry?
    suspend fun upsert(entry: ReflectionEntry)
    suspend fun delete(id: String)
}
