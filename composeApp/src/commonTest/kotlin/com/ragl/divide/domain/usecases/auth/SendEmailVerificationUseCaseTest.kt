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

class SendEmailVerificationUseCaseTest {

    private val mockUserRepository = mock(of<UserRepository>())
    private lateinit var useCase: SendEmailVerificationUseCase

    @BeforeTest
    fun setUp() {
        useCase = SendEmailVerificationUseCase(mockUserRepository)
    }

    @Test
    fun `should return success when email verification is sent successfully`() = runTest {
        // Given
        coEvery { mockUserRepository.sendEmailVerification() } returns Unit

        // When
        val result = useCase()

        // Then
        assertTrue(result is SendEmailVerificationUseCase.Result.Success)
        coVerify { mockUserRepository.sendEmailVerification() }
    }

    @Test
    fun `should return error when repository throws exception`() = runTest {
        // Given
        val expectedException = Exception("Network error")
        coEvery { mockUserRepository.sendEmailVerification() } throws expectedException

        // When
        val result = useCase()

        // Then
        assertTrue(result is SendEmailVerificationUseCase.Result.Error)
        assertEquals(expectedException, result.exception)
        coVerify { mockUserRepository.sendEmailVerification() }
    }
}
