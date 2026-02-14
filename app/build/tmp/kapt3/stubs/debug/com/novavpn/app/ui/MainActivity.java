package com.novavpn.app.ui;

import android.os.Bundle;
import androidx.activity.ComponentActivity;
import com.novavpn.app.util.Logger;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.compose.ui.Modifier;
import com.novavpn.app.ui.theme.NovaVpnTheme;
import com.novavpn.app.vpn.BootReceiver;
import com.novavpn.app.viewmodel.VpnViewModel;
import com.wireguard.android.backend.GoBackend;
import dagger.hilt.android.AndroidEntryPoint;

@dagger.hilt.android.AndroidEntryPoint()
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000*\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\b\u0007\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\u0012\u0010\f\u001a\u00020\r2\b\u0010\u000e\u001a\u0004\u0018\u00010\u000fH\u0014R\u0014\u0010\u0003\u001a\b\u0012\u0004\u0012\u00020\u00050\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001b\u0010\u0006\u001a\u00020\u00078BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b\n\u0010\u000b\u001a\u0004\b\b\u0010\t\u00a8\u0006\u0010"}, d2 = {"Lcom/novavpn/app/ui/MainActivity;", "Landroidx/activity/ComponentActivity;", "()V", "vpnPermissionLauncher", "Landroidx/activity/result/ActivityResultLauncher;", "Landroid/content/Intent;", "vpnViewModel", "Lcom/novavpn/app/viewmodel/VpnViewModel;", "getVpnViewModel", "()Lcom/novavpn/app/viewmodel/VpnViewModel;", "vpnViewModel$delegate", "Lkotlin/Lazy;", "onCreate", "", "savedInstanceState", "Landroid/os/Bundle;", "app_debug"})
public final class MainActivity extends androidx.activity.ComponentActivity {
    @org.jetbrains.annotations.NotNull()
    private final kotlin.Lazy vpnViewModel$delegate = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.activity.result.ActivityResultLauncher<android.content.Intent> vpnPermissionLauncher = null;
    
    public MainActivity() {
        super(0);
    }
    
    private final com.novavpn.app.viewmodel.VpnViewModel getVpnViewModel() {
        return null;
    }
    
    @java.lang.Override()
    protected void onCreate(@org.jetbrains.annotations.Nullable()
    android.os.Bundle savedInstanceState) {
    }
}