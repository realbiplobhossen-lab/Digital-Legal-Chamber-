package com.example.ui.screens

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.LegalViewModel
import kotlinx.coroutines.launch

@Composable
fun VoiceNotesScreen(viewModel: LegalViewModel) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val tasks by viewModel.allTasks.collectAsState()
    val isAiLoading by viewModel.isAiLoading.collectAsState()

    var speechText by remember { mutableStateOf("") }
    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                val results = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                if (!results.isNullOrEmpty()) {
                    speechText = results[0]
                    // Process transcribed text through AI
                    viewModel.processVoiceInstructions(speechText) {
                        speechText = ""
                    }
                }
            }
        }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "🎙️ সিনিয়রের ভয়েস নির্দেশাবলী (Voice Tasks)",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "সিনিয়র আইনজীবীর অডিও বা মুখের কথা রেকর্ড করুন। AI স্বয়ংক্রিয়ভাবে সেখান থেকে টাস্ক বা করণীয় কাজ বের করে ডেটাবেজে যুক্ত করবে।",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (isAiLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(36.dp), color = MaterialTheme.colorScheme.tertiary)
                    Text(text = "AI আপনার ভয়েস বিশ্লেষণ করছে...", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                } else {
                    Button(
                        onClick = {
                            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "bn-BD")
                                putExtra(RecognizerIntent.EXTRA_PROMPT, "আপনার নির্দেশ বলুন...")
                            }
                            speechRecognizerLauncher.launch(intent)
                        },
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                        modifier = Modifier.size(72.dp)
                    ) {
                        Icon(Icons.Default.Mic, contentDescription = "Record Voice", modifier = Modifier.size(32.dp), tint = MaterialTheme.colorScheme.onTertiary)
                    }
                    Text(text = "ট্যাপ করে রেকর্ড শুরু করুন", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        Divider()

        Text(
            text = "📋 বিন্যস্ত করণীয় কাজের তালিকা",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )

        val voiceTasks = tasks.filter { it.isVoiceRecorded }

        if (voiceTasks.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(text = "এখনও কোনো ভয়েস টাস্ক নেই।", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(voiceTasks) { task ->
                    VoiceTaskItem(
                        task = task,
                        onToggleComplete = { viewModel.updateTaskStatus(task.id, !task.isCompleted) },
                        onDelete = { viewModel.deleteTask(task) }
                    )
                }
            }
        }
    }
}

@Composable
fun VoiceTaskItem(
    task: com.example.data.DailyTask,
    onToggleComplete: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (task.isCompleted) MaterialTheme.colorScheme.surface.copy(alpha = 0.6f) else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = task.isCompleted,
                onCheckedChange = { onToggleComplete() }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.taskText,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (task.isCompleted) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface,
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                )
                if (task.isVoiceRecorded) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Campaign,
                            contentDescription = "Voice task icon",
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "সিনিয়রের ভয়েস নোট থেকে বিন্যস্ত",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.tertiary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete task",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

