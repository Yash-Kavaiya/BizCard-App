package com.example.ui.screens.scan

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.CropFree
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ExtractedCardData
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun ScanScreen(
    viewModel: ScanViewModel,
    onNavigateBack: () -> Unit,
    onLeadSaved: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val cameraPermissionState = rememberPermissionState(android.Manifest.permission.CAMERA)
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scanner Mode", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Layers,
                            contentDescription = "Batch Mode",
                            tint = if (state.isBatchMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (state.isBatchMode) "Batch (${state.batchCount})" else "Single",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Switch(
                            checked = state.isBatchMode,
                            onCheckedChange = { viewModel.toggleBatchMode() },
                            modifier = Modifier.testTag("toggle_batch_mode_switch")
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.Black)
        ) {
            if (cameraPermissionState.status.isGranted) {
                // Live Camera View with CameraX ImageAnalysis for QR code
                CameraPreview(
                    onImageCaptured = { bitmap ->
                        if (state.scanMode == ScanMode.CARD_OCR) {
                            viewModel.processCardImage(bitmap)
                        }
                    },
                    onQrCodeScanned = if (state.scanMode == ScanMode.QR_CODE) {
                        { qrText -> viewModel.processQrCode(qrText) }
                    } else null
                )

                // Top Mode Toggle Selector Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .padding(top = 16.dp, start = 16.dp, end = 16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = state.scanMode == ScanMode.CARD_OCR,
                                onClick = { viewModel.setScanMode(ScanMode.CARD_OCR) },
                                label = { Text("Card OCR Photo", fontSize = 13.sp, fontWeight = FontWeight.SemiBold) },
                                leadingIcon = { Icon(imageVector = Icons.Default.Camera, contentDescription = null, modifier = Modifier.size(16.dp)) },
                                shape = RoundedCornerShape(20.dp),
                                modifier = Modifier.testTag("mode_card_ocr_chip")
                            )

                            FilterChip(
                                selected = state.scanMode == ScanMode.QR_CODE,
                                onClick = { viewModel.setScanMode(ScanMode.QR_CODE) },
                                label = { Text("QR Code Scan", fontSize = 13.sp, fontWeight = FontWeight.SemiBold) },
                                leadingIcon = { Icon(imageVector = Icons.Default.QrCodeScanner, contentDescription = null, modifier = Modifier.size(16.dp)) },
                                shape = RoundedCornerShape(20.dp),
                                modifier = Modifier.testTag("mode_qr_code_chip")
                            )
                        }
                    }
                }

                // Overlay Target Frame
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp, vertical = 80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (state.scanMode == ScanMode.QR_CODE) {
                        // Square QR Target Box
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.75f)
                                    .aspectRatio(1.0f)
                                    .border(
                                        width = 3.dp,
                                        color = if (state.isProcessing) Color(0xFF00E5FF) else MaterialTheme.colorScheme.primary,
                                        shape = RoundedCornerShape(20.dp)
                                    )
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.QrCode,
                                    contentDescription = null,
                                    tint = Color.White.copy(alpha = 0.4f),
                                    modifier = Modifier.size(80.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Card(
                                shape = CircleShape,
                                colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.7f))
                            ) {
                                Text(
                                    text = "Center QR code or vCard inside frame",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                                )
                            }
                        }
                    } else {
                        // Business Card Ratio Guide Box
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1.586f)
                                .border(
                                    width = 3.dp,
                                    color = if (state.isProcessing) Color(0xFF00E5FF) else Color.White.copy(alpha = 0.8f),
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .padding(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CropFree,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.6f),
                                modifier = Modifier
                                    .size(32.dp)
                                    .align(Alignment.TopStart)
                            )
                            Icon(
                                imageVector = Icons.Default.CropFree,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.6f),
                                modifier = Modifier
                                    .size(32.dp)
                                    .align(Alignment.BottomEnd)
                            )
                        }
                    }
                }
            } else {
                // Permission Fallback Screen
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Camera,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Camera Permission Required",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Grant camera permission to capture business cards and scan QR code cards live.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = { cameraPermissionState.launchPermissionRequest() },
                        shape = CircleShape,
                        modifier = Modifier.testTag("request_camera_permission_button")
                    ) {
                        Text("Grant Camera Permission", fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            // Bottom Control Bar
            val triggerCardShape = RoundedCornerShape(28.dp)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
                    .border(
                        width = 0.5.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        shape = triggerCardShape
                    ),
                shape = triggerCardShape,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (state.scanMode == ScanMode.QR_CODE) {
                        Text(
                            text = "Live CameraX scanning active. Or test sample QR payloads:",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = {
                                    val vCardSample = """
                                        BEGIN:VCARD
                                        VERSION:3.0
                                        N:Tech;Sarah;;;
                                        FN:Sarah Tech
                                        ORG:CloudScale Inc
                                        TITLE:CTO & Co-Founder
                                        TEL:+15559876543
                                        EMAIL:sarah.tech@cloudscale.io
                                        URL:https://cloudscale.io
                                        NOTE:Met at AI Tech Summit 2026
                                        END:VCARD
                                    """.trimIndent()
                                    viewModel.processQrCode(vCardSample)
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("scan_sample_vcard_qr_button"),
                                shape = CircleShape
                            ) {
                                Text("vCard QR", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }

                            OutlinedButton(
                                onClick = {
                                    val meCardSample = "MECARD:N:Patel,Rajesh;ORG:FinTech Global;TEL:+15553334444;EMAIL:rajesh@fintechglobal.com;URL:https://fintechglobal.com;NOTE:Met at AI Expo 2026;;"
                                    viewModel.processQrCode(meCardSample)
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("scan_sample_mecard_qr_button"),
                                shape = CircleShape
                            ) {
                                Text("MECARD QR", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }

                            OutlinedButton(
                                onClick = {
                                    val urlSample = "https://apexinnovations.com/card/alex-morgan"
                                    viewModel.processQrCode(urlSample)
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("scan_sample_url_qr_button"),
                                shape = CircleShape
                            ) {
                                Text("Card URL QR", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    } else {
                        Text(
                            text = "Align card inside frame or test with pre-built cards:",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = {
                                    val (bitmap, text) = SampleCards.generateTechCardBitmap()
                                    viewModel.processCardImage(bitmap, text)
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("scan_sample_tech_card_button"),
                                shape = CircleShape
                            ) {
                                Text("Tech Card", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }

                            OutlinedButton(
                                onClick = {
                                    val (bitmap, text) = SampleCards.generateGujaratiCardBitmap()
                                    viewModel.processCardImage(bitmap, text)
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("scan_sample_gujarati_card_button"),
                                shape = CircleShape
                            ) {
                                Text("Gujarati Card", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }

                            Button(
                                onClick = {
                                    val (bitmap, text) = SampleCards.generateTechCardBitmap()
                                    viewModel.processCardImage(bitmap, text)
                                },
                                modifier = Modifier.testTag("capture_card_camera_button"),
                                shape = CircleShape,
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Icon(imageVector = Icons.Default.Camera, contentDescription = "Capture")
                            }
                        }
                    }
                }
            }

            // Processing Overlay
            if (state.isProcessing) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.75f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 4.dp,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (state.scanMode == ScanMode.QR_CODE) "Decoding QR / vCard Data..." else "Gemini AI Extracting Contact Data...",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (state.scanMode == ScanMode.QR_CODE) "Parsing vCard, MECARD & Digital Card fields" else "Standardizing fields, phone numbers & OCR",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }

        // Extracted Review Bottom Sheet
        state.extractedData?.let { extracted ->
            ModalBottomSheet(
                onDismissRequest = { viewModel.dismissReview() },
                sheetState = bottomSheetState
            ) {
                ExtractedCardReviewSheet(
                    data = extracted,
                    capturedBitmap = state.capturedBitmap,
                    onConfirm = { editedData, notes ->
                        viewModel.saveExtractedLead(editedData, notes)
                        onLeadSaved()
                    },
                    onCancel = { viewModel.dismissReview() }
                )
            }
        }
    }
}

@Composable
fun ExtractedCardReviewSheet(
    data: ExtractedCardData,
    capturedBitmap: Bitmap?,
    onConfirm: (ExtractedCardData, String) -> Unit,
    onCancel: () -> Unit
) {
    var name by remember { mutableStateOf(data.fullName) }
    var title by remember { mutableStateOf(data.jobTitle) }
    var company by remember { mutableStateOf(data.company) }
    var phone by remember { mutableStateOf(data.phonePrimary) }
    var email by remember { mutableStateOf(data.emailPrimary) }
    var website by remember { mutableStateOf(data.website) }
    var linkedIn by remember { mutableStateOf(data.linkedIn) }
    var industry by remember { mutableStateOf(data.industry) }
    var address by remember { mutableStateOf(data.address) }
    var notes by remember { mutableStateOf(data.notes) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Gemini Extracted Lead",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            IconButton(onClick = onCancel) {
                Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
            }
        }

        capturedBitmap?.let { bmp ->
            Image(
                bitmap = bmp.asImageBitmap(),
                contentDescription = "Card Image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(12.dp))
            )
        }

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Full Name") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("review_input_name"),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Job Title") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
            OutlinedTextField(
                value = company,
                onValueChange = { company = it },
                label = { Text("Company") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Phone") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = website,
                onValueChange = { website = it },
                label = { Text("Website") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
            OutlinedTextField(
                value = industry,
                onValueChange = { industry = it },
                label = { Text("Industry") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
        }

        OutlinedTextField(
            value = address,
            onValueChange = { address = it },
            label = { Text("Address") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text("Notes / Languages") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2,
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                val updated = data.copy(
                    fullName = name,
                    jobTitle = title,
                    company = company,
                    phonePrimary = phone,
                    emailPrimary = email,
                    website = website,
                    linkedIn = linkedIn,
                    industry = industry,
                    address = address
                )
                onConfirm(updated, notes)
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("save_extracted_lead_button"),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(imageVector = Icons.Default.Check, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Confirm & Save Lead")
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}
