package com.example.data.local

import com.example.data.model.LeadSource
import com.example.data.model.LeadStatus
import com.example.data.model.RelationshipType
import org.junit.Assert.assertEquals
import org.junit.Test

class ConvertersTest {

  private val converters = Converters()

  @Test
  fun enumsRoundTripByName() {
    LeadStatus.entries.forEach { assertEquals(it, converters.toStatus(converters.fromStatus(it))) }
    LeadSource.entries.forEach { assertEquals(it, converters.toSource(converters.fromSource(it))) }
    RelationshipType.entries.forEach {
      assertEquals(it, converters.toRelationship(converters.fromRelationship(it)))
    }
  }

  @Test
  fun unknownValuesFallBackToDefaults() {
    assertEquals(LeadStatus.NEW, converters.toStatus("NOT_A_STATUS"))
    assertEquals(LeadSource.SCAN, converters.toSource(""))
    assertEquals(RelationshipType.PROSPECT, converters.toRelationship("legacy"))
  }
}
