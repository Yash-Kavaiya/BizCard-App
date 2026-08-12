package com.example.ui.screens.leads

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.LeadEntity
import com.example.data.model.LeadSource
import com.example.data.model.LeadStatus
import com.example.data.repository.LeadRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class LeadsFilterState(
    val searchQuery: String = "",
    val selectedStatus: LeadStatus? = null,
    val selectedSource: LeadSource? = null,
    val showStarredOnly: Boolean = false,
    val isRefreshing: Boolean = false
)

class LeadsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = LeadRepository(application.applicationContext)

    private val _filterState = MutableStateFlow(LeadsFilterState())
    val filterState: StateFlow<LeadsFilterState> = _filterState.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val filteredLeads: StateFlow<List<LeadEntity>> = _filterState.flatMapLatest { filter ->
        repository.searchLeads(filter.searchQuery)
    }.combine(_filterState) { list, filter ->
        list.filter { lead ->
            val matchesStatus = filter.selectedStatus == null || lead.status == filter.selectedStatus
            val matchesSource = filter.selectedSource == null || lead.source == filter.selectedSource
            val matchesStarred = !filter.showStarredOnly || lead.isStarred
            matchesStatus && matchesSource && matchesStarred
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun setSearchQuery(query: String) {
        _filterState.value = _filterState.value.copy(searchQuery = query)
    }

    fun setStatusFilter(status: LeadStatus?) {
        _filterState.value = _filterState.value.copy(selectedStatus = status)
    }

    fun setSourceFilter(source: LeadSource?) {
        _filterState.value = _filterState.value.copy(selectedSource = source)
    }

    fun toggleStarredFilter() {
        _filterState.value = _filterState.value.copy(
            showStarredOnly = !_filterState.value.showStarredOnly
        )
    }

    fun toggleStarLead(lead: LeadEntity) {
        viewModelScope.launch {
            repository.updateLead(lead.copy(isStarred = !lead.isStarred))
        }
    }

    fun updateLeadStatus(lead: LeadEntity, newStatus: LeadStatus) {
        viewModelScope.launch {
            repository.updateLead(lead.copy(status = newStatus, updatedTimestamp = System.currentTimeMillis()))
        }
    }

    fun deleteLead(leadId: Long) {
        viewModelScope.launch {
            repository.deleteLead(leadId)
        }
    }

    suspend fun getLeadById(id: Long): LeadEntity? {
        return repository.getLeadById(id)
    }

    fun saveLead(lead: LeadEntity) {
        viewModelScope.launch {
            if (lead.id == 0L) {
                repository.insertLead(lead)
            } else {
                repository.updateLead(lead)
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _filterState.value = _filterState.value.copy(isRefreshing = true)
            kotlinx.coroutines.delay(650)
            _filterState.value = _filterState.value.copy(isRefreshing = false)
        }
    }
}
