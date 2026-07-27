package com.ragl.divide.domain.usecases.auth

import com.ragl.divide.data.models.User
import com.ragl.divide.domain.repositories.UserRepository
import com.ragl.divide.domain.services.AnalyticsService
import com.ragl.divide.testing.FakeAnalyticsService
import com.ragl.divide.testing.FakeUserRepository
import com.ragl.divide.testing.assertCalled
import com.ragl.divide.testing.assertNotCalled
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SignUpWithEmailUseCaseTest {

    private val mockUserRepository = FakeUserRepository()
    private val mockAnalyticsService = FakeAnalyticsService()
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
        
        mockUserRepository.onSignUpWithEmailAndPassword = { _, _, _ -> expectedUser }
        mockAnalyticsService.onLogEvent = { _, _ -> }
        mockUserRepository.onSignOut = {}

        // When
        val result = useCase(email, password, name)

        // Then
        assertTrue(result is SignUpWithEmailUseCase.Result.Success)
        check(mockAnalyticsService.callCount("logEvent") == 1)
        assertEquals("sign_up", mockAnalyticsService.invocations.last().arguments[0])
        assertCalled(mockUserRepository, "signOut")
    }

    @Test
    fun `should return error when user repository returns null`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "password123"
        val name = "Test User"
        
        mockUserRepository.onSignUpWithEmailAndPassword = { _, _, _ -> null }

        // When
        val result = useCase(email, password, name)

        // Then
        assertTrue(result is SignUpWithEmailUseCase.Result.Error)
        assertEquals("Failed to sign up", result.exception.message)
        assertNotCalled(mockAnalyticsService, "logEvent")
    }

    @Test
    fun `should return error when repository throws exception`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "password123"
        val name = "Test User"
        val expectedException = Exception("Network error")
        
        mockUserRepository.onSignUpWithEmailAndPassword = { _, _, _ -> throw expectedException }
        mockAnalyticsService.onLogError = { _, _ -> }

        // When
        val result = useCase(email, password, name)

        // Then
        assertTrue(result is SignUpWithEmailUseCase.Result.Error)
        assertEquals(expectedException, result.exception)
        check(mockAnalyticsService.callCount("logError") == 1)
        assertEquals("Error en registro con email", mockAnalyticsService.invocations.last().arguments[1])
    }
}
