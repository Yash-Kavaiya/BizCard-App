package com.example.data.model

import com.squareup.moshi.JsonClass

enum class LeadStatus(val label: String) {
    NEW("New"),
    CONTACTED("Contacted"),
    QUALIFIED("Qualified"),
    CONVERTED("Converted"),
    ARCHIVED("Archived")
}

enum class LeadSource(val label: String) {
    SCAN("Card Scan"),
    NFC("NFC Tap"),
    QR("QR Code"),
    MANUAL("Manual Entry")
}

enum class RelationshipType(val label: String) {
    PROSPECT("Prospect"),
    CLIENT("Client"),
    PARTNER("Partner"),
    INVESTOR("Investor"),
    VENDOR("Vendor"),
    OTHER("Other")
}

@JsonClass(generateAdapter = true)
data class ExtractedCardData(
    val fullName: String = "",
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
    val confidenceScore: Float = 0.9f,
    val reviewRequired: Boolean = false,
    val detectedLanguages: List<String> = listOf("English")
)

data class PersonalCard(
    val name: String = "Alex Morgan",
    val title: String = "VP of Business Development",
    val company: String = "Apex Innovations Inc.",
    val email: String = "alex.morgan@apexinnovations.com",
    val phone: String = "+1 (555) 234-5678",
    val website: String = "https://apexinnovations.com",
    val linkedIn: String = "linkedin.com/in/alexmorgan-biz",
    val address: String = "742 Evergreen Terrace, San Francisco, CA",
    val bio: String = "Building strategic enterprise partnerships & AI integrations.",
    val qrPayload: String = ""
)

data class AiCrmInsights(
    val summary: String = "",
    val highPriorityFollowUps: List<String> = emptyList(),
    val keyOpportunities: List<String> = emptyList(),
    val industryTrends: List<String> = emptyList(),
    val generatedAt: Long = System.currentTimeMillis()
)
