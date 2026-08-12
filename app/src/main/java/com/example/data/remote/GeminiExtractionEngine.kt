package com.example.data.remote

import android.content.Context
import android.graphics.Bitmap
import android.util.Base64
import com.example.data.local.LeadEntity
import com.example.data.model.AiCrmInsights
import com.example.data.model.ExtractedCardData
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.util.regex.Pattern

class GeminiExtractionEngine(private val context: Context) {

    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
    private val extractedCardAdapter = moshi.adapter(ExtractedCardData::class.java)

    suspend fun extractCardDataFromImage(
        bitmap: Bitmap,
        rawOcrText: String = ""
    ): ExtractedCardData = withContext(Dispatchers.IO) {
        val apiKey = GeminiClient.getApiKey()
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            // Fallback to local Regex OCR parsing
            return@withContext parseRawTextWithRegex(rawOcrText)
        }

        try {
            val base64Image = bitmap.toBase64()
            val prompt = """
                Analyze this business card image (and raw OCR text if provided) and extract all contact details into a JSON object matching this schema:
                {
                  "fullName": "First Last",
                  "jobTitle": "Role Title",
                  "company": "Company Name",
                  "department": "Department if specified",
                  "phonePrimary": "+1...",
                  "phoneSecondary": "+1...",
                  "emailPrimary": "email@domain.com",
                  "website": "https://...",
                  "linkedIn": "linkedin.com/in/...",
                  "twitter": "@handle",
                  "address": "Full Street Address, City, State, ZIP",
                  "industry": "Estimated Industry e.g. Technology, Finance, Healthcare, Real Estate, Manufacturing",
                  "notes": "Any extra notes, tags or languages found on card e.g. Gujarati, Hindi, English",
                  "confidenceScore": 0.95,
                  "reviewRequired": false,
                  "detectedLanguages": ["English"]
                }

                Clean OCR errors, expand abbreviations, format phone numbers into international format if possible.
                Raw OCR Text Context:
                $rawOcrText
            """.trimIndent()

            val request = GenerateContentRequest(
                contents = listOf(
                    Content(
                        parts = listOf(
                            Part(text = prompt),
                            Part(inlineData = InlineData(mimeType = "image/jpeg", data = base64Image))
                        )
                    )
                ),
                generationConfig = GenerationConfig(
                    responseMimeType = "application/json",
                    temperature = 0.2f
                )
            )

            val response = GeminiClient.service.generateContent(apiKey, request)
            val jsonText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text

            if (!jsonText.isNullByBlank()) {
                val cleanedJson = jsonText!!.trim().removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
                val parsed = extractedCardAdapter.fromJson(cleanedJson)
                if (parsed != null && (parsed.fullName.isNotBlank() || parsed.company.isNotBlank() || parsed.emailPrimary.isNotBlank())) {
                    return@withContext parsed
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Fallback to offline Regex OCR parsing
        parseRawTextWithRegex(rawOcrText)
    }

    suspend fun generateCrmInsights(leads: List<LeadEntity>): AiCrmInsights = withContext(Dispatchers.IO) {
        val apiKey = GeminiClient.getApiKey()
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY" || leads.isEmpty()) {
            return@withContext generateFallbackInsights(leads)
        }

        try {
            val leadsSummary = leads.take(25).joinToString("\n") { lead ->
                "- ${lead.fullName} (${lead.jobTitle} at ${lead.company}) | Status: ${lead.status.label} | Industry: ${lead.industry} | Source: ${lead.source.label}"
            }

            val prompt = """
                You are an AI Business Development & Lead Management Advisor for BizCard AI.
                Analyze the following list of active business leads and provide strategic CRM insights:
                
                LEADS LIST:
                $leadsSummary

                Provide a structured response in JSON format with fields:
                {
                  "summary": "Overall synthesis of lead collection and pipeline health.",
                  "highPriorityFollowUps": ["Bullet points of immediate follow-up recommendations"],
                  "keyOpportunities": ["Key account expansion or cross-industry opportunities identified"],
                  "industryTrends": ["Breakdown of lead distributions by sector or seniority level"]
                }
            """.trimIndent()

            val request = GenerateContentRequest(
                contents = listOf(Content(parts = listOf(Part(text = prompt)))),
                generationConfig = GenerationConfig(responseMimeType = "application/json", temperature = 0.4f)
            )

            val response = GeminiClient.service.generateContent(apiKey, request)
            val jsonText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text

            if (!jsonText.isNullByBlank()) {
                val cleanedJson = jsonText!!.trim().removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
                val adapter = moshi.adapter(AiCrmInsights::class.java)
                val parsed = adapter.fromJson(cleanedJson)
                if (parsed != null) return@withContext parsed
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        generateFallbackInsights(leads)
    }

    private fun parseRawTextWithRegex(rawText: String): ExtractedCardData {
        if (rawText.isBlank()) {
            return ExtractedCardData(
                fullName = "Card Contact",
                notes = "Scanned card - please review details."
            )
        }

        val lines = rawText.split("\n").map { it.trim() }.filter { it.isNotBlank() }

        var email = ""
        var phone1 = ""
        var phone2 = ""
        var website = ""
        var name = ""
        var company = ""
        var title = ""

        val emailMatcher = Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}").matcher(rawText)
        if (emailMatcher.find()) {
            email = emailMatcher.group()
        }

        val phoneMatcher = Pattern.compile("(\\+?\\d{1,3}[-.\\s]?)?\\(?\\d{3}\\)?[-.\\s]?\\d{3}[-.\\s]?\\d{4}").matcher(rawText)
        val phonesList = mutableListOf<String>()
        while (phoneMatcher.find()) {
            phonesList.add(phoneMatcher.group())
        }
        if (phonesList.isNotEmpty()) phone1 = phonesList[0]
        if (phonesList.size > 1) phone2 = phonesList[1]

        val webMatcher = Pattern.compile("(https?://)?(www\\.)?[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}").matcher(rawText)
        while (webMatcher.find()) {
            val found = webMatcher.group()
            if (!found.contains("@")) {
                website = if (!found.startsWith("http")) "https://$found" else found
                break
            }
        }

        for (line in lines) {
            if (name.isBlank() && !line.contains("@") && !line.contains(".com") && !line.matches(Regex(".*\\d{5,}.*"))) {
                if (line.length in 3..35 && line.split(" ").size in 1..4) {
                    name = line
                    continue
                }
            }
            if (name.isNotBlank() && title.isBlank() && (
                line.contains("Manager", ignoreCase = true) ||
                line.contains("Director", ignoreCase = true) ||
                line.contains("VP", ignoreCase = true) ||
                line.contains("CEO", ignoreCase = true) ||
                line.contains("Engineer", ignoreCase = true) ||
                line.contains("Consultant", ignoreCase = true) ||
                line.contains("Founder", ignoreCase = true) ||
                line.contains("Lead", ignoreCase = true)
            )) {
                title = line
                continue
            }
            if (company.isBlank() && line != name && line != title && !line.contains("@") && !line.contains("http")) {
                if (line.length in 3..40) {
                    company = line
                }
            }
        }

        return ExtractedCardData(
            fullName = name.ifBlank { "Scanned Card Lead" },
            jobTitle = title,
            company = company,
            phonePrimary = phone1,
            phoneSecondary = phone2,
            emailPrimary = email,
            website = website,
            notes = "Raw OCR Text extracted locally:\n$rawText",
            confidenceScore = 0.75f,
            reviewRequired = true
        )
    }

    private fun generateFallbackInsights(leads: List<LeadEntity>): AiCrmInsights {
        val total = leads.size
        val newLeads = leads.count { it.status == com.example.data.model.LeadStatus.NEW }
        val qualified = leads.count { it.status == com.example.data.model.LeadStatus.QUALIFIED }
        val converted = leads.count { it.status == com.example.data.model.LeadStatus.CONVERTED }

        val companiesCount = leads.map { it.company }.filter { it.isNotBlank() }.distinct().size

        return AiCrmInsights(
            summary = "You have $total total leads in your network across $companiesCount unique companies. $newLeads new contacts require initial outreach.",
            highPriorityFollowUps = listOf(
                "Follow up with $newLeads 'New' leads within 48 hours to maximize conversion rate.",
                "Reach out to $qualified 'Qualified' leads to schedule follow-up discovery calls."
            ),
            keyOpportunities = listOf(
                "Multiple contacts detected from top target companies - leverage multi-threading in deal cycles.",
                "Review lead tags and star high-value prospects for priority follow-up."
            ),
            industryTrends = listOf(
                "Current Pipeline Conversion: ${if (total > 0) (converted * 100 / total) else 0}% converted leads.",
                "Top acquisition channel: Business Card Scans and NFC Taps."
            )
        )
    }

    private fun Bitmap.toBase64(): String {
        val outputStream = ByteArrayOutputStream()
        compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
    }

    private fun String?.isNullByBlank(): Boolean = this.isNullOrBlank()
}
