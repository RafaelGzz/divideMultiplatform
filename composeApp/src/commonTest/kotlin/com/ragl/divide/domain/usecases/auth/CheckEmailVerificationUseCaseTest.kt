package com.ragl.divide.domain.usecases.auth

import com.ragl.divide.domain.repositories.UserRepository
import com.ragl.divide.testing.FakeUserRepository
import com.ragl.divide.testing.assertCalled
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CheckEmailVerificationUseCaseTest {

    private val mockUserRepository = FakeUserRepository()
    private lateinit var useCase: CheckEmailVerificationUseCase

    @BeforeTest
    fun setUp() {
        useCase = CheckEmailVerificationUseCase(mockUserRepository)
    }

    @Test
    fun `should return success with verified true when email is verified`() = runTest {
        // Given
        mockUserRepository.onIsEmailVerified = { true }

        // When
        val result = useCase()

        // Then
        assertTrue(result is CheckEmailVerificationUseCase.Result.Success)
        assertEquals(true, result.isVerified)
        assertCalled(mockUserRepository, "isEmailVerified")
    }

    @Test
    fun `should return success with verified false when email is not verified`() = runTest {
        // Given
        mockUserRepository.onIsEmailVerified = { false }

        // When
        val result = useCase()

        // Then
        assertTrue(result is CheckEmailVerificationUseCase.Result.Success)
        assertEquals(false, result.isVerified)
        assertCalled(mockUserRepository, "isEmailVerified")
    }

    @Test
    fun `should return error when repository throws exception`() = runTest {
        // Given
        val expectedException = Exception("Network error")
        mockUserRepository.onIsEmailVerified = { throw expectedException }

        // When
        val result = useCase()

        // Then
        assertTrue(result is CheckEmailVerificationUseCase.Result.Error)
        assertEquals(expectedException, result.exception)
        assertCalled(mockUserRepository, "isEmailVerified")
    }
}
