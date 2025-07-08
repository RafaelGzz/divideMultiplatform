package com.ragl.divide.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dividemultiplatform.composeapp.generated.resources.Res
import dividemultiplatform.composeapp.generated.resources.select_image_from_gallery
import dividemultiplatform.composeapp.generated.resources.select_image_source
import dividemultiplatform.composeapp.generated.resources.take_photo
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImagePickerDialog(
    onGalleryClick: () -> Unit,
    onCameraClick: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Text(
            stringResource(Res.string.select_image_source),
            style = MaterialTheme.typography.titleMedium.copy(color = MaterialTheme.colorScheme.primary),
            modifier = Modifier.padding(start = 16.dp, bottom = 16.dp)
        )
        ListItem(
            headlineContent = {
                Text(
                    stringResource(Res.string.select_image_from_gallery),
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            modifier = Modifier.clickable(onClick = onGalleryClick)
        )
        ListItem(
            headlineContent = {
                Text(
                    stringResource(Res.string.take_photo),
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            modifier = Modifier.clickable(onClick = onCameraClick)
        )
    }
} 