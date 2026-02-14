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
            ConnectionCard(state = state, tryConnect = tryConnect, onDisconnect = viewModel::disconnect, onClearError = viewModel::clearError)
            Spacer(modifier = Modifier.height(24.dp))
            StatsCard(state)
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
    onDisconnect: () -> Unit,
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
                        targetState is ConnectionState.Disconnecting -> fadeIn(animationSpec = tween(1200, easing = LinearEasing)) togetherWith fadeOut(animationSpec = tween(400))
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
                    is ConnectionState.Disconnecting -> {
                        val disconnectingScale by animateFloatAsState(
                            targetValue = 0.92f,
                            animationSpec = tween(durationMillis = 1400, easing = LinearEasing),
                            label = "disconnectingScale"
                        )
                        val disconnectingAlpha by animateFloatAsState(
                            targetValue = 0.85f,
                            animationSpec = tween(durationMillis = 1200, easing = LinearEasing),
                            label = "disconnectingAlpha"
                        )
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .scale(disconnectingScale)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.3f * disconnectingAlpha)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "…",
                                fontSize = 32.sp,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = disconnectingAlpha)
                            )
                        }
                    }
                    is ConnectionState.Connected -> {
                        val flowTransition = rememberInfiniteTransition(label = "dataFlow")
                        val flowRotation by flowTransition.animateFloat(
                            initialValue = 0f,
                            targetValue = 360f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(4000, easing = LinearEasing),
                                repeatMode = RepeatMode.Restart
                            ),
                            label = "flowRotation"
                        )
                        val flowRotationReverse by flowTransition.animateFloat(
                            initialValue = 360f,
                            targetValue = 0f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(6000, easing = LinearEasing),
                                repeatMode = RepeatMode.Restart
                            ),
                            label = "flowRotationRev"
                        )
                        val dashPhase by flowTransition.animateFloat(
                            initialValue = 0f,
                            targetValue = 35f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(1500, easing = LinearEasing),
                                repeatMode = RepeatMode.Restart
                            ),
                            label = "dashPhase"
                        )
                        val primaryColor = MaterialTheme.colorScheme.primary
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.size(140.dp)
                        ) {
                            Canvas(
                                modifier = Modifier
                                    .size(140.dp)
                                    .rotate(flowRotation)
                            ) {
                                drawCircle(
                                    color = primaryColor.copy(alpha = 0.5f),
                                    radius = size.minDimension / 2f - 2.dp.toPx(),
                                    style = Stroke(
                                        width = 2.5.dp.toPx(),
                                        pathEffect = PathEffect.dashPathEffect(
                                            floatArrayOf(12.dp.toPx(), 18.dp.toPx()),
                                            phase = dashPhase
                                        )
                                    )
                                )
                            }
                            Canvas(
                                modifier = Modifier
                                    .size(128.dp)
                                    .rotate(flowRotationReverse)
                            ) {
                                drawCircle(
                                    color = primaryColor.copy(alpha = 0.35f),
                                    radius = size.minDimension / 2f - 2.dp.toPx(),
                                    style = Stroke(
                                        width = 2.dp.toPx(),
                                        pathEffect = PathEffect.dashPathEffect(
                                            floatArrayOf(8.dp.toPx(), 14.dp.toPx()),
                                            phase = dashPhase * 0.7f
                                        )
                                    )
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .size(120.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("ON", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
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
                    is ConnectionState.Connecting -> {
                        val remaining = state.connectingTimeoutRemainingSeconds
                        if (remaining != null && remaining > 0)
                            "Connecting… (${remaining}s)"
                        else
                            "Connecting…"
                    }
                    is ConnectionState.Connected -> "Connected"
                    is ConnectionState.Disconnecting -> "Disconnecting…"
                    is ConnectionState.Error -> (state.connectionState as ConnectionState.Error).message
                    else -> "Tap to connect"
                },
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (state.connectionState is ConnectionState.Connecting && state.connectingTimeoutRemainingSeconds != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Timeout in ${state.connectingTimeoutRemainingSeconds}s — check server reachability",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
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
                    androidx.compose.material3.FilledTonalButton(
                        onClick = onDisconnect,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Disconnect")
                    }
                }
                is ConnectionState.Disconnecting -> {
                    androidx.compose.material3.FilledTonalButton(
                        onClick = { },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = false
                    ) {
                        Text("Disconnecting…")
                    }
                }
                is ConnectionState.Error -> {
                    androidx.compose.material3.Button(
                        onClick = tryConnect,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Reconnect")
                    }
                }
                else -> {
                    androidx.compose.material3.Button(
                        onClick = tryConnect,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = state.connectionState !is ConnectionState.Connecting
                    ) {
                        Text("Connect")
                    }
                }
            }
        }
    }
}

private fun formatDuration(seconds: Long): String {
    if (seconds < 60) return "${seconds}s"
    val m = seconds / 60
    val s = seconds % 60
    return if (m < 60) "${m}m ${s}s" else "${m / 60}h ${m % 60}m ${s}s"
}

private fun formatBytes(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "${bytes / 1024} KB"
        bytes < 1024L * 1024 * 1024 -> String.format("%.2f MB", bytes / (1024.0 * 1024))
        else -> String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024))
    }
}

@Composable
private fun StatRow(
    iconContent: @Composable () -> Unit,
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(24.dp), contentAlignment = Alignment.Center) {
                iconContent()
            }
            Spacer(modifier = Modifier.size(12.dp))
            Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
        }
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = valueColor)
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
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                "Statistics",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.height(12.dp))
            if (state.connectionState is ConnectionState.Connected) {
                StatRow(
                    iconContent = {
                        Icon(Icons.Filled.Schedule, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
                    },
                    label = "Duration",
                    value = formatDuration(state.connectionTimeSeconds)
                )
                state.publicIp?.let { ip ->
                    StatRow(
                        iconContent = {
                            Icon(Icons.Filled.Public, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
                        },
                        label = "Public IP",
                        value = ip
                    )
                }
                StatRow(
                    iconContent = {
                        Icon(Icons.Filled.ArrowDownward, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
                    },
                    label = "Downloaded",
                    value = formatBytes(state.statsRxBytes),
                    valueColor = MaterialTheme.colorScheme.primary
                )
                StatRow(
                    iconContent = {
                        Icon(Icons.Filled.ArrowUpward, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.secondary)
                    },
                    label = "Uploaded",
                    value = formatBytes(state.statsTxBytes),
                    valueColor = MaterialTheme.colorScheme.secondary
                )
            } else {
                Text("Not connected", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
            }
        }
    }
}
