package com.novavpn.app.ui;

import androidx.compose.animation.core.RepeatMode;
import androidx.compose.foundation.layout.Arrangement;
import androidx.compose.material.icons.Icons;
import androidx.compose.material3.CardDefaults;
import androidx.compose.runtime.Composable;
import androidx.compose.ui.Alignment;
import androidx.compose.ui.Modifier;
import androidx.compose.ui.graphics.Brush;
import androidx.compose.ui.graphics.drawscope.Stroke;
import androidx.compose.ui.text.font.FontWeight;
import com.novavpn.app.viewmodel.ConnectionState;
import com.novavpn.app.viewmodel.VpnUiState;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000D\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\u000b\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\b\n\u0002\u0010\t\n\u0002\b\u0003\u001a:\u0010\u0000\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00032\f\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00010\u00052\f\u0010\u0006\u001a\b\u0012\u0004\u0012\u00020\u00010\u00052\f\u0010\u0007\u001a\b\u0012\u0004\u0012\u00020\u00010\u0005H\u0003\u001aD\u0010\b\u001a\u00020\u00012\f\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00010\u00052\u0006\u0010\t\u001a\u00020\n2\f\u0010\u000b\u001a\b\u0012\u0004\u0012\u00020\u00010\u00052\f\u0010\f\u001a\b\u0012\u0004\u0012\u00020\u00010\u00052\b\b\u0002\u0010\r\u001a\u00020\u000eH\u0007\u001a?\u0010\u000f\u001a\u00020\u00012\u0011\u0010\u0010\u001a\r\u0012\u0004\u0012\u00020\u00010\u0005\u00a2\u0006\u0002\b\u00112\u0006\u0010\u0012\u001a\u00020\u00132\u0006\u0010\u0014\u001a\u00020\u00132\b\b\u0002\u0010\u0015\u001a\u00020\u0016H\u0003\u00f8\u0001\u0000\u00a2\u0006\u0004\b\u0017\u0010\u0018\u001a\u0010\u0010\u0019\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u0003H\u0003\u001a$\u0010\u001a\u001a\u00020\u00012\f\u0010\u001b\u001a\b\u0012\u0004\u0012\u00020\u00010\u00052\f\u0010\u001c\u001a\b\u0012\u0004\u0012\u00020\u00010\u0005H\u0003\u001a\u0010\u0010\u001d\u001a\u00020\u00132\u0006\u0010\u001e\u001a\u00020\u001fH\u0002\u001a\u0010\u0010 \u001a\u00020\u00132\u0006\u0010!\u001a\u00020\u001fH\u0002\u0082\u0002\u0007\n\u0005\b\u00a1\u001e0\u0001\u00a8\u0006\""}, d2 = {"ConnectionCard", "", "state", "Lcom/novavpn/app/viewmodel/VpnUiState;", "tryConnect", "Lkotlin/Function0;", "onDisconnect", "onClearError", "HomeScreen", "autoConnectRequested", "", "onNavigateToSettings", "onNavigateToLogs", "viewModel", "Lcom/novavpn/app/viewmodel/VpnViewModel;", "StatRow", "iconContent", "Landroidx/compose/runtime/Composable;", "label", "", "value", "valueColor", "Landroidx/compose/ui/graphics/Color;", "StatRow-g2O1Hgs", "(Lkotlin/jvm/functions/Function0;Ljava/lang/String;Ljava/lang/String;J)V", "StatsCard", "TopBar", "onSettings", "onLogs", "formatBytes", "bytes", "", "formatDuration", "seconds", "app_debug"})
public final class HomeScreenKt {
    
    @androidx.compose.runtime.Composable()
    public static final void HomeScreen(@org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> tryConnect, boolean autoConnectRequested, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onNavigateToSettings, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onNavigateToLogs, @org.jetbrains.annotations.NotNull()
    com.novavpn.app.viewmodel.VpnViewModel viewModel) {
    }
    
    @androidx.compose.runtime.Composable()
    private static final void TopBar(kotlin.jvm.functions.Function0<kotlin.Unit> onSettings, kotlin.jvm.functions.Function0<kotlin.Unit> onLogs) {
    }
    
    @androidx.compose.runtime.Composable()
    private static final void ConnectionCard(com.novavpn.app.viewmodel.VpnUiState state, kotlin.jvm.functions.Function0<kotlin.Unit> tryConnect, kotlin.jvm.functions.Function0<kotlin.Unit> onDisconnect, kotlin.jvm.functions.Function0<kotlin.Unit> onClearError) {
    }
    
    private static final java.lang.String formatDuration(long seconds) {
        return null;
    }
    
    private static final java.lang.String formatBytes(long bytes) {
        return null;
    }
    
    @androidx.compose.runtime.Composable()
    private static final void StatsCard(com.novavpn.app.viewmodel.VpnUiState state) {
    }
}