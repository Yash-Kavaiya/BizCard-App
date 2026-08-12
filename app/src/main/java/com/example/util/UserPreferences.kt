package com.example.util

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.CrmConfig
import com.example.data.model.PersonalCard
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class UserPreferences(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("user_card_prefs", Context.MODE_PRIVATE)

    private val _personalCardFlow = MutableStateFlow(getPersonalCard())
    val personalCardFlow: StateFlow<PersonalCard> = _personalCardFlow.asStateFlow()

    private val _crmConfigFlow = MutableStateFlow(getCrmConfig())
    val crmConfigFlow: StateFlow<CrmConfig> = _crmConfigFlow.asStateFlow()

    fun getPersonalCard(): PersonalCard {
        return PersonalCard(
            name = prefs.getString("name", "Alex Morgan") ?: "Alex Morgan",
            title = prefs.getString("title", "VP of Business Development") ?: "VP of Business Development",
            company = prefs.getString("company", "Apex Innovations Inc.") ?: "Apex Innovations Inc.",
            email = prefs.getString("email", "alex.morgan@apexinnovations.com") ?: "alex.morgan@apexinnovations.com",
            phone = prefs.getString("phone", "+1 (555) 234-5678") ?: "+1 (555) 234-5678",
            website = prefs.getString("website", "https://apexinnovations.com") ?: "https://apexinnovations.com",
            linkedIn = prefs.getString("linkedIn", "linkedin.com/in/alexmorgan-biz") ?: "linkedin.com/in/alexmorgan-biz",
            address = prefs.getString("address", "742 Evergreen Terrace, San Francisco, CA") ?: "742 Evergreen Terrace, San Francisco, CA",
            bio = prefs.getString("bio", "Building strategic enterprise partnerships & AI integrations.") ?: "Building strategic enterprise partnerships & AI integrations."
        )
    }

    fun savePersonalCard(card: PersonalCard) {
        prefs.edit()
            .putString("name", card.name)
            .putString("title", card.title)
            .putString("company", card.company)
            .putString("email", card.email)
            .putString("phone", card.phone)
            .putString("website", card.website)
            .putString("linkedIn", card.linkedIn)
            .putString("address", card.address)
            .putString("bio", card.bio)
            .apply()

        _personalCardFlow.value = card
    }

    fun getCrmConfig(): CrmConfig {
        return CrmConfig(
            googleSheetsWebhookUrl = prefs.getString("gs_webhook_url", "") ?: "",
            hubspotApiKey = prefs.getString("hubspot_api_key", "") ?: "",
            salesforceApiKey = prefs.getString("salesforce_api_key", "") ?: "",
            zohoApiKey = prefs.getString("zoho_api_key", "") ?: "",
            customWebhookUrl = prefs.getString("custom_webhook_url", "") ?: "",
            customWebhookAuthHeader = prefs.getString("custom_auth_header", "Authorization") ?: "Authorization",
            customWebhookApiKey = prefs.getString("custom_api_key", "") ?: "",
            autoExportEnabled = prefs.getBoolean("auto_export_enabled", false)
        )
    }

    fun saveCrmConfig(config: CrmConfig) {
        prefs.edit()
            .putString("gs_webhook_url", config.googleSheetsWebhookUrl)
            .putString("hubspot_api_key", config.hubspotApiKey)
            .putString("salesforce_api_key", config.salesforceApiKey)
            .putString("zoho_api_key", config.zohoApiKey)
            .putString("custom_webhook_url", config.customWebhookUrl)
            .putString("custom_auth_header", config.customWebhookAuthHeader)
            .putString("custom_api_key", config.customWebhookApiKey)
            .putBoolean("auto_export_enabled", config.autoExportEnabled)
            .apply()

        _crmConfigFlow.value = config
    }
}
