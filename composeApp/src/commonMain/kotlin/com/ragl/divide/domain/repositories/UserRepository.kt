package com.ragl.divide.domain.repositories

import com.ragl.divide.data.models.Expense
import com.ragl.divide.data.models.Payment
import com.ragl.divide.data.models.User
import dev.gitlive.firebase.auth.FirebaseUser
import dev.gitlive.firebase.storage.File

interface UserRepository {
    fun getFirebaseUser(): FirebaseUser?
    suspend fun createUserInDatabase(): User
    suspend fun getUser(id: String): User
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
    suspend fun addGroupToUser(id: String, userId: String)
    suspend fun removeGroupFromUser(groupId: String, userId: String)
    suspend fun sendEmailVerification()
    suspend fun isEmailVerified(): Boolean
    suspend fun saveProfilePhoto(photo: File): String
    suspend fun getProfilePhoto(userId: String): String
    suspend fun updateUserName(newName: String): Boolean
}