package com.example.data.sync

import android.content.Context
import android.util.Log
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.data.local.AppDatabase
import com.example.util.ExportCrmService
import com.example.util.UserPreferences
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Pushes a newly created lead to every configured integration (Google Sheets, HubSpot,
 * Salesforce, Zoho, custom webhook) when "Auto-Export New Leads" is enabled.
 *
 * Runs only with network connectivity and retries network failures with exponential backoff, so a
 * card scanned offline at an event is exported once the device is back online.
 */
class AutoExportWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val leadId = inputData.getLong(KEY_LEAD_ID, 0L)
        if (leadId <= 0L) return Result.failure()

        val config = UserPreferences.get(applicationContext).getCrmConfig()
        // The user may have switched auto-export off while this job was waiting for network.
        if (!config.autoExportEnabled) return Result.success()

        val lead = AppDatabase.getDatabase(applicationContext).leadDao().getLeadById(leadId)
            ?: return Result.success() // Deleted before it could be exported.

        val results = ExportCrmService.syncLeadToAllActiveIntegrations(config, lead)
        val failures = results.mapNotNull { it.exceptionOrNull() }
        if (failures.isEmpty()) return Result.success()

        failures.forEach { Log.w(TAG, "Auto-export of lead $leadId failed: ${it.message}") }

        // Only network errors are worth retrying; HTTP 4xx (bad token, bad URL) will not fix itself.
        val transient = failures.any { it is IOException }
        return if (transient && runAttemptCount < MAX_ATTEMPTS) Result.retry() else Result.success()
    }

    companion object {
        private const val TAG = "AutoExportWorker"
        private const val KEY_LEAD_ID = "lead_id"
        private const val MAX_ATTEMPTS = 5

        /** Enqueues an export of [leadId] if auto-export is enabled and an integration is set. */
        fun enqueueIfEnabled(context: Context, leadId: Long) {
            if (leadId <= 0L) return
            val config = UserPreferences.get(context).getCrmConfig()
            if (!config.autoExportEnabled || !config.hasAnyIntegration()) return

            val request = OneTimeWorkRequestBuilder<AutoExportWorker>()
                .setInputData(workDataOf(KEY_LEAD_ID to leadId))
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
                .build()

            WorkManager.getInstance(context.applicationContext)
                .enqueueUniqueWork("auto_export_lead_$leadId", ExistingWorkPolicy.KEEP, request)
        }
    }
}
