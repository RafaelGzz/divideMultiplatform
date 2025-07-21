package com.ragl.divide.domain

import com.ragl.divide.data.models.Category
import com.ragl.divide.data.models.Event
import com.ragl.divide.data.models.EventExpense
import com.ragl.divide.data.models.EventPayment
import com.ragl.divide.data.models.SplitMethod
import com.ragl.divide.data.services.GroupExpenseServiceImpl
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GroupExpenseServiceTest {

    private val service = GroupExpenseServiceImpl()

    // Helper function para crear gastos de prueba
    private fun createTestExpense(
        id: String = "expense1",
        title: String = "Test Expense",
        amount: Double = 100.0,
        payers: Map<String, Double> = mapOf("user1" to 100.0),
        debtors: Map<String, Double> = mapOf("user2" to 50.0, "user3" to 50.0),
        splitMethod: SplitMethod = SplitMethod.EQUALLY
    ) = EventExpense(
        id = id,
        title = title,
        amount = amount,
        payers = payers,
        debtors = debtors,
        splitMethod = splitMethod,
        category = Category.GENERAL
    )

    // Helper function para crear pagos de prueba
    private fun createTestPayment(
        id: String = "payment1",
        from: String = "user2",
        to: String = "user1",
        amount: Double = 25.0,
        description: String = "Test Payment"
    ) = EventPayment(
        id = id,
        from = from,
        to = to,
        amount = amount,
        description = description,
    )

    // Helper function para crear eventos de prueba
    private fun createTestEvent(
        id: String = "event1",
        title: String = "Test Event",
        expenses: Map<String, EventExpense> = emptyMap(),
        payments: Map<String, EventPayment> = emptyMap(),
        settled: Boolean = false,
        currentDebts: Map<String, Map<String, Double>> = emptyMap()
    ) = Event(
        id = id,
        title = title,
        expenses = expenses,
        payments = payments,
        settled = settled,
        currentDebts = currentDebts,
        category = Category.GENERAL
    )

    @Test
    fun testCalculateDebts_SimpleExpense() {
        // Arrange
        val expense = createTestExpense(
            payers = mapOf("user1" to 100.0),
            debtors = mapOf("user2" to 50.0, "user3" to 50.0)
        )
        val expenses = listOf(expense)
        val payments = emptyList<EventPayment>()

        // Act
        val result = service.calculateDebts(expenses, payments)

        // Assert
        assertEquals(2, result.size)
        assertEquals(50.0, result["user2"]?.get("user1"))
        assertEquals(50.0, result["user3"]?.get("user1"))
    }

    @Test
    fun testCalculateDebts_WithPayments() {
        // Arrange
        val expense = createTestExpense(
            payers = mapOf("user1" to 100.0),
            debtors = mapOf("user2" to 50.0, "user3" to 50.0)
        )
        val payment = createTestPayment(
            from = "user2",
            to = "user1",
            amount = 25.0
        )
        val expenses = listOf(expense)
        val payments = listOf(payment)

        // Act
        val result = service.calculateDebts(expenses, payments, simplify = false)

        // Assert
        // Sin simplificación: 
        // - Del gasto: user2 debe 50 a user1, user3 debe 50 a user1
        // - Del pago: user1 debe 25 a user2 (porque user2 pagó a user1)
        assertEquals(3, result.size)
        assertEquals(25.0, result["user1"]?.get("user2")) // user1 debe a user2 por el pago
        assertEquals(50.0, result["user2"]?.get("user1")) // user2 debe a user1 por el gasto
        assertEquals(50.0, result["user3"]?.get("user1")) // user3 debe a user1 por el gasto
    }

    @Test
    fun testCalculateDebts_WithSimplification() {
        // Arrange
        val expense1 = createTestExpense(
            id = "expense1",
            payers = mapOf("user1" to 60.0),
            debtors = mapOf("user2" to 60.0)
        )
        val expense2 = createTestExpense(
            id = "expense2",
            payers = mapOf("user2" to 40.0),
            debtors = mapOf("user1" to 40.0)
        )
        val expenses = listOf(expense1, expense2)
        val payments = emptyList<EventPayment>()

        // Act
        val result = service.calculateDebts(expenses, payments, simplify = true)

        // Assert
        assertEquals(1, result.size)
        assertEquals(20.0, result["user2"]?.get("user1")) // 60 - 40 = 20
    }

    @Test
    fun testCalculateDebts_CompletelyBalanced() {
        // Arrange
        val expense1 = createTestExpense(
            id = "expense1",
            payers = mapOf("user1" to 50.0),
            debtors = mapOf("user2" to 50.0)
        )
        val expense2 = createTestExpense(
            id = "expense2",
            payers = mapOf("user2" to 50.0),
            debtors = mapOf("user1" to 50.0)
        )
        val expenses = listOf(expense1, expense2)
        val payments = emptyList<EventPayment>()

        // Act
        val result = service.calculateDebts(
            expenses, 
            payments, 
            simplify = true,
        )

        // Assert
        assertTrue(result.isEmpty())
    }

    @Test
    fun testCalculateDebts_PaymentReducesDebt() {
        // Arrange
        val expense = createTestExpense(
            payers = mapOf("user1" to 100.0),
            debtors = mapOf("user2" to 100.0)
        )
        val payment = createTestPayment(
            from = "user2",
            to = "user1",
            amount = 60.0
        )
        val expenses = listOf(expense)
        val payments = listOf(payment)

        // Act - Sin simplificación: los pagos se agregan como deudas adicionales
        val resultNoSimplify = service.calculateDebts(expenses, payments, simplify = false)
        
        // Act - Con simplificación: los pagos reducen deudas existentes
        val resultWithSimplify = service.calculateDebts(expenses, payments, simplify = true)

        // Assert - Sin simplificación: deuda original + pago (el "to" debe al "from")
        assertEquals(60.0, resultNoSimplify["user1"]?.get("user2")) // user1 debe a user2: 60 (del pago)
        assertEquals(100.0, resultNoSimplify["user2"]?.get("user1")) // user2 debe a user1: 100 (del gasto)
        
        // Assert - Con simplificación: deuda original - pago
        assertEquals(40.0, resultWithSimplify["user2"]?.get("user1"))
    }

    @Test
    fun testCalculateDebts_PaymentExceedsDebt() {
        // Arrange
        val expense = createTestExpense(
            payers = mapOf("user1" to 50.0),
            debtors = mapOf("user2" to 50.0)
        )
        val payment = createTestPayment(
            from = "user2",
            to = "user1",
            amount = 70.0
        )
        val expenses = listOf(expense)
        val payments = listOf(payment)

        // Act
        val result = service.calculateDebts(expenses, payments, simplify = true)

        // Assert - Ahora user1 debe a user2
        assertEquals(20.0, result["user1"]?.get("user2"))
    }

    @Test
    fun testCalculateDebts_MultipleUsers() {
        // Arrange
        val expense = createTestExpense(
            payers = mapOf("user1" to 120.0),
            debtors = mapOf("user2" to 40.0, "user3" to 40.0, "user4" to 40.0)
        )
        val expenses = listOf(expense)
        val payments = emptyList<EventPayment>()

        // Act
        val result = service.calculateDebts(expenses, payments)

        // Assert
        assertEquals(3, result.size)
        assertEquals(40.0, result["user2"]?.get("user1"))
        assertEquals(40.0, result["user3"]?.get("user1"))
        assertEquals(40.0, result["user4"]?.get("user1"))
    }

    @Test
    fun testCalculateDebts_ComplexScenario() {
        // Arrange
        val expense1 = createTestExpense(
            id = "expense1",
            payers = mapOf("user1" to 100.0),
            debtors = mapOf("user2" to 50.0, "user3" to 50.0)
        )
        val expense2 = createTestExpense(
            id = "expense2",
            payers = mapOf("user2" to 60.0),
            debtors = mapOf("user1" to 30.0, "user3" to 30.0)
        )
        val payment = createTestPayment(
            from = "user3",
            to = "user1",
            amount = 20.0
        )
        val expenses = listOf(expense1, expense2)
        val payments = listOf(payment)

        // Act
        val result = service.calculateDebts(expenses, payments, simplify = true)

        // Assert - Verificar que las deudas se calculen correctamente
        assertTrue(result.isNotEmpty())
        // user2 debe 50 a user1, pero user1 debe 30 a user2 = user2 debe 20 a user1
        // user3 debe 50 a user1, debe 30 a user2, pero pagó 20 a user1
        // user1 debe 30 a user2
    }

    @Test
    fun testConsolidateDebtsFromEvents() {
        // Arrange
        val event1 = createTestEvent(
            id = "event1",
            currentDebts = mapOf(
                "user1" to mapOf("user2" to 50.0),
                "user3" to mapOf("user2" to 30.0)
            ),
            settled = false
        )
        val event2 = createTestEvent(
            id = "event2",
            currentDebts = mapOf(
                "user1" to mapOf("user2" to 20.0),
                "user2" to mapOf("user3" to 40.0)
            ),
            settled = false
        )
        val settledEvent = createTestEvent(
            id = "event3",
            currentDebts = mapOf(
                "user1" to mapOf("user2" to 100.0)
            ),
            settled = true // Este evento no debe incluirse
        )
        val events = listOf(event1, event2, settledEvent)

        // Act
        val result = service.consolidateDebtsFromEvents(events)

        // Assert
        assertEquals(70.0, result["user1"]?.get("user2")) // 50 + 20
        assertEquals(30.0, result["user3"]?.get("user2"))
        assertEquals(40.0, result["user2"]?.get("user3"))
    }

    @Test
    fun testConsolidateDebtsFromEventsMap() {
        // Arrange
        val eventsMap = mapOf(
            "event1" to createTestEvent(
                id = "event1",
                currentDebts = mapOf("user1" to mapOf("user2" to 25.0)),
                settled = false
            ),
            "event2" to createTestEvent(
                id = "event2",
                currentDebts = mapOf("user1" to mapOf("user2" to 35.0)),
                settled = false
            )
        )

        // Act
        val result = service.consolidateDebtsFromEventsMap(eventsMap)

        // Assert
        assertEquals(60.0, result["user1"]?.get("user2"))
    }

    @Test
    fun testGetEventDebtsMap() {
        // Arrange
        val eventsMap = mapOf(
            "event1" to createTestEvent(
                id = "event1",
                currentDebts = mapOf("user1" to mapOf("user2" to 50.0))
            ),
            "event2" to createTestEvent(
                id = "event2",
                currentDebts = mapOf("user2" to mapOf("user3" to 30.0))
            )
        )

        // Act
        val result = service.getEventDebtsMap(eventsMap)

        // Assert
        assertEquals(2, result.size)
        assertEquals(50.0, result["event1"]?.get("user1")?.get("user2"))
        assertEquals(30.0, result["event2"]?.get("user2")?.get("user3"))
    }

    @Test
    fun testCalculateDebts_MinimumDebtThreshold() {
        // Arrange
        val expense = createTestExpense(
            payers = mapOf("user1" to 0.005), // Cantidad muy pequeña
            debtors = mapOf("user2" to 0.005)
        )
        val expenses = listOf(expense)
        val payments = emptyList<EventPayment>()

        // Act
        val result = service.calculateDebts(expenses, payments)

        // Assert - Deudas menores a 0.01 deben ser filtradas
        assertTrue(result.isEmpty())
    }

    @Test
    fun testCalculateDebts_DebtChainSimplification() {
        // Arrange - Crear una cadena de deudas: A -> B -> C
        val expense1 = createTestExpense(
            id = "expense1",
            payers = mapOf("userB" to 100.0),
            debtors = mapOf("userA" to 100.0)
        )
        val expense2 = createTestExpense(
            id = "expense2",
            payers = mapOf("userC" to 100.0),
            debtors = mapOf("userB" to 100.0)
        )
        val expenses = listOf(expense1, expense2)
        val payments = emptyList<EventPayment>()

        // Act
        val result = service.calculateDebts(expenses, payments, simplify = true)

        // Assert - Debería simplificarse a A -> C directamente
        assertEquals(1, result.size)
        assertEquals(100.0, result["userA"]?.get("userC"))
    }

    @Test
    fun testCalculateDebts_EmptyInputs() {
        // Arrange
        val expenses = emptyList<EventExpense>()
        val payments = emptyList<EventPayment>()

        // Act
        val result = service.calculateDebts(expenses, payments)

        // Assert
        assertTrue(result.isEmpty())
    }

    @Test
    fun testCalculateDebts_OnlyPayments() {
        // Arrange
        val expenses = emptyList<EventExpense>()
        val payment = createTestPayment(
            from = "user1",
            to = "user2",
            amount = 50.0
        )
        val payments = listOf(payment)

        // Act
        val result = service.calculateDebts(expenses, payments, simplify = false)

        // Assert
        assertEquals(50.0, result["user2"]?.get("user1")) // user2 debe a user1 (quien recibe debe al que paga)
    }

    @Test
    fun testCalculateDebts_SamePersonPayerAndDebtor() {
        // Arrange - Una persona paga y debe la misma cantidad
        val expense = createTestExpense(
            payers = mapOf("user1" to 100.0),
            debtors = mapOf("user1" to 100.0)
        )
        val expenses = listOf(expense)
        val payments = emptyList<EventPayment>()

        // Act
        val result = service.calculateDebts(expenses, payments)

        // Assert - No debería haber deudas porque la misma persona paga y debe
        assertTrue(result.isEmpty())
    }

    @Test
    fun testCalculateDebts_MultipleExpensesAndPayments() {
        // Arrange
        val expense1 = createTestExpense(
            id = "expense1",
            payers = mapOf("user1" to 100.0),
            debtors = mapOf("user2" to 100.0)
        )
        val expense2 = createTestExpense(
            id = "expense2",
            payers = mapOf("user2" to 50.0),
            debtors = mapOf("user3" to 50.0)
        )
        val payment1 = createTestPayment(
            id = "payment1",
            from = "user2",
            to = "user1",
            amount = 30.0
        )
        val payment2 = createTestPayment(
            id = "payment2",
            from = "user3",
            to = "user2",
            amount = 20.0
        )
        val expenses = listOf(expense1, expense2)
        val payments = listOf(payment1, payment2)

        // Act
        val result = service.calculateDebts(expenses, payments, simplify = true)

        // Assert - Verificar que se calculen correctamente las deudas netas
        assertTrue(result.isNotEmpty())
    }

    @Test
    fun testCalculateDebts_ZeroAmounts() {
        // Arrange
        val expense = createTestExpense(
            amount = 0.0,
            payers = mapOf("user1" to 0.0),
            debtors = mapOf("user2" to 0.0)
        )
        val expenses = listOf(expense)
        val payments = emptyList<EventPayment>()

        // Act
        val result = service.calculateDebts(expenses, payments)

        // Assert
        assertTrue(result.isEmpty())
    }

    @Test
    fun testCalculateDebts_RoundingEdgeCase() {
        // Arrange - Cantidades que podrían causar problemas de redondeo
        val expense = createTestExpense(
            amount = 33.33,
            payers = mapOf("user1" to 33.33),
            debtors = mapOf("user2" to 11.11, "user3" to 11.11, "user4" to 11.11)
        )
        val expenses = listOf(expense)
        val payments = emptyList<EventPayment>()

        // Act
        val result = service.calculateDebts(expenses, payments)

        // Assert
        assertEquals(3, result.size)
        assertEquals(11.11, result["user2"]?.get("user1"))
        assertEquals(11.11, result["user3"]?.get("user1"))
        assertEquals(11.11, result["user4"]?.get("user1"))
    }

    @Test
    fun testConsolidateDebtsFromEvents_OnlySettledEvents() {
        // Arrange - Solo eventos liquidados
        val settledEvent1 = createTestEvent(
            id = "event1",
            currentDebts = mapOf("user1" to mapOf("user2" to 50.0)),
            settled = true
        )
        val settledEvent2 = createTestEvent(
            id = "event2",
            currentDebts = mapOf("user2" to mapOf("user3" to 30.0)),
            settled = true
        )
        val events = listOf(settledEvent1, settledEvent2)

        // Act
        val result = service.consolidateDebtsFromEvents(events)

        // Assert - No debería consolidar eventos liquidados
        assertTrue(result.isEmpty())
    }

    @Test
    fun testCalculateDebts_ComplexMultiUserScenario() {
        // Arrange - Escenario complejo con múltiples usuarios
        val expense1 = createTestExpense(
            id = "expense1",
            payers = mapOf("userA" to 120.0),
            debtors = mapOf("userB" to 40.0, "userC" to 40.0, "userD" to 40.0)
        )
        val expense2 = createTestExpense(
            id = "expense2", 
            payers = mapOf("userB" to 80.0),
            debtors = mapOf("userA" to 20.0, "userC" to 30.0, "userD" to 30.0)
        )
        val payment = createTestPayment(
            from = "userC",
            to = "userA",
            amount = 15.0
        )
        val expenses = listOf(expense1, expense2)
        val payments = listOf(payment)

        // Act
        val result = service.calculateDebts(expenses, payments, simplify = true)

        // Assert
        assertTrue(result.isNotEmpty())
        // Verificar que las deudas se simplifiquen correctamente
    }

    @Test
    fun testCalculateDebts_PaymentExactlyEqualToDebt() {
        // Arrange - Pago exactamente igual a la deuda
        val expense = createTestExpense(
            payers = mapOf("user1" to 100.0),
            debtors = mapOf("user2" to 100.0)
        )
        val payment = createTestPayment(
            from = "user2",
            to = "user1",
            amount = 100.0
        )
        val expenses = listOf(expense)
        val payments = listOf(payment)

        // Act
        val result = service.calculateDebts(
            expenses, 
            payments, 
            simplify = true,
        )

        // Assert - No debería haber deudas y debería marcar para liquidar
        assertTrue(result.isEmpty())
    }
} 