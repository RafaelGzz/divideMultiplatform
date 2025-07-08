package com.ragl.divide.domain.services

import kotlinx.coroutines.flow.StateFlow

interface AppStateService {
    val isLoading: StateFlow<Boolean>
    val errorState: StateFlow<String?>
    val successState: StateFlow<String?>
    
    fun showLoading()
    fun hideLoading()
    fun handleError(message: String?)
    fun clearError()
    fun handleSuccess(message: String)
    fun clearSuccess()
} 