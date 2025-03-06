package com.ragl.divide.ui.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import platform.UIKit.*
import platform.Foundation.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
actual fun DateTimePickerDialog(
    onDismissRequest: () -> Unit,
    onConfirmClick: (Long) -> Unit,
    initialTime: Long
) {
    // Crea un UIDatePicker nativo
    val datePicker = UIDatePicker().apply {
        datePickerMode = UIDatePickerMode.UIDatePickerModeDateAndTime
        date = NSDate.dateWithTimeIntervalSince1970(initialTime.toDouble() / 1000)
        locale = NSLocale.currentLocale
        timeZone = NSTimeZone.localTimeZone
    }

    // Crea un UIAlertController para mostrar el picker
    val alertController = UIAlertController.alertControllerWithTitle(
        null,
        null,
        UIAlertControllerStyleActionSheet
    )

    // Agrega el datePicker al alert controller
    alertController.view.addSubview(datePicker)

    // Configura las restricciones de Auto Layout para el datePicker
    datePicker.translatesAutoresizingMaskIntoConstraints = false
    datePicker.leadingAnchor.constraintEqualToAnchor(alertController.view.leadingAnchor).active = true
    datePicker.trailingAnchor.constraintEqualToAnchor(alertController.view.trailingAnchor).active = true
    datePicker.topAnchor.constraintEqualToAnchor(alertController.view.topAnchor, constant = 20.0).active = true
    datePicker.bottomAnchor.constraintEqualToAnchor(alertController.view.bottomAnchor, constant = -60.0).active = true

    // Agrega las acciones (botones)
    alertController.addAction(
        UIAlertAction.actionWithTitle(
            "OK",
            UIAlertActionStyleDefault
        ) { _ ->
            val selectedTimeMillis = (datePicker.date.timeIntervalSince1970 * 1000).toLong()
            onConfirmClick(selectedTimeMillis)
            onDismissRequest()
        }
    )

    alertController.addAction(
        UIAlertAction.actionWithTitle(
            "Cancel",
            UIAlertActionStyleCancel
        ) { _ ->
            onDismissRequest()
        }
    )

    // Muestra el alert controller
    UIApplication.sharedApplication.keyWindow?.rootViewController?.presentViewController(
        alertController,
        true,
        null
    )
}