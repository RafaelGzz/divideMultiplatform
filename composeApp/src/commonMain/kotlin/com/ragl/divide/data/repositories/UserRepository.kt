package com.ragl.divide.data.repositories

import com.ragl.divide.data.models.Expense
import com.ragl.divide.data.models.Payment
import com.ragl.divide.data.models.User
import com.ragl.divide.domain.repositories.UserRepository
import com.ragl.divide.presentation.utils.logMessage
import dev.gitlive.firebase.auth.FirebaseAuth
import dev.gitlive.firebase.auth.FirebaseUser
import dev.gitlive.firebase.database.FirebaseDatabase
import dev.gitlive.firebase.storage.File
import dev.gitlive.firebase.storage.FirebaseStorage
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.firstOrNull
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class UserRepositoryImpl(
    private val auth: FirebaseAuth,
    private val database: FirebaseDatabase,
    private val storage: FirebaseStorage
) : UserRepository {
    init {
        database.reference("users")
    }

    override fun getCurrentUser(): FirebaseUser? = auth.currentUser

    override suspend fun signUpWithEmailAndPassword(
        email: String,
        password: String,
        name: String
    ): User? {

        val user = auth.createUserWithEmailAndPassword(email, password).user
        return if (user != null) {
            sendEmailVerification()
            user.updateProfile(displayName = name)
            createUserInDatabase()
        } else {
            null
        }
    }

    override suspend fun signInWithEmailAndPassword(email: String, password: String): User? {
        val user = auth.signInWithEmailAndPassword(email, password).user
        return if (user != null) {
            getUser(user.uid)
        } else {
            null
        }
    }

    override suspend fun createUserInDatabase(): User {
        val startTime = Clock.System.now().toEpochMilliseconds()
        val user = getCurrentUser() ?: throw Exception("User not signed in")
        val googleProvider = user.providerData.find { it.providerId == "google.com" }

        val userData = User(
            user.uid,
            googleProvider?.email ?: user.email ?: "",
            googleProvider?.email?.split("@")?.firstOrNull() ?: user.displayName ?: "Usuario",
            googleProvider?.photoURL ?: ""
        )
        database.reference("users/${user.uid}").setValue(userData)
        val executionTime = Clock.System.now().toEpochMilliseconds() - startTime
        logMessage("UserRepository", "createUserInDatabase executed in ${executionTime}ms")
        return userData
    }

    override suspend fun getUser(id: String): User {
        val startTime = Clock.System.now().toEpochMilliseconds()
        val userRef = database.reference("users/$id")
        val user = userRef.valueEvents.firstOrNull()?.value<User?>() ?: User()
        val executionTime = Clock.System.now().toEpochMilliseconds() - startTime
        logMessage("UserRepository", "getUser: ${user.uuid} - executed in ${executionTime}ms")
        return user
    }

    override suspend fun signOut() {
        auth.signOut()
    }

    override suspend fun sendEmailVerification() {
        auth.currentUser?.sendEmailVerification()
    }

    override suspend fun isEmailVerified(): Boolean {
        auth.currentUser?.reload()
        val user = auth.currentUser ?: return false
        val providerData = user.providerData
        return if (providerData.any { it.providerId == "google.com" }) {
            true
        } else {
            user.isEmailVerified
        }
    }

    override suspend fun saveProfilePhoto(photo: File): String {
        val startTime = Clock.System.now().toEpochMilliseconds()
        val userId = getCurrentUser()?.uid ?: throw Exception("User not signed in")

        val photoRef = storage.reference("userPictures/$userId.jpg")
        photoRef.putFile(photo)

        val downloadUrl = photoRef.getDownloadUrl()

        val userRef = database.reference("users/$userId")
        userRef.child("photoUrl").setValue(downloadUrl)
        val executionTime = Clock.System.now().toEpochMilliseconds() - startTime
        logMessage("UserRepository", "saveProfilePhoto: $downloadUrl - executed in ${executionTime}ms")
        return downloadUrl
    }

    override suspend fun getProfilePhoto(userId: String): String {
        val startTime = Clock.System.now().toEpochMilliseconds()
        val storageRef = storage.reference("userPictures/$userId.jpg")
        val downloadUrl = storageRef.getDownloadUrl()
        val executionTime = Clock.System.now().toEpochMilliseconds() - startTime
        logMessage("UserRepository", "getProfilePhoto: $downloadUrl - executed in ${executionTime}ms")
        return downloadUrl
    }

    override suspend fun getExpense(id: String): Expense {
        val startTime = Clock.System.now().toEpochMilliseconds()
        val user = getCurrentUser() ?: return Expense()
        val expenseRef = database.reference("users/${user.uid}/expenses/${id}")
        val expense = expenseRef.valueEvents.firstOrNull()?.value<Expense>() ?: Expense()
        val executionTime = Clock.System.now().toEpochMilliseconds() - startTime
        logMessage("UserRepository", "getExpense: $expense - executed in ${executionTime}ms")
        return expense
    }

    override suspend fun getExpenses(): Map<String, Expense> = coroutineScope {
        val startTime = Clock.System.now().toEpochMilliseconds()
        val expenses = mutableMapOf<String, Expense>()
        val user = getCurrentUser() ?: return@coroutineScope expenses
        val userRef = database.reference("users/${user.uid}/expenses")
        val snapshot = userRef.valueEvents.firstOrNull()

        // Paralelizar el procesamiento de los gastos
        val children = snapshot?.children?.toList() ?: emptyList()
        val deferredResults = children.map { childSnapshot ->
            async {
                childSnapshot.value<Expense>()
            }
        }

        val results = deferredResults.awaitAll()
        results.forEach { expense ->
            expenses[expense.id] = expense
        }

        val executionTime = Clock.System.now().toEpochMilliseconds() - startTime
        logMessage("UserRepository", "getExpenses: ${expenses.size} - executed in ${executionTime}ms")
        expenses
    }

    override suspend fun saveExpense(expense: Expense): Expense {
        val startTime = Clock.System.now().toEpochMilliseconds()
        val user = getCurrentUser() ?: throw Exception("User not signed in")
        val id = expense.id.ifEmpty { "id${Clock.System.now().toEpochMilliseconds()}" }
        val savedExpense = expense.copy(id = id)
        database.reference("users/${user.uid}/expenses").child(id).setValue(savedExpense)
        val executionTime = Clock.System.now().toEpochMilliseconds() - startTime
        logMessage("UserRepository", "saveExpense: $savedExpense - executed in ${executionTime}ms")
        return savedExpense
    }

    override suspend fun getExpensePayments(expenseId: String): Map<String, Payment> = coroutineScope {
            val startTime = Clock.System.now().toEpochMilliseconds()
            val payments = mutableMapOf<String, Payment>()
            val user = getCurrentUser() ?: return@coroutineScope payments
            val userRef = database.reference("users/${user.uid}/expenses/$expenseId/payments")
            val snapshot = userRef.valueEvents.firstOrNull()

            // Paralelizar el procesamiento de los pagos
            val children = snapshot?.children?.toList() ?: emptyList()
            val deferredResults = children.map { childSnapshot ->
                async {
                    childSnapshot.value<Payment>()
                }
            }

            val results = deferredResults.awaitAll()
            results.forEach { payment ->
                payments[payment.id] = payment
            }

            val executionTime = Clock.System.now().toEpochMilliseconds() - startTime
            logMessage("UserRepository", "getExpensePayments: ${payments.size} - executed in ${executionTime}ms")
            payments
        }

    override suspend fun saveExpensePayment(payment: Payment, expenseId: String, expensePaid: Boolean): Payment = coroutineScope {
        val startTime = Clock.System.now().toEpochMilliseconds()
        val user = getCurrentUser() ?: throw Exception("User not signed in")
        val id = "id${Clock.System.now().toEpochMilliseconds()}"

        // Paralelizar las operaciones de lectura y escritura
        val amountPaidRef = database.reference("users/${user.uid}/expenses/$expenseId/amountPaid")
        val amountPaidDeferred = async {
            try {
                amountPaidRef.valueEvents.firstOrNull()?.value<Double>()?.plus(payment.amount)
            } catch (e: Exception) {
                amountPaidRef.valueEvents.firstOrNull()?.value<Long>()?.plus(payment.amount)
            }
        }

        val savedPayment = payment.copy(id = id)
        val paymentsRef = database.reference("users/${user.uid}/expenses/$expenseId/payments")

        // Ejecutar operaciones en paralelo
        val amountPaid = amountPaidDeferred.await()

        val updateAmountDeferred = async { amountPaidRef.setValue(amountPaid) }
        val savePaymentDeferred = async { paymentsRef.child(id).setValue(savedPayment) }
        val updatePaidDeferred = async {
            if (expensePaid) {
                database.reference("users/${user.uid}/expenses/$expenseId/paid").setValue(true)
            }
        }

        // Esperar a que todas las operaciones terminen
        updateAmountDeferred.await()
        savePaymentDeferred.await()
        updatePaidDeferred.await()

        val executionTime = Clock.System.now().toEpochMilliseconds() - startTime
        logMessage("UserRepository", "saveExpensePayment: $savedPayment - executed in ${executionTime}ms")
        savedPayment
    }

    override suspend fun deleteExpensePayment(paymentId: String, amount: Double, expenseId: String) = coroutineScope {
        val startTime = Clock.System.now().toEpochMilliseconds()
        val user = getCurrentUser() ?: return@coroutineScope

        val amountPaidRef = database.reference("users/${user.uid}/expenses/$expenseId/amountPaid")
        val paidRef = database.reference("users/${user.uid}/expenses/$expenseId/paid")
        val paymentsRef = database.reference("users/${user.uid}/expenses/$expenseId/payments")

        // Paralelizar las operaciones de lectura
        val amountPaidDeferred = async {
            try {
                amountPaidRef.valueEvents.firstOrNull()?.value<Double>()?.minus(amount)
            } catch (e: Exception) {
                amountPaidRef.valueEvents.firstOrNull()?.value<Long>()?.minus(amount)
            }
        }

        val paidStatusDeferred = async {
            paidRef.valueEvents.firstOrNull()?.value<Boolean>()
        }

        val amountPaid = amountPaidDeferred.await()
        val paidStatus = paidStatusDeferred.await()

        // Paralelizar las operaciones de escritura
        val updateAmountDeferred = async { amountPaidRef.setValue(amountPaid) }
        val updatePaidDeferred = async {
            if (paidStatus == true) {
                paidRef.setValue(false)
            }
        }
        val removePaymentDeferred = async { paymentsRef.child(paymentId).removeValue() }

        // Esperar a que todas las operaciones terminen
        updateAmountDeferred.await()
        updatePaidDeferred.await()
        removePaymentDeferred.await()

        val executionTime = Clock.System.now().toEpochMilliseconds() - startTime
        logMessage("UserRepository", "deleteExpensePayment: $paymentId - executed in ${executionTime}ms")
    }

    override suspend fun deleteExpense(id: String) {
        val startTime = Clock.System.now().toEpochMilliseconds()
        val user = getCurrentUser() ?: return
        val expensesRef = database.reference("users/${user.uid}/expenses")
        expensesRef.child(id).removeValue()
        val executionTime = Clock.System.now().toEpochMilliseconds() - startTime
        logMessage("UserRepository", "deleteExpense: $id - executed in ${executionTime}ms")
    }

    override suspend fun addGroupToUser(id: String, userId: String){
        val startTime = Clock.System.now().toEpochMilliseconds()
        val groupRef = database.reference("users/$userId/groups")
        groupRef.child(id).setValue(id)
        val executionTime = Clock.System.now().toEpochMilliseconds() - startTime
        logMessage("UserRepository", "addGroupToUser: $id - executed in ${executionTime}ms")
    }

    override suspend fun removeGroupFromUser(groupId: String, userId: String) {
        val startTime = Clock.System.now().toEpochMilliseconds()
        val groupRef = database.reference("users/$userId/groups")
        groupRef.child(groupId).removeValue()
        val executionTime = Clock.System.now().toEpochMilliseconds() - startTime
        logMessage("UserRepository", "removeGroupFromUser: $groupId - executed in ${executionTime}ms")
    }

    override suspend fun updateUserName(newName: String): Boolean {
        val startTime = Clock.System.now().toEpochMilliseconds()
        return try {
            val userId = getCurrentUser()?.uid ?: throw Exception("User not signed in")

            // Actualizar en Firebase Auth
            getCurrentUser()?.updateProfile(displayName = newName)

            // Actualizar en Firebase Database
            val userRef = database.reference("users/$userId")
            userRef.child("name").setValue(newName)

            val executionTime = Clock.System.now().toEpochMilliseconds() - startTime
            logMessage("UserRepository", "updateUserName: $newName - executed in ${executionTime}ms")
            true
        } catch (e: Exception) {
            logMessage("UserRepository", "updateUserName failed: ${e.message}")
            false
        }
    }
}