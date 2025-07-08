package com.ragl.divide.presentation.components

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

@Composable
actual fun ImagePicker(
    onImageSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // Launcher para galería
    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val imageUrl = saveImageFromUri(context, uri)
                onImageSelected(imageUrl)
            }
        } else {
            onDismiss()
        }
    }
    
    // Launcher para cámara
    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            scope.launch {
                val imageUrl = saveImageFromBitmap(context, bitmap)
                onImageSelected(imageUrl)
            }
        } else {
            onDismiss()
        }
    }
    
    // Decidir qué launcher iniciar basado en la fuente seleccionada
    val launchGallery = {
        galleryLauncher.launch("image/*")
    }
    
    val launchCamera = {
        cameraLauncher.launch(null)
    }
    
    // Interfaz de selección (se maneja desde el componente común)
    ImagePickerDialog(
        onGalleryClick = launchGallery,
        onCameraClick = launchCamera,
        onDismiss = onDismiss
    )
}

private suspend fun saveImageFromUri(context: Context, uri: Uri): String = withContext(Dispatchers.IO) {
    val inputStream = context.contentResolver.openInputStream(uri)
    val file = File(context.cacheDir, "image_${UUID.randomUUID()}.jpg")
    FileOutputStream(file).use { outputStream ->
        inputStream?.copyTo(outputStream)
    }
    inputStream?.close()
    file.absolutePath
}

private suspend fun saveImageFromBitmap(context: Context, bitmap: android.graphics.Bitmap): String = withContext(Dispatchers.IO) {
    val file = File(context.cacheDir, "image_${UUID.randomUUID()}.jpg")
    FileOutputStream(file).use { outputStream ->
        bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, outputStream)
    }
    file.absolutePath
} 