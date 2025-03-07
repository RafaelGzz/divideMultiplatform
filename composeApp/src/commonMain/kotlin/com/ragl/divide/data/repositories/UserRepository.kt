package com.ragl.divide.data.repositories

import com.ragl.divide.data.models.Expense
import com.ragl.divide.data.models.Payment
import com.ragl.divide.data.models.User
import dev.gitlive.firebase.auth.AuthCredential
import dev.gitlive.firebase.auth.FirebaseAuth
import dev.gitlive.firebase.auth.FirebaseUser
import dev.gitlive.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimePeriod
import kotlinx.datetime.DateTimeUnit

interface UserRepository {
    fun isUserSignedIn(): Boolean
    suspend fun createUserInDatabase(): User
    suspend fun getUser(id: String): User
    fun getFirebaseUser(): FirebaseUser?
    suspend fun signInWithCredential(credential: AuthCredential): User?
    suspend fun signInWithEmailAndPassword(email: String, password: String): User?
    suspend fun signUpWithEmailAndPassword(email: String, password: String, name: String): User?
    suspend fun signOut()
    suspend fun getExpense(id: String): Expense
    suspend fun getExpenses(): Map<String, Expense>
    suspend fun saveExpense(expense: Expense): Expense
    suspend fun deleteExpense(id: String)
    suspend fun getExpensePayments(expenseId: String): Map<String, Payment>
    suspend fun saveExpensePayment(payment: Payment, expenseId: String, expensePaid: Boolean): Payment
    suspend fun deleteExpensePayment(paymentId: String, amount: Double, expenseId: String)
    suspend fun saveGroup(id: String, userId: String)
    suspend fun leaveGroup(groupId: String, userId: String)
}

class UserRepositoryImpl(
    private val auth: FirebaseAuth,
    private val database: FirebaseDatabase
) : UserRepository {
    init {
        database.reference("users")
    }

    override fun isUserSignedIn(): Boolean = auth.currentUser != null

    override suspend fun signUpWithEmailAndPassword(
        email: String,
        password: String,
        name: String
    ): User? {
        val user = auth.createUserWithEmailAndPassword(email, password).user
        return if (user != null) {
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
        val user = getFirebaseUser()!!
        val userRef = database.reference("users/${user.uid}")

        // Get the first provider data (assuming there is only one)
        val providerData = user.providerData[1]

        val userData = User(
            user.uid,
            providerData.email ?: user.email.orEmpty(),
            providerData.displayName ?: user.displayName.orEmpty(),
            (providerData.photoURL ?: "").toString()
        )
        userRef.setValue(userData)
        return userData
    }

    override suspend fun getUser(id: String): User {
        val userRef = database.reference("users/$id")
        val snapshot = userRef.valueEvents.firstOrNull()
        return snapshot?.value<User>() ?: User()
    }

    override fun getFirebaseUser(): FirebaseUser? = auth.currentUser

    override suspend fun signInWithCredential(
        credential: AuthCredential
    ): User? {
        val res = auth.signInWithCredential(credential)
        return if (res.user != null) {
            if (res.additionalUserInfo!!.isNewUser) {
                createUserInDatabase()
            } else
                getUser(res.user!!.uid)
        } else {
            null
        }
    }

    override suspend fun signOut() {
        auth.signOut()
    }

    override suspend fun getExpense(id: String): Expense {
        val user = getFirebaseUser() ?: return Expense()
        val expenseRef = database.reference("users/${user.uid}/expenses/${id}")
        val snapshot = expenseRef.valueEvents.firstOrNull()
        return snapshot?.value<Expense>() ?: Expense()
    }

    override suspend fun getExpenses(): Map<String, Expense> {
        val expenses = mutableMapOf<String, Expense>()
        val user = getFirebaseUser() ?: return expenses
        val userRef = database.reference("users/${user.uid}/expenses")
        val snapshot = userRef.valueEvents.firstOrNull()
        snapshot?.children?.forEach {
            it.value<Expense>().let { expense -> expenses[expense.id] = expense }
        }
        return expenses
    }

    override suspend fun saveExpense(expense: Expense): Expense {
        val user = getFirebaseUser() ?: throw Exception("User not signed in")
        val id = expense.id.ifEmpty { "id${Clock.System.now().toEpochMilliseconds()}" }
        val savedExpense = expense.copy(id = id)
        database.reference("users/${user.uid}/expenses").child(id).setValue(savedExpense)
        return savedExpense
    }

    override suspend fun getExpensePayments(expenseId: String): Map<String, Payment> {
        val payments = mutableMapOf<String, Payment>()
        val user = getFirebaseUser() ?: return payments
        val userRef = database.reference("users/${user.uid}/expenses/$expenseId/payments")
        val snapshot = userRef.valueEvents.firstOrNull()
        snapshot?.children?.forEach {
            it.value<Payment>().let { payment -> payments[payment.id] = payment }
        }
        return payments
    }

    override suspend fun saveExpensePayment(payment: Payment, expenseId: String, expensePaid: Boolean): Payment {
        val user = getFirebaseUser() ?: throw Exception("User not signed in")
        val id = "id${Clock.System.now().toEpochMilliseconds()}"

        val amountPaidRef =
            database.reference("users/${user.uid}/expenses/$expenseId/amountPaid")
        val amountPaid = try {
            amountPaidRef.valueEvents.firstOrNull()?.value<Double>()?.plus(payment.amount)
        } catch (e: Exception) {
            amountPaidRef.valueEvents.firstOrNull()?.value<Long>()?.plus(payment.amount)
        }
        amountPaidRef.setValue(amountPaid)

        val savedPayment = payment.copy(id = id)
        val paymentsRef = database.reference("users/${user.uid}/expenses/$expenseId/payments")
        paymentsRef.child(id).setValue(savedPayment)

        if(expensePaid)
            database.reference("users/${user.uid}/expenses/$expenseId/paid").setValue(true)

        return savedPayment
    }

    override suspend fun deleteExpensePayment(paymentId: String, amount: Double, expenseId: String) {
        val user = getFirebaseUser() ?: return

        val amountPaidRef = database.reference("users/${user.uid}/expenses/$expenseId/amountPaid")
        val amountPaid = try {
            amountPaidRef.valueEvents.firstOrNull()?.value<Double>()?.minus(amount)
        } catch (e: Exception) {
            amountPaidRef.valueEvents.firstOrNull()?.value<Long>()?.minus(amount)
        }
        amountPaidRef.setValue(amountPaid)

        val paymentsRef = database.reference("users/${user.uid}/expenses/$expenseId/payments")
        paymentsRef.child(paymentId).removeValue()
    }

    override suspend fun deleteExpense(id: String) {
        val user = getFirebaseUser() ?: return
        val expensesRef = database.reference("users/${user.uid}/expenses")
        expensesRef.child(id).removeValue()
    }

    override suspend fun saveGroup(id: String, userId: String){
        val groupRef = database.reference("users/$userId/groups")
        groupRef.child(id).setValue(id)
    }

    override suspend fun leaveGroup(groupId: String, userId: String) {
        val groupRef = database.reference("users/$userId/groups")
        groupRef.child(groupId).removeValue()
    }
}