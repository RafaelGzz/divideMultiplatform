package com.ragl.divide.ui.screens.onboarding

import cafe.adriel.voyager.core.model.ScreenModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

data class OnboardingState(
    val currentPage: Int = 0,
    val isCompleted: Boolean = false
)

class OnboardingViewModel: ScreenModel {

    private val _state = MutableStateFlow(OnboardingState())
    val state = _state.asStateFlow()

    val totalPages = 3

    fun nextPage() {
        val currentPage = _state.value.currentPage
        if (currentPage < totalPages - 1) {
            _state.value = _state.value.copy(currentPage = currentPage + 1)
        }
    }

    fun setPage(page: Int) {
        if (page in 0 until totalPages) {
            _state.value = _state.value.copy(currentPage = page)
        }
    }
}