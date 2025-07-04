package com.ragl.divide.data.services

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class AppStateService {
    
    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()
    
    // Error state
    private val _errorState = MutableStateFlow<String?>(null)
    val errorState = _errorState.asStateFlow()
    
    // Success state
    private val _successState = MutableStateFlow<String?>(null)
    val successState = _successState.asStateFlow()
    
    // Loading methods
    fun showLoading() {
        _isLoading.value = true
    }
    
    fun hideLoading() {
        _isLoading.value = false
    }
    
    // Error methods
    fun handleError(message: String?) {
        _errorState.value = message
    }
    
    fun clearError() {
        _errorState.value = null
    }
    
    // Success methods
    fun handleSuccess(message: String) {
        _successState.value = message
    }
    
    fun clearSuccess() {
        _successState.value = null
    }
} 