package com.example.data.sync

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.work.ListenableWorker
import androidx.work.testing.TestListenableWorkerBuilder
import androidx.work.workDataOf
import com.example.data.model.CrmConfig
import com.example.util.UserPreferences
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class AutoExportWorkerTest {

  private lateinit var context: Context
  private lateinit var prefs: UserPreferences

  @Before
  fun setUp() {
    context = ApplicationProvider.getApplicationContext()
    prefs = UserPreferences.get(context)
  }

  private fun runWorker(leadId: Long?): ListenableWorker.Result = runBlocking {
    val builder = TestListenableWorkerBuilder<AutoExportWorker>(context)
    if (leadId != null) builder.setInputData(workDataOf("lead_id" to leadId))
    builder.build().doWork()
  }

  @Test
  fun missingLeadIdFails() {
    assertEquals(ListenableWorker.Result.failure(), runWorker(null))
  }

  @Test
  fun disabledAutoExportIsANoOp() {
    prefs.saveCrmConfig(CrmConfig(customWebhookUrl = "https://example.com/hook", autoExportEnabled = false))
    assertEquals(ListenableWorker.Result.success(), runWorker(42L))
  }

  @Test
  fun deletedLeadIsSkipped() {
    prefs.saveCrmConfig(CrmConfig(customWebhookUrl = "https://example.com/hook", autoExportEnabled = true))
    assertEquals(ListenableWorker.Result.success(), runWorker(999_999L))
  }

  @Test
  fun preferencesAreSharedAcrossCallers() {
    val other = UserPreferences.get(context)
    assertSame(prefs, other)
    val config = CrmConfig(hubspotApiKey = "pat-123", autoExportEnabled = true)
    prefs.saveCrmConfig(config)
    assertEquals(config, other.crmConfigFlow.value)
  }

  @Test
  fun hasAnyIntegrationReflectsConfiguredTargets() {
    assertFalse(CrmConfig().hasAnyIntegration())
    assertFalse(CrmConfig(autoExportEnabled = true, customWebhookAuthHeader = "X-Key").hasAnyIntegration())
    assertTrue(CrmConfig(googleSheetsWebhookUrl = "https://script.google.com/x").hasAnyIntegration())
    assertTrue(CrmConfig(zohoApiKey = "z").hasAnyIntegration())
    assertTrue(CrmConfig(salesforceApiKey = "s").hasAnyIntegration())
  }
}
