package com.example.ui.screens.leads

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
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
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.LeadEntity
import com.example.data.model.LeadSource
import com.example.data.model.LeadStatus
import com.example.data.model.RelationshipType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditLeadScreen(
    leadId: Long?,
    viewModel: LeadsViewModel,
    onNavigateBack: () -> Unit
) {
    var fullName by remember { mutableStateOf("") }
    var jobTitle by remember { mutableStateOf("") }
    var company by remember { mutableStateOf("") }
    var phonePrimary by remember { mutableStateOf("") }
    var phoneSecondary by remember { mutableStateOf("") }
    var emailPrimary by remember { mutableStateOf("") }
    var website by remember { mutableStateOf("") }
    var linkedIn by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var industry by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var status by remember { mutableStateOf(LeadStatus.NEW) }
    var source by remember { mutableStateOf(LeadSource.MANUAL) }
    var relationshipType by remember { mutableStateOf(RelationshipType.PROSPECT) }

    var showStatusMenu by remember { mutableStateOf(false) }
    var showSourceMenu by remember { mutableStateOf(false) }
    var showRelMenu by remember { mutableStateOf(false) }

    LaunchedEffect(leadId) {
        if (leadId != null && leadId != 0L) {
            val existing = viewModel.getLeadById(leadId)
            existing?.let {
                fullName = it.fullName
                jobTitle = it.jobTitle
                company = it.company
                phonePrimary = it.phonePrimary
                phoneSecondary = it.phoneSecondary
                emailPrimary = it.emailPrimary
                website = it.website
                linkedIn = it.linkedIn
                address = it.address
                industry = it.industry
                tags = it.tags
                notes = it.notes
                status = it.status
                source = it.source
                relationshipType = it.relationshipType
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (leadId == null || leadId == 0L) "New Contact" else "Edit Contact",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (fullName.isNotBlank()) {
                                viewModel.saveLead(
                                    LeadEntity(
                                        id = leadId ?: 0L,
                                        fullName = fullName.trim(),
                                        jobTitle = jobTitle.trim(),
                                        company = company.trim(),
                                        phonePrimary = phonePrimary.trim(),
                                        phoneSecondary = phoneSecondary.trim(),
                                        emailPrimary = emailPrimary.trim(),
                                        website = website.trim(),
                                        linkedIn = linkedIn.trim(),
                                        address = address.trim(),
                                        industry = industry.trim(),
                                        tags = tags.trim(),
                                        notes = notes.trim(),
                                        status = status,
                                        source = source,
                                        relationshipType = relationshipType
                                    )
                                )
                                onNavigateBack()
                            }
                        },
                        modifier = Modifier.testTag("save_lead_button")
                    ) {
                        Icon(imageVector = Icons.Default.Save, contentDescription = "Save Contact")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .consumeWindowInsets(paddingValues)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = { Text("Full Name *") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_full_name"),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = jobTitle,
                    onValueChange = { jobTitle = it },
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
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = phonePrimary,
                    onValueChange = { phonePrimary = it },
                    label = { Text("Primary Phone") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = emailPrimary,
                    onValueChange = { emailPrimary = it },
                    label = { Text("Primary Email") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
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
                    value = linkedIn,
                    onValueChange = { linkedIn = it },
                    label = { Text("LinkedIn") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = industry,
                    onValueChange = { industry = it },
                    label = { Text("Industry") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = tags,
                    onValueChange = { tags = it },
                    label = { Text("Tags (comma separated)") },
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

            // Status Selector
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = { showStatusMenu = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Pipeline Status: ${status.label}")
                }
                DropdownMenu(
                    expanded = showStatusMenu,
                    onDismissRequest = { showStatusMenu = false }
                ) {
                    LeadStatus.values().forEach { st ->
                        DropdownMenuItem(
                            text = { Text(st.label) },
                            onClick = {
                                status = st
                                showStatusMenu = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    if (fullName.isNotBlank()) {
                        viewModel.saveLead(
                            LeadEntity(
                                id = leadId ?: 0L,
                                fullName = fullName.trim(),
                                jobTitle = jobTitle.trim(),
                                company = company.trim(),
                                phonePrimary = phonePrimary.trim(),
                                phoneSecondary = phoneSecondary.trim(),
                                emailPrimary = emailPrimary.trim(),
                                website = website.trim(),
                                linkedIn = linkedIn.trim(),
                                address = address.trim(),
                                industry = industry.trim(),
                                tags = tags.trim(),
                                notes = notes.trim(),
                                status = status,
                                source = source,
                                relationshipType = relationshipType
                            )
                        )
                        onNavigateBack()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("submit_lead_button"),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(if (leadId == null || leadId == 0L) "Create Lead" else "Update Lead")
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
