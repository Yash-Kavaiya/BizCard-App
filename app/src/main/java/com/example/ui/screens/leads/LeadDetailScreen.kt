package com.example.ui.screens.leads

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Note
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.LeadEntity
import com.example.data.model.LeadStatus
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.TableChart
import android.widget.Toast
import com.example.ui.components.ContactActionRow
import com.example.ui.components.SourceBadge
import com.example.ui.components.StatusBadge
import com.example.util.ContactsHelper
import com.example.util.ExportCrmService
import com.example.util.UserPreferences
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeadDetailScreen(
    leadId: Long,
    viewModel: LeadsViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (Long) -> Unit
) {
    val context = LocalContext.current
    var lead by remember { mutableStateOf<LeadEntity?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showStatusMenu by remember { mutableStateOf(false) }

    LaunchedEffect(leadId) {
        lead = viewModel.getLeadById(leadId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Contact Details", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    lead?.let { current ->
                        IconButton(onClick = { viewModel.toggleStarLead(current); lead = current.copy(isStarred = !current.isStarred) }) {
                            Icon(
                                imageVector = if (current.isStarred) Icons.Default.Star else Icons.Outlined.StarBorder,
                                contentDescription = "Star",
                                tint = if (current.isStarred) Color(0xFFEAB308) else MaterialTheme.colorScheme.onSurface
                            )
                        }
                        IconButton(onClick = { onNavigateToEdit(current.id) }) {
                            Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit Contact")
                        }
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete Contact",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        val currentLead = lead
        if (currentLead == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("Loading contact details...")
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header Card
                val headerShape = RoundedCornerShape(26.dp)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 0.5.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                            shape = headerShape
                        ),
                    shape = headerShape,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = currentLead.fullName.take(2).uppercase(),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = currentLead.fullName,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        if (currentLead.jobTitle.isNotBlank() || currentLead.company.isNotBlank()) {
                            Text(
                                text = listOf(currentLead.jobTitle, currentLead.company).filter { it.isNotBlank() }.joinToString(" at "),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box {
                                OutlinedButton(
                                    onClick = { showStatusMenu = true },
                                    shape = CircleShape
                                ) {
                                    StatusBadge(status = currentLead.status)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Change Status", style = MaterialTheme.typography.labelMedium)
                                }
                                DropdownMenu(
                                    expanded = showStatusMenu,
                                    onDismissRequest = { showStatusMenu = false }
                                ) {
                                    LeadStatus.values().forEach { st ->
                                        DropdownMenuItem(
                                            text = { Text(st.label) },
                                            onClick = {
                                                viewModel.updateLeadStatus(currentLead, st)
                                                lead = currentLead.copy(status = st)
                                                showStatusMenu = false
                                            }
                                        )
                                    }
                                }
                            }

                            SourceBadge(source = currentLead.source)
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        ContactActionRow(lead = currentLead)
                    }
                }

                // Export & CRM Integration Card
                val cardShape = RoundedCornerShape(22.dp)
                val scope = rememberCoroutineScope()
                val userPrefs = remember { UserPreferences(context) }
                val crmConfig = userPrefs.getCrmConfig()

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 0.5.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                            shape = cardShape
                        ),
                    shape = cardShape,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Export & CRM Sync",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    ContactsHelper.addToDeviceContacts(context, currentLead)
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("detail_save_contact_button"),
                                shape = CircleShape
                            ) {
                                Icon(imageVector = Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Add Contact", fontSize = 12.sp)
                            }

                            Button(
                                onClick = {
                                    scope.launch {
                                        ExportCrmService.shareVcfFile(context, listOf(currentLead), "${currentLead.fullName.replace(" ", "_")}.vcf")
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("detail_share_vcf_button"),
                                shape = CircleShape,
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                            ) {
                                Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Share vCard", fontSize = 12.sp)
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    if (crmConfig.googleSheetsWebhookUrl.isBlank()) {
                                        Toast.makeText(context, "Google Sheets Webhook URL not configured in Settings.", Toast.LENGTH_LONG).show()
                                    } else {
                                        scope.launch {
                                            val res = ExportCrmService.pushToGoogleSheets(crmConfig.googleSheetsWebhookUrl, listOf(currentLead))
                                            Toast.makeText(context, res.getOrElse { "Failed: ${it.message}" }, Toast.LENGTH_LONG).show()
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("detail_push_gs_button"),
                                shape = CircleShape
                            ) {
                                Icon(imageVector = Icons.Default.TableChart, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFF107C41))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Google Sheet", fontSize = 12.sp)
                            }

                            OutlinedButton(
                                onClick = {
                                    scope.launch {
                                        val results = ExportCrmService.syncLeadToAllActiveIntegrations(crmConfig, currentLead)
                                        if (results.isEmpty()) {
                                            Toast.makeText(context, "No active CRM API keys saved in Settings.", Toast.LENGTH_LONG).show()
                                        } else {
                                            val successCount = results.count { it.isSuccess }
                                            Toast.makeText(context, "Synced lead to $successCount CRM integration(s)!", Toast.LENGTH_LONG).show()
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("detail_push_crm_button"),
                                shape = CircleShape
                            ) {
                                Icon(imageVector = Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Sync CRM", fontSize = 12.sp)
                            }
                        }
                    }
                }

                // Field Details Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 0.5.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                            shape = cardShape
                        ),
                    shape = cardShape,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "Contact Attributes",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )

                        DetailRow(icon = Icons.Default.Call, label = "Primary Phone", value = currentLead.phonePrimary)
                        DetailRow(icon = Icons.Default.Call, label = "Secondary Phone", value = currentLead.phoneSecondary)
                        DetailRow(icon = Icons.Default.Email, label = "Primary Email", value = currentLead.emailPrimary)
                        DetailRow(icon = Icons.Default.Language, label = "Website", value = currentLead.website)
                        DetailRow(icon = Icons.Default.Business, label = "LinkedIn Profile", value = currentLead.linkedIn)
                        DetailRow(icon = Icons.Default.Work, label = "Industry Sector", value = currentLead.industry)
                        DetailRow(icon = Icons.Default.LocationOn, label = "Address", value = currentLead.address)
                    }
                }

                // Notes Card
                if (currentLead.notes.isNotBlank() || currentLead.tags.isNotBlank()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = 0.5.dp,
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                shape = cardShape
                            ),
                        shape = cardShape,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Notes & Tags",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            if (currentLead.tags.isNotBlank()) {
                                Text(
                                    text = "Tags: ${currentLead.tags}",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            if (currentLead.notes.isNotBlank()) {
                                Text(
                                    text = currentLead.notes,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Contact?") },
            text = { Text("Are you sure you want to delete ${lead?.fullName} from your database?") },
            confirmButton = {
                Button(
                    onClick = {
                        lead?.id?.let { viewModel.deleteLead(it) }
                        showDeleteDialog = false
                        onNavigateBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun DetailRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    if (value.isBlank()) return
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
