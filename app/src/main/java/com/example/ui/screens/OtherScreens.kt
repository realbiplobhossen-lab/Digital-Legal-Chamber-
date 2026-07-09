package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.FeeRecord
import com.example.data.LegalContact
import com.example.viewmodel.LegalViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

// ==========================================================
// 1. DOCUMENT SCANNER SCREEN
// ==========================================================
@Composable
fun DocumentScannerScreen(viewModel: LegalViewModel) {
    var scannerStep by remember { mutableStateOf(0) } // 0: Viewport, 1: Scanning loader, 2: OCR Result
    var scanningProgress by remember { mutableStateOf(0f) }
    val scope = rememberCoroutineScope()

    val simulatedOcrText = """
        মহামান্য ১ম অতিরিক্ত জেলা জজ আদালত, ঢাকা।
        দেওয়ানী মোকাদ্দমা নং - ৪৫৬ / ২০২৪ইং।
        
        আব্দুর রহমান ................................. বাদী।
        বনাম
        আলমগীর হোসেন ......................... বিবাদী।
        
        বিষয়: দেওয়ানী কার্যবিধি আইনের ৩৯ আদেশের ১ ও ২ রুল মোতাবেক অন্তর্বর্তীকালীন অস্থায়ী নিষেধাজ্ঞার আবেদন।
    """.trimIndent()

    Box(modifier = Modifier.fillMaxSize().background(Color.Black).padding(16.dp)) {
        if (scannerStep == 0) {
            Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween, horizontalAlignment = Alignment.CenterHorizontally) {
                Text("📷 ডকুমেন্ট বা আরজি স্ক্যান করুন", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp)
                        .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(16.dp))
                        .background(Color.DarkGray.copy(alpha = 0.4f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("ক্যামেরার ফ্রেমের ভেতর কাগজটি সোজা রাখুন", color = Color.White.copy(alpha = 0.6f), fontSize = 13.sp)
                }
                Button(
                    onClick = {
                        scannerStep = 1
                        scope.launch {
                            while (scanningProgress < 1f) {
                                delay(150)
                                scanningProgress += 0.1f
                            }
                            scannerStep = 2
                        }
                    },
                    modifier = Modifier.padding(bottom = 24.dp).testTag("capture_button")
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = "Capture")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("স্ক্যান করুন")
                }
            }
        } else if (scannerStep == 1) {
            Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(progress = { scanningProgress }, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(16.dp))
                Text("এআই প্রযুক্তি দ্বারা লেখা এক্সট্র্যাক্ট (OCR) হচ্ছে...", color = Color.White, fontSize = 14.sp)
            }
        } else {
            Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(14.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("🔍 এক্সট্র্যাক্টকৃত বাংলা টেক্সট (OCR)", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    IconButton(onClick = { scannerStep = 0; scanningProgress = 0f }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Re-scan")
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Card(modifier = Modifier.weight(1f).fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                    Box(modifier = Modifier.padding(14.dp)) {
                        Text(text = simulatedOcrText, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = { viewModel.insertDraft("স্ক্যানকৃত আরজি নথি", simulatedOcrText) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = "Save Draft")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("ড্রাফটে রাখুন")
                    }
                }
            }
        }
    }
}

// ==========================================================
// 2. LAWYERS & CONTACTS DIRECTORY SCREEN
// ==========================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LegalContactScreen(viewModel: LegalViewModel) {
    val contacts by viewModel.allContacts.collectAsState()
    var isAddingContact by remember { mutableStateOf(false) }

    var name by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("Advocate") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { isAddingContact = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Contact")
            }
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(contacts) { contact ->
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                    Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer), contentAlignment = Alignment.Center) {
                            Text(contact.name.take(1), fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(contact.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text("পদবী: ${contact.role} • ফোন: ${contact.phone}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }

        if (isAddingContact) {
            AlertDialog(
                onDismissRequest = { isAddingContact = false },
                title = { Text("নতুন কন্টাক্ট ডিরেক্টরি") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("নাম (Name)") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("মোবাইল নম্বর (Phone)") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("চেম্বার/কোর্ট ঠিকানা (Address)") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("ইমেইল (Email)") }, modifier = Modifier.fillMaxWidth())
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        if (name.isNotEmpty() && phone.isNotEmpty()) {
                            viewModel.insertClient(name, phone, 0.0, 0.0) // fallback shortcut
                            isAddingContact = false
                        }
                    }) { Text("যোগ করুন") }
                }
            )
        }
    }
}

