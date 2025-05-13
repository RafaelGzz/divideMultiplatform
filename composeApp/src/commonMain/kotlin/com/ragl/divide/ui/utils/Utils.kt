package com.ragl.divide.ui.utils

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.skydoves.landscapist.coil3.CoilImage
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.Eye
import compose.icons.fontawesomeicons.solid.EyeSlash
import kotlin.math.round

expect fun logMessage(tag: String, message: String)

expect fun formatCurrency(value: Double, local: String): String

expect fun formatDate(epochMilliseconds: Long, pattern: String = "dd/MM/yyyy hh:mm a"): String

expect class Strings{
    fun getNotificationTitleString(title: String): String
    fun getNotificationBodyString(): String
    fun getTwoSelected(): String
    fun getPercentagesSum(): String
    fun getTwoMustPay(): String
    fun getSumMustBe(amount: String): String
}

fun Double.toTwoDecimals(): Double {
    return round(this * 100) / 100
}

fun validateQuantity(input: String, updateInput: (String) -> Unit) {
    if (input.isEmpty()) updateInput("") else if(!input.contains(',')){
        val parsed = input.toDoubleOrNull()
        parsed?.let {
            val decimalPart = input.substringAfter(".", "")
            if (decimalPart.length <= 2 && parsed <= 999999999.99) {
                updateInput(input)
            }
        }
    }
}

@Composable
fun DivideTextField(
    modifier: Modifier = Modifier,
    prefix: @Composable (() -> Unit)? = null,
    suffix: @Composable (() -> Unit)? = null,
    label: String,
    input: String,
    enabled: Boolean = true,
    placeholder: @Composable (() -> Unit)? = null,
    //keyboardActions: KeyboardActions = KeyboardActions(),
    singleLine: Boolean = true,
//    errorText: Boolean = true,
    error: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    autoCorrect: Boolean = true,
    onValueChange: (String) -> Unit,
    onAction: () -> Unit = {}
) {
    var passwordVisible by rememberSaveable {
        mutableStateOf(false)
    }
    var icon by remember { mutableStateOf<@Composable (() -> Unit)?>(null) }
    LaunchedEffect(input) {
        if (input.isNotEmpty()) {
            icon = when (keyboardType) {
                KeyboardType.Password -> {
                    {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible)
                                    FontAwesomeIcons.Solid.EyeSlash
                                else FontAwesomeIcons.Solid.Eye,
                                if (passwordVisible) "Hide password" else "Show password",
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }

                KeyboardType.Text -> {
                    {
                        IconButton(onClick = { onValueChange("") }) {
                            Icon(
                                imageVector = Icons.Filled.Clear,
                                contentDescription = "Clear"
                            )
                        }
                    }
                }

                else -> {
                    null
                }
            }
        } else {
            icon = null
        }
    }
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        TextField(
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                unfocusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                unfocusedPrefixColor = MaterialTheme.colorScheme.onPrimaryContainer,
                focusedPrefixColor = MaterialTheme.colorScheme.onPrimaryContainer,
                disabledContainerColor = MaterialTheme.colorScheme.primaryContainer,
                disabledIndicatorColor = Color.Transparent,
                disabledTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                disabledPrefixColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
            ),
            enabled = enabled,
            prefix = prefix,
            suffix = suffix,
            placeholder = placeholder,
            singleLine = singleLine,
            value = input,
            visualTransformation = if (!passwordVisible && keyboardType == KeyboardType.Password) PasswordVisualTransformation() else VisualTransformation.None,
            trailingIcon = icon,
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = imeAction,
                keyboardType = keyboardType,
                autoCorrectEnabled = autoCorrect
            ),
            keyboardActions = KeyboardActions(
                onDone = { onAction() }
            ),
            onValueChange = { onValueChange(it) },
            textStyle = MaterialTheme.typography.bodyMedium,
            modifier = Modifier
                .fillMaxWidth()
                .clip(ShapeDefaults.Medium)
        )
        if (!error.isNullOrEmpty())
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        else
            Spacer(modifier = Modifier.padding(top = 4.dp))
    }
}

@Composable
fun FriendItem(
    modifier: Modifier = Modifier,
    hasLeadingContent: Boolean = true,
    headline: String,
    supporting: String = "",
    photoUrl: String = "",
    enabled: Boolean = true,
    colors: CardColors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
    ),
    icon: ImageVector = Icons.Filled.Person,
    trailingContent: @Composable (() -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    val interactionSource by remember { mutableStateOf(MutableInteractionSource()) }
    val supportingContent: @Composable (() -> Unit)? = if (supporting.isNotEmpty()) {
        @Composable {
            Text(
                text = supporting,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            )
        }
    } else {
        null
    }
    Card(
        modifier = modifier
            .clickable(interactionSource = interactionSource, indication = null) {
                if (onClick != null) {
                    onClick()
                }
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = colors
    ) {
        ListItem(
            modifier = if(hasLeadingContent) Modifier.padding(vertical = 4.dp) else Modifier,
            colors = ListItemDefaults.colors(
                containerColor = Color.Transparent,
            ),
            headlineContent = {
                Text(
                    text = headline,
                    color = if (enabled) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.primary.copy(
                        alpha = 0.5f
                    ),
                    overflow = TextOverflow.Ellipsis,
                    softWrap = true
                )
            },
            supportingContent = supportingContent,
            leadingContent = if (hasLeadingContent) {
                {
                    if (photoUrl.isNotEmpty()) {
                        ProfileImage(
                            photoUrl = photoUrl,
                            icon = icon,
                            enabled = enabled,
                            supporting = supporting.isNotEmpty()
                        )
                    } else {
                        Icon(
                            icon,
                            contentDescription = null,
                            tint = if (enabled) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onPrimary.copy(
                                alpha = 0.5f
                            ),
                            modifier = Modifier
                                .padding(vertical = if (supporting.isNotEmpty()) 0.dp else 2.dp)
                                .clip(CircleShape)
                                .size(52.dp)
                                .background(
                                    if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(
                                        alpha = 0.5f
                                    )
                                )
                                .padding(12.dp)
                        )
                    }
                }
            } else {
                null
            },
            trailingContent = trailingContent
        )
    }

    fun String?.toBoolean(): Boolean? {
        return when (this) {
            "true" -> true
            "false" -> false
            else -> null
        }
    }
}

@Composable
fun ProfileImage(
    photoUrl: String,
    modifier: Modifier = Modifier,
    size: Int = 52,
    icon: ImageVector = Icons.Filled.Person,
    enabled: Boolean = true,
    supporting: Boolean = false
) {
    // Si la URL está vacía, mostramos el icono predeterminado
    if (photoUrl.isEmpty()) {
        DefaultProfileIcon(
            modifier = modifier,
            size = size,
            icon = icon,
            enabled = enabled,
            supporting = supporting
        )
    }
    
    // Usamos un Box para mostrar un indicador de carga mientras se carga la imagen
    Box(
        modifier = modifier
            .clip(CircleShape)
            .size(size.dp),
        contentAlignment = Alignment.Center
    ) {
        CoilImage(
            imageModel = { photoUrl },
        )
    }
}

@Composable
private fun DefaultProfileIcon(
    modifier: Modifier = Modifier,
    size: Int = 52,
    icon: ImageVector = Icons.Filled.Person,
    enabled: Boolean = true,
    supporting: Boolean = false
) {
    Icon(
        icon,
        contentDescription = null,
        tint = if (enabled) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onPrimary.copy(
            alpha = 0.5f
        ),
        modifier = modifier
            .padding(vertical = if (supporting) 0.dp else 2.dp)
            .clip(CircleShape)
            .size(size.dp)
            .background(
                if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(
                    alpha = 0.5f
                )
            )
            .padding(size.dp / 4)
    )
}