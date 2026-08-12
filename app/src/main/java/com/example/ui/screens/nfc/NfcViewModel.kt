package com.example.ui.screens.nfc

import android.app.Application
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.Tag
import android.nfc.tech.Ndef
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.LeadEntity
import com.example.data.model.LeadSource
import com.example.data.model.LeadStatus
import com.example.data.repository.LeadRepository
import com.example.util.VCardUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class NfcUiState(
    val isListening: Boolean = true,
    val isWritingMode: Boolean = false,
    val statusMessage: String = "Ready to tap NFC tag or device",
    val readLead: LeadEntity? = null,
    val isProcessing: Boolean = false,
    val writeSuccess: Boolean = false,
    val errorMessage: String? = null
)

class NfcViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = LeadRepository(application.applicationContext)

    private val _uiState = MutableStateFlow(NfcUiState())
    val uiState: StateFlow<NfcUiState> = _uiState.asStateFlow()

    fun setWritingMode(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(
            isWritingMode = enabled,
            statusMessage = if (enabled) "Hold NFC Tag near back of device to write profile" else "Ready to tap NFC tag or device"
        )
    }

    fun processNdefMessage(ndefMessage: NdefMessage) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isProcessing = true)

            val records = ndefMessage.records
            var vCardText = ""
            var urlText = ""

            for (record in records) {
                val payload = String(record.payload, Charsets.UTF_8)
                if (payload.contains("BEGIN:VCARD")) {
                    vCardText = payload
                    break
                } else if (payload.startsWith("http") || payload.startsWith("www")) {
                    urlText = payload
                }
            }

            if (vCardText.isNotBlank()) {
                val parsedLead = VCardUtils.parseVCardToLead(vCardText)
                _uiState.value = _uiState.value.copy(
                    isProcessing = false,
                    readLead = parsedLead,
                    statusMessage = "vCard Contact Read Successfully!"
                )
            } else if (urlText.isNotBlank()) {
                val lead = LeadEntity(
                    fullName = "NFC Web Contact",
                    website = urlText,
                    source = LeadSource.NFC,
                    notes = "Captured from NFC NDEF URL: $urlText"
                )
                _uiState.value = _uiState.value.copy(
                    isProcessing = false,
                    readLead = lead,
                    statusMessage = "NFC Web Link Read Successfully!"
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isProcessing = false,
                    errorMessage = "Unrecognized NFC NDEF Format"
                )
            }
        }
    }

    fun writeProfileToNfcTag(tag: Tag, vCardPayload: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isProcessing = true)
            try {
                val ndef = Ndef.get(tag)
                if (ndef != null) {
                    ndef.connect()
                    val record = NdefRecord.createMime("text/vcard", vCardPayload.toByteArray(Charsets.UTF_8))
                    val message = NdefMessage(arrayOf(record))
                    ndef.writeNdefMessage(message)
                    ndef.close()

                    _uiState.value = _uiState.value.copy(
                        isProcessing = false,
                        writeSuccess = true,
                        statusMessage = "Successfully wrote vCard profile to NFC Tag!"
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isProcessing = false,
                        errorMessage = "Tag does not support NDEF format"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isProcessing = false,
                    errorMessage = "Failed to write NFC tag: ${e.localizedMessage}"
                )
            }
        }
    }

    fun simulateNfcTap() {
        val sampleVCard = """
            BEGIN:VCARD
            VERSION:3.0
            N:Shah;Anand;;;
            FN:Anand Shah
            ORG:Apex Fintech Solutions
            TITLE:Chief Technology Officer
            TEL;TYPE=CELL:+1 (415) 555-0182
            EMAIL;TYPE=INTERNET:anand.shah@apexfintech.io
            URL:https://apexfintech.io
            NOTE:Met at TechCrunch Disrupt - Interested in AI pipeline integration
            END:VCARD
        """.trimIndent()

        val parsed = VCardUtils.parseVCardToLead(sampleVCard)
        _uiState.value = _uiState.value.copy(
            readLead = parsed,
            statusMessage = "Simulated NFC Contact Tap Received!"
        )
    }

    fun saveReadLead() {
        viewModelScope.launch {
            _uiState.value.readLead?.let { lead ->
                repository.insertLead(lead)
                _uiState.value = _uiState.value.copy(
                    readLead = null,
                    statusMessage = "Lead saved to your pipeline!"
                )
            }
        }
    }

    fun dismissReadLead() {
        _uiState.value = _uiState.value.copy(readLead = null)
    }
}
