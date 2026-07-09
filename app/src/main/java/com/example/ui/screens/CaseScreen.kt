package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.LawCase
import com.example.viewmodel.LegalViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaseScreen(viewModel: LegalViewModel) {
    val cases by viewModel.allCases.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedCase by remember { mutableStateOf<LawCase?>(null) }
    var isAddingCase by remember { mutableStateOf(false) }

    // Dialog state variables
    var title by remember { mutableStateOf("") }
    var number by remember { mutableStateOf("") }
    var court by remember { mutableStateOf("") }
    var judge by remember { mutableStateOf("") }
    var opposite by remember { mutableStateOf("") }
    var nextDate by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var clientName by remember { mutableStateOf("") }
    var clientPhone by remember { mutableStateOf("") }

    val filteredCases = cases.filter {
        it.caseTitle.contains(searchQuery, ignoreCase = true) ||
                it.caseNumber.contains(searchQuery, ignoreCase = true) ||
                it.courtName.contains(searchQuery, ignoreCase = true) ||
                it.clientName.contains(searchQuery, ignoreCase = true)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (selectedCase == null) {
            // Case List View
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("মামলার নাম, নম্বর বা ক্লায়েন্ট দিয়ে খুঁজুন...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Text(
                    text = "মামলা এবং ডায়েরি তালিকা (${filteredCases.size})",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                if (filteredCases.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.FolderOpen, contentDescription = "Empty", tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("কোনো মামলা পাওয়া যায়নি।", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f))
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(filteredCases) { lawCase ->
                            CaseRowCard(
                                lawCase = lawCase,
                                onClick = { selectedCase = lawCase }
                            )
                        }
                    }
                }
            }
        } else {
            // Case Detail View
            CaseDetailView(
                lawCase = selectedCase!!,
                onBack = { selectedCase = null },
                onUpdateCase = { updated ->
                    viewModel.updateCaseTimelineAndSchedules(updated.id, updated)
                    selectedCase = updated
                },
                onDelete = {
                    viewModel.deleteCase(selectedCase!!)
                    selectedCase = null
                }
            )
        }

        // Floating Action Button to add cases
        if (selectedCase == null && !isAddingCase) {
            FloatingActionButton(
                onClick = { isAddingCase = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(24.dp)
                    .testTag("add_case_fab"),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Case")
            }
        }

        // Add Case dialog sheet
        if (isAddingCase) {
            AlertDialog(
                onDismissRequest = { isAddingCase = false },
                title = { Text("নতুন মামলা যুক্ত করুন (Add Case)", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                text = {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item {
                            OutlinedTextField(
                                value = title,
                                onValueChange = { title = it },
                                label = { Text("মামলার শিরোনাম (Case Title)") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        item {
                            OutlinedTextField(
                                value = number,
                                onValueChange = { number = it },
                                label = { Text("মামলা নম্বর (Case Number)") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        item {
                            OutlinedTextField(
                                value = court,
                                onValueChange = { court = it },
                                label = { Text("আদালতের নাম (Court Name)") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        item {
                            OutlinedTextField(
                                value = judge,
                                onValueChange = { judge = it },
                                label = { Text("বিজ্ঞ বিচারক (Judge)") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        item {
                            OutlinedTextField(
                                value = opposite,
                                onValueChange = { opposite = it },
                                label = { Text("বিপক্ষীয় দল (Opposite Party)") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        item {
                            OutlinedTextField(
                                value = nextDate,
                                onValueChange = { nextDate = it },
                                label = { Text("পরবর্তী তারিখ (YYYY-MM-DD)") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        item {
                            OutlinedTextField(
                                value = clientName,
                                onValueChange = { clientName = it },
                                label = { Text("ক্লায়েন্টের নাম (Client Name)") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        item {
                            OutlinedTextField(
                                value = clientPhone,
                                onValueChange = { clientPhone = it },
                                label = { Text("ক্লায়েন্ট ফোন (Client Phone)") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        item {
                            OutlinedTextField(
                                value = notes,
                                onValueChange = { notes = it },
                                label = { Text("ব্যক্তিগত নোট ও সারসংক্ষেপ") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (title.isNotEmpty() && number.isNotEmpty()) {
                                viewModel.insertCase(title, number, court, judge, opposite, nextDate, notes, clientName, clientPhone)
                                isAddingCase = false
                                title = ""
                                number = ""
                                court = ""
                                judge = ""
                                opposite = ""
                                nextDate = ""
                                notes = ""
                                clientName = ""
                                clientPhone = ""
                            }
                        }
                    ) {
                        Text("সংরক্ষণ করুন")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { isAddingCase = false }) {
                        Text("বাতিল")
                    }
                }
            )
        }
    }
}

@Composable
fun CaseRowCard(lawCase: LawCase, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .testTag("case_card_${lawCase.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Gavel,
                    contentDescription = "Gavel Icon",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = lawCase.caseTitle,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "নম্বর: ${lawCase.caseNumber}",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "পরবর্তী তারিখ: ${lawCase.nextHearingDate.ifEmpty { "ধার্য নেই" }}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (lawCase.nextHearingDate.isNotEmpty()) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "View Details",
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
fun CaseDetailView(
    lawCase: LawCase,
    onBack: () -> Unit,
    onUpdateCase: (LawCase) -> Unit,
    onDelete: () -> Unit
) {
    var personalNotesText by remember { mutableStateOf(lawCase.personalNotes) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top app bar equivalent
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "মামলার বিবরণী",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete Case", tint = MaterialTheme.colorScheme.error)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Case Title Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = lawCase.caseTitle,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "নম্বর: ${lawCase.caseNumber}",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Case Meta Grid
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    DetailItem(label = "আদালতের নাম:", value = lawCase.courtName.ifEmpty { "উলেখ নেই" })
                    DetailItem(label = "বিজ্ঞ বিচারক:", value = lawCase.judgeName.ifEmpty { "উলেখ নেই" })
                    DetailItem(label = "বিপক্ষীয় দল:", value = lawCase.oppositeParty.ifEmpty { "উলেখ নেই" })
                    DetailItem(label = "ক্লায়েন্টের নাম:", value = lawCase.clientName.ifEmpty { "উলেখ নেই" })
                    DetailItem(label = "ক্লায়েন্টের ফোন:", value = lawCase.clientPhone.ifEmpty { "উলেখ নেই" })
                    DetailItem(label = "পরবর্তী শুনানির তারিখ:", value = lawCase.nextHearingDate.ifEmpty { "ধার্য নেই" })
                }
            }

            // Personal Notes Input Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "📝 ব্যক্তিগত গোপনীয় চেম্বার নোট",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = personalNotesText,
                        onValueChange = { personalNotesText = it; onUpdateCase(lawCase.copy(personalNotes = it)) },
                        label = { Text("মজার ঘটনা, আর্গুমেন্ট, বা জেরা করার গুরুত্বপূর্ণ পয়েন্ট...") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 5
                    )
                }
            }
        }
    }
}

@Composable
fun DetailItem(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

