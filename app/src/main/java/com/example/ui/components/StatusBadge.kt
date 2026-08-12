package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LeadSource
import com.example.data.model.LeadStatus

@Composable
fun StatusBadge(
    status: LeadStatus,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val (backgroundColor, textColor) = when (status) {
        LeadStatus.NEW -> if (isDark) Color(0x330A84FF) to Color(0xFF64D2FF) else Color(0xFFE5F1FF) to Color(0xFF0066CC)
        LeadStatus.CONTACTED -> if (isDark) Color(0x33FF9F0A) to Color(0xFFFFD60A) else Color(0xFFFFF4E5) to Color(0xFFC77000)
        LeadStatus.QUALIFIED -> if (isDark) Color(0x335E5CE6) to Color(0xFFBF5AF2) else Color(0xFFEEEDFD) to Color(0xFF4740B4)
        LeadStatus.CONVERTED -> if (isDark) Color(0x3330D158) to Color(0xFF30D158) else Color(0xFFE8F8EC) to Color(0xFF1E8238)
        LeadStatus.ARCHIVED -> if (isDark) Color(0x338E8E93) to Color(0xFFA2A2A7) else Color(0xFFF2F2F7) to Color(0xFF636366)
    }

    Box(
        modifier = modifier
            .background(backgroundColor, CircleShape)
            .padding(horizontal = 10.dp, vertical = 3.dp)
    ) {
        Text(
            text = status.label,
            color = textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun SourceBadge(
    source: LeadSource,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val (backgroundColor, textColor) = when (source) {
        LeadSource.SCAN -> if (isDark) Color(0x33BF5AF2) to Color(0xFFBF5AF2) else Color(0xFFF7ECFE) to Color(0xFF8E24AA)
        LeadSource.NFC -> if (isDark) Color(0x3364D2FF) to Color(0xFF64D2FF) else Color(0xFFE1F5FE) to Color(0xFF0277BD)
        LeadSource.QR -> if (isDark) Color(0x33FF9F0A) to Color(0xFFFFD60A) else Color(0xFFFFF3E0) to Color(0xFFE65100)
        LeadSource.MANUAL -> if (isDark) Color(0x338E8E93) to Color(0xFFA2A2A7) else Color(0xFFE5E5EA) to Color(0xFF48484A)
    }

    Box(
        modifier = modifier
            .background(backgroundColor, CircleShape)
            .padding(horizontal = 9.dp, vertical = 2.dp)
    ) {
        Text(
            text = source.label,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

