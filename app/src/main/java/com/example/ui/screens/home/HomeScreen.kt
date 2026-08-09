package com.example.ui.screens.home

import android.Manifest
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ai.AssistantState
import com.example.ai.VoiceAssistantViewModel
import com.example.data.prefs.AppPreferences
import com.example.ui.components.AIOrb
import com.example.ui.components.GlassMicButton
import com.example.ui.components.OrbState
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.PurpleAccent
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.rememberMultiplePermissionsState

import android.app.Activity
import com.example.MainActivity

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun HomeScreen(
    onNavigateToSettings: () -> Unit = {}
) {
    val context = LocalContext.current
    val appPreferences = remember { AppPreferences(context) }
    val aiName by appPreferences.assistantNameFlow.collectAsState(initial = "AI Assistant")
    
    val voiceViewModel: VoiceAssistantViewModel = viewModel()
    val assistantState by voiceViewModel.state.collectAsState()
    val spokenText by voiceViewModel.spokenText.collectAsState()

    val recordAudioPermission = rememberPermissionState(Manifest.permission.RECORD_AUDIO)
    val multiplePermissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.CALL_PHONE,
            Manifest.permission.SEND_SMS,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )
    
    LaunchedEffect(Unit) {
        if (!multiplePermissionsState.allPermissionsGranted) {
            multiplePermissionsState.launchMultiplePermissionRequest()
        }

        val activity = context as? Activity
        val isWakeWord = activity?.intent?.getBooleanExtra("wake_word_detected", false) == true
        if (isWakeWord && recordAudioPermission.status.isGranted) {
            voiceViewModel.toggleListening()
            activity.intent?.removeExtra("wake_word_detected")
        }
    }

    val orbState = when (assistantState) {
        AssistantState.IDLE -> OrbState.IDLE
        AssistantState.LISTENING -> OrbState.LISTENING
        AssistantState.THINKING -> OrbState.THINKING
        AssistantState.SPEAKING -> OrbState.SPEAKING
        AssistantState.ERROR -> OrbState.ERROR
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Background ambient lights
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 50.dp, end = 20.dp)
                    .size(300.dp)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(PurpleAccent.copy(alpha = 0.15f), Color.Transparent),
                            radius = 400f
                        )
                    )
                    .blur(80.dp)
            )

            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(bottom = 100.dp, start = 20.dp)
                    .size(250.dp)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(CyanAccent.copy(alpha = 0.1f), Color.Transparent),
                            radius = 300f
                        )
                    )
                    .blur(80.dp)
            )

            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Top Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { /* TODO: Open drawer/menu */ },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Menu",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Text(
                        text = aiName,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    IconButton(
                        onClick = onNavigateToSettings,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))
                
                if (spokenText.isNotBlank()) {
                    Text(
                        text = "\"$spokenText\"",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = MaterialTheme.colorScheme.onBackground,
                            textAlign = TextAlign.Center
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 32.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(32.dp))

                // Hero Section - AI Orb
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(3f),
                    contentAlignment = Alignment.Center
                ) {
                    AIOrb(state = orbState)
                }

                Spacer(modifier = Modifier.weight(1f))

                // Bottom Mic Area
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 64.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = when (assistantState) {
                                AssistantState.LISTENING -> "Listening..."
                                AssistantState.THINKING -> "Thinking..."
                                AssistantState.SPEAKING -> "Speaking..."
                                AssistantState.ERROR -> "Couldn't hear that."
                                else -> "Tap to speak"
                            },
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        GlassMicButton(
                            isListening = assistantState == AssistantState.LISTENING,
                            onClick = {
                                if (recordAudioPermission.status.isGranted) {
                                    voiceViewModel.toggleListening()
                                } else {
                                    recordAudioPermission.launchPermissionRequest()
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

