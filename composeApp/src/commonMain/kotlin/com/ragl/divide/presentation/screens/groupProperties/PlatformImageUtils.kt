package com.ragl.divide.presentation.screens.groupProperties

import dev.gitlive.firebase.storage.File

expect object PlatformImageUtils {
    fun createFirebaseFile(path: String): File?
} 