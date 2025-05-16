package com.ragl.divide.ui.screens.groupProperties

import androidx.core.net.toUri
import dev.gitlive.firebase.storage.File
import java.io.IOException

actual object PlatformImageUtils {
    actual fun createFirebaseFile(path: String): File? {
        return try {
            // Convertir path a URI
            val uri = "file://$path".toUri()
            File(uri)
        } catch (e: IOException) {
            null
        }
    }
} 