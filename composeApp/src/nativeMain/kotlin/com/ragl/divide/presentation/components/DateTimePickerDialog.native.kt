package com.ragl.divide.presentation.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import com.ragl.divide.presentation.utils.logMessage
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCAction
import platform.CoreGraphics.CGRectContainsPoint
import platform.CoreGraphics.CGRectMake
import platform.Foundation.NSDate
import platform.Foundation.NSLocale
import platform.Foundation.NSTimeZone
import platform.Foundation.currentLocale
import platform.Foundation.dateWithTimeIntervalSince1970
import platform.Foundation.localTimeZone
import platform.Foundation.timeIntervalSince1970
import platform.UIKit.NSTextAlignmentCenter
import platform.UIKit.UIApplication
import platform.UIKit.UIButton
import platform.UIKit.UIButtonTypeSystem
import platform.UIKit.UIColor
import platform.UIKit.UIControlEventTouchUpInside
import platform.UIKit.UIControlStateNormal
import platform.UIKit.UIDatePicker
import platform.UIKit.UIDatePickerMode
import platform.UIKit.UIDatePickerStyle
import platform.UIKit.UIFont
import platform.UIKit.UIGestureRecognizerDelegateProtocol
import platform.UIKit.UILabel
import platform.UIKit.UITapGestureRecognizer
import platform.UIKit.UIView
import platform.UIKit.labelColor
import platform.UIKit.systemBackgroundColor
import platform.UIKit.systemBlueColor
import platform.UIKit.systemGrayColor
import platform.darwin.NSObject
import platform.darwin.sel_registerName

@OptIn(ExperimentalMaterial3Api::class, ExperimentalForeignApi::class)
@Composable
actual fun DateTimePickerDialog(
    onDismissRequest: () -> Unit,
    onConfirmClick: (Long) -> Unit,
    initialTime: Long
) {
    // Convertir initialTime de milisegundos a segundos para NSDate
    val initialTimeSeconds = initialTime / 1000.0
    
    // Obtener el rootViewController
    val rootViewController = UIApplication.sharedApplication.keyWindow?.rootViewController
        ?: return
    
    // Crear un contenedor para el diálogo
    var containerView = UIView(frame = CGRectMake(0.0, 0.0, 320.0, 400.0)).apply {
        backgroundColor = UIColor.systemBackgroundColor
        layer.cornerRadius = 14.0
        clipsToBounds = true
    }
    
    // Crear título
    val titleLabel = UILabel().apply {
        text = "Seleccionar fecha y hora"
        textAlignment = NSTextAlignmentCenter
        font = UIFont.boldSystemFontOfSize(17.0)
        textColor = UIColor.labelColor
    }
    
    // Crear el UIDatePicker nativo
    val datePicker = UIDatePicker().apply {
        datePickerMode = UIDatePickerMode.UIDatePickerModeDateAndTime
        date = NSDate.dateWithTimeIntervalSince1970(initialTimeSeconds)
        locale = NSLocale.currentLocale
        timeZone = NSTimeZone.localTimeZone
        preferredDatePickerStyle = UIDatePickerStyle.UIDatePickerStyleWheels
    }
    
    // Crear botones para Ok y Cancelar
    val cancelButton = UIButton.buttonWithType(UIButtonTypeSystem).apply {
        setTitle("Cancelar", UIControlStateNormal)
        setTitleColor(UIColor.systemBlueColor, UIControlStateNormal)
    }
    
    // Objeto para manejar el evento de cancelar
    val cancelAction = object : NSObject() {
        @ObjCAction
        fun handleCancel() {
            containerView.superview?.removeFromSuperview()
            onDismissRequest()
        }
    }
    cancelButton.addTarget(
        cancelAction,
        sel_registerName("handleCancel"),
        UIControlEventTouchUpInside
    )
    
    // Objeto para manejar el evento de confirmar
    val okAction = object : NSObject() {
        @ObjCAction
        fun handleOk() {
            val selectedTimeMillis = (datePicker.date.timeIntervalSince1970 * 1000).toLong()
            logMessage("DateTimePickerDialog", "Fecha seleccionada: $selectedTimeMillis")
            containerView.superview?.removeFromSuperview()
            onConfirmClick(selectedTimeMillis)
            onDismissRequest()
        }
    }
    
    val okButton = UIButton.buttonWithType(UIButtonTypeSystem).apply {
        setTitle("OK", UIControlStateNormal)
        setTitleColor(UIColor.systemBlueColor, UIControlStateNormal)
        titleLabel?.font = UIFont.boldSystemFontOfSize(17.0)
        addTarget(
            okAction,
            sel_registerName("handleOk"),
            UIControlEventTouchUpInside
        )
    }
    
    // Crear divisor superior
    val topDivider = UIView().apply {
        backgroundColor = UIColor.systemGrayColor.colorWithAlphaComponent(0.3)
    }
    
    // Crear divisor inferior
    val bottomDivider = UIView().apply {
        backgroundColor = UIColor.systemGrayColor.colorWithAlphaComponent(0.3)
    }
    
    // Añadir vistas al contenedor
    containerView.addSubview(titleLabel)
    containerView.addSubview(topDivider)
    containerView.addSubview(datePicker)
    containerView.addSubview(bottomDivider)
    containerView.addSubview(cancelButton)
    containerView.addSubview(okButton)
    
    // Configurar restricciones de AutoLayout
    containerView.translatesAutoresizingMaskIntoConstraints = false
    titleLabel.translatesAutoresizingMaskIntoConstraints = false
    topDivider.translatesAutoresizingMaskIntoConstraints = false
    datePicker.translatesAutoresizingMaskIntoConstraints = false
    bottomDivider.translatesAutoresizingMaskIntoConstraints = false
    cancelButton.translatesAutoresizingMaskIntoConstraints = false
    okButton.translatesAutoresizingMaskIntoConstraints = false
    
    titleLabel.topAnchor.constraintEqualToAnchor(containerView.topAnchor, 16.0).active = true
    titleLabel.leadingAnchor.constraintEqualToAnchor(containerView.leadingAnchor).active = true
    titleLabel.trailingAnchor.constraintEqualToAnchor(containerView.trailingAnchor).active = true
    
    topDivider.topAnchor.constraintEqualToAnchor(titleLabel.bottomAnchor, 16.0).active = true
    topDivider.leadingAnchor.constraintEqualToAnchor(containerView.leadingAnchor).active = true
    topDivider.trailingAnchor.constraintEqualToAnchor(containerView.trailingAnchor).active = true
    topDivider.heightAnchor.constraintEqualToConstant(0.5).active = true
    
    datePicker.topAnchor.constraintEqualToAnchor(topDivider.bottomAnchor, 10.0).active = true
    datePicker.leadingAnchor.constraintEqualToAnchor(containerView.leadingAnchor).active = true
    datePicker.trailingAnchor.constraintEqualToAnchor(containerView.trailingAnchor).active = true
    
    bottomDivider.topAnchor.constraintEqualToAnchor(datePicker.bottomAnchor, 10.0).active = true
    bottomDivider.leadingAnchor.constraintEqualToAnchor(containerView.leadingAnchor).active = true
    bottomDivider.trailingAnchor.constraintEqualToAnchor(containerView.trailingAnchor).active = true
    bottomDivider.heightAnchor.constraintEqualToConstant(0.5).active = true
    
    cancelButton.topAnchor.constraintEqualToAnchor(bottomDivider.bottomAnchor, 8.0).active = true
    cancelButton.leadingAnchor.constraintEqualToAnchor(containerView.leadingAnchor, 16.0).active = true
    cancelButton.bottomAnchor.constraintEqualToAnchor(containerView.bottomAnchor, -16.0).active = true
    
    okButton.topAnchor.constraintEqualToAnchor(bottomDivider.bottomAnchor, 8.0).active = true
    okButton.trailingAnchor.constraintEqualToAnchor(containerView.trailingAnchor, -16.0).active = true
    okButton.bottomAnchor.constraintEqualToAnchor(containerView.bottomAnchor, -16.0).active = true
    
    // Crear visualización animada del DatePicker
    var dimView = UIView(frame = rootViewController.view.bounds).apply {
        backgroundColor = UIColor.blackColor.colorWithAlphaComponent(0.4)
        alpha = 0.0
    }
    
    dimView.addSubview(containerView)
    rootViewController.view.addSubview(dimView)
    
    // Configurar las restricciones del contenedor en la vista oscurecida
    containerView.centerXAnchor.constraintEqualToAnchor(dimView.centerXAnchor).active = true
    containerView.centerYAnchor.constraintEqualToAnchor(dimView.centerYAnchor).active = true
    containerView.widthAnchor.constraintEqualToConstant(320.0).active = true
    
    // Objeto para manejar el tap gesture
    class TapHandler : NSObject(), UIGestureRecognizerDelegateProtocol {
        var dimView: UIView? = null
        var containerView: UIView? = null
        var onDismiss: () -> Unit = {}
        
        @ObjCAction
        fun handleTap(gestureRecognizer: UITapGestureRecognizer) {
            val point = gestureRecognizer.locationInView(dimView)
            containerView?.let { container ->
                // Usar CGRectContainsPoint para verificar si el punto está dentro del rectángulo
                if (!CGRectContainsPoint(container.frame, point)) {
                    UIView.animateWithDuration(0.2, animations = {
                        dimView?.alpha = 0.0
                    }) { _ ->
                        dimView?.removeFromSuperview()
                        onDismiss()
                    }
                }
            }
        }
    }
    
    // Crear la instancia del manejador de tap
    val tapHandler = TapHandler()
    // Configurar referencias
    tapHandler.dimView = dimView
    tapHandler.containerView = containerView
    tapHandler.onDismiss = onDismissRequest
    
    val tapGesture = UITapGestureRecognizer(tapHandler, sel_registerName("handleTap:"))
    dimView.addGestureRecognizer(tapGesture)
    
    // Animar la aparición del diálogo
    UIView.animateWithDuration(0.3) {
        dimView.alpha = 1.0
    }
}