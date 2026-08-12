package com.example.ui.screens.scan

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.LeadEntity
import com.example.data.model.ExtractedCardData
import com.example.data.model.LeadSource
import com.example.data.model.LeadStatus
import com.example.data.repository.LeadRepository
import com.example.util.VCardUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class ScanMode {
    CARD_OCR,
    QR_CODE
}

data class ScanUiState(
    val scanMode: ScanMode = ScanMode.CARD_OCR,
    val isProcessing: Boolean = false,
    val extractedData: ExtractedCardData? = null,
    val isBatchMode: Boolean = false,
    val batchCount: Int = 0,
    val errorMessage: String? = null,
    val capturedBitmap: Bitmap? = null
)

class ScanViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = LeadRepository(application.applicationContext)

    private val _uiState = MutableStateFlow(ScanUiState())
    val uiState: StateFlow<ScanUiState> = _uiState.asStateFlow()

    fun setScanMode(mode: ScanMode) {
        _uiState.value = _uiState.value.copy(scanMode = mode)
    }

    fun toggleBatchMode() {
        _uiState.value = _uiState.value.copy(isBatchMode = !_uiState.value.isBatchMode)
    }

    fun processQrCode(qrContent: String) {
        if (_uiState.value.isProcessing || _uiState.value.extractedData != null) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isProcessing = true,
                errorMessage = null
            )

            val extracted = VCardUtils.parseQrPayload(qrContent)

            _uiState.value = _uiState.value.copy(
                isProcessing = false,
                extractedData = extracted
            )
        }
    }

    fun processCardImage(bitmap: Bitmap, rawOcrText: String = "") {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isProcessing = true,
                errorMessage = null,
                capturedBitmap = bitmap
            )

            val extracted = repository.extractCardFromImage(bitmap, rawOcrText)

            _uiState.value = _uiState.value.copy(
                isProcessing = false,
                extractedData = extracted
            )
        }
    }

    fun saveExtractedLead(data: ExtractedCardData, notesOverride: String = "") {
        viewModelScope.launch {
            val entity = LeadEntity(
                fullName = data.fullName.ifBlank { "Scanned Card Contact" },
                jobTitle = data.jobTitle,
                company = data.company,
                department = data.department,
                phonePrimary = data.phonePrimary,
                phoneSecondary = data.phoneSecondary,
                emailPrimary = data.emailPrimary,
                website = data.website,
                linkedIn = data.linkedIn,
                twitter = data.twitter,
                address = data.address,
                industry = data.industry,
                notes = if (notesOverride.isNotBlank()) notesOverride else data.notes,
                status = LeadStatus.NEW,
                source = if (_uiState.value.scanMode == ScanMode.QR_CODE) LeadSource.QR else LeadSource.SCAN,
                tags = "Scanned, ${data.industry}".trim(',', ' '),
                confidenceScore = data.confidenceScore,
                reviewRequired = data.reviewRequired
            )

            val savedId = repository.insertLead(entity)

            if (_uiState.value.isBatchMode) {
                _uiState.value = _uiState.value.copy(
                    extractedData = null,
                    batchCount = _uiState.value.batchCount + 1
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    extractedData = null
                )
            }
        }
    }

    fun dismissReview() {
        _uiState.value = _uiState.value.copy(extractedData = null)
    }
}
