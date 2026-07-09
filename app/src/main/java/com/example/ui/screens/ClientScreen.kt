package com.example.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Client
import com.example.viewmodel.LegalViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientScreen(viewModel: LegalViewModel) {
    val context = LocalContext.current
    val clients by viewModel.allClients.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var isAddingClient by remember { mutableStateOf(false) }
    var selectedClient by remember { mutableStateOf<Client?>(null) }

    // Forms fields
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var nid by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var profession by remember { mutableStateOf("") }
    var totalFeeInput by remember { mutableStateOf("") }
    var paidFeeInput by remember { mutableStateOf("") }

    val filteredClients = clients.filter {
        it.name.contains(searchQuery, ignoreCase = true) ||
                it.phone.contains(searchQuery, ignoreCase = true) ||
                it.nid.contains(searchQuery, ignoreCase = true)
    }

    Box(modifier = Modifier.fillMaxSize()) {
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
                placeholder = { Text("মক্কেলের নাম, ফোন নম্বর বা NID লিখুন...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Text(
                text = "মক্কেল ডিরেক্টরি (${filteredClients.size})",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            if (filteredClients.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Group, contentDescription = "Empty", tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("কোনো মক্কেল নিবন্ধিত নেই।", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f))
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredClients) { client ->
                        ClientRowCard(
                            client = client,
                            onDial = {
                                val intent = Intent(Intent.ACTION_DIAL).apply {
                                    data = Uri.parse("tel:${client.phone}")
                                }
                                context.startActivity(intent)
                            },
                            onWhatsApp = {
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW).apply {
                                        data = Uri.parse("https://api.whatsapp.com/send?phone=${client.phone}")
                                    }
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    // Fallback
                                }
                            },
                            onClick = { selectedClient = client }
                        )
                    }
                }
            }
        }

        // Add Client FAB
        if (!isAddingClient) {
            FloatingActionButton(
                onClick = { isAddingClient = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(24.dp)
                    .testTag("add_client_fab"),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.PersonAdd, contentDescription = "Add Client")
            }
        }

        // View Client Details Bottom Sheet / Dialog
        if (selectedClient != null) {
            val client = selectedClient!!
            val dues = (client.totalFee - client.paidFee).coerceAtLeast(0.0)

            AlertDialog(
                onDismissRequest = { selectedClient = null },
                title = { Text(client.name, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Phone, contentDescription = "Phone", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(text = "মোবাইল: ${client.phone}", fontSize = 14.sp)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Badge, contentDescription = "NID", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(text = "জাতীয় পরিচয়পত্র: ${client.nid.ifEmpty { "উল্লেখ নেই" }}", fontSize = 14.sp)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Work, contentDescription = "Profession", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(text = "পেশা: ${client.profession.ifEmpty { "উল্লেখ নেই" }}", fontSize = 14.sp)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Email, contentDescription = "Email", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(text = "ইমেইল: ${client.email.ifEmpty { "উল্লেখ নেই" }}", fontSize = 14.sp)
                        }
                        Row(verticalAlignment = Alignment.Top) {
                            Icon(Icons.Default.Home, contentDescription = "Address", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(text = "ঠিকানা: ${client.address.ifEmpty { "উল্লেখ নেই" }}", fontSize = 14.sp)
                        }
                        Divider(modifier = Modifier.padding(vertical = 4.dp))
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("মোট চুক্তি ফি:", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            Text("৳%,.0f".format(client.totalFee), fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                        }
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("পরিশোধিত ফি:", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            Text("৳%,.0f".format(client.paidFee), fontSize = 13.sp, color = Color(0xFF2E7D32))
                        }
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("বকেয়া পাওনা:", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text("৳%,.0f".format(dues), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = if (dues > 0) MaterialTheme.colorScheme.error else Color.Gray)
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { selectedClient = null }) {
                        Text("বন্ধ করুন")
                    }
                }
            )
        }

        // Add Client Dialog Sheet
        if (isAddingClient) {
            AlertDialog(
                onDismissRequest = { isAddingClient = false },
                title = { Text("নতুন মক্কেল নিবন্ধন করুন", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                text = {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item {
                            OutlinedTextField(
                                value = name,
                                onValueChange = { name = it },
                                label = { Text("পূর্ণ নাম (Full Name)") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        item {
                            OutlinedTextField(
                                value = phone,
                                onValueChange = { phone = it },
                                label = { Text("মোবাইল নম্বর (Phone)") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        item {
                            OutlinedTextField(
                                value = nid,
                                onValueChange = { nid = it },
                                label = { Text("জাতীয় পরিচয়পত্র (NID)") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        item {
                            OutlinedTextField(
                                value = profession,
                                onValueChange = { profession = it },
                                label = { Text("পেশা (Profession)") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        item {
                            OutlinedTextField(
                                value = email,
                                onValueChange = { email = it },
                                label = { Text("ইমেইল (Email)") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        item {
                            OutlinedTextField(
                                value = address,
                                onValueChange = { address = it },
                                label = { Text("ঠিকানা (Address)") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        item {
                            OutlinedTextField(
                                value = totalFeeInput,
                                onValueChange = { totalFeeInput = it },
                                label = { Text("মোট চুক্তি ফি (Total Fee)") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        item {
                            OutlinedTextField(
                                value = paidFeeInput,
                                onValueChange = { paidFeeInput = it },
                                label = { Text("অগ্রিম/পরিশোধিত ফি (Paid)") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (name.isNotEmpty() && phone.isNotEmpty()) {
                                val totalVal = totalFeeInput.toDoubleOrNull() ?: 0.0
                                val paidVal = paidFeeInput.toDoubleOrNull() ?: 0.0
                                viewModel.insertClient(name, phone, nid, email, address, profession, totalVal, paidVal)
                                isAddingClient = false
                                // Reset fields
                                name = ""
                                phone = ""
                                nid = ""
                                email = ""
                                address = ""
                                profession = ""
                                totalFeeInput = ""
                                paidFeeInput = ""
                            }
                        }
                    ) {
                        Text("সংরক্ষণ করুন")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { isAddingClient = false }) {
                        Text("বাতিল")
                    }
                }
            )
        }
    }
}

@Composable
fun ClientRowCard(
    client: Client,
    onDial: () -> Unit,
    onWhatsApp: () -> Unit,
    onClick: () -> Unit
) {
    val dues = (client.totalFee - client.paidFee).coerceAtLeast(0.0)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .testTag("client_card_${client.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profile icon
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = client.name.take(1).uppercase(),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = client.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "মোবাইল: ${client.phone}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                if (dues > 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "বকেয়া: ৳%,.0f".format(dues),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            // Dial and WhatsApp shortcuts
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(onClick = onWhatsApp) {
                    Icon(
                        imageVector = Icons.Default.Chat,
                        contentDescription = "WhatsApp",
                        tint = Color(0xFF25D366),
                        modifier = Modifier.size(22.dp)
                    )
                }
                IconButton(onClick = onDial) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = "Call Client",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}

