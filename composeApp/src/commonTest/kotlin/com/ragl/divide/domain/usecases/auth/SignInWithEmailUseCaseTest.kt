package com.ragl.divide.domain.usecases.auth

import com.ragl.divide.data.models.User
import com.ragl.divide.domain.repositories.UserRepository
import com.ragl.divide.domain.services.AnalyticsService
import io.mockative.any
import io.mockative.coEvery
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

class SignInWithEmailUseCaseTest {

    private val mockUserRepository = mock(of<UserRepository>())
    private val mockAnalyticsService = mock(of<AnalyticsService>())
    private lateinit var useCase: SignInWithEmailUseCase

    @BeforeTest
    fun setUp() {
        useCase = SignInWithEmailUseCase(mockUserRepository, mockAnalyticsService)
    }

    @Test
    fun `should return success when login is successful and email is verified`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "password123"
        val expectedUser = User(
            uuid = "user123",
            email = email,
            name = "Test User"
        )
        
        coEvery { mockUserRepository.signInWithEmailAndPassword(email, password) } returns expectedUser
        coEvery { mockUserRepository.isEmailVerified() } returns true
        every { mockAnalyticsService.logEvent(any(), any()) } returns Unit

        // When
        val result = useCase(email, password)

        // Then
        assertTrue(result is SignInWithEmailUseCase.Result.Success)
        assertEquals(expectedUser, result.user)
        verify { mockAnalyticsService.logEvent(
            eq("login"),
            any()
        )}
    }

    @Test
    fun `should return EmailNotVerified when email is not verified`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "password123"
        val user = User(uuid = "user123", email = email, name = "Test User")
        
        coEvery { mockUserRepository.signInWithEmailAndPassword(email, password) } returns user
        coEvery { mockUserRepository.isEmailVerified() } returns false

        // When
        val result = useCase(email, password)

        // Then
        assertTrue(result is SignInWithEmailUseCase.Result.EmailNotVerified)
        verify { mockAnalyticsService.logEvent(any(), any()) }
    }

    @Test
    fun `should return error when user repository returns null`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "password123"
        
        coEvery { mockUserRepository.signInWithEmailAndPassword(email, password) } returns null

        // When
        val result = useCase(email, password)

        // Then
        assertTrue(result is SignInWithEmailUseCase.Result.Error)
        assertEquals("Failed to login", result.exception.message)
        verify { mockAnalyticsService.logEvent(any(), any()) }
    }

    @Test
    fun `should return error when repository throws exception`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "password123"
        val expectedException = Exception("Network error")
        
        coEvery { mockUserRepository.signInWithEmailAndPassword(email, password) } throws expectedException
        every { mockAnalyticsService.logError(any(), any()) } returns Unit

        // When
        val result = useCase(email, password)

        // Then
        assertTrue(result is SignInWithEmailUseCase.Result.Error)
        assertEquals(expectedException, result.exception)
        verify { mockAnalyticsService.logError(any(), eq("Error en login con email")) }
    }
}
