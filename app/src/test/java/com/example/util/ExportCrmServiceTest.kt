package com.example.util

import com.example.data.local.LeadEntity
import com.example.data.model.LeadSource
import com.example.data.model.LeadStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExportCrmServiceTest {

  private val lead =
    LeadEntity(
      fullName = "Jane \"JJ\" Doe",
      company = "Acme, Inc.",
      jobTitle = "CTO",
      emailPrimary = "jane@acme.test",
      notes = "Line one\nLine two",
      status = LeadStatus.QUALIFIED,
      source = LeadSource.NFC,
      confidenceScore = 0.9f,
    )

  @Test
  fun csv_hasHeaderAndOneRowPerLead() {
    val lines = ExportCrmService.generateCsvContent(listOf(lead, lead)).trimEnd().lines()

    assertEquals(3, lines.size)
    assertTrue(lines[0].startsWith("Full Name,Company,Job Title"))
  }

  @Test
  fun csv_escapesQuotesCommasAndNewlines() {
    val row = ExportCrmService.generateCsvContent(listOf(lead)).trimEnd().lines()[1]

    assertTrue(row.startsWith("\"Jane \"\"JJ\"\" Doe\",\"Acme, Inc.\",\"CTO\""))
    assertTrue(row.contains("\"Line one Line two\""))
    assertTrue(row.contains("\"Qualified\",\"NFC Tap\",\"90%\""))
  }

  @Test
  fun vcf_containsOneCardPerLead() {
    val vcf = ExportCrmService.generateVcfContent(listOf(lead, lead.copy(fullName = "John Roe")))

    assertEquals(2, Regex("BEGIN:VCARD").findAll(vcf).count())
    assertEquals(2, Regex("END:VCARD").findAll(vcf).count())
    assertTrue(vcf.contains("FN:John Roe"))
    assertTrue(vcf.contains("EMAIL:jane@acme.test"))
  }

  @Test
  fun vcf_omitsBlankOptionalFields() {
    val vcf = ExportCrmService.generateVcfContent(listOf(LeadEntity(fullName = "Only Name")))

    assertFalse(vcf.contains("ORG:"))
    assertFalse(vcf.contains("TEL;"))
    assertFalse(vcf.contains("EMAIL:"))
  }

  @Test
  fun salesforceEndpoint_isBuiltFromInstanceHost() {
    val expected = "https://acme.my.salesforce.com/services/data/v58.0/sobjects/Lead"

    assertEquals(expected, ExportCrmService.salesforceLeadEndpoint("https://acme.my.salesforce.com").toString())
    assertEquals(expected, ExportCrmService.salesforceLeadEndpoint(" acme.my.salesforce.com/ ").toString())
    assertEquals(
      expected,
      ExportCrmService.salesforceLeadEndpoint("https://acme.my.salesforce.com/lightning/page?x=1#top").toString(),
    )
  }

  @Test
  fun salesforceEndpoint_rejectsBlankInvalidAndCleartextUrls() {
    assertNull(ExportCrmService.salesforceLeadEndpoint(""))
    assertNull(ExportCrmService.salesforceLeadEndpoint("   "))
    assertNull(ExportCrmService.salesforceLeadEndpoint("http://acme.my.salesforce.com"))
    assertNull(ExportCrmService.salesforceLeadEndpoint("ftp://acme.my.salesforce.com"))
    assertNull(ExportCrmService.salesforceLeadEndpoint("https://acme salesforce.com"))
  }
}
