package com.ragl.divide.domain.usecases.auth

import com.ragl.divide.domain.repositories.UserRepository
import com.ragl.divide.domain.services.ScheduleNotificationService
import io.mockative.coEvery
import io.mockative.coVerify
import io.mockative.every
import io.mockative.mock
import io.mockative.of
import io.mockative.verify
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SignOutUseCaseTest {

    private val mockUserRepository = mock(of<UserRepository>())
    private val mockScheduleNotificationService = mock(of<ScheduleNotificationService>())
    private lateinit var useCase: SignOutUseCase

    @BeforeTest
    fun setUp() {
        useCase = SignOutUseCase(mockUserRepository, mockScheduleNotificationService)
    }

    @Test
    fun `should return success when signout is successful`() = runTest {
        // Given
        coEvery { mockUserRepository.signOut() } returns Unit
        every { mockScheduleNotificationService.cancelAllNotifications() } returns Unit
        every { mockUserRepository.getCurrentUser() } returns null

        // When
        val result = useCase()

        // Then
        assertTrue(result is SignOutUseCase.Result.Success)
        verify { mockScheduleNotificationService.cancelAllNotifications() }
        coVerify { mockUserRepository.signOut() }
    }

    @Test
    fun `should return error when repository throws exception`() = runTest {
        // Given
        val expectedException = Exception("Network error")
        coEvery { mockUserRepository.signOut() } throws expectedException
        coEvery { mockScheduleNotificationService.cancelAllNotifications() } returns Unit

        // When
        val result = useCase()

        // Then
        assertTrue(result is SignOutUseCase.Result.Error)
        assertEquals(expectedException, result.exception)
        coVerify { mockScheduleNotificationService.cancelAllNotifications() }
    }
}
