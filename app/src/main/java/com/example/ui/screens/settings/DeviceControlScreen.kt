package com.example.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.components.GlassCard
import com.example.ui.theme.PinkAccent

import android.content.Intent
import android.provider.Settings
import androidx.compose.ui.platform.LocalContext
import android.app.admin.DevicePolicyManager
import android.content.ComponentName

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceControlScreen(onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    
    val openSettings: (String) -> Unit = { action ->
        try {
            val intent = Intent(action).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    val openDeviceAdmin: () -> Unit = {
        try {
            val componentName = ComponentName(context, com.example.ai.tools.MyAdminReceiver::class.java)
            val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
                putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName)
                putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Required to lock the screen.")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Device Control", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Ambient glows
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 50.dp, end = 20.dp)
                    .size(250.dp)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(PinkAccent.copy(alpha = 0.15f), Color.Transparent),
                            radius = 350f
                        )
                    )
                    .blur(60.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Manage supported device features from your assistant.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                DeviceControlItem("🎙️ Wake Word Service", "Available", "Start") { 
                    try {
                        val intent = Intent(context, com.example.services.WakeWordService::class.java)
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                            context.startForegroundService(intent)
                        } else {
                            context.startService(intent)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                
                DeviceControlItem("🔋 Ignore Battery Opt.", "Required for Background", "Enable") {
                    try {
                        val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                            data = android.net.Uri.parse("package:${context.packageName}")
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                
                DeviceControlItem("🪟 Draw Over Apps", "Required for Screen Wake", "Enable") {
                    try {
                        val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
                            data = android.net.Uri.parse("package:${context.packageName}")
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                DeviceControlItem("🔦 Flashlight", "Available", "Manage") { openSettings(Settings.ACTION_DISPLAY_SETTINGS) }
                DeviceControlItem("🔊 Audio & Volume", "Available", "Manage") { openSettings(Settings.ACTION_SOUND_SETTINGS) }
                DeviceControlItem("📱 App Permissions", "Grant standard permissions", "Manage") {
                    try {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = android.net.Uri.parse("package:${context.packageName}")
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                DeviceControlItem("📱 All Apps", "Available", "Manage") { openSettings(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS) }
                DeviceControlItem("🖥️ Screen Control", "Available", "Manage") { openSettings(Settings.ACTION_DISPLAY_SETTINGS) }
                DeviceControlItem("♿ Accessibility", "Available", "Manage") { openSettings(Settings.ACTION_ACCESSIBILITY_SETTINGS) }
                DeviceControlItem("🔔 Notifications", "Available", "Manage") { openSettings("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS") }
                DeviceControlItem("🛡️ Device Admin", "Available", "Enable") { openDeviceAdmin() }
                DeviceControlItem("☀️ Write Settings", "Required for Brightness", "Enable") {
                    try {
                        val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS).apply {
                            data = android.net.Uri.parse("package:${context.packageName}")
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                DeviceControlItem("📶 Connectivity", "Available", "Manage") { openSettings(Settings.ACTION_WIFI_SETTINGS) }
                DeviceControlItem("🎵 Media Control", "Available", "Manage") { openSettings(Settings.ACTION_SOUND_SETTINGS) }
                DeviceControlItem("🔋 Device Information", "Available", "View") { openSettings(Settings.ACTION_DEVICE_INFO_SETTINGS) }
                
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun DeviceControlItem(title: String, status: String, actionText: String, onClick: () -> Unit) {
    GlassCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                )
                Text(
                    text = status,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            TextButton(onClick = onClick) {
                Text(actionText, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}
