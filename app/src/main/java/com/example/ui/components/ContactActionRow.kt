package com.example.ui.components

import android.content.Context
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.data.local.LeadEntity
import com.example.util.ContactsHelper
import com.example.util.VCardUtils

@Composable
fun ContactActionRow(
    lead: LeadEntity,
    modifier: Modifier = Modifier,
    context: Context = LocalContext.current
) {
    val isDark = isSystemInDarkTheme()

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        // Call Action
        if (lead.phonePrimary.isNotBlank()) {
            FilledTonalIconButton(
                onClick = { ContactsHelper.dialPhone(context, lead.phonePrimary) },
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = if (isDark) Color(0x3330D158) else Color(0xFFE8F8EC),
                    contentColor = if (isDark) Color(0xFF30D158) else Color(0xFF1E8238)
                )
            ) {
                Icon(imageVector = Icons.Default.Call, contentDescription = "Call")
            }
        }

        // WhatsApp Action
        if (lead.phonePrimary.isNotBlank()) {
            FilledTonalIconButton(
                onClick = { ContactsHelper.openWhatsApp(context, lead.phonePrimary, "Hi ${lead.fullName}, great connecting with you!") },
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = if (isDark) Color(0x3330D158) else Color(0xFFE8F8EC),
                    contentColor = if (isDark) Color(0xFF30D158) else Color(0xFF166534)
                )
            ) {
                Icon(imageVector = Icons.Outlined.Chat, contentDescription = "WhatsApp")
            }
        }

        // Email Action
        if (lead.emailPrimary.isNotBlank()) {
            FilledTonalIconButton(
                onClick = { ContactsHelper.sendEmail(context, lead.emailPrimary, subject = "Following up - BizCard AI") },
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = if (isDark) Color(0x330A84FF) else Color(0xFFE5F1FF),
                    contentColor = if (isDark) Color(0xFF64D2FF) else Color(0xFF0066CC)
                )
            ) {
                Icon(imageVector = Icons.Default.Email, contentDescription = "Email")
            }
        }

        // Website Action
        if (lead.website.isNotBlank()) {
            FilledTonalIconButton(
                onClick = { ContactsHelper.openWebsite(context, lead.website) },
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = if (isDark) Color(0x33BF5AF2) else Color(0xFFF7ECFE),
                    contentColor = if (isDark) Color(0xFFBF5AF2) else Color(0xFF8E24AA)
                )
            ) {
                Icon(imageVector = Icons.Default.Language, contentDescription = "Website")
            }
        }

        // Add to Contacts Action
        FilledTonalIconButton(
            onClick = { ContactsHelper.addToDeviceContacts(context, lead) },
            colors = IconButtonDefaults.filledTonalIconButtonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        ) {
            Icon(imageVector = Icons.Default.PersonAdd, contentDescription = "Save to Phonebook")
        }

        // Share Action
        FilledTonalIconButton(
            onClick = {
                val vCard = VCardUtils.generateVCardFromLead(lead)
                ContactsHelper.shareText(context, "Share ${lead.fullName}", vCard)
            },
            colors = IconButtonDefaults.filledTonalIconButtonColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurface
            )
        ) {
            Icon(imageVector = Icons.Default.Share, contentDescription = "Share vCard")
        }
    }
}

