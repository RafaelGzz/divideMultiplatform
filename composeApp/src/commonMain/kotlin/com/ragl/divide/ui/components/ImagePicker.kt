package com.ragl.divide.ui.components

import androidx.compose.runtime.Composable

@Composable
expect fun ImagePicker(
    onImageSelected: (String) -> Unit,
    onDismiss: () -> Unit
) 