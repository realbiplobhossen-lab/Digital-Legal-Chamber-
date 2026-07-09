package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.lazy.LazyColumn
import com.example.ui.screens.*
import com.example.viewmodel.LegalViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LegalApp(viewModel: LegalViewModel) {
    var currentScreen by remember { mutableStateOf(\"DASHBOARD\") }

    // PIN lock security states
    val isPinEnabled by viewModel.isPinEnabled.collectAsState()
    val isAppUnlocked by viewModel.isAppUnlocked.collectAsState()
    val savedPin by viewModel.savedPin.collectAsState()
    var pinInput by remember { mutableStateOf(\"\") }
    var pinErrorMessage by remember { mutableStateOf(\"\") }

    if (isPinEnabled && !isAppUnlocked) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = \"App Locked\",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = \"ডিজিটাল চেম্বার লকড\",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = \"অনুগ্রহ করে আপনার ৪ ডিজিটের সিকিউরিটি পিন দিন\",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                OutlinedTextField(
                    value = pinInput,
                    onValueChange = {
                        if (it.length <= 4) {
                            pinInput = it
                            pinErrorMessage = \"\"
                            if (it == savedPin) {
                                viewModel.setAppUnlockState(true)
                            }
                        }
                    },
                    label = { Text(\"পিন কোড\") },
                    modifier = Modifier
                        .width(180.dp)
                        .testTag(\"pin_input_field\"),
                    shape = RoundedCornerShape(12.dp),
                    textAlign = TextAlign.Center,
                    singleLine = true
                )
                if (pinErrorMessage.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = pinErrorMessage, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        if (pinInput == savedPin) {
                            viewModel.setAppUnlockState(true)
                        } else {
                            pinErrorMessage = \"ভুল পিন! আবার চেষ্টা করুন।\"
                        }
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(\"আনলক করুন\")
                }
            }
        }
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = when (currentScreen) {
                                \"DASHBOARD\" -> \"⚖️ ডিজিটাল লিগ্যাল চেম্বার\"
                                \"CASES\" -> \"📁 মামলা ডায়েরি\"
                                \"CLIENTS\" -> \"👥 মক্কেল প্রোফাইল\"
                                \"VOICE_TASKS\" -> \"🎙️ সিনিয়র ডিক্টেশন\"
                                \"AI_ASSISTANT\" -> \"🤖 এআই লিগ্যাল অ্যাসিস্ট্যান্ট\"
                                \"SCANNER\" -> \"📷 ডকুমেন্ট স্ক্যানার\"
                                \"LIBRARY\" -> \"📚 আইন ও নজির লাইব্রেরি\"
                                else -> \"লিগ্যাল চেম্বার\"
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    },
                    navigationIcon = {
                        if (currentScreen != \"DASHBOARD\") {
                            IconButton(onClick = { currentScreen = \"DASHBOARD\" }) {
                                Icon(Icons.Default.ArrowBack, contentDescription = \"Back\")
                            }
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            viewModel.configurePin(\"1234\", !isPinEnabled)
                        }) {
                            Icon(
                                imageVector = if (isPinEnabled) Icons.Default.Security else Icons.Default.LockOpen,
                                contentDescription = \"Toggle PIN Security\"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when (currentScreen) {
                    \"DASHBOARD\" -> DashboardScreen(
                        viewModel = viewModel,
                        onNavigateToCases = { currentScreen = \"CASES\" },
                        onNavigateToClients = { currentScreen = \"CLIENTS\" },
                        onNavigateToVoiceTasks = { currentScreen = \"VOICE_TASKS\" },
                        onNavigateToAiAssistant = { currentScreen = \"AI_ASSISTANT\" },
                        onNavigateToScanner = { currentScreen = \"SCANNER\" },
                        onNavigateToLibrary = { currentScreen = \"LIBRARY\" }
                    )
                    \"CASES\" -> CaseScreen(viewModel = viewModel)
                    \"CLIENTS\" -> ClientScreen(viewModel = viewModel)
                    \"VOICE_TASKS\" -> VoiceTaskScreen(viewModel = viewModel)
                    \"AI_ASSISTANT\" -> AiAssistantScreen(viewModel = viewModel)
                    \"SCANNER\" -> DocumentScannerScreen(viewModel = viewModel)
                    \"LIBRARY\" -> LawLibraryScreen(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun DashboardToolCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = title, tint = color, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = description, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            }
            Icon(Icons.Default.ChevronRight, contentDescription = \"Enter Tool\", tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f))
        }
    }
                 }
