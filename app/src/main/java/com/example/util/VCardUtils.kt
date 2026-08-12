package com.example.util

import com.example.data.local.LeadEntity
import com.example.data.model.ExtractedCardData
import com.example.data.model.LeadSource
import com.example.data.model.PersonalCard

object VCardUtils {

    fun generateVCard(
        name: String,
        title: String = "",
        company: String = "",
        phone: String = "",
        email: String = "",
        website: String = "",
        address: String = "",
        notes: String = ""
    ): String {
        val nameParts = name.trim().split(" ")
        val lastName = if (nameParts.size > 1) nameParts.last() else ""
        val firstName = if (nameParts.isNotEmpty()) nameParts.first() else name

        return StringBuilder().apply {
            appendLine("BEGIN:VCARD")
            appendLine("VERSION:3.0")
            appendLine("N:$lastName;$firstName;;;")
            appendLine("FN:$name")
            if (company.isNotBlank()) appendLine("ORG:$company")
            if (title.isNotBlank()) appendLine("TITLE:$title")
            if (phone.isNotBlank()) appendLine("TEL;TYPE=CELL,VOICE:$phone")
            if (email.isNotBlank()) appendLine("EMAIL;TYPE=INTERNET:$email")
            if (website.isNotBlank()) appendLine("URL:$website")
            if (address.isNotBlank()) appendLine("ADR;TYPE=WORK:;;$address;;;;")
            if (notes.isNotBlank()) appendLine("NOTE:$notes")
            appendLine("END:VCARD")
        }.toString()
    }

    fun generateVCardFromLead(lead: LeadEntity): String {
        return generateVCard(
            name = lead.fullName,
            title = lead.jobTitle,
            company = lead.company,
            phone = lead.phonePrimary.ifBlank { lead.phoneSecondary },
            email = lead.emailPrimary,
            website = lead.website,
            address = lead.address,
            notes = lead.notes
        )
    }

    fun generateVCardFromPersonalCard(card: PersonalCard): String {
        return generateVCard(
            name = card.name,
            title = card.title,
            company = card.company,
            phone = card.phone,
            email = card.email,
            website = card.website,
            address = card.address,
            notes = card.bio
        )
    }

    fun parseVCard(vCardText: String): ExtractedCardData {
        var fn = ""
        var org = ""
        var title = ""
        var tel = ""
        var email = ""
        var url = ""
        var adr = ""
        var note = ""

        vCardText.lines().forEach { line ->
            val trimmed = line.trim()
            when {
                trimmed.startsWith("FN:", ignoreCase = true) -> fn = trimmed.substringAfter(":")
                trimmed.startsWith("ORG:", ignoreCase = true) -> org = trimmed.substringAfter(":")
                trimmed.startsWith("TITLE:", ignoreCase = true) -> title = trimmed.substringAfter(":")
                trimmed.startsWith("TEL", ignoreCase = true) -> if (tel.isBlank()) tel = trimmed.substringAfter(":")
                trimmed.startsWith("EMAIL", ignoreCase = true) -> if (email.isBlank()) email = trimmed.substringAfter(":")
                trimmed.startsWith("URL", ignoreCase = true) -> url = trimmed.substringAfter(":")
                trimmed.startsWith("ADR", ignoreCase = true) -> adr = trimmed.substringAfter(":").replace(";", " ").trim()
                trimmed.startsWith("NOTE:", ignoreCase = true) -> note = trimmed.substringAfter(":")
            }
        }

        return ExtractedCardData(
            fullName = fn.ifBlank { "NFC Lead" },
            jobTitle = title,
            company = org,
            phonePrimary = tel,
            emailPrimary = email,
            website = url,
            address = adr,
            notes = note.ifBlank { "Imported via NFC Tap vCard" },
            confidenceScore = 1.0f
        )
    }

    fun parseVCardToLead(vCardText: String): LeadEntity {
        val extracted = parseVCard(vCardText)
        return LeadEntity(
            fullName = extracted.fullName,
            jobTitle = extracted.jobTitle,
            company = extracted.company,
            phonePrimary = extracted.phonePrimary,
            emailPrimary = extracted.emailPrimary,
            website = extracted.website,
            address = extracted.address,
            notes = extracted.notes,
            source = LeadSource.NFC
        )
    }

    fun parseQrPayload(payload: String): ExtractedCardData {
        val trimmed = payload.trim()
        return when {
            trimmed.contains("BEGIN:VCARD", ignoreCase = true) -> {
                val parsed = parseVCard(trimmed)
                parsed.copy(
                    fullName = parsed.fullName.ifBlank { "QR vCard Lead" },
                    notes = if (parsed.notes.isNotBlank() && !parsed.notes.startsWith("Imported via")) {
                        "${parsed.notes} (Scanned via CameraX QR vCard)"
                    } else {
                        "Scanned via CameraX QR Code (vCard)"
                    }
                )
            }
            trimmed.startsWith("MECARD:", ignoreCase = true) -> {
                parseMeCard(trimmed)
            }
            trimmed.startsWith("http://", ignoreCase = true) || trimmed.startsWith("https://", ignoreCase = true) -> {
                ExtractedCardData(
                    fullName = "Digital Card Contact",
                    website = trimmed,
                    notes = "Scanned Digital Business Card URL: $trimmed",
                    confidenceScore = 0.95f
                )
            }
            else -> {
                ExtractedCardData(
                    fullName = "QR Code Lead",
                    notes = "Scanned QR Payload: $trimmed",
                    confidenceScore = 0.85f
                )
            }
        }
    }

    private fun parseMeCard(meCardText: String): ExtractedCardData {
        var name = ""
        var org = ""
        var tel = ""
        var email = ""
        var url = ""
        var note = ""

        val body = meCardText.substringAfter("MECARD:").trimEnd(';')
        body.split(";").forEach { part ->
            val pair = part.split(":", limit = 2)
            if (pair.size == 2) {
                val key = pair[0].trim().uppercase()
                val value = pair[1].trim()
                when (key) {
                    "N" -> name = value.replace(",", " ")
                    "ORG" -> org = value
                    "TEL" -> tel = value
                    "EMAIL" -> email = value
                    "URL" -> url = value
                    "NOTE" -> note = value
                }
            }
        }

        return ExtractedCardData(
            fullName = name.ifBlank { "QR MECARD Contact" },
            company = org,
            phonePrimary = tel,
            emailPrimary = email,
            website = url,
            notes = if (note.isNotBlank()) note else "Scanned via CameraX MECARD QR Code",
            confidenceScore = 1.0f
        )
    }
}
