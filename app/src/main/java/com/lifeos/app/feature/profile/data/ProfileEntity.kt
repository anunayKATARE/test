package com.lifeos.app.feature.profile.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.lifeos.app.core.demo.DemoTemplate
import com.lifeos.app.core.demo.Profile
import java.time.Instant

@Entity(tableName = "profiles")
data class ProfileEntity(
    @PrimaryKey val id: String,
    val name: String,
    val isDemo: Boolean,
    val demoTemplate: String?,
    val createdAt: Long,
)

fun ProfileEntity.toDomain() = Profile(
    id = id,
    name = name,
    isDemo = isDemo,
    demoTemplate = DemoTemplate.fromId(demoTemplate),
    createdAt = Instant.ofEpochMilli(createdAt),
)

fun Profile.toEntity() = ProfileEntity(
    id = id,
    name = name,
    isDemo = isDemo,
    demoTemplate = demoTemplate?.id,
    createdAt = createdAt.toEpochMilli(),
)
