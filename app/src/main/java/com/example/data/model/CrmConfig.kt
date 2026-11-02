package com.example.data.model

data class CrmConfig(
    val googleSheetsWebhookUrl: String = "",
    val hubspotApiKey: String = "",
    val salesforceApiKey: String = "",
    val salesforceInstanceUrl: String = "",
    val zohoApiKey: String = "",
    val customWebhookUrl: String = "",
    val customWebhookAuthHeader: String = "Authorization",
    val customWebhookApiKey: String = "",
    val autoExportEnabled: Boolean = false
) {
    /** True when at least one push target is configured (mirrors syncLeadToAllActiveIntegrations). */
    fun hasAnyIntegration(): Boolean =
        googleSheetsWebhookUrl.isNotBlank() ||
            hubspotApiKey.isNotBlank() ||
            salesforceApiKey.isNotBlank() ||
            zohoApiKey.isNotBlank() ||
            customWebhookUrl.isNotBlank()
}
