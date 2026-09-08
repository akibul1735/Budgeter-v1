package com.example.util

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Event bridge to route OAuth callbacks from MainActivity intent to BudgetViewModel
 */
object DropboxAuthBridge {
    private val _authCodes = MutableSharedFlow<String>(extraBufferCapacity = 5)
    val authCodes = _authCodes.asSharedFlow()

    private val _authErrors = MutableSharedFlow<String>(extraBufferCapacity = 5)
    val authErrors = _authErrors.asSharedFlow()

    fun onAuthCodeReceived(code: String) {
        _authCodes.tryEmit(code)
    }

    fun onAuthErrorReceived(error: String) {
        _authErrors.tryEmit(error)
    }
}
