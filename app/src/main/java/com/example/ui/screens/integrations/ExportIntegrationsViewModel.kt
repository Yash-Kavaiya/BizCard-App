package com.example.ui.screens.integrations

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.LeadEntity
import com.example.data.model.CrmConfig
import com.example.util.ContactsHelper
import com.example.util.ExportCrmService
import com.example.util.UserPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ExportIntegrationsUiState(
    val crmConfig: CrmConfig = CrmConfig(),
    val isExporting: Boolean = false,
    val isTesting: Boolean = false,
    val statusMessage: String? = null,
    val errorMessage: String? = null,
    val totalLeadsCount: Int = 0
)

class ExportIntegrationsViewModel(application: Application) : AndroidViewModel(application) {

    private val userPreferences = UserPreferences(application)
    private val leadDao = AppDatabase.getDatabase(application).leadDao()

    private val _uiState = MutableStateFlow(ExportIntegrationsUiState())
    val uiState: StateFlow<ExportIntegrationsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            userPreferences.crmConfigFlow.collect { config ->
                _uiState.value = _uiState.value.copy(crmConfig = config)
            }
        }

        viewModelScope.launch {
            leadDao.getAllLeads().collect { leads ->
                _uiState.value = _uiState.value.copy(totalLeadsCount = leads.size)
            }
        }
    }

    fun updateCrmConfig(config: CrmConfig) {
        userPreferences.saveCrmConfig(config)
        _uiState.value = _uiState.value.copy(
            statusMessage = "Integration configuration saved!"
        )
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(statusMessage = null, errorMessage = null)
    }

    fun exportAllToGoogleSheets(context: Context) {
        viewModelScope.launch {
            val url = _uiState.value.crmConfig.googleSheetsWebhookUrl
            if (url.isBlank()) {
                _uiState.value = _uiState.value.copy(errorMessage = "Please enter your Google Sheets Webhook URL first.")
                return@launch
            }

            _uiState.value = _uiState.value.copy(isExporting = true, statusMessage = null, errorMessage = null)
            val leads = leadDao.getAllLeadsList()
            if (leads.isEmpty()) {
                _uiState.value = _uiState.value.copy(isExporting = false, errorMessage = "No leads in database to export.")
                return@launch
            }

            val result = ExportCrmService.pushToGoogleSheets(url, leads)
            _uiState.value = _uiState.value.copy(
                isExporting = false,
                statusMessage = result.getOrNull(),
                errorMessage = result.exceptionOrNull()?.message
            )
        }
    }

    fun exportAllToCustomWebhook() {
        viewModelScope.launch {
            val config = _uiState.value.crmConfig
            if (config.customWebhookUrl.isBlank()) {
                _uiState.value = _uiState.value.copy(errorMessage = "Please enter Custom Webhook / Zapier URL first.")
                return@launch
            }

            _uiState.value = _uiState.value.copy(isExporting = true, statusMessage = null, errorMessage = null)
            val leads = leadDao.getAllLeadsList()

            val result = ExportCrmService.pushToCustomWebhook(
                config.customWebhookUrl,
                config.customWebhookAuthHeader,
                config.customWebhookApiKey,
                leads
            )

            _uiState.value = _uiState.value.copy(
                isExporting = false,
                statusMessage = result.getOrNull(),
                errorMessage = result.exceptionOrNull()?.message
            )
        }
    }

    fun exportAllToVcf(context: Context) {
        viewModelScope.launch {
            val leads = leadDao.getAllLeadsList()
            if (leads.isEmpty()) {
                _uiState.value = _uiState.value.copy(errorMessage = "No leads available to export.")
                return@launch
            }
            ExportCrmService.shareVcfFile(context, leads)
        }
    }

    fun exportAllToCsv(context: Context) {
        viewModelScope.launch {
            val leads = leadDao.getAllLeadsList()
            if (leads.isEmpty()) {
                _uiState.value = _uiState.value.copy(errorMessage = "No leads available to export.")
                return@launch
            }
            ExportCrmService.shareCsvFile(context, leads)
        }
    }

    fun addAllToDeviceContacts(context: Context) {
        viewModelScope.launch {
            val leads = leadDao.getAllLeadsList()
            if (leads.isEmpty()) {
                _uiState.value = _uiState.value.copy(errorMessage = "No leads in database.")
                return@launch
            }
            for (lead in leads) {
                ContactsHelper.addToDeviceContacts(context, lead)
            }
        }
    }

    fun testGoogleSheetsConnection() {
        viewModelScope.launch {
            val url = _uiState.value.crmConfig.googleSheetsWebhookUrl
            if (url.isBlank()) {
                _uiState.value = _uiState.value.copy(errorMessage = "Please enter Google Sheets Webhook URL.")
                return@launch
            }

            _uiState.value = _uiState.value.copy(isTesting = true)
            val dummyLead = LeadEntity(
                fullName = "Ping Test",
                company = "LeadTap CRM Test",
                jobTitle = "System Verification",
                emailPrimary = "ping@leadtap.test",
                phonePrimary = "+15550001111",
                notes = "Test connection ping from LeadTap"
            )

            val res = ExportCrmService.pushToGoogleSheets(url, listOf(dummyLead))
            _uiState.value = _uiState.value.copy(
                isTesting = false,
                statusMessage = if (res.isSuccess) "Google Sheets Webhook Connected Successfully!" else null,
                errorMessage = res.exceptionOrNull()?.message
            )
        }
    }

    fun testHubSpotConnection() {
        viewModelScope.launch {
            val key = _uiState.value.crmConfig.hubspotApiKey
            if (key.isBlank()) {
                _uiState.value = _uiState.value.copy(errorMessage = "Please enter HubSpot Private App API Key.")
                return@launch
            }

            _uiState.value = _uiState.value.copy(isTesting = true)
            val dummyLead = LeadEntity(
                fullName = "HubSpot Ping",
                company = "LeadTap Integration",
                emailPrimary = "hubspot.ping@leadtap.test",
                phonePrimary = "+15551112222"
            )

            val res = ExportCrmService.pushToHubSpot(key, dummyLead)
            _uiState.value = _uiState.value.copy(
                isTesting = false,
                statusMessage = if (res.isSuccess) "HubSpot API Key Verified & Connected!" else null,
                errorMessage = res.exceptionOrNull()?.message
            )
        }
    }
}
