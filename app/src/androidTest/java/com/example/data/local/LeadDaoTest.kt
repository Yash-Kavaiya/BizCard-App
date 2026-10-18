package com.example.data.local

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.data.model.LeadSource
import com.example.data.model.LeadStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/** Runs against the device's real SQLite engine with an in-memory database. */
@RunWith(AndroidJUnit4::class)
class LeadDaoTest {

  private lateinit var db: AppDatabase
  private lateinit var dao: LeadDao

  @Before
  fun setUp() {
    db =
      Room.inMemoryDatabaseBuilder(
          InstrumentationRegistry.getInstrumentation().targetContext,
          AppDatabase::class.java,
        )
        .build()
    dao = db.leadDao()
  }

  @After
  fun tearDown() {
    db.close()
  }

  @Test
  fun insertAndReadBack() = runTest {
    val id = dao.insertLead(LeadEntity(fullName = "Jane Doe", company = "Acme"))

    val stored = dao.getLeadById(id)
    assertEquals("Jane Doe", stored?.fullName)
    assertEquals("Acme", stored?.company)
  }

  @Test
  fun starredLeadsAreListedFirst() = runTest {
    dao.insertLead(LeadEntity(fullName = "Older", createdTimestamp = 1_000))
    dao.insertLead(LeadEntity(fullName = "Starred", isStarred = true, createdTimestamp = 500))
    dao.insertLead(LeadEntity(fullName = "Newest", createdTimestamp = 2_000))

    val names = dao.getAllLeads().first().map { it.fullName }
    assertEquals(listOf("Starred", "Newest", "Older"), names)
  }

  @Test
  fun searchMatchesCompanyAndTags() = runTest {
    dao.insertLead(LeadEntity(fullName = "A", company = "FinPulse"))
    dao.insertLead(LeadEntity(fullName = "B", tags = "fintech, ai"))
    dao.insertLead(LeadEntity(fullName = "C", company = "Other"))

    assertEquals(listOf("A"), dao.searchLeads("Pulse").first().map { it.fullName })
    assertEquals(listOf("B"), dao.searchLeads("fintech").first().map { it.fullName })
  }

  @Test
  fun filtersByStatusAndSource() = runTest {
    dao.insertLead(LeadEntity(fullName = "Q", status = LeadStatus.QUALIFIED, source = LeadSource.NFC))
    dao.insertLead(LeadEntity(fullName = "N", status = LeadStatus.NEW, source = LeadSource.SCAN))

    assertEquals(listOf("Q"), dao.getLeadsByStatus(LeadStatus.QUALIFIED).first().map { it.fullName })
    assertEquals(listOf("N"), dao.getLeadsBySource(LeadSource.SCAN).first().map { it.fullName })
    assertEquals(1, dao.getCountByStatus(LeadStatus.NEW).first())
  }

  @Test
  fun deleteById() = runTest {
    val id = dao.insertLead(LeadEntity(fullName = "Temp"))

    dao.deleteLeadById(id)

    assertNull(dao.getLeadById(id))
    assertEquals(0, dao.getLeadCount().first())
  }
}
