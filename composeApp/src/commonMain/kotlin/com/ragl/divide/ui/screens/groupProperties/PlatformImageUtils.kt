package com.ragl.divide.ui.screens.groupProperties

import dev.gitlive.firebase.storage.File

expect object PlatformImageUtils {
    fun createFirebaseFile(path: String): File?
} 