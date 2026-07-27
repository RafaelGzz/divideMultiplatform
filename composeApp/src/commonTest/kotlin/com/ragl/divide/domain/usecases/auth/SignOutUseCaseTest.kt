package com.ragl.divide.domain.usecases.auth

import com.ragl.divide.domain.repositories.UserRepository
import com.ragl.divide.domain.services.ScheduleNotificationService
import com.ragl.divide.testing.FakeScheduleNotificationService
import com.ragl.divide.testing.FakeUserRepository
import com.ragl.divide.testing.assertCalled
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SignOutUseCaseTest {

    private val mockUserRepository = FakeUserRepository()
    private val mockScheduleNotificationService = FakeScheduleNotificationService()
    private lateinit var useCase: SignOutUseCase

    @BeforeTest
    fun setUp() {
        useCase = SignOutUseCase(mockUserRepository, mockScheduleNotificationService)
    }

    @Test
    fun `should return success when signout is successful`() = runTest {
        // Given
        mockUserRepository.onSignOut = {}
        mockScheduleNotificationService.onCancelAllNotifications = {}
        mockUserRepository.onGetCurrentUser = { null }

        // When
        val result = useCase()

        // Then
        assertTrue(result is SignOutUseCase.Result.Success)
        assertCalled(mockScheduleNotificationService, "cancelAllNotifications")
        assertCalled(mockUserRepository, "signOut")
    }

    @Test
    fun `should return error when repository throws exception`() = runTest {
        // Given
        val expectedException = Exception("Network error")
        mockUserRepository.onSignOut = { throw expectedException }
        mockScheduleNotificationService.onCancelAllNotifications = {}

        // When
        val result = useCase()

        // Then
        assertTrue(result is SignOutUseCase.Result.Error)
        assertEquals(expectedException, result.exception)
        assertCalled(mockScheduleNotificationService, "cancelAllNotifications")
    }
}
