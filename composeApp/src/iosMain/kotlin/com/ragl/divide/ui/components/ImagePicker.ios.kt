package com.ragl.divide.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.refTo
import platform.Foundation.NSData
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.writeToFile
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import platform.UIKit.UIImagePickerController
import platform.UIKit.UIImagePickerControllerDelegateProtocol
import platform.UIKit.UIImagePickerControllerSourceType
import platform.UIKit.UINavigationControllerDelegateProtocol
import platform.UIKit.UIViewController
import platform.darwin.NSObject
import platform.posix.memcpy
import kotlin.random.Random

@Composable
actual fun ImagePicker(
    onImageSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    // Interfaz de selección (se maneja desde el componente común)
    ImagePickerDialog(
        onGalleryClick = { showImagePicker(UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypePhotoLibrary, onImageSelected, onDismiss) },
        onCameraClick = { showImagePicker(UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypeCamera, onImageSelected, onDismiss) },
        onDismiss = onDismiss
    )
}

private fun showImagePicker(
    sourceType: UIImagePickerControllerSourceType,
    onImageSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val delegate = ImagePickerDelegate(onImageSelected, onDismiss)
    val picker = UIImagePickerController()
    picker.sourceType = sourceType
    picker.delegate = delegate
    
    val rootViewController = UIViewController.currentViewController()
    rootViewController.presentViewController(picker, true, null)
}

private class ImagePickerDelegate(
    private val onImageSelected: (String) -> Unit,
    private val onDismiss: () -> Unit
) : NSObject(), UIImagePickerControllerDelegateProtocol, UINavigationControllerDelegateProtocol {

    @OptIn(ExperimentalForeignApi::class)
    override fun imagePickerController(
        picker: UIImagePickerController,
        didFinishPickingMediaWithInfo: Map<Any?, *>
    ) {
        val image = didFinishPickingMediaWithInfo["UIImagePickerControllerOriginalImage"] as? UIImage
        if (image != null) {
            // Guardar imagen en un archivo temporal
            val nsData = UIImageJPEGRepresentation(image, 0.8) ?: return
            val tempDirectoryPath = NSTemporaryDirectory()
            val randomFileName = "image_${Random.nextInt(100000)}.jpg"
            val fileURL = NSURL.fileURLWithPath(tempDirectoryPath + randomFileName)
            
            nsData.writeToFile(fileURL.path!!, true)
            onImageSelected(fileURL.path!!)
        } else {
            onDismiss()
        }
        
        // Cerrar el picker
        picker.dismissViewControllerAnimated(true, null)
    }

    override fun imagePickerControllerDidCancel(picker: UIImagePickerController) {
        onDismiss()
        picker.dismissViewControllerAnimated(true, null)
    }
}

// Extensión para obtener el ViewController actual
private fun UIViewController.Companion.currentViewController(): UIViewController {
    val keyWindow = platform.UIKit.UIApplication.sharedApplication.keyWindow
    var viewController = keyWindow?.rootViewController
    
    while (viewController?.presentedViewController != null) {
        viewController = viewController.presentedViewController
    }
    
    return viewController!!
} 