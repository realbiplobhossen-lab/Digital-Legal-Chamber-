package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    // --- Clients ---
    @Query("SELECT * FROM clients ORDER BY name ASC")
    fun getAllClients(): Flow<List<Client>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClient(client: Client): Long

    @Delete
    suspend fun deleteClient(client: Client)

    @Query("SELECT * FROM clients WHERE id = :id")
    suspend fun getClientById(id: Long): Client?

    // --- Cases ---
    @Query("SELECT * FROM cases ORDER BY id DESC")
    fun getAllCases(): Flow<List<LawCase>>

    @Query("SELECT * FROM cases WHERE nextDate != '' ORDER BY nextDate ASC")
    fun getUpcomingCases(): Flow<List<LawCase>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCase(lawCase: LawCase): Long

    @Delete
    suspend fun deleteCase(lawCase: LawCase)

    @Query("SELECT * FROM cases WHERE id = :id")
    suspend fun getCaseById(id: Long): LawCase?

    // --- Daily Tasks (আজকের করণীয় কাজ) ---
    @Query("SELECT * FROM tasks ORDER BY id DESC")
    fun getAllTasks(): Flow<List<DailyTask>>

    @Query("SELECT * FROM tasks WHERE date = :date ORDER BY id DESC")
    fun getTasksForDate(date: String): Flow<List<DailyTask>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: DailyTask): Long

    @Query("UPDATE tasks SET isCompleted = :completed WHERE id = :id")
    suspend fun updateTaskStatus(id: Long, completed: Boolean)

    @Delete
    suspend fun deleteTask(task: DailyTask)

    @Query("DELETE FROM tasks")
    suspend fun clearAllTasks()

    // --- Legal Drafts ---
    @Query("SELECT * FROM drafts ORDER BY lastModified DESC")
    fun getAllDrafts(): Flow<List<LegalDraft>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDraft(draft: LegalDraft): Long

    @Delete
    suspend fun deleteDraft(draft: LegalDraft)

    // --- Law Book ---
    @Query("SELECT * FROM law_books")
    fun getAllLawSections(): Flow<List<LawBook>>

    @Query("SELECT * FROM law_books WHERE actName LIKE '%' || :query || '%' OR section LIKE '%' || :query || '%' OR title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%'")
    fun searchLaws(query: String): Flow<List<LawBook>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLawSection(law: LawBook): Long

    // --- Case Law Database ---
    @Query("SELECT * FROM caselaws")
    fun getAllCaseLaws(): Flow<List<CaseLaw>>

    @Query("SELECT * FROM caselaws WHERE citation LIKE '%' || :query || '%' OR title LIKE '%' || :query || '%' OR ratioDecidendi LIKE '%' || :query || '%' OR keywords LIKE '%' || :query || '%'")
    fun searchCaseLaws(query: String): Flow<List<CaseLaw>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCaseLaw(caseLaw: CaseLaw): Long

    // --- Fees ---
    @Query("SELECT * FROM fees ORDER BY date DESC")
    fun getAllFees(): Flow<List<FeeRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFeeRecord(fee: FeeRecord): Long

    @Delete
    suspend fun deleteFeeRecord(fee: FeeRecord)

    // --- Contacts ---
    @Query("SELECT * FROM contacts ORDER BY name ASC")
    fun getAllContacts(): Flow<List<LegalContact>>

    @Query("SELECT * FROM contacts WHERE role = :role ORDER BY name ASC")
    fun getContactsByRole(role: String): Flow<List<LegalContact>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: LegalContact): Long

    @Delete
    suspend fun deleteContact(contact: LegalContact)
}

