package com.ragl.divide.data.services

import com.ragl.divide.domain.services.AppStateService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class AppStateServiceImpl : AppStateService {
    
    // Loading state
    private val _isLoading = MutableStateFlow(false)
    override val isLoading = _isLoading.asStateFlow()
    
    // Error state
    private val _errorState = MutableStateFlow<String?>(null)
    override val errorState = _errorState.asStateFlow()
    
    // Success state
    private val _successState = MutableStateFlow<String?>(null)
    override val successState = _successState.asStateFlow()
    
    // Loading methods
    override fun showLoading() {
        _isLoading.value = true
    }
    
    override fun hideLoading() {
        _isLoading.value = false
    }
    
    // Error methods
    override fun handleError(message: String?) {
        _errorState.value = message
    }
    
    override fun clearError() {
        _errorState.value = null
    }
    
    // Success methods
    override fun handleSuccess(message: String) {
        _successState.value = message
    }
    
    override fun clearSuccess() {
        _successState.value = null
    }
} 