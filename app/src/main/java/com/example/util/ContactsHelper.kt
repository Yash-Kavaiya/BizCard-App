package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract
import android.widget.Toast
import com.example.data.local.LeadEntity

object ContactsHelper {

    fun dialPhone(context: Context, phoneNumber: String) {
        if (phoneNumber.isBlank()) return
        try {
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${phoneNumber.trim()}"))
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Cannot open dialer: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun openWhatsApp(context: Context, phoneNumber: String, initialMessage: String = "") {
        if (phoneNumber.isBlank()) return
        val cleanNumber = phoneNumber.replace(Regex("[^0-9+]"), "")
        try {
            val url = "https://api.whatsapp.com/send?phone=$cleanNumber&text=${Uri.encode(initialMessage)}"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Cannot open WhatsApp: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun sendEmail(context: Context, emailAddress: String, subject: String = "", body: String = "") {
        if (emailAddress.isBlank()) return
        try {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:${emailAddress.trim()}")
                putExtra(Intent.EXTRA_SUBJECT, subject)
                putExtra(Intent.EXTRA_TEXT, body)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Cannot open Email client: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun openWebsite(context: Context, url: String) {
        if (url.isBlank()) return
        try {
            var fullUrl = url.trim()
            if (!fullUrl.startsWith("http://") && !fullUrl.startsWith("https://")) {
                fullUrl = "https://$fullUrl"
            }
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(fullUrl))
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Cannot open URL: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun openLinkedIn(context: Context, linkedInProfileOrName: String) {
        if (linkedInProfileOrName.isBlank()) return
        try {
            val clean = linkedInProfileOrName.trim()
            val url = if (clean.startsWith("http") || clean.contains("linkedin.com")) {
                if (!clean.startsWith("http")) "https://$clean" else clean
            } else {
                "https://www.linkedin.com/search/results/all/?keywords=${Uri.encode(clean)}"
            }
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Cannot open LinkedIn: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun addToDeviceContacts(context: Context, lead: LeadEntity) {
        try {
            val intent = Intent(ContactsContract.Intents.Insert.ACTION).apply {
                type = ContactsContract.RawContacts.CONTENT_TYPE
                putExtra(ContactsContract.Intents.Insert.NAME, lead.fullName)
                putExtra(ContactsContract.Intents.Insert.JOB_TITLE, lead.jobTitle)
                putExtra(ContactsContract.Intents.Insert.COMPANY, lead.company)
                putExtra(ContactsContract.Intents.Insert.PHONE, lead.phonePrimary)
                putExtra(ContactsContract.Intents.Insert.SECONDARY_PHONE, lead.phoneSecondary)
                putExtra(ContactsContract.Intents.Insert.EMAIL, lead.emailPrimary)
                putExtra(ContactsContract.Intents.Insert.POSTAL, lead.address)
                putExtra(ContactsContract.Intents.Insert.NOTES, "Lead Source: ${lead.source.label} | ${lead.notes}")
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to save contact: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun shareText(context: Context, title: String, content: String) {
        try {
            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, content)
                type = "text/plain"
            }
            val shareIntent = Intent.createChooser(sendIntent, title)
            context.startActivity(shareIntent)
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to share: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
