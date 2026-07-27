package com.ragl.divide.domain.usecases.auth

import com.ragl.divide.data.models.User
import com.ragl.divide.domain.repositories.UserRepository
import com.ragl.divide.domain.services.AnalyticsService
import com.ragl.divide.testing.FakeAnalyticsService
import com.ragl.divide.testing.FakeUserRepository
import com.ragl.divide.testing.assertCalled
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SignInWithEmailUseCaseTest {

    private val mockUserRepository = FakeUserRepository()
    private val mockAnalyticsService = FakeAnalyticsService()
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
        
        mockUserRepository.onSignInWithEmailAndPassword = { _, _ -> expectedUser }
        mockUserRepository.onIsEmailVerified = { true }
        mockAnalyticsService.onLogEvent = { _, _ -> }

        // When
        val result = useCase(email, password)

        // Then
        assertTrue(result is SignInWithEmailUseCase.Result.Success)
        assertEquals(expectedUser, result.user)
        assertCalled(mockAnalyticsService, "logEvent", "login", mapOf("method" to "email"))
    }

    @Test
    fun `should return EmailNotVerified when email is not verified`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "password123"
        val user = User(uuid = "user123", email = email, name = "Test User")
        
        mockUserRepository.onSignInWithEmailAndPassword = { _, _ -> user }
        mockUserRepository.onIsEmailVerified = { false }

        // When
        val result = useCase(email, password)

        // Then
        assertTrue(result is SignInWithEmailUseCase.Result.EmailNotVerified)
    }

    @Test
    fun `should return error when user repository returns null`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "password123"
        
        mockUserRepository.onSignInWithEmailAndPassword = { _, _ -> null }

        // When
        val result = useCase(email, password)

        // Then
        assertTrue(result is SignInWithEmailUseCase.Result.Error)
        assertEquals("Failed to login", result.exception.message)
    }

    @Test
    fun `should return error when repository throws exception`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "password123"
        val expectedException = Exception("Network error")
        
        mockUserRepository.onSignInWithEmailAndPassword = { _, _ -> throw expectedException }
        mockAnalyticsService.onLogError = { _, _ -> }

        // When
        val result = useCase(email, password)

        // Then
        assertTrue(result is SignInWithEmailUseCase.Result.Error)
        assertEquals(expectedException, result.exception)
        check(mockAnalyticsService.callCount("logError") == 1)
        assertEquals("Error en login con email", mockAnalyticsService.invocations.last().arguments[1])
    }
}
