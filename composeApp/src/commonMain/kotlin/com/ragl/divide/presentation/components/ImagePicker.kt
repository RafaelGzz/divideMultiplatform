package com.ragl.divide.presentation.components

import androidx.compose.runtime.Composable

@Composable
expect fun ImagePicker(
    onImageSelected: (String) -> Unit,
    onDismiss: () -> Unit
) 