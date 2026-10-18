package com.example.util

import android.nfc.NdefMessage
import android.nfc.NdefRecord

sealed interface NdefPayload {
    data class VCard(val text: String) : NdefPayload
    data class Url(val url: String) : NdefPayload
    data object Unsupported : NdefPayload
}

/**
 * Extracts a contact payload from an NDEF message delivered by an NFC intent.
 *
 * MainActivity is exported, so any installed app can deliver a forged NFC intent; everything
 * here treats the message as untrusted input.
 */
object NdefPayloadParser {

    /** Larger than any real NFC tag (NTAG216 holds 888 bytes) while bounding forged payloads. */
    const val MAX_MESSAGE_BYTES = 32 * 1024

    fun parse(message: NdefMessage): NdefPayload {
        if (message.byteArrayLength > MAX_MESSAGE_BYTES) return NdefPayload.Unsupported

        var url: String? = null
        for (record in message.records) {
            val text = when {
                record.tnf == NdefRecord.TNF_MIME_MEDIA -> record.payload.toString(Charsets.UTF_8)
                record.isWellKnown(NdefRecord.RTD_TEXT) -> decodeTextRecord(record.payload)
                else -> null
            }
            if (text != null && text.contains("BEGIN:VCARD", ignoreCase = true)) {
                return NdefPayload.VCard(text)
            }
            if (url == null) {
                url = webUrlOrNull(record.toUri()?.toString()) ?: webUrlOrNull(text)
            }
        }
        return url?.let { NdefPayload.Url(it) } ?: NdefPayload.Unsupported
    }

    private fun NdefRecord.isWellKnown(rtd: ByteArray): Boolean =
        tnf == NdefRecord.TNF_WELL_KNOWN && type.contentEquals(rtd)

    /** NFC Forum Text RTD: status byte (bit 7 = UTF-16, bits 0-5 = language code length). */
    private fun decodeTextRecord(payload: ByteArray): String? {
        if (payload.isEmpty()) return null
        val status = payload[0].toInt()
        val languageCodeLength = status and 0x3F
        val textStart = 1 + languageCodeLength
        if (textStart > payload.size) return null
        val charset = if (status and 0x80 != 0) Charsets.UTF_16 else Charsets.UTF_8
        return String(payload, textStart, payload.size - textStart, charset)
    }

    private fun webUrlOrNull(candidate: String?): String? {
        val trimmed = candidate?.trim().orEmpty()
        return when {
            trimmed.startsWith("https://", ignoreCase = true) ||
                trimmed.startsWith("http://", ignoreCase = true) -> trimmed
            trimmed.startsWith("www.", ignoreCase = true) -> "https://$trimmed"
            else -> null
        }
    }
}
