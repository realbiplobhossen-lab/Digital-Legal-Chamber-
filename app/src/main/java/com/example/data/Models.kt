package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "clients")
data class Client(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phone: String,
    val nid: String = "",
    val email: String = "",
    val address: String = "",
    val profession: String = "",
    val totalFee: Double = 0.0,
    val paidFee: Double = 0.0,
    val photoUri: String? = null
)

@Entity(tableName = "cases")
data class LawCase(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val caseTitle: String,
    val caseNumber: String,
    val courtName: String,
    val judge: String = "",
    val oppositeParty: String = "",
    val advocate: String = "",
    val stageOfCase: String = "",
    val nextDate: String = "",
    val previousOrders: String = "",
    val timeline: String = "", // JSON or comma-separated milestones
    val witnessList: String = "",
    val evidenceList: String = "",
    val personalNotes: String = "",
    val clientName: String = "",
    val clientPhone: String = "",
    val isImportant: Boolean = false
)

@Entity(tableName = "tasks")
data class DailyTask(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val taskText: String,
    val date: String,
    val isCompleted: Boolean = false,
    val isVoiceGenerated: Boolean = false
)

@Entity(tableName = "drafts")
data class LegalDraft(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val draftContent: String,
    val lastUpdated: String
)

@Entity(tableName = "laws")
data class LawBook(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String, // e.g., Constitution, Penal Code, CPC, Specific Relief Act
    val section: String, // e.g., Section 420
    val title: String, // Section title
    val description: String, // Bangla explanation of section
    val punishment: String = "", // Punishment details
    val caseNotes: String = "" // Landmark references
)

@Entity(tableName = "caselaws")
data class CaseLaw(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val citation: String, // e.g., 72 DLR (AD) 123
    val title: String,
    val ratioDecidendi: String,
    val facts: String,
    val principles: String,
    val keywords: String
)

@Entity(tableName = "fees")
data class FeeRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val clientName: String,
    val caseNumber: String = "",
    val amount: Double,
    val purpose: String,
    val date: String,
    val description: String = ""
)

@Entity(tableName = "contacts")
data class LegalContact(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val role: String, // e.g., Advocate, Client, Court Clerk, Witness
    val phone: String,
    val address: String = "",
    val email: String = ""
)

