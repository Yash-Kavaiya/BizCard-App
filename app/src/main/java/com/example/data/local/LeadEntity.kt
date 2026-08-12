package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.LeadSource
import com.example.data.model.LeadStatus
import com.example.data.model.RelationshipType

@Entity(tableName = "leads")
data class LeadEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fullName: String,
    val jobTitle: String = "",
    val company: String = "",
    val department: String = "",
    val phonePrimary: String = "",
    val phoneSecondary: String = "",
    val emailPrimary: String = "",
    val website: String = "",
    val linkedIn: String = "",
    val twitter: String = "",
    val address: String = "",
    val industry: String = "",
    val notes: String = "",
    val voiceNotes: String = "",
    val photoUri: String? = null,
    val cardImageUri: String? = null,
    val status: LeadStatus = LeadStatus.NEW,
    val source: LeadSource = LeadSource.SCAN,
    val relationshipType: RelationshipType = RelationshipType.PROSPECT,
    val tags: String = "", // Comma separated tags
    val isStarred: Boolean = false,
    val confidenceScore: Float = 0.95f,
    val reviewRequired: Boolean = false,
    val followUpDateTimestamp: Long? = null,
    val createdTimestamp: Long = System.currentTimeMillis(),
    val updatedTimestamp: Long = System.currentTimeMillis()
)
