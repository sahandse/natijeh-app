package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.update.UpdateInfo
import com.example.data.update.UpdateManager
import kotlinx.coroutines.launch

private val AccentGreen = Color(0xFF18C964)
private val BgDark = Color(0xFF0B0E14)
private val SurfaceDark = Color(0xFF161B22)
private val Muted = Color(0xFF94A3B8)

private sealed class UpdateUiState {
    object Idle : UpdateUiState()
    object Checking : UpdateUiState()
    object UpToDate : UpdateUiState()
    data class Available(val info: UpdateInfo) : UpdateUiState()
    object Downloading : UpdateUiState()
    data class Error(val message: String) : UpdateUiState()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var updateState by remember { mutableStateOf<UpdateUiState>(UpdateUiState.Idle) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("تنظیمات", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "بازگشت", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BgDark)
            )
        },
        containerColor = BgDark
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("به‌روزرسانی برنامه", color = AccentGreen, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("نسخه فعلی", color = Muted, style = MaterialTheme.typography.bodyMedium)
                        Text(
                            UpdateManager.currentVersionName(context),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    when (val state = updateState) {
                        is UpdateUiState.UpToDate -> InfoBanner("شما از آخرین نسخه برنامه استفاده می‌کنید.", AccentGreen)
                        is UpdateUiState.Available -> {
                            InfoBanner("نسخه جدید ${state.info.versionName} منتشر شده است!", Color(0xFFFBBF24))
                            Text(state.info.releaseNotes, color = Muted, style = MaterialTheme.typography.bodySmall)
                            Button(
                                onClick = {
                                    val url = state.info.downloadUrl
                                    if (url != null) {
                                        updateState = UpdateUiState.Downloading
                                        UpdateManager.downloadAndInstall(context, url, state.info.versionName)
                                    }
                                },
                                enabled = state.info.downloadUrl != null,
                                colors = ButtonDefaults.buttonColors(containerColor = AccentGreen, contentColor = Color(0xFF0B0E14)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    if (state.info.downloadUrl != null) "دانلود و نصب بروزرسانی" else "فایل نصبی موجود نیست",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        is UpdateUiState.Downloading -> InfoBanner("در حال دانلود... اعلان دانلود را دنبال کنید.", Color(0xFF38BDF8))
                        is UpdateUiState.Error -> InfoBanner(state.message, Color(0xFFEF4444))
                        else -> {}
                    }

                    Button(
                        onClick = {
                            updateState = UpdateUiState.Checking
                            scope.launch {
                                val result = UpdateManager.checkForUpdate(context)
                                updateState = result.fold(
                                    onSuccess = { info -> if (info == null) UpdateUiState.UpToDate else UpdateUiState.Available(info) },
                                    onFailure = { e -> UpdateUiState.Error(e.message ?: "خطا در بررسی نسخه جدید") }
                                )
                            }
                        },
                        enabled = updateState !is UpdateUiState.Checking,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF21262D), contentColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (updateState is UpdateUiState.Checking) {
                            CircularProgressIndicator(color = AccentGreen, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.Refresh, contentDescription = null, tint = AccentGreen, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("بررسی به‌روزرسانی", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoBanner(text: String, color: Color) {
    Surface(shape = RoundedCornerShape(10.dp), color = color.copy(alpha = 0.15f)) {
        Text(text, color = color, modifier = Modifier.padding(10.dp), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
    }
}
