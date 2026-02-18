package com.novavpn.app.ui;

import androidx.compose.animation.core.RepeatMode;
import androidx.compose.foundation.layout.Arrangement;
import androidx.compose.material.icons.Icons;
import androidx.compose.material3.CardDefaults;
import androidx.compose.runtime.Composable;
import androidx.compose.ui.Alignment;
import androidx.compose.ui.Modifier;
import androidx.compose.ui.graphics.Brush;
import androidx.compose.ui.graphics.PathEffect;
import androidx.compose.ui.graphics.drawscope.Stroke;
import androidx.compose.ui.text.font.FontWeight;
import com.novavpn.app.viewmodel.ConnectionState;
import com.novavpn.app.viewmodel.VpnUiState;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000$\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0007\u001a:\u0010\u0000\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00032\f\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00010\u00052\f\u0010\u0006\u001a\b\u0012\u0004\u0012\u00020\u00010\u00052\f\u0010\u0007\u001a\b\u0012\u0004\u0012\u00020\u00010\u0005H\u0003\u001aP\u0010\b\u001a\u00020\u00012\u0006\u0010\t\u001a\u00020\n2\f\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00010\u00052\f\u0010\u0006\u001a\b\u0012\u0004\u0012\u00020\u00010\u00052\u0006\u0010\u000b\u001a\u00020\f2\f\u0010\r\u001a\b\u0012\u0004\u0012\u00020\u00010\u00052\f\u0010\u000e\u001a\b\u0012\u0004\u0012\u00020\u00010\u0005H\u0007\u001a\b\u0010\u000f\u001a\u00020\u0001H\u0003\u001a$\u0010\u0010\u001a\u00020\u00012\f\u0010\u0011\u001a\b\u0012\u0004\u0012\u00020\u00010\u00052\f\u0010\u0012\u001a\b\u0012\u0004\u0012\u00020\u00010\u0005H\u0003\u00a8\u0006\u0013"}, d2 = {"ConnectionCard", "", "state", "Lcom/novavpn/app/viewmodel/VpnUiState;", "tryConnect", "Lkotlin/Function0;", "tryDisconnect", "onClearError", "HomeScreen", "viewModel", "Lcom/novavpn/app/viewmodel/VpnViewModel;", "autoConnectRequested", "", "onNavigateToSettings", "onNavigateToLogs", "StatsCard", "TopBar", "onSettings", "onLogs", "app_debug"})
public final class HomeScreenKt {
    
    @androidx.compose.runtime.Composable()
    public static final void HomeScreen(@org.jetbrains.annotations.NotNull()
    com.novavpn.app.viewmodel.VpnViewModel viewModel, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> tryConnect, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> tryDisconnect, boolean autoConnectRequested, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onNavigateToSettings, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onNavigateToLogs) {
    }
    
    @androidx.compose.runtime.Composable()
    private static final void TopBar(kotlin.jvm.functions.Function0<kotlin.Unit> onSettings, kotlin.jvm.functions.Function0<kotlin.Unit> onLogs) {
    }
    
    @androidx.compose.runtime.Composable()
    private static final void ConnectionCard(com.novavpn.app.viewmodel.VpnUiState state, kotlin.jvm.functions.Function0<kotlin.Unit> tryConnect, kotlin.jvm.functions.Function0<kotlin.Unit> tryDisconnect, kotlin.jvm.functions.Function0<kotlin.Unit> onClearError) {
    }
    
    @androidx.compose.runtime.Composable()
    private static final void StatsCard() {
    }
}