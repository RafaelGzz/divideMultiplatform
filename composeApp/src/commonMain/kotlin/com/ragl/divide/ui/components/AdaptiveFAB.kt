package com.ragl.divide.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.ragl.divide.ui.utils.WindowWidthSizeClass
import com.ragl.divide.ui.utils.getWindowWidthSizeClass

@Composable
fun AdaptiveFAB(
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String? = null,
    text: String? = null,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = MaterialTheme.colorScheme.onPrimary
) {
    val windowSizeClass = getWindowWidthSizeClass()
    
    when (windowSizeClass) {
        WindowWidthSizeClass.Compact -> {
            FloatingActionButton(
                onClick = onClick,
                modifier = modifier,
                containerColor = containerColor,
                contentColor = contentColor,
                shape = ShapeDefaults.Medium
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = contentDescription,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        WindowWidthSizeClass.Medium,
        WindowWidthSizeClass.Expanded -> {
            ExtendedFloatingActionButton(
                onClick = onClick,
                icon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = contentDescription,
                        modifier = Modifier.size(24.dp)
                    )
                },
                text = {
                    Text(text = text ?: contentDescription ?: "")
                },
                modifier = modifier,
                containerColor = containerColor,
                contentColor = contentColor,
                shape = ShapeDefaults.Medium
            )
        }
    }
} 