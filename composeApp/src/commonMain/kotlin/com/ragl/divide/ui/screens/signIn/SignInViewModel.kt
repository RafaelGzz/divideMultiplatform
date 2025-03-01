package com.ragl.divide.ui.screens.signIn

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ragl.divide.data.repositories.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SignInViewModel(
    private val userRepository: UserRepository
): ViewModel() {

    private var _isLoading = MutableStateFlow(false)
    var isLoading = _isLoading.asStateFlow()

    init {
        viewModelScope.launch {
//            preferencesRepository.startDestinationFlow.collect {
//                _startDestination.value = Screen.stringToScreen(it)
//            }
            _isLoading.value = false
        }
    }fun signInWithEmailAndPassword(
        email: String, password: String,
        onSuccessfulLogin: () -> Unit,
        onFailedLogin: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                if (userRepository.signInWithEmailAndPassword(email, password) != null) {
                    //preferencesRepository.saveStartDestination(Screen.Home.route)
                    onSuccessfulLogin()
                } else onFailedLogin("Failed to Log in")
            } catch (e: Exception) {
                //Log.e("UserViewModel", "signInWithEmailAndPassword: ", e)
                onFailedLogin(e.message ?: "Unknown error")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun signUpWithEmailAndPassword(
        email: String, password: String, name: String,
        onSuccessfulLogin: () -> Unit,
        onFailedLogin: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                if (userRepository.signUpWithEmailAndPassword(email, password, name) != null) {
                    //preferencesRepository.saveStartDestination(Screen.Home.route)
                    onSuccessfulLogin()
                } else onFailedLogin("Failed to Log in")
            } catch (e: Exception) {
                onFailedLogin(e.message ?: "Unknown error")
                //Log.e("UserViewModel", "signUpWithEmailAndPassword: ", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

//    fun signInWithGoogle(
//        context: Context,
//        onSuccessfulLogin: () -> Unit,
//        onFailedLogin: (String) -> Unit
//    ) {
//        viewModelScope.launch {
//            try {
//                _isLoading.value = true
//                val authCredential = getAuthCredential(context)
//                if (userRepository.signInWithCredential(authCredential) != null) {
//                    //preferencesRepository.saveStartDestination(Screen.Home.route)
//                    onSuccessfulLogin()
//                } else {
//                    onFailedLogin("Failed to sign in")
//                }
//            } catch (e: Exception) {
//                onFailedLogin(e.message ?: "Unknown error")
//                //Log.e("UserViewModel", "signInWithGoogle: ", e)
//            } finally {
//                _isLoading.value = false
//            }
//        }
//    }

}