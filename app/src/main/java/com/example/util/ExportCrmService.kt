package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.local.LeadEntity
import com.example.data.model.CrmConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

object ExportCrmService {

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

    // -------------------------------------------------------------
    // 1. VCF & CSV FILE GENERATION & SHARING
    // -------------------------------------------------------------

    fun generateVcfContent(leads: List<LeadEntity>): String {
        val sb = StringBuilder()
        for (lead in leads) {
            sb.append("BEGIN:VCARD\n")
            sb.append("VERSION:3.0\n")
            sb.append("N:;${lead.fullName};;;\n")
            sb.append("FN:${lead.fullName}\n")
            if (lead.company.isNotBlank()) sb.append("ORG:${lead.company}\n")
            if (lead.jobTitle.isNotBlank()) sb.append("TITLE:${lead.jobTitle}\n")
            if (lead.phonePrimary.isNotBlank()) sb.append("TEL;TYPE=CELL:${lead.phonePrimary}\n")
            if (lead.phoneSecondary.isNotBlank()) sb.append("TEL;TYPE=WORK:${lead.phoneSecondary}\n")
            if (lead.emailPrimary.isNotBlank()) sb.append("EMAIL:${lead.emailPrimary}\n")
            if (lead.website.isNotBlank()) sb.append("URL:${lead.website}\n")
            if (lead.address.isNotBlank()) sb.append("ADR;TYPE=WORK:;;${lead.address.replace("\n", " ")};;;;\n")
            if (lead.notes.isNotBlank() || lead.tags.isNotBlank()) {
                val noteText = "Source: ${lead.source.label} | Industry: ${lead.industry} | Tags: ${lead.tags} | Notes: ${lead.notes}".trim()
                sb.append("NOTE:$noteText\n")
            }
            sb.append("END:VCARD\n\n")
        }
        return sb.toString()
    }

    suspend fun shareVcfFile(context: Context, leads: List<LeadEntity>, fileName: String = "LeadTap_Contacts.vcf") {
        withContext(Dispatchers.IO) {
            try {
                val vcfContent = generateVcfContent(leads)
                val exportDir = File(context.cacheDir, "exports")
                if (!exportDir.exists()) exportDir.mkdirs()

                val file = File(exportDir, fileName)
                FileWriter(file).use { writer ->
                    writer.write(vcfContent)
                }

                val uri: Uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )

                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/x-vcard"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_SUBJECT, "LeadTap Contacts Export (${leads.size} contacts)")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

                val chooser = Intent.createChooser(intent, "Export VCF Contacts via")
                chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(chooser)
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error exporting VCF: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun generateCsvContent(leads: List<LeadEntity>): String {
        val sb = StringBuilder()
        sb.append("Full Name,Company,Job Title,Primary Phone,Secondary Phone,Primary Email,Website,LinkedIn,Industry,Address,Notes,Tags,Status,Source,Confidence,Created Date\n")

        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

        for (lead in leads) {
            val dateStr = try { sdf.format(Date(lead.createdTimestamp)) } catch (e: Exception) { "" }
            val fields = listOf(
                lead.fullName,
                lead.company,
                lead.jobTitle,
                lead.phonePrimary,
                lead.phoneSecondary,
                lead.emailPrimary,
                lead.website,
                lead.linkedIn,
                lead.industry,
                lead.address,
                lead.notes.replace("\n", " "),
                lead.tags,
                lead.status.label,
                lead.source.label,
                "${(lead.confidenceScore * 100).toInt()}%",
                dateStr
            )

            val line = fields.joinToString(",") { field ->
                "\"" + field.replace("\"", "\"\"") + "\""
            }
            sb.append(line).append("\n")
        }
        return sb.toString()
    }

    suspend fun shareCsvFile(context: Context, leads: List<LeadEntity>, fileName: String = "LeadTap_Leads_Database.csv") {
        withContext(Dispatchers.IO) {
            try {
                val csvContent = generateCsvContent(leads)
                val exportDir = File(context.cacheDir, "exports")
                if (!exportDir.exists()) exportDir.mkdirs()

                val file = File(exportDir, fileName)
                FileWriter(file).use { writer ->
                    writer.write(csvContent)
                }

                val uri: Uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )

                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/csv"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_SUBJECT, "LeadTap Database CSV (${leads.size} leads)")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

                val chooser = Intent.createChooser(intent, "Share CSV Database via")
                chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(chooser)
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error exporting CSV: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // -------------------------------------------------------------
    // 2. GOOGLE SHEETS WEBHOOK INTEGRATION
    // -------------------------------------------------------------

    suspend fun pushToGoogleSheets(webhookUrl: String, leads: List<LeadEntity>): Result<String> {
        return withContext(Dispatchers.IO) {
            if (webhookUrl.isBlank()) {
                return@withContext Result.failure(Exception("Google Sheets Webhook URL is empty. Please configure in settings."))
            }

            try {
                val jsonArray = JSONArray()
                for (lead in leads) {
                    val obj = JSONObject().apply {
                        put("fullName", lead.fullName)
                        put("company", lead.company)
                        put("jobTitle", lead.jobTitle)
                        put("phonePrimary", lead.phonePrimary)
                        put("phoneSecondary", lead.phoneSecondary)
                        put("emailPrimary", lead.emailPrimary)
                        put("website", lead.website)
                        put("linkedIn", lead.linkedIn)
                        put("industry", lead.industry)
                        put("address", lead.address)
                        put("notes", lead.notes)
                        put("tags", lead.tags)
                        put("status", lead.status.label)
                        put("source", lead.source.label)
                    }
                    jsonArray.put(obj)
                }

                val requestBody = jsonArray.toString().toRequestBody(JSON_MEDIA_TYPE)
                val request = Request.Builder()
                    .url(webhookUrl.trim())
                    .post(requestBody)
                    .build()

                httpClient.newCall(request).execute().use { response ->
                    val code = response.code
                    val responseStr = response.body?.string() ?: ""

                    if (response.isSuccessful || code == 302 || code == 200) {
                        Result.success("Exported ${leads.size} lead(s) to Google Sheets successfully!")
                    } else {
                        Result.failure(Exception("HTTP $code: ${responseStr.take(150)}"))
                    }
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // -------------------------------------------------------------
    // 3. HUBSPOT CRM API INTEGRATION
    // -------------------------------------------------------------

    suspend fun pushToHubSpot(apiKey: String, lead: LeadEntity): Result<String> {
        return withContext(Dispatchers.IO) {
            if (apiKey.isBlank()) {
                return@withContext Result.failure(Exception("HubSpot Private App API Key is missing."))
            }

            try {
                val nameParts = lead.fullName.trim().split(" ", limit = 2)
                val firstName = nameParts.getOrNull(0) ?: lead.fullName
                val lastName = nameParts.getOrNull(1) ?: ""

                val properties = JSONObject().apply {
                    put("firstname", firstName)
                    put("lastname", lastName)
                    put("email", lead.emailPrimary)
                    put("phone", lead.phonePrimary)
                    put("company", lead.company)
                    put("jobtitle", lead.jobTitle)
                    put("website", lead.website)
                    put("industry", lead.industry)
                    put("address", lead.address)
                }

                val bodyObj = JSONObject().apply {
                    put("properties", properties)
                }

                val requestBody = bodyObj.toString().toRequestBody(JSON_MEDIA_TYPE)
                val request = Request.Builder()
                    .url("https://api.hubapi.com/crm/v3/objects/contacts")
                    .addHeader("Authorization", "Bearer ${apiKey.trim()}")
                    .post(requestBody)
                    .build()

                httpClient.newCall(request).execute().use { response ->
                    val responseStr = response.body?.string() ?: ""

                    if (response.isSuccessful || response.code == 201) {
                        Result.success("Lead '${lead.fullName}' created in HubSpot CRM!")
                    } else if (response.code == 409) {
                        Result.success("Lead '${lead.fullName}' already exists in HubSpot CRM.")
                    } else {
                        Result.failure(Exception("HubSpot Error ${response.code}: ${responseStr.take(120)}"))
                    }
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // -------------------------------------------------------------
    // 4. SALESFORCE & ZOHO CRM INTEGRATION
    // -------------------------------------------------------------

    private const val SALESFORCE_API_VERSION = "v58.0"

    /**
     * Salesforce only serves the REST API from the org's own My Domain host
     * (e.g. https://acme.my.salesforce.com), so the endpoint must be built from the user's
     * instance URL. Returns null for anything that is not a valid HTTPS URL.
     */
    internal fun salesforceLeadEndpoint(instanceUrl: String): HttpUrl? {
        val trimmed = instanceUrl.trim().trimEnd('/')
        if (trimmed.isEmpty()) return null
        val base = (if ("://" in trimmed) trimmed else "https://$trimmed").toHttpUrlOrNull()
            ?: return null
        if (!base.isHttps) return null
        return base.newBuilder()
            .encodedPath("/services/data/$SALESFORCE_API_VERSION/sobjects/Lead")
            .query(null)
            .fragment(null)
            .build()
    }

    suspend fun pushToSalesforce(instanceUrl: String, apiKey: String, lead: LeadEntity): Result<String> {
        return withContext(Dispatchers.IO) {
            if (apiKey.isBlank()) {
                return@withContext Result.failure(Exception("Salesforce API Token is missing."))
            }
            val endpoint = salesforceLeadEndpoint(instanceUrl)
                ?: return@withContext Result.failure(
                    Exception("Salesforce instance URL is missing or invalid (e.g. https://yourcompany.my.salesforce.com).")
                )

            try {
                val nameParts = lead.fullName.trim().split(" ", limit = 2)
                val firstName = nameParts.getOrNull(0) ?: lead.fullName
                val lastName = nameParts.getOrNull(1) ?: "Contact"

                val json = JSONObject().apply {
                    put("FirstName", firstName)
                    put("LastName", lastName)
                    put("Company", lead.company.ifBlank { "Independent" })
                    put("Title", lead.jobTitle)
                    put("Email", lead.emailPrimary)
                    put("Phone", lead.phonePrimary)
                    put("Website", lead.website)
                    put("Industry", lead.industry)
                    put("LeadSource", lead.source.label)
                }

                val requestBody = json.toString().toRequestBody(JSON_MEDIA_TYPE)
                val request = Request.Builder()
                    .url(endpoint)
                    .addHeader("Authorization", "Bearer ${apiKey.trim()}")
                    .post(requestBody)
                    .build()

                httpClient.newCall(request).execute().use { response ->
                    if (response.isSuccessful || response.code == 201) {
                        Result.success("Lead '${lead.fullName}' synced to Salesforce!")
                    } else {
                        Result.failure(Exception("Salesforce HTTP ${response.code}: ${response.body?.string()?.take(100)}"))
                    }
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun pushToZoho(apiKey: String, lead: LeadEntity): Result<String> {
        return withContext(Dispatchers.IO) {
            if (apiKey.isBlank()) {
                return@withContext Result.failure(Exception("Zoho API Key is missing."))
            }

            try {
                val nameParts = lead.fullName.trim().split(" ", limit = 2)
                val firstName = nameParts.getOrNull(0) ?: lead.fullName
                val lastName = nameParts.getOrNull(1) ?: "Lead"

                val leadObj = JSONObject().apply {
                    put("First_Name", firstName)
                    put("Last_Name", lastName)
                    put("Company", lead.company.ifBlank { "N/A" })
                    put("Designation", lead.jobTitle)
                    put("Email", lead.emailPrimary)
                    put("Phone", lead.phonePrimary)
                    put("Website", lead.website)
                    put("Industry", lead.industry)
                }

                val payload = JSONObject().apply {
                    put("data", JSONArray().put(leadObj))
                }

                val request = Request.Builder()
                    .url("https://www.zohoapis.com/crm/v2/Leads")
                    .addHeader("Authorization", "Zoho-oauthtoken ${apiKey.trim()}")
                    .post(payload.toString().toRequestBody(JSON_MEDIA_TYPE))
                    .build()

                httpClient.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        Result.success("Lead '${lead.fullName}' synced to Zoho CRM!")
                    } else {
                        Result.failure(Exception("Zoho HTTP ${response.code}: ${response.body?.string()?.take(100)}"))
                    }
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // -------------------------------------------------------------
    // 5. CUSTOM REST API / ZAPIER WEBHOOK INTEGRATION
    // -------------------------------------------------------------

    suspend fun pushToCustomWebhook(
        webhookUrl: String,
        authHeaderName: String,
        apiKey: String,
        leads: List<LeadEntity>
    ): Result<String> {
        return withContext(Dispatchers.IO) {
            if (webhookUrl.isBlank()) {
                return@withContext Result.failure(Exception("Custom Webhook URL is missing."))
            }

            try {
                val array = JSONArray()
                for (lead in leads) {
                    val obj = JSONObject().apply {
                        put("id", lead.id)
                        put("fullName", lead.fullName)
                        put("company", lead.company)
                        put("jobTitle", lead.jobTitle)
                        put("phonePrimary", lead.phonePrimary)
                        put("phoneSecondary", lead.phoneSecondary)
                        put("emailPrimary", lead.emailPrimary)
                        put("website", lead.website)
                        put("linkedIn", lead.linkedIn)
                        put("industry", lead.industry)
                        put("address", lead.address)
                        put("notes", lead.notes)
                        put("status", lead.status.label)
                        put("source", lead.source.label)
                    }
                    array.put(obj)
                }

                val builder = Request.Builder()
                    .url(webhookUrl.trim())
                    .post(array.toString().toRequestBody(JSON_MEDIA_TYPE))

                if (apiKey.isNotBlank()) {
                    val headerName = authHeaderName.ifBlank { "Authorization" }
                    val headerVal = if (headerName.equals("Authorization", ignoreCase = true) && !apiKey.startsWith("Bearer ", ignoreCase = true)) {
                        "Bearer ${apiKey.trim()}"
                    } else {
                        apiKey.trim()
                    }
                    builder.addHeader(headerName, headerVal)
                }

                httpClient.newCall(builder.build()).execute().use { response ->
                    val code = response.code
                    if (response.isSuccessful || code == 200 || code == 201) {
                        Result.success("Pushed ${leads.size} lead(s) to Custom Webhook!")
                    } else {
                        Result.failure(Exception("Webhook HTTP $code: ${response.body?.string()?.take(100)}"))
                    }
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // -------------------------------------------------------------
    // 6. AUTO-SYNC DISPATCHER
    // -------------------------------------------------------------

    suspend fun syncLeadToAllActiveIntegrations(config: CrmConfig, lead: LeadEntity): List<Result<String>> {
        val results = mutableListOf<Result<String>>()

        if (config.googleSheetsWebhookUrl.isNotBlank()) {
            results.add(pushToGoogleSheets(config.googleSheetsWebhookUrl, listOf(lead)))
        }
        if (config.hubspotApiKey.isNotBlank()) {
            results.add(pushToHubSpot(config.hubspotApiKey, lead))
        }
        if (config.salesforceApiKey.isNotBlank()) {
            results.add(pushToSalesforce(config.salesforceInstanceUrl, config.salesforceApiKey, lead))
        }
        if (config.zohoApiKey.isNotBlank()) {
            results.add(pushToZoho(config.zohoApiKey, lead))
        }
        if (config.customWebhookUrl.isNotBlank()) {
            results.add(pushToCustomWebhook(config.customWebhookUrl, config.customWebhookAuthHeader, config.customWebhookApiKey, listOf(lead)))
        }

        return results
    }
}
