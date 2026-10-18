package com.example.util

import com.example.data.model.LeadSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class VCardUtilsTest {

  @Test
  fun parseVCard_extractsCoreFields() {
    val vCard =
      """
      BEGIN:VCARD
      VERSION:3.0
      N:Shah;Anand;;;
      FN:Anand Shah
      ORG:Apex Fintech Solutions
      TITLE:Chief Technology Officer
      TEL;TYPE=CELL:+1 (415) 555-0182
      EMAIL;TYPE=INTERNET:anand.shah@apexfintech.io
      URL:https://apexfintech.io
      NOTE:Met at TechCrunch Disrupt
      END:VCARD
      """
        .trimIndent()

    val data = VCardUtils.parseVCard(vCard)

    assertEquals("Anand Shah", data.fullName)
    assertEquals("Apex Fintech Solutions", data.company)
    assertEquals("Chief Technology Officer", data.jobTitle)
    assertEquals("+1 (415) 555-0182", data.phonePrimary)
    assertEquals("anand.shah@apexfintech.io", data.emailPrimary)
    assertEquals("https://apexfintech.io", data.website)
    assertEquals("Met at TechCrunch Disrupt", data.notes)
  }

  @Test
  fun generateVCard_roundTripsThroughParser() {
    val vCard =
      VCardUtils.generateVCard(
        name = "Priya Sharma",
        title = "Chief Product Officer",
        company = "FinPulse Technologies",
        phone = "+91 98765 43210",
        email = "priya.sharma@finpulse.io",
        website = "https://finpulse.io",
      )

    assertTrue(vCard.startsWith("BEGIN:VCARD"))
    assertTrue(vCard.trimEnd().endsWith("END:VCARD"))
    assertTrue(vCard.contains("N:Sharma;Priya;;;"))

    val parsed = VCardUtils.parseVCard(vCard)
    assertEquals("Priya Sharma", parsed.fullName)
    assertEquals("Chief Product Officer", parsed.jobTitle)
    assertEquals("FinPulse Technologies", parsed.company)
    assertEquals("+91 98765 43210", parsed.phonePrimary)
    assertEquals("priya.sharma@finpulse.io", parsed.emailPrimary)
    assertEquals("https://finpulse.io", parsed.website)
  }

  @Test
  fun parseVCardToLead_marksSourceAsNfc() {
    val lead = VCardUtils.parseVCardToLead("BEGIN:VCARD\nFN:Jane Doe\nEND:VCARD")

    assertEquals("Jane Doe", lead.fullName)
    assertEquals(LeadSource.NFC, lead.source)
  }

  @Test
  fun parseQrPayload_meCard() {
    val data =
      VCardUtils.parseQrPayload(
        "MECARD:N:Patel,Rajesh;ORG:FinTech Global;TEL:+15553334444;" +
          "EMAIL:rajesh@fintechglobal.com;URL:https://fintechglobal.com;;"
      )

    assertEquals("Patel Rajesh", data.fullName)
    assertEquals("FinTech Global", data.company)
    assertEquals("+15553334444", data.phonePrimary)
    assertEquals("rajesh@fintechglobal.com", data.emailPrimary)
    assertEquals("https://fintechglobal.com", data.website)
  }

  @Test
  fun parseQrPayload_url() {
    val data = VCardUtils.parseQrPayload("  https://apexinnovations.com/card/alex-morgan ")

    assertEquals("https://apexinnovations.com/card/alex-morgan", data.website)
    assertEquals("Digital Card Contact", data.fullName)
  }

  @Test
  fun parseQrPayload_unknownTextIsKeptInNotes() {
    val data = VCardUtils.parseQrPayload("hello world")

    assertEquals("QR Code Lead", data.fullName)
    assertTrue(data.notes.contains("hello world"))
  }
}
