package com.ragl.divide.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.DollarSign
import dividemultiplatform.composeapp.generated.resources.Res
import dividemultiplatform.composeapp.generated.resources.add_expense
import dividemultiplatform.composeapp.generated.resources.make_a_payment
import org.jetbrains.compose.resources.stringResource

@Composable
fun EventFABGroup(
    fabIcon: ImageVector = Icons.Default.Add,
    onAddExpenseClick: () -> Unit,
    onAddPaymentClick: () -> Unit,
    containerColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = MaterialTheme.colorScheme.onPrimary,
    expandedContainerColor: Color = MaterialTheme.colorScheme.secondary,
    expandedContentColor: Color = MaterialTheme.colorScheme.onSecondary,
) {
    var isExpanded by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isExpanded) 1f else 0f,
        animationSpec = spring(dampingRatio = 1f),
        label = "scale"
    )
    val rotation by animateFloatAsState(
        targetValue = if (isExpanded) 315f else 0f,
        animationSpec = spring(dampingRatio = 3f),
        label = "rotation"
    )

    Column {
        Column(
            modifier = Modifier.offset(
                x = animateDpAsState(
                    targetValue = if (isExpanded) 0.dp else 60.dp,
                    animationSpec = spring(dampingRatio = 1f),
                    label = "x"
                ).value, y = animateDpAsState(
                    targetValue = if (isExpanded) 0.dp else 100.dp,
                    animationSpec = spring(dampingRatio = 1f),
                    label = "y"
                ).value
            ).scale(scale)
        ) {
            // Customize the content of the expanded box as needed
            Button(
                onClick = { onAddExpenseClick() },
                shape = ShapeDefaults.Medium,
                colors = ButtonDefaults.buttonColors(
                    containerColor = containerColor,
                    contentColor = contentColor
                ),
                contentPadding = PaddingValues(horizontal = 12.dp),
                modifier = Modifier.height(60.dp).align(Alignment.End)
            ) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(Res.string.add_expense),
                    maxLines = 1,
                    softWrap = true,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = { onAddPaymentClick() },
                shape = ShapeDefaults.Medium,
                colors = ButtonDefaults.buttonColors(
                    containerColor = containerColor,
                    contentColor = contentColor
                ),
                contentPadding = PaddingValues(horizontal = 12.dp),
                modifier = Modifier.height(60.dp).align(Alignment.End)
            ) {
                Icon(
                    FontAwesomeIcons.Solid.DollarSign,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(Res.string.make_a_payment),
                    maxLines = 1,
                    softWrap = true,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        FloatingActionButton(
            onClick = {
                isExpanded = !isExpanded
            },
            shape = ShapeDefaults.Medium,
            containerColor = if (isExpanded) expandedContainerColor else containerColor,
            contentColor = if (isExpanded) expandedContentColor else contentColor,
            modifier = Modifier.align(Alignment.End)
        ) {
            Icon(
                imageVector = fabIcon,
                contentDescription = null,
                modifier = Modifier.size(24.dp).rotate(rotation)
            )
        }
    }
}