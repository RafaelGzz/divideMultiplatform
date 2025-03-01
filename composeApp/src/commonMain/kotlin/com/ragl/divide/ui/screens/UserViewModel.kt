package com.ragl.divide.ui.screens

import androidx.lifecycle.viewModelScope
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.ragl.divide.data.models.Expense
import com.ragl.divide.data.models.Group
import com.ragl.divide.data.models.User
import com.ragl.divide.data.repositories.UserRepository
import com.ragl.divide.ui.utils.logMessage
import dev.gitlive.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AppState(
    val isLoading: Boolean = false,
    val expenses: Map<String, Expense> = emptyMap(),
    val groups: Map<String, Group> = emptyMap(),
    val friends: Map<String, User> = emptyMap(),
    val selectedGroupMembers: List<User> = emptyList(),
    val user: User = User()
)

class UserViewModel(
    private var userRepository: UserRepository
): ScreenModel {

    private val _state = MutableStateFlow(AppState())
    val state = _state.asStateFlow()

    init {
        if(userRepository.getFirebaseUser() != null){
            getUserData()
        }
    }

    fun getUserData() {
        screenModelScope.launch {
            _state.update {
                it.copy(isLoading = true)
            }
            try {
                val user = userRepository.getUser(userRepository.getFirebaseUser()!!.uid)
//                val groups = groupRepository.getGroups(user.groups)
//                val expenses = userRepository.getExpenses()
//                val friends = friendsRepository.getFriends(user.friends)
                _state.update {
                    it.copy(
//                        expenses = expenses,
//                        groups = groups,
//                        friends = friends,
                        user = user
                    )
                }
                logMessage("HomeViewModel", "getUserData: ${user.name}")
            } catch (e: Exception) {
                e.printStackTrace()
                logMessage("HomeViewModel", e.message.toString())
            }
            _state.update {
                it.copy(isLoading = false)
            }
        }
    }

    fun signInWithEmailAndPassword(
        email: String,
        password: String,
        onFailedLogin: (String) -> Unit
    ) {
        screenModelScope.launch {
            try {
                _state.update {
                    it.copy(isLoading = true)
                }
                if (userRepository.signInWithEmailAndPassword(email, password) != null) {
                    //preferencesRepository.saveStartDestination(Screen.Home.route)
                    getUserData()
                } else onFailedLogin("Failed to Log in")
            } catch (e: Exception) {
                //Log.e("UserViewModel", "signInWithEmailAndPassword: ", e)
                logMessage("UserViewModel", e.message.toString())
                onFailedLogin(e.message ?: "Unknown error")
            } finally {
                _state.update {
                    it.copy(isLoading = false)
                }
            }
        }
    }

    fun signUpWithEmailAndPassword(
        email: String, password: String, name: String,
        onFailedLogin: (String) -> Unit
    ) {
        screenModelScope.launch {
            try {
                _state.update {
                    it.copy(isLoading = true)
                }
                if (userRepository.signUpWithEmailAndPassword(email, password, name) != null) {
                    //preferencesRepository.saveStartDestination(Screen.Home.route)
                    getUserData()
                } else onFailedLogin("Failed to Log in")
            } catch (e: Exception) {
                onFailedLogin(e.message ?: "Unknown error")
                logMessage("UserViewModel", e.message.toString())
                //Log.e("UserViewModel", "signUpWithEmailAndPassword: ", e)
            } finally {
                _state.update {
                    it.copy(isLoading = false)
                }
            }
        }
    }

    fun signInWithGoogle(
        result: Result<FirebaseUser?>
    ){
        screenModelScope.launch {
            try {
                _state.update {
                    it.copy(isLoading = true)
                }

                if (result.getOrNull() != null) {
                    //preferencesRepository.saveStartDestination(Screen.Home.route)
                    getUserData()
                } else {
                    //onFailedLogin("Failed to sign in")
                }

            } catch (e: Exception) {
                //onFailedLogin(e.message ?: "Unknown error")
                logMessage("UserViewModel", e.message.toString())
                //Log.e("UserViewModel", "signUpWithEmailAndPassword: ", e)
            } finally {
                _state.update {
                    it.copy(isLoading = false)
                }
            }
        }
    }

}