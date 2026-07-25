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

class DeleteEventUseCaseTest {

    private val mockGroupRepository = mock(of<GroupRepository>())
    private val mockUserStateHolder = mock(of<UserStateHolder>())
    private lateinit var useCase: DeleteEventUseCase

    @BeforeTest
    fun setUp() {
        useCase = DeleteEventUseCase(mockGroupRepository, mockUserStateHolder)
    }

    @Test
    fun `should return success when event is deleted successfully`() = runTest {
        // Given
        val groupId = "group123"
        val eventId = "event123"

        coEvery { mockGroupRepository.deleteEvent(groupId, eventId) } returns Unit
        coEvery { mockUserStateHolder.deleteEvent(groupId, eventId) } returns Unit

        // When
        val result = useCase(groupId, eventId)

        // Then
        assertTrue(result is DeleteEventUseCase.Result.Success)
        coVerify { mockGroupRepository.deleteEvent(groupId, eventId) }
        coVerify { mockUserStateHolder.deleteEvent(groupId, eventId) }
    }

    @Test
    fun `should return error when repository throws exception`() = runTest {
        // Given
        val groupId = "group123"
        val eventId = "event123"
        val expectedException = Exception("Database error")

        coEvery { mockGroupRepository.deleteEvent(groupId, eventId) } throws expectedException

        // When
        val result = useCase(groupId, eventId)

        // Then
        assertTrue(result is DeleteEventUseCase.Result.Error)
        assertEquals(expectedException, (result as DeleteEventUseCase.Result.Error).exception)
        coVerify { mockUserStateHolder.deleteEvent(any(), any()) }.wasNotInvoked()
    }
}
