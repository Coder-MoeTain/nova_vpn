package com.novavpn.app.viewmodel;

import androidx.lifecycle.ViewModel;
import com.novavpn.app.data.LogRepository;
import dagger.hilt.android.lifecycle.HiltViewModel;
import javax.inject.Inject;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u001e\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010\u000e\n\u0002\b\u0003\b\u0007\u0018\u00002\u00020\u0001B\u000f\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00070\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\b\u0010\t\u00a8\u0006\n"}, d2 = {"Lcom/novavpn/app/viewmodel/LogsViewModel;", "Landroidx/lifecycle/ViewModel;", "logRepository", "Lcom/novavpn/app/data/LogRepository;", "(Lcom/novavpn/app/data/LogRepository;)V", "logText", "Lkotlinx/coroutines/flow/Flow;", "", "getLogText", "()Lkotlinx/coroutines/flow/Flow;", "app_debug"})
@dagger.hilt.android.lifecycle.HiltViewModel()
public final class LogsViewModel extends androidx.lifecycle.ViewModel {
    @org.jetbrains.annotations.NotNull()
    private final com.novavpn.app.data.LogRepository logRepository = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.Flow<java.lang.String> logText = null;
    
    @javax.inject.Inject()
    public LogsViewModel(@org.jetbrains.annotations.NotNull()
    com.novavpn.app.data.LogRepository logRepository) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.Flow<java.lang.String> getLogText() {
        return null;
    }
}