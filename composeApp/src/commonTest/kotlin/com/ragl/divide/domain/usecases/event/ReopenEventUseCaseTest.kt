package com.ragl.divide.domain.usecases.event

import com.ragl.divide.domain.repositories.GroupRepository
import com.ragl.divide.domain.stateHolders.UserStateHolder
import io.mockative.any
import io.mockative.coEvery
import io.mockative.coVerify
import io.mockative.mock
import io.mockative.of
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ReopenEventUseCaseTest {

    private val mockGroupRepository = mock(of<GroupRepository>())
    private val mockUserStateHolder = mock(of<UserStateHolder>())
    private lateinit var useCase: ReopenEventUseCase

    @BeforeTest
    fun setUp() {
        useCase = ReopenEventUseCase(mockGroupRepository, mockUserStateHolder)
    }

    @Test
    fun `should return success when event is reopened successfully`() = runTest {
        // Given
        val groupId = "group123"
        val eventId = "event123"

        coEvery { mockGroupRepository.reopenEvent(groupId, eventId) } returns Unit
        coEvery { mockUserStateHolder.reopenEvent(groupId, eventId) } returns Unit

        // When
        val result = useCase(groupId, eventId)

        // Then
        assertTrue(result is ReopenEventUseCase.Result.Success)
        coVerify { mockGroupRepository.reopenEvent(groupId, eventId) }
        coVerify { mockUserStateHolder.reopenEvent(groupId, eventId) }
    }

    @Test
    fun `should return error when repository throws exception`() = runTest {
        // Given
        val groupId = "group123"
        val eventId = "event123"
        val expectedException = Exception("Database error")

        coEvery { mockGroupRepository.reopenEvent(groupId, eventId) } throws expectedException

        // When
        val result = useCase(groupId, eventId)

        // Then
        assertTrue(result is ReopenEventUseCase.Result.Error)
        assertEquals(expectedException, (result as ReopenEventUseCase.Result.Error).exception)
        coVerify { mockUserStateHolder.reopenEvent(any(), any()) }.wasNotInvoked()
    }
}
