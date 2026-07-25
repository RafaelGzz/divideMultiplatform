package com.ragl.divide.domain.usecases.auth

import com.ragl.divide.domain.repositories.UserRepository
import io.mockative.coEvery
import io.mockative.coVerify
import io.mockative.mock
import io.mockative.of
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CheckEmailVerificationUseCaseTest {

    private val mockUserRepository = mock(of<UserRepository>())
    private lateinit var useCase: CheckEmailVerificationUseCase

    @BeforeTest
    fun setUp() {
        useCase = CheckEmailVerificationUseCase(mockUserRepository)
    }

    @Test
    fun `should return success with verified true when email is verified`() = runTest {
        // Given
        coEvery { mockUserRepository.isEmailVerified() } returns true

        // When
        val result = useCase()

        // Then
        assertTrue(result is CheckEmailVerificationUseCase.Result.Success)
        assertEquals(true, result.isVerified)
        coVerify { mockUserRepository.isEmailVerified() }
    }

    @Test
    fun `should return success with verified false when email is not verified`() = runTest {
        // Given
        coEvery { mockUserRepository.isEmailVerified() } returns false

        // When
        val result = useCase()

        // Then
        assertTrue(result is CheckEmailVerificationUseCase.Result.Success)
        assertEquals(false, result.isVerified)
        coVerify { mockUserRepository.isEmailVerified() }
    }

    @Test
    fun `should return error when repository throws exception`() = runTest {
        // Given
        val expectedException = Exception("Network error")
        coEvery { mockUserRepository.isEmailVerified() } throws expectedException

        // When
        val result = useCase()

        // Then
        assertTrue(result is CheckEmailVerificationUseCase.Result.Error)
        assertEquals(expectedException, result.exception)
        coVerify { mockUserRepository.isEmailVerified() }
    }
}
