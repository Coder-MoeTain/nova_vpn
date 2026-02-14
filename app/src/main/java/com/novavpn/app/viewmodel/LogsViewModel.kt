package com.novavpn.app.viewmodel

import androidx.lifecycle.ViewModel
import com.novavpn.app.data.LogRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class LogsViewModel @Inject constructor(
    private val logRepository: LogRepository
) : ViewModel() {

    val logText = logRepository.lines.map { lines ->
        lines.joinToString("\n").ifEmpty { "" }
    }
}
