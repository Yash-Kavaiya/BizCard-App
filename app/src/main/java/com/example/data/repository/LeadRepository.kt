package com.example.data.repository

import android.content.Context
import android.graphics.Bitmap
import com.example.data.local.AppDatabase
import com.example.data.local.LeadEntity
import com.example.data.local.UserPreferences
import com.example.data.model.AiCrmInsights
import com.example.data.model.ExtractedCardData
import com.example.data.model.LeadSource
import com.example.data.model.LeadStatus
import com.example.data.model.PersonalCard
import com.example.data.model.RelationshipType
import com.example.data.remote.GeminiExtractionEngine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class LeadRepository(private val context: Context) {

    private val db = AppDatabase.getDatabase(context)
    private val dao = db.leadDao()
    private val geminiEngine = GeminiExtractionEngine(context)
    val preferences = UserPreferences(context)

    val allLeads: Flow<List<LeadEntity>> = dao.getAllLeads()
    val totalLeadCount: Flow<Int> = dao.getLeadCount()

    fun searchLeads(query: String): Flow<List<LeadEntity>> {
        return if (query.isBlank()) dao.getAllLeads() else dao.searchLeads(query)
    }

    fun getLeadsByStatus(status: LeadStatus): Flow<List<LeadEntity>> {
        return dao.getLeadsByStatus(status)
    }

    suspend fun getLeadById(id: Long): LeadEntity? = dao.getLeadById(id)

    suspend fun insertLead(lead: LeadEntity): Long = dao.insertLead(lead)

    suspend fun updateLead(lead: LeadEntity) = dao.updateLead(lead)

    suspend fun deleteLead(id: Long) = dao.deleteLeadById(id)

    suspend fun deleteAllLeads() = dao.deleteAllLeads()

    suspend fun extractCardFromImage(bitmap: Bitmap, rawOcr: String = ""): ExtractedCardData {
        return geminiEngine.extractCardDataFromImage(bitmap, rawOcr)
    }

    suspend fun generateCrmInsights(): AiCrmInsights {
        val currentLeads = dao.getAllLeads().first()
        return geminiEngine.generateCrmInsights(currentLeads)
    }

    suspend fun seedInitialDataIfEmpty() {
        val count = dao.getLeadCount().first()
        if (count == 0) {
            val now = System.currentTimeMillis()
            val dayMs = 86400000L

            val initialLeads = listOf(
                LeadEntity(
                    fullName = "Priya Sharma",
                    jobTitle = "Chief Product Officer",
                    company = "FinPulse Technologies",
                    department = "Product & AI",
                    phonePrimary = "+91 98765 43210",
                    emailPrimary = "priya.sharma@finpulse.io",
                    website = "https://finpulse.io",
                    linkedIn = "linkedin.com/in/priyasharma-tech",
                    address = "Bandra Kurla Complex, Mumbai, Maharashtra",
                    industry = "Fintech",
                    notes = "Met at TechSparks 2026. Interested in automated AI lead capture for their field sales force.",
                    status = LeadStatus.QUALIFIED,
                    source = LeadSource.SCAN,
                    relationshipType = RelationshipType.PROSPECT,
                    tags = "Fintech, AI, High Value",
                    isStarred = true,
                    createdTimestamp = now - (2 * dayMs)
                ),
                LeadEntity(
                    fullName = "Rajesh Patel",
                    jobTitle = "Managing Director",
                    company = "Surat Diamond & Gems Corp",
                    department = "Executive",
                    phonePrimary = "+91 98250 11223",
                    emailPrimary = "rajesh@suratgems.com",
                    website = "https://suratgems.com",
                    linkedIn = "linkedin.com/in/rajesh-patel-surat",
                    address = "Ring Road, Surat, Gujarat",
                    industry = "Manufacturing & Export",
                    notes = "Guajarati business card scanned. High interest in digital catalog NFC tags.",
                    status = LeadStatus.CONTACTED,
                    source = LeadSource.NFC,
                    relationshipType = RelationshipType.CLIENT,
                    tags = "Gujarat, Manufacturing, NFC",
                    isStarred = true,
                    createdTimestamp = now - (4 * dayMs)
                ),
                LeadEntity(
                    fullName = "Sarah Jenkins",
                    jobTitle = "Partner & VC Lead",
                    company = "Horizon Ventures",
                    department = "Investment",
                    phonePrimary = "+1 (415) 890-2341",
                    emailPrimary = "s.jenkins@horizonvc.com",
                    website = "https://horizonvc.com",
                    linkedIn = "linkedin.com/in/sarahjenkins-vc",
                    address = "Sand Hill Rd, Menlo Park, CA",
                    industry = "Venture Capital",
                    notes = "Tapped card at Silicon Valley Summit. Wants demo of LeadTap CRM integration.",
                    status = LeadStatus.NEW,
                    source = LeadSource.NFC,
                    relationshipType = RelationshipType.INVESTOR,
                    tags = "VC, Investor, Silicon Valley",
                    isStarred = true,
                    createdTimestamp = now - (1 * dayMs)
                ),
                LeadEntity(
                    fullName = "Aarav Mehta",
                    jobTitle = "Head of Enterprise Sales",
                    company = "CloudScale Solutions",
                    department = "Revenue",
                    phonePrimary = "+91 99001 88776",
                    emailPrimary = "aarav.m@cloudscale.net",
                    website = "https://cloudscale.net",
                    linkedIn = "linkedin.com/in/aarav-mehta-sales",
                    address = "Cyber City, Gurugram, Haryana",
                    industry = "Cloud & SaaS",
                    notes = "Exchanged contacts at SaaSX. Seeking team licensing for 50 sales reps.",
                    status = LeadStatus.CONVERTED,
                    source = LeadSource.SCAN,
                    relationshipType = RelationshipType.CLIENT,
                    tags = "Enterprise, SaaS, Closed-Won",
                    isStarred = false,
                    createdTimestamp = now - (10 * dayMs)
                ),
                LeadEntity(
                    fullName = "Elena Rostova",
                    jobTitle = "VP of Global Supply Chain",
                    company = "Nordic Logistics Group",
                    department = "Operations",
                    phonePrimary = "+44 20 7946 0912",
                    emailPrimary = "elena.r@nordiclogistics.co.uk",
                    website = "https://nordiclogistics.co.uk",
                    linkedIn = "linkedin.com/in/elena-rostova",
                    address = "London, EC2A 4NE, United Kingdom",
                    industry = "Logistics",
                    notes = "Scanning physical card from European Logistics Expo.",
                    status = LeadStatus.NEW,
                    source = LeadSource.SCAN,
                    relationshipType = RelationshipType.PROSPECT,
                    tags = "International, Europe",
                    isStarred = false,
                    createdTimestamp = now - (3 * dayMs)
                )
            )

            dao.insertLeads(initialLeads)
        }
    }
}
