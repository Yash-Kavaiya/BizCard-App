package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.LeadSource
import com.example.data.model.LeadStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface LeadDao {

    @Query("SELECT * FROM leads ORDER BY isStarred DESC, createdTimestamp DESC")
    fun getAllLeads(): Flow<List<LeadEntity>>

    @Query("SELECT * FROM leads ORDER BY createdTimestamp DESC")
    suspend fun getAllLeadsList(): List<LeadEntity>

    @Query("SELECT * FROM leads WHERE id = :id")
    suspend fun getLeadById(id: Long): LeadEntity?

    @Query("""
        SELECT * FROM leads 
        WHERE fullName LIKE '%' || :query || '%' 
           OR company LIKE '%' || :query || '%' 
           OR jobTitle LIKE '%' || :query || '%'
           OR industry LIKE '%' || :query || '%'
           OR tags LIKE '%' || :query || '%'
        ORDER BY createdTimestamp DESC
    """)
    fun searchLeads(query: String): Flow<List<LeadEntity>>

    @Query("SELECT * FROM leads WHERE status = :status ORDER BY createdTimestamp DESC")
    fun getLeadsByStatus(status: LeadStatus): Flow<List<LeadEntity>>

    @Query("SELECT * FROM leads WHERE source = :source ORDER BY createdTimestamp DESC")
    fun getLeadsBySource(source: LeadSource): Flow<List<LeadEntity>>

    @Query("SELECT * FROM leads WHERE isStarred = 1 ORDER BY createdTimestamp DESC")
    fun getStarredLeads(): Flow<List<LeadEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLead(lead: LeadEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLeads(leads: List<LeadEntity>)

    @Update
    suspend fun updateLead(lead: LeadEntity)

    @Delete
    suspend fun deleteLead(lead: LeadEntity)

    @Query("DELETE FROM leads WHERE id = :id")
    suspend fun deleteLeadById(id: Long)

    @Query("DELETE FROM leads")
    suspend fun deleteAllLeads()

    @Query("SELECT COUNT(*) FROM leads")
    fun getLeadCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM leads WHERE status = :status")
    fun getCountByStatus(status: LeadStatus): Flow<Int>

    @Query("SELECT COUNT(*) FROM leads WHERE createdTimestamp >= :sinceTimestamp")
    fun getRecentLeadCount(sinceTimestamp: Long): Flow<Int>
}
