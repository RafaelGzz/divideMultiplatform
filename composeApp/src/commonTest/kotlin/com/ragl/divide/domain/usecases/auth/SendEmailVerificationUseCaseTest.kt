package com.ragl.divide.domain.usecases.auth

import com.ragl.divide.domain.repositories.UserRepository
import com.ragl.divide.testing.FakeUserRepository
import com.ragl.divide.testing.assertCalled
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SendEmailVerificationUseCaseTest {

    private val mockUserRepository = FakeUserRepository()
    private lateinit var useCase: SendEmailVerificationUseCase

    @BeforeTest
    fun setUp() {
        useCase = SendEmailVerificationUseCase(mockUserRepository)
    }

    @Test
    fun `should return success when email verification is sent successfully`() = runTest {
        // Given
        mockUserRepository.onSendEmailVerification = {}

        // When
        val result = useCase()

        // Then
        assertTrue(result is SendEmailVerificationUseCase.Result.Success)
        assertCalled(mockUserRepository, "sendEmailVerification")
    }

    @Test
    fun `should return error when repository throws exception`() = runTest {
        // Given
        val expectedException = Exception("Network error")
        mockUserRepository.onSendEmailVerification = { throw expectedException }

        // When
        val result = useCase()

        // Then
        assertTrue(result is SendEmailVerificationUseCase.Result.Error)
        assertEquals(expectedException, result.exception)
        assertCalled(mockUserRepository, "sendEmailVerification")
    }
}
