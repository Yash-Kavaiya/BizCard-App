package com.example.ui.screens.mycard

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.PersonalCard
import com.example.ui.components.QrCodeCanvas
import com.example.util.ContactsHelper
import com.example.util.WebsiteGenerator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyCardScreen(
    viewModel: MyCardViewModel
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val currentCard = state.personalCard

    var name by remember(currentCard) { mutableStateOf(currentCard.name) }
    var title by remember(currentCard) { mutableStateOf(currentCard.title) }
    var company by remember(currentCard) { mutableStateOf(currentCard.company) }
    var phone by remember(currentCard) { mutableStateOf(currentCard.phone) }
    var email by remember(currentCard) { mutableStateOf(currentCard.email) }
    var website by remember(currentCard) { mutableStateOf(currentCard.website) }
    var linkedIn by remember(currentCard) { mutableStateOf(currentCard.linkedIn) }
    var bio by remember(currentCard) { mutableStateOf(currentCard.bio) }

    var showWebsiteSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Digital Business Card", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(
                        onClick = { viewModel.toggleEditing() },
                        modifier = Modifier.testTag("toggle_edit_my_card_button")
                    ) {
                        Icon(
                            imageVector = if (state.isEditing) Icons.Default.Save else Icons.Default.Edit,
                            contentDescription = "Edit Card",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Virtual Business Card Graphic Canvas
            val digitalCardShape = RoundedCornerShape(26.dp)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("my_digital_card_preview"),
                shape = digitalCardShape,
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(Color(0xFF1C1C1E), Color(0xFF2C2C2E))
                            )
                        )
                        .padding(24.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = currentCard.name.ifBlank { "Your Name" },
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = currentCard.title.ifBlank { "Title / Role" },
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Color(0xFFA2A2A7)
                                )
                                Text(
                                    text = currentCard.company.ifBlank { "Company Name" },
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF64D2FF)
                                )
                            }

                            // Dynamic QR Code Module Canvas
                            QrCodeCanvas(
                                payload = state.vCardPayload,
                                modifier = Modifier.size(90.dp),
                                darkColor = Color(0xFF1C1C1E),
                                lightColor = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            if (currentCard.phone.isNotBlank()) {
                                Text("Phone: ${currentCard.phone}", color = Color(0xFFE5E5EA), fontSize = 13.sp)
                            }
                            if (currentCard.email.isNotBlank()) {
                                Text("Email: ${currentCard.email}", color = Color(0xFFE5E5EA), fontSize = 13.sp)
                            }
                            if (currentCard.website.isNotBlank()) {
                                Text("Web: ${currentCard.website}", color = Color(0xFFE5E5EA), fontSize = 13.sp)
                            }
                        }
                    }
                }
            }

            // Share Actions & Website Preview Trigger
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        ContactsHelper.shareText(
                            context,
                            "Share ${currentCard.name}'s vCard",
                            state.vCardPayload
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("share_vcard_button"),
                    shape = CircleShape
                ) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Share vCard", fontWeight = FontWeight.SemiBold)
                }

                OutlinedButton(
                    onClick = { showWebsiteSheet = true },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("preview_card_website_button"),
                    shape = CircleShape
                ) {
                    Icon(imageVector = Icons.Default.Language, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Preview Website", fontWeight = FontWeight.SemiBold)
                }
            }

            // Profile Edit Form
            AnimatedVisibility(visible = state.isEditing) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Edit Profile Attributes",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Full Name") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("edit_my_name_input"),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = title,
                                onValueChange = { title = it },
                                label = { Text("Job Title") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            OutlinedTextField(
                                value = company,
                                onValueChange = { company = it },
                                label = { Text("Company") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = phone,
                                onValueChange = { phone = it },
                                label = { Text("Phone Number") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            OutlinedTextField(
                                value = email,
                                onValueChange = { email = it },
                                label = { Text("Email Address") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = website,
                                onValueChange = { website = it },
                                label = { Text("Website") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            OutlinedTextField(
                                value = linkedIn,
                                onValueChange = { linkedIn = it },
                                label = { Text("LinkedIn") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }

                        OutlinedTextField(
                            value = bio,
                            onValueChange = { bio = it },
                            label = { Text("Bio / Tagline") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2,
                            shape = RoundedCornerShape(12.dp)
                        )

                        Button(
                            onClick = {
                                val updated = currentCard.copy(
                                    name = name,
                                    title = title,
                                    company = company,
                                    phone = phone,
                                    email = email,
                                    website = website,
                                    linkedIn = linkedIn,
                                    bio = bio
                                )
                                viewModel.saveCard(updated)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("save_my_card_button"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Save, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Save Digital Card")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(60.dp))
        }

        // Live Website Interactive Bottom Sheet Preview
        if (showWebsiteSheet) {
            ModalBottomSheet(
                onDismissRequest = { showWebsiteSheet = false },
                sheetState = sheetState,
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
            ) {
                val generatedHtml = remember(currentCard) {
                    WebsiteGenerator.generateDigitalCardHtml(currentCard)
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.92f)
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Digital Card Website",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Interactive Dark & Light Mode Web Card",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Button(
                            onClick = {
                                ContactsHelper.shareText(
                                    context,
                                    "Website HTML - ${currentCard.name}",
                                    generatedHtml
                                )
                            },
                            shape = CircleShape
                        ) {
                            Icon(imageVector = Icons.Default.Code, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Share HTML", fontSize = 13.sp)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(20.dp)
                            )
                    ) {
                        AndroidView(
                            factory = { ctx ->
                                WebView(ctx).apply {
                                    webViewClient = WebViewClient()
                                    settings.javaScriptEnabled = true
                                    settings.domStorageEnabled = true
                                    loadDataWithBaseURL(null, generatedHtml, "text/html", "UTF-8", null)
                                }
                            },
                            update = { webView ->
                                webView.loadDataWithBaseURL(null, generatedHtml, "text/html", "UTF-8", null)
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}

