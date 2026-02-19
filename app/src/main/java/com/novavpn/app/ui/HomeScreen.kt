package com.novavpn.app.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.novavpn.app.viewmodel.ConnectionState
import com.novavpn.app.viewmodel.VpnUiState

@Composable
fun HomeScreen(
    viewModel: com.novavpn.app.viewmodel.VpnViewModel,
    tryConnect: () -> Unit,
    tryDisconnect: () -> Unit,
    autoConnectRequested: Boolean,
    onNavigateToSettings: () -> Unit,
    onNavigateToLogs: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(autoConnectRequested) {
        if (autoConnectRequested) tryConnect()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            TopBar(onSettings = onNavigateToSettings, onLogs = onNavigateToLogs)
            Spacer(modifier = Modifier.height(24.dp))
            ConnectionCard(state = state, tryConnect = tryConnect, tryDisconnect = tryDisconnect, onClearError = viewModel::clearError)
            Spacer(modifier = Modifier.height(24.dp))
            StatsCard(state = state)
        }
    }
}

@Composable
private fun TopBar(onSettings: () -> Unit, onLogs: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp)
    ) {
        Text(
            "NovaVPN",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.align(Alignment.CenterStart)
        )
        Row(
            modifier = Modifier.align(Alignment.CenterEnd),
            horizontalArrangement = Arrangement.End
        ) {
            IconButton(onClick = onLogs) {
                Icon(Icons.Default.List, contentDescription = "Logs", tint = MaterialTheme.colorScheme.onSurface)
            }
            IconButton(onClick = onSettings) {
                Icon(Icons.Default.Settings, contentDescription = "Settings", tint = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}

@Composable
private fun ConnectionCard(
    state: VpnUiState,
    tryConnect: () -> Unit,
    tryDisconnect: () -> Unit,
    onClearError: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Card(
        modifier = Modifier
            .padding(horizontal = 24.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val connectionState = state.connectionState
            AnimatedContent(
                targetState = connectionState,
                transitionSpec = {
                    when {
                        targetState is ConnectionState.Connecting -> scaleIn(
                            initialScale = 0.7f,
                            animationSpec = tween(400, easing = LinearEasing)
                        ) + fadeIn(animationSpec = tween(300)) togetherWith scaleOut(
                            targetScale = 0.9f,
                            animationSpec = tween(250)
                        ) + fadeOut(animationSpec = tween(200))
                        else -> fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(200))
                    }
                },
                label = "connectionState"
            ) { currentState ->
                when (currentState) {
                    is ConnectionState.Connecting -> {
                        val rotation by infiniteTransition.animateFloat(
                            initialValue = 0f,
                            targetValue = 360f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(2000, easing = LinearEasing),
                                repeatMode = RepeatMode.Restart
                            ),
                            label = "rotation"
                        )
                        val rotationReverse by infiniteTransition.animateFloat(
                            initialValue = 360f,
                            targetValue = 0f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(3000, easing = LinearEasing),
                                repeatMode = RepeatMode.Restart
                            ),
                            label = "rotationRev"
                        )
                        val ringAlpha by infiniteTransition.animateFloat(
                            initialValue = 0.2f,
                            targetValue = 0.6f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(800, easing = LinearEasing),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "ringAlpha"
                        )
                        val primaryColor = MaterialTheme.colorScheme.primary
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.size(140.dp)
                        ) {
                            Canvas(
                                modifier = Modifier
                                    .size(140.dp)
                                    .rotate(rotation)
                            ) {
                                drawCircle(
                                    color = primaryColor.copy(alpha = ringAlpha),
                                    radius = size.minDimension / 2f - 2.dp.toPx(),
                                    style = Stroke(width = 3.dp.toPx())
                                )
                            }
                            Canvas(
                                modifier = Modifier
                                    .size(120.dp)
                                    .rotate(rotationReverse)
                            ) {
                                drawCircle(
                                    color = primaryColor.copy(alpha = ringAlpha * 0.7f),
                                    radius = size.minDimension / 2f - 2.dp.toPx(),
                                    style = Stroke(width = 2.dp.toPx())
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .size(120.dp)
                                    .scale(scale)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(48.dp),
                                    color = MaterialTheme.colorScheme.primary,
                                    strokeWidth = 3.dp
                                )
                            }
                        }
                    }
                    is ConnectionState.Connected -> {
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .scale(scale)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(
                                            Color(0xFF4CAF50).copy(alpha = 0.5f),
                                            Color(0xFF4CAF50).copy(alpha = 0.15f)
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("VPN", fontSize = 18.sp, color = Color(0xFF2E7D32))
                        }
                    }
                    is ConnectionState.Disconnecting -> {
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(40.dp), strokeWidth = 2.dp)
                        }
                    }
                    is ConnectionState.Error -> {
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.error.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("!", fontSize = 48.sp, color = MaterialTheme.colorScheme.error)
                        }
                    }
                    else -> {
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .scale(scale)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Connect", fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                when (state.connectionState) {
                    is ConnectionState.Connecting -> "Connecting…"
                    is ConnectionState.Connected -> "Connected"
                    is ConnectionState.Disconnecting -> "Disconnecting…"
                    is ConnectionState.Error -> (state.connectionState as ConnectionState.Error).message
                    else -> state.successMessage ?: "Tap to connect"
                },
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (state.connectionState is ConnectionState.Connected && state.connectionTestResult != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    state.connectionTestResult,
                    style = MaterialTheme.typography.bodyMedium,
                    color = when (state.connectionTestResult) {
                        "Internet OK" -> MaterialTheme.colorScheme.primary
                        "Checking…" -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    }
                )
            }
            if (state.lastError != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    state.lastError,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
                state.errorHint?.let { hint ->
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        hint,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                androidx.compose.material3.TextButton(onClick = onClearError) {
                    Text("Dismiss")
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            when (state.connectionState) {
                is ConnectionState.Connected -> {
                    androidx.compose.material3.OutlinedButton(
                        onClick = tryDisconnect,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Disconnect")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
                is ConnectionState.Error -> {
                    androidx.compose.material3.Button(
                        onClick = tryConnect,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Try again")
                    }
                }
                else -> {
                    androidx.compose.material3.Button(
                        onClick = tryConnect,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = state.connectionState !is ConnectionState.Connecting && state.connectionState !is ConnectionState.Disconnecting
                    ) {
                        Text("Connect")
                    }
                }
            }
        }
    }
}

@Composable
private fun StatsCard(state: VpnUiState) {
    Card(
        modifier = Modifier
            .padding(horizontal = 24.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                "Data Statistics",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(16.dp))
            if (state.connectionState is ConnectionState.Connected) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.ArrowDownward,
                                contentDescription = "Download",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                "Download",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            formatBytes(state.rxBytes),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.ArrowUpward,
                                contentDescription = "Upload",
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                "Upload",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            formatBytes(state.txBytes),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            } else {
                Text(
                    "Connect to VPN to see statistics",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}

private fun formatBytes(bytes: Long): String {
    return when {
        bytes >= 1_000_000_000 -> String.format("%.2f GB", bytes / 1_000_000_000.0)
        bytes >= 1_000_000 -> String.format("%.2f MB", bytes / 1_000_000.0)
        bytes >= 1_000 -> String.format("%.2f KB", bytes / 1_000.0)
        else -> "$bytes B"
    }
}
