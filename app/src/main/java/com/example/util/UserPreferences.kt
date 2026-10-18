package com.example.util

import android.content.Context
import android.content.SharedPreferences
import com.example.BuildConfig
import com.example.data.model.CrmConfig
import com.example.data.model.PersonalCard
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class UserPreferences(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("user_card_prefs", Context.MODE_PRIVATE)

    // Holds API tokens; excluded from backups in backup_rules.xml / data_extraction_rules.xml.
    private val crmPrefs: SharedPreferences = context.getSharedPreferences("crm_integrations", Context.MODE_PRIVATE)

    private val defaultCard = if (BuildConfig.DEBUG) PersonalCard.SAMPLE else PersonalCard()

    private val _personalCardFlow = MutableStateFlow(getPersonalCard())
    val personalCardFlow: StateFlow<PersonalCard> = _personalCardFlow.asStateFlow()

    private val _crmConfigFlow = MutableStateFlow(getCrmConfig())
    val crmConfigFlow: StateFlow<CrmConfig> = _crmConfigFlow.asStateFlow()

    fun getPersonalCard(): PersonalCard {
        val d = defaultCard
        return PersonalCard(
            name = prefs.getString("name", d.name) ?: d.name,
            title = prefs.getString("title", d.title) ?: d.title,
            company = prefs.getString("company", d.company) ?: d.company,
            email = prefs.getString("email", d.email) ?: d.email,
            phone = prefs.getString("phone", d.phone) ?: d.phone,
            website = prefs.getString("website", d.website) ?: d.website,
            linkedIn = prefs.getString("linkedIn", d.linkedIn) ?: d.linkedIn,
            address = prefs.getString("address", d.address) ?: d.address,
            bio = prefs.getString("bio", d.bio) ?: d.bio
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
            googleSheetsWebhookUrl = crmPrefs.getString("gs_webhook_url", "") ?: "",
            hubspotApiKey = crmPrefs.getString("hubspot_api_key", "") ?: "",
            salesforceApiKey = crmPrefs.getString("salesforce_api_key", "") ?: "",
            salesforceInstanceUrl = crmPrefs.getString("salesforce_instance_url", "") ?: "",
            zohoApiKey = crmPrefs.getString("zoho_api_key", "") ?: "",
            customWebhookUrl = crmPrefs.getString("custom_webhook_url", "") ?: "",
            customWebhookAuthHeader = crmPrefs.getString("custom_auth_header", "Authorization") ?: "Authorization",
            customWebhookApiKey = crmPrefs.getString("custom_api_key", "") ?: "",
            autoExportEnabled = crmPrefs.getBoolean("auto_export_enabled", false)
        )
    }

    fun saveCrmConfig(config: CrmConfig) {
        crmPrefs.edit()
            .putString("gs_webhook_url", config.googleSheetsWebhookUrl)
            .putString("hubspot_api_key", config.hubspotApiKey)
            .putString("salesforce_api_key", config.salesforceApiKey)
            .putString("salesforce_instance_url", config.salesforceInstanceUrl)
            .putString("zoho_api_key", config.zohoApiKey)
            .putString("custom_webhook_url", config.customWebhookUrl)
            .putString("custom_auth_header", config.customWebhookAuthHeader)
            .putString("custom_api_key", config.customWebhookApiKey)
            .putBoolean("auto_export_enabled", config.autoExportEnabled)
            .apply()

        _crmConfigFlow.value = config
    }
}
