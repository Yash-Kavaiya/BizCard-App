package com.example.ui.screens.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.local.LeadEntity
import com.example.data.model.AiCrmInsights
import com.example.data.model.LeadSource
import com.example.data.model.LeadStatus
import com.example.data.repository.LeadRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DashboardUiState(
    val totalLeads: Int = 0,
    val newLeadsCount: Int = 0,
    val conversionRate: Int = 0,
    val scanVsNfcRatio: String = "0 / 0",
    val leadsByStatus: Map<LeadStatus, Int> = emptyMap(),
    val aiInsights: AiCrmInsights = AiCrmInsights(),
    val isGeneratingInsights: Boolean = false,
    val isRefreshing: Boolean = false,
    val recentLeads: List<LeadEntity> = emptyList()
)

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = LeadRepository(application.applicationContext)

    val allLeads: StateFlow<List<LeadEntity>> = repository.allLeads.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            if (BuildConfig.DEBUG) {
                repository.seedInitialDataIfEmpty()
            }
            allLeads.collect { leads ->
                calculateMetrics(leads)
            }
        }
    }

    private fun calculateMetrics(leads: List<LeadEntity>) {
        val total = leads.size
        val newLeads = leads.count { it.status == LeadStatus.NEW }
        val converted = leads.count { it.status == LeadStatus.CONVERTED }
        val conversion = if (total > 0) (converted * 100 / total) else 0

        val scans = leads.count { it.source == LeadSource.SCAN }
        val nfc = leads.count { it.source == LeadSource.NFC }

        val statusMap = LeadStatus.values().associateWith { status ->
            leads.count { it.status == status }
        }

        _uiState.value = _uiState.value.copy(
            totalLeads = total,
            newLeadsCount = newLeads,
            conversionRate = conversion,
            scanVsNfcRatio = "$scans Scans / $nfc NFC",
            leadsByStatus = statusMap,
            recentLeads = leads.take(5)
        )
    }

    fun generateAiInsights() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isGeneratingInsights = true)
            val insights = repository.generateCrmInsights()
            _uiState.value = _uiState.value.copy(
                aiInsights = insights,
                isGeneratingInsights = false
            )
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRefreshing = true)
            val leads = allLeads.value
            calculateMetrics(leads)
            kotlinx.coroutines.delay(650)
            _uiState.value = _uiState.value.copy(isRefreshing = false)
        }
    }
}
