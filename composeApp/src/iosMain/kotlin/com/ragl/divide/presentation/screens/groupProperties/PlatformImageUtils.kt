package com.ragl.divide.presentation.screens.groupProperties

import dev.gitlive.firebase.storage.File
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL

actual object PlatformImageUtils {
    actual fun createFirebaseFile(path: String): File? {
        return try {
            // Verificar que el archivo existe
            val fileExists = NSFileManager.defaultManager.fileExistsAtPath(path)
            if (fileExists) {
                // Convertir path a NSURL
                val nsurl = NSURL.fileURLWithPath(path)
                File(nsurl)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
} 