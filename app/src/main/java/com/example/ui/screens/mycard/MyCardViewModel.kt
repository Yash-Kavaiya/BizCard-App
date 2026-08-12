package com.example.ui.screens.mycard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.PersonalCard
import com.example.util.UserPreferences
import com.example.util.VCardUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class MyCardUiState(
    val personalCard: PersonalCard = PersonalCard(),
    val vCardPayload: String = "",
    val isEditing: Boolean = false,
    val isSaved: Boolean = false
)

class MyCardViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = UserPreferences(application.applicationContext)

    private val _uiState = MutableStateFlow(MyCardUiState())
    val uiState: StateFlow<MyCardUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            prefs.personalCardFlow.collect { card ->
                val vCard = VCardUtils.generateVCardFromPersonalCard(card)
                _uiState.value = _uiState.value.copy(
                    personalCard = card,
                    vCardPayload = vCard
                )
            }
        }
    }

    fun toggleEditing() {
        _uiState.value = _uiState.value.copy(isEditing = !_uiState.value.isEditing)
    }

    fun saveCard(card: PersonalCard) {
        viewModelScope.launch {
            prefs.savePersonalCard(card)
            val vCard = VCardUtils.generateVCardFromPersonalCard(card)
            _uiState.value = _uiState.value.copy(
                personalCard = card,
                vCardPayload = vCard,
                isEditing = false,
                isSaved = true
            )
        }
    }
}
