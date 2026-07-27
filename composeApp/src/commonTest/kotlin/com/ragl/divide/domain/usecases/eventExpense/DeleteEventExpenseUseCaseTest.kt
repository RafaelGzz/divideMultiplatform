package com.ragl.divide.domain.usecases.eventExpense

import com.ragl.divide.data.models.EventExpense
import com.ragl.divide.domain.repositories.GroupRepository
import com.ragl.divide.domain.stateHolders.UserStateHolder
import com.ragl.divide.testing.FakeGroupRepository
import com.ragl.divide.testing.FakeUserStateHolder
import com.ragl.divide.testing.assertCalled
import com.ragl.divide.testing.assertNotCalled
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DeleteEventExpenseUseCaseTest {

    private val mockGroupRepository = FakeGroupRepository()
    private val mockUserStateHolder = FakeUserStateHolder()
    private lateinit var useCase: DeleteEventExpenseUseCase

    @BeforeTest
    fun setUp() {
        useCase = DeleteEventExpenseUseCase(mockGroupRepository, mockUserStateHolder)
    }

    @Test
    fun `should return success when event expense is deleted successfully`() = runTest {
        // Given
        val groupId = "group123"
        val eventExpense = EventExpense(
            id = "expense123",
            title = "Test Expense",
            amount = 100.0,
            eventId = "event123"
        )

        mockGroupRepository.onDeleteEventExpense = { _, _ -> }
        mockUserStateHolder.onDeleteEventExpense = { _, _ -> }

        // When
        val result = useCase(groupId, eventExpense)

        // Then
        assertTrue(result is DeleteEventExpenseUseCase.Result.Success)
        assertCalled(mockGroupRepository, "deleteEventExpense", groupId, eventExpense)
        assertCalled(mockUserStateHolder, "deleteEventExpense", groupId, eventExpense)
    }

    @Test
    fun `should return error when repository throws exception`() = runTest {
        // Given
        val groupId = "group123"
        val eventExpense = EventExpense(
            id = "expense123",
            title = "Test Expense",
            amount = 100.0,
            eventId = "event123"
        )
        val expectedException = Exception("Database error")

        mockGroupRepository.onDeleteEventExpense = { _, _ -> throw expectedException }

        // When
        val result = useCase(groupId, eventExpense)

        // Then
        assertTrue(result is DeleteEventExpenseUseCase.Result.Error)
        assertEquals(expectedException, (result as DeleteEventExpenseUseCase.Result.Error).exception)
        assertNotCalled(mockUserStateHolder, "deleteEventExpense")
    }
}
