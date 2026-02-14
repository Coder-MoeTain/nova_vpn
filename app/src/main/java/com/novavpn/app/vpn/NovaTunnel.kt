package com.novavpn.app.vpn

import android.os.Handler
import android.os.Looper
import com.novavpn.app.util.Logger
import com.wireguard.android.backend.Tunnel
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

/**
 * Single tunnel instance for NovaVPN. State changes reported to [onStateChange].
 * Callback is posted to the main queue to avoid re-entrancy and StackOverflowError.
 * Rapid backend callbacks (e.g. UP, DOWN, UP, DOWN in the same moment) are coalesced:
 * only the latest state is delivered once per batch.
 */
class NovaTunnel(
    private val onStateChange: (Tunnel.State) -> Unit
) : Tunnel {

    private val mainHandler = Handler(Looper.getMainLooper())
    private val pendingState = AtomicReference<Tunnel.State?>(null)
    private val runnablePosted = AtomicBoolean(false)

    private val coalescingRunnable: Runnable = Runnable {
        val state = pendingState.getAndSet(null)
        runnablePosted.set(false)
        if (state != null) {
            Logger.d("NovaTunnel: applying state=$state")
            onStateChange(state)
        }
        if (pendingState.get() != null && runnablePosted.compareAndSet(false, true)) {
            mainHandler.post(coalescingRunnable)
        }
    }

    override fun getName(): String = TUNNEL_NAME

    override fun onStateChange(newState: Tunnel.State) {
        Logger.d("NovaTunnel: onStateChange newState=$newState")
        pendingState.set(newState)
        if (runnablePosted.compareAndSet(false, true)) {
            mainHandler.post(coalescingRunnable)
        }
    }

    companion object {
        const val TUNNEL_NAME = "novavpn"
    }
}
