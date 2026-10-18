package com.example.ui.screens.nfc

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.TapAndPlay
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.BuildConfig
import com.example.ui.components.ContactActionRow
import com.example.ui.components.SourceBadge
import com.example.ui.components.StatusBadge

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NfcScreen(
    viewModel: NfcViewModel,
    onNavigateBack: () -> Unit,
    onLeadSaved: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    val infiniteTransition = rememberInfiniteTransition(label = "NfcPulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseScale"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("NFC Tap Exchange", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Read vs Write Mode Toggle
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = !state.isWritingMode,
                    onClick = { viewModel.setWritingMode(false) },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                    modifier = Modifier.testTag("nfc_read_mode_tab")
                ) {
                    Text("Read Tap")
                }
                SegmentedButton(
                    selected = state.isWritingMode,
                    onClick = { viewModel.setWritingMode(true) },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                    modifier = Modifier.testTag("nfc_write_mode_tab")
                ) {
                    Text("Write to Tag")
                }
            }

            // Animated Visual Target Wave
            Box(
                modifier = Modifier
                    .size(240.dp),
                contentAlignment = Alignment.Center
            ) {
                // Outer Pulsing Ripple Ring
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .scale(scale)
                        .background(
                            color = if (state.isWritingMode) Color(0x330EA5E9) else Color(0x336366F1),
                            shape = CircleShape
                        )
                )

                // Inner Circle Button
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .background(
                            color = if (state.isWritingMode) Color(0xFF0EA5E9) else Color(0xFF6366F1),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (state.isProcessing) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(48.dp))
                    } else {
                        Icon(
                            imageVector = if (state.isWritingMode) Icons.Default.TapAndPlay else Icons.Default.Sensors,
                            contentDescription = "NFC Sensor",
                            tint = Color.White,
                            modifier = Modifier.size(64.dp)
                        )
                    }
                }
            }

            // Status Card
            val statusCardShape = RoundedCornerShape(24.dp)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 0.5.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        shape = statusCardShape
                    ),
                shape = statusCardShape,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (state.isWritingMode) "NFC Tag Writer Active" else "Ready for NFC Contact Tap",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = state.statusMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    state.errorMessage?.let { err ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = err,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Read Lead Result Sheet Card
            state.readLead?.let { lead ->
                val leadCardShape = RoundedCornerShape(22.dp)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 0.5.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                            shape = leadCardShape
                        ),
                    shape = leadCardShape,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = lead.fullName,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = listOf(lead.jobTitle, lead.company).filter { it.isNotBlank() }.joinToString(" at "),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            SourceBadge(source = lead.source)
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        ContactActionRow(lead = lead)

                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = { viewModel.dismissReadLead() },
                                modifier = Modifier.weight(1f),
                                shape = CircleShape
                            ) {
                                Text("Discard", fontWeight = FontWeight.SemiBold)
                            }
                            Button(
                                onClick = {
                                    viewModel.saveReadLead()
                                    onLeadSaved()
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("save_nfc_lead_button"),
                                shape = CircleShape
                            ) {
                                Text("Save Lead", fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }

            // Simulator Trigger Button for Instant Verification without Physical NFC Tag
            if (BuildConfig.DEBUG) {
                OutlinedButton(
                    onClick = { viewModel.simulateNfcTap() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("simulate_nfc_tap_button"),
                    shape = CircleShape
                ) {
                    Icon(imageVector = Icons.Default.Nfc, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Simulate NFC Tag Tap (Test)", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
