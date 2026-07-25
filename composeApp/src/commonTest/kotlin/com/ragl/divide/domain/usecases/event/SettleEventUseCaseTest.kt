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

class SettleEventUseCaseTest {

    private val mockGroupRepository = mock(of<GroupRepository>())
    private val mockUserStateHolder = mock(of<UserStateHolder>())
    private lateinit var useCase: SettleEventUseCase

    @BeforeTest
    fun setUp() {
        useCase = SettleEventUseCase(mockGroupRepository, mockUserStateHolder)
    }

    @Test
    fun `should return success when event is settled successfully`() = runTest {
        // Given
        val groupId = "group123"
        val eventId = "event123"

        coEvery { mockGroupRepository.settleEvent(groupId, eventId) } returns Unit
        coEvery { mockUserStateHolder.settleEvent(groupId, eventId) } returns Unit

        // When
        val result = useCase(groupId, eventId)

        // Then
        assertTrue(result is SettleEventUseCase.Result.Success)
        coVerify { mockGroupRepository.settleEvent(groupId, eventId) }
        coVerify { mockUserStateHolder.settleEvent(groupId, eventId) }
    }

    @Test
    fun `should return error when repository throws exception`() = runTest {
        // Given
        val groupId = "group123"
        val eventId = "event123"
        val expectedException = Exception("Database error")

        coEvery { mockGroupRepository.settleEvent(groupId, eventId) } throws expectedException

        // When
        val result = useCase(groupId, eventId)

        // Then
        assertTrue(result is SettleEventUseCase.Result.Error)
        assertEquals(expectedException, (result as SettleEventUseCase.Result.Error).exception)
        coVerify { mockUserStateHolder.settleEvent(any(), any()) }.wasNotInvoked()
    }
}
