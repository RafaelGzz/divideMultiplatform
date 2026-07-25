package com.ragl.divide.domain.usecases.auth

import com.ragl.divide.data.models.User
import com.ragl.divide.domain.repositories.UserRepository
import com.ragl.divide.domain.services.AnalyticsService
import io.mockative.any
import io.mockative.coEvery
import io.mockative.coVerify
import io.mockative.eq
import io.mockative.every
import io.mockative.mock
import io.mockative.of
import io.mockative.verify
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SignUpWithEmailUseCaseTest {

    private val mockUserRepository = mock(of<UserRepository>())
    private val mockAnalyticsService = mock(of<AnalyticsService>())
    private lateinit var useCase: SignUpWithEmailUseCase

    @BeforeTest
    fun setUp() {
        useCase = SignUpWithEmailUseCase(mockUserRepository, mockAnalyticsService)
    }

    @Test
    fun `should return success when signup is successful`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "password123"
        val name = "Test User"
        val expectedUser = User(
            uuid = "user123",
            email = email,
            name = name
        )
        
        coEvery { mockUserRepository.signUpWithEmailAndPassword(email, password, name) } returns expectedUser
        every { mockAnalyticsService.logEvent(any(), any()) } returns Unit
        coEvery { mockUserRepository.signOut() } returns Unit

        // When
        val result = useCase(email, password, name)

        // Then
        assertTrue(result is SignUpWithEmailUseCase.Result.Success)
        verify {
            mockAnalyticsService.logEvent(
                eq("sign_up"),
                any()
            )
        }
        coVerify { mockUserRepository.signOut() }
    }

    @Test
    fun `should return error when user repository returns null`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "password123"
        val name = "Test User"
        
        coEvery { mockUserRepository.signUpWithEmailAndPassword(email, password, name) } returns null

        // When
        val result = useCase(email, password, name)

        // Then
        assertTrue(result is SignUpWithEmailUseCase.Result.Error)
        assertEquals("Failed to sign up", result.exception.message)
        verify{ mockAnalyticsService.logEvent(any(), any()) }.wasNotInvoked()
    }

    @Test
    fun `should return error when repository throws exception`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "password123"
        val name = "Test User"
        val expectedException = Exception("Network error")
        
        coEvery { mockUserRepository.signUpWithEmailAndPassword(email, password, name) } throws expectedException
        every { mockAnalyticsService.logError(any(), any()) } returns Unit

        // When
        val result = useCase(email, password, name)

        // Then
        assertTrue(result is SignUpWithEmailUseCase.Result.Error)
        assertEquals(expectedException, result.exception)
        verify { mockAnalyticsService.logError(any(), eq("Error en registro con email")) }
    }
}
