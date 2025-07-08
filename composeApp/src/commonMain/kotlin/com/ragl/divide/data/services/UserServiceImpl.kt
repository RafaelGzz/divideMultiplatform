package com.ragl.divide.data.services

import com.ragl.divide.data.models.UserInfo
import com.ragl.divide.domain.repositories.FriendsRepository
import com.ragl.divide.domain.repositories.PreferencesRepository
import com.ragl.divide.domain.repositories.UserRepository
import com.ragl.divide.domain.services.AppStateService
import com.ragl.divide.domain.services.ScheduleNotificationService
import com.ragl.divide.domain.services.UserService
import com.ragl.divide.domain.stateHolders.UserStateHolder
import com.ragl.divide.presentation.screens.groupProperties.PlatformImageUtils
import com.ragl.divide.presentation.utils.Strings
import com.ragl.divide.presentation.utils.logMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class UserServiceImpl(
    private val userRepository: UserRepository,
    private val friendsRepository: FriendsRepository,
    private val preferencesRepository: PreferencesRepository,
    private val scheduleNotificationService: ScheduleNotificationService,
    private val strings: Strings,
    private val appStateService: AppStateService,
    private val userStateHolder: UserStateHolder
) : UserService {

    private val coroutineScope = CoroutineScope(Dispatchers.IO + Job())

    override suspend fun isFirstTime(): Boolean {
        return try {
            preferencesRepository.isFirstTimeFlow.first()
        } catch (e: Exception) {
            logMessage("UserService", "Error checking first time: ${e.message}")
            false
        }
    }

    override fun completeOnboarding() {
        coroutineScope.launch {
            preferencesRepository.setFirstTime(false)
            logMessage("UserService", "Onboarding completed")
        }
    }

    override fun changeDarkMode(isDarkMode: Boolean?) {
        coroutineScope.launch {
            preferencesRepository.saveDarkMode(isDarkMode)
        }
    }

    override fun signOut(onSignOut: () -> Unit) {
        coroutineScope.launch {
            try {
                appStateService.showLoading()
                scheduleNotificationService.cancelAllNotifications()
                userRepository.signOut()
                if (userRepository.getFirebaseUser() == null) {
                    onSignOut()
                    userStateHolder.updateUserState(com.ragl.divide.domain.stateHolders.UserState())
                }
            } catch (e: Exception) {
                logMessage("UserService", e.message.toString())
            } finally {
                appStateService.hideLoading()
            }
        }
    }

    override fun updateProfileImage(
        imagePath: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        coroutineScope.launch {
            try {
                appStateService.showLoading()
                val imageFile = PlatformImageUtils.createFirebaseFile(imagePath)

                if (imageFile != null) {
                    val downloadUrl = userRepository.saveProfilePhoto(imageFile)
                    userStateHolder.updateProfileImage(downloadUrl)
                    onSuccess()
                } else {
                    logMessage("UserService", "Could not process image")
                    onError(strings.getCouldNotProcessImage())
                }
            } catch (e: Exception) {
                logMessage("UserService", e.toString())
                onError(e.message ?: strings.getUnknownError())
            } finally {
                appStateService.hideLoading()
            }
        }
    }

    override fun updateUserName(newName: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        coroutineScope.launch {
            try {
                appStateService.showLoading()
                val trimmedName = newName.trim()

                if (trimmedName.isBlank()) {
                    onError(strings.getUsernameEmpty())
                    return@launch
                }

                val success = userRepository.updateUserName(trimmedName)
                if (success) {
                    userStateHolder.updateUserName(trimmedName)
                    onSuccess()
                } else {
                    onError(strings.getUnknownError())
                }
            } catch (e: Exception) {
                logMessage("UserService", e.toString())
                onError(e.message ?: strings.getUnknownError())
            } finally {
                appStateService.hideLoading()
            }
        }
    }

    override fun sendFriendRequest(friend: UserInfo) {
        coroutineScope.launch {
            try {
                appStateService.showLoading()
                if (friendsRepository.sendFriendRequest(friend.uuid)) {
                    userStateHolder.sendFriendRequest(friend)
                    appStateService.handleSuccess(strings.getFriendRequestSent())
                } else {
                    appStateService.handleError(strings.getFailedToSendFriendRequest())
                }
            } catch (e: Exception) {
                appStateService.handleError(e.message ?: strings.getUnknownError())
            } finally {
                appStateService.hideLoading()
            }
        }
    }

    override fun acceptFriendRequest(friend: UserInfo) {
        coroutineScope.launch {
            try {
                appStateService.showLoading()
                if (friendsRepository.acceptFriendRequest(friend.uuid)) {
                    userStateHolder.acceptFriendRequest(friend)
                    appStateService.handleSuccess(strings.getFriendRequestAccepted())
                } else {
                    appStateService.handleError(strings.getFailedToAcceptFriendRequest())
                }
            } catch (e: Exception) {
                appStateService.handleError(e.message ?: strings.getUnknownError())
            } finally {
                appStateService.hideLoading()
            }
        }
    }

    override fun rejectFriendRequest(friend: UserInfo) {
        coroutineScope.launch {
            try {
                appStateService.showLoading()
                if (friendsRepository.rejectFriendRequest(friend.uuid)) {
                    userStateHolder.rejectFriendRequest(friend)
                    appStateService.handleSuccess(strings.getFriendRequestRejected())
                } else {
                    appStateService.handleError(strings.getFailedToRejectFriendRequest())
                }
            } catch (e: Exception) {
                appStateService.handleError(e.message ?: strings.getUnknownError())
            } finally {
                appStateService.hideLoading()
            }
        }
    }

    override fun cancelFriendRequest(friend: UserInfo) {
        coroutineScope.launch {
            try {
                appStateService.showLoading()
                if (friendsRepository.cancelFriendRequest(friend.uuid)) {
                    userStateHolder.cancelFriendRequest(friend)
                    appStateService.handleSuccess(strings.getFriendRequestCanceled())
                } else {
                    appStateService.handleError(strings.getFailedToCancelFriendRequest())
                }
            } catch (e: Exception) {
                appStateService.handleError(e.message ?: strings.getUnknownError())
            } finally {
                appStateService.hideLoading()
            }
        }
    }

    override fun removeFriend(friend: UserInfo) {
        coroutineScope.launch {
            try {
                appStateService.showLoading()
                if (friendsRepository.removeFriend(friend.uuid)) {
                    userStateHolder.removeFriend(friend)
                    appStateService.handleSuccess(strings.getFriendRemoved())
                } else {
                    appStateService.handleError(strings.getFailedToRemoveFriend())
                }
            } catch (e: Exception) {
                appStateService.handleError(e.message ?: strings.getUnknownError())
            } finally {
                appStateService.hideLoading()
            }
        }
    }
} 