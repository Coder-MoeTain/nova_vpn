package com.novavpn.app.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LogRepository @Inject constructor() {
    private val _lines = MutableStateFlow<List<String>>(emptyList())
    val lines: StateFlow<List<String>> = _lines.asStateFlow()

    fun add(line: String) {
        _lines.value = (_lines.value + line).takeLast(500)
    }

    fun getLogs(): String = _lines.value.joinToString("\n").ifEmpty { "No logs yet." }
}
