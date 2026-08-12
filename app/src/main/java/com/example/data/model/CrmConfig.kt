package com.example.data.model

data class CrmConfig(
    val googleSheetsWebhookUrl: String = "",
    val hubspotApiKey: String = "",
    val salesforceApiKey: String = "",
    val zohoApiKey: String = "",
    val customWebhookUrl: String = "",
    val customWebhookAuthHeader: String = "Authorization",
    val customWebhookApiKey: String = "",
    val autoExportEnabled: Boolean = false
)
