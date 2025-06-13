package com.ragl.divide.ui.utils

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ragl.divide.ui.components.NetworkImage
import com.ragl.divide.ui.components.NetworkImageType
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.Eye
import compose.icons.fontawesomeicons.solid.EyeSlash
import kotlin.math.round

expect fun logMessage(tag: String, message: String)

expect fun formatCurrency(value: Double, local: String): String

expect fun formatDate(epochMilliseconds: Long, pattern: String = "dd/MM/yyyy hh:mm a"): String

expect class Strings {
    fun getAppName(): String
    fun getNotificationBodyString(title: String): String
    fun getTwoSelected(): String
    fun getPercentagesSum(): String
    fun getTwoMustPay(): String
    fun getSumMustBe(amount: String): String
    fun getEmailRequired(): String
    fun getEmailNotValid(): String
    fun getPasswordRequired(): String
    fun getEmailAddressRequired(): String
    fun getInvalidEmailAddress(): String
    fun getUsernameEmpty(): String
    fun getUsernameRequirements(): String
    fun getPasswordMinLength(): String
    fun getPasswordRequirements(): String
    fun getPasswordsNotMatch(): String
    fun getSomethingWentWrong(): String
    fun getTitleRequired(): String
    fun getAmountRequired(): String
    fun getInvalidAmount(): String
    fun getAmountMustBeGreater(): String
    fun getUnknownError(): String
    fun getErrorDeletingPayment(): String
    fun getEmailPasswordInvalid(): String
    fun getEmailAlreadyInUse(): String
    fun getFailedToLogin(): String
    fun getEmailNotVerified(): String
    fun getVerificationEmailSent(): String
    fun getUnusualActivity(): String
    fun getCannotDeleteGroup(): String
    fun getCannotLeaveGroup(): String
    fun getCannotDeleteEvent(): String
    fun getCouldNotProcessImage(): String
    fun getFriendRequestSent(): String
    fun getFriendRequestAccepted(): String
    fun getFriendRequestRejected(): String
    fun getFriendRequestCanceled(): String
    fun getFailedToSendFriendRequest(): String
    fun getFailedToAcceptFriendRequest(): String
    fun getFailedToRejectFriendRequest(): String
    fun getFailedToCancelFriendRequest(): String
    fun getFriendRemoved(): String
    fun getFailedToRemoveFriend(): String
    fun getExpenseAlreadyPaid(): String
    fun getCongratulations(title: String): String
}

fun Double.toTwoDecimals(): Double {
    return round(this * 100) / 100
}

fun validateQuantity(input: String, updateInput: (String) -> Unit) {
    if (input.isEmpty()) updateInput("") else if (!input.contains(',')) {
        val parsed = input.toDoubleOrNull()
        parsed?.let {
            val decimalPart = input.substringAfter(".", "")
            if (decimalPart.length <= 2 && parsed <= 999999.99) {
                updateInput(input)
            }
        }
    }
}

@Composable
fun DivideTextField(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    prefix: @Composable (() -> Unit)? = null,
    suffix: @Composable (() -> Unit)? = null,
    enabled: Boolean = true,
    placeholder: @Composable (() -> Unit)? = null,
    singleLine: Boolean = true,
    error: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    autoCorrect: Boolean = true,
    capitalizeFirstLetter: Boolean = true,
    onValueChange: (String) -> Unit = {},
    onAction: () -> Unit = {},
    validate: () -> Unit = {}
) {
    var passwordVisible by rememberSaveable {
        mutableStateOf(false)
    }
    var icon by remember { mutableStateOf<@Composable (() -> Unit)?>(null) }
    var isFocused by remember { mutableStateOf(false) }

    LaunchedEffect(value) {
        if (value.isNotEmpty() && enabled) {
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
        OutlinedTextField(
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                unfocusedIndicatorColor = Color.Transparent,
                unfocusedPrefixColor = MaterialTheme.colorScheme.onSurfaceVariant,
                focusedPrefixColor = MaterialTheme.colorScheme.onSurface,
                disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                disabledIndicatorColor = Color.Transparent,
                disabledTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledPrefixColor = MaterialTheme.colorScheme.onSurfaceVariant,
                errorContainerColor = MaterialTheme.colorScheme.errorContainer,
                errorTextColor = MaterialTheme.colorScheme.onErrorContainer,
            ),
            enabled = enabled,
            isError = !error.isNullOrEmpty(),
            prefix = prefix,
            suffix = suffix,
            placeholder = placeholder,
            singleLine = singleLine,
            value = value,
            visualTransformation = if (!passwordVisible && keyboardType == KeyboardType.Password) PasswordVisualTransformation() else VisualTransformation.None,
            trailingIcon = icon,
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = imeAction,
                keyboardType = keyboardType,
                autoCorrectEnabled = autoCorrect,
                capitalization = if (capitalizeFirstLetter && keyboardType == KeyboardType.Text) KeyboardCapitalization.Sentences else KeyboardCapitalization.None
            ),
            keyboardActions = KeyboardActions(
                onDone = { onAction() }
            ),
            onValueChange = { onValueChange(it) },
            textStyle = MaterialTheme.typography.bodyMedium,
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { focusState ->
                    if (isFocused && !focusState.isFocused) {
                        validate()
                    }
                    isFocused = focusState.isFocused
                }
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
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        contentColor = MaterialTheme.colorScheme.onSurface
    ),
    trailingContent: @Composable (() -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    val interactionSource by remember { mutableStateOf(MutableInteractionSource()) }
    val supportingContent: @Composable (() -> Unit)? = if (supporting.isNotEmpty()) {
        @Composable {
            Text(
                text = supporting,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
            modifier = if (hasLeadingContent) Modifier.padding(vertical = 4.dp) else Modifier,
            colors = ListItemDefaults.colors(
                containerColor = Color.Transparent,
            ),
            headlineContent = {
                Text(
                    text = headline,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.primary.copy(
                        alpha = 0.5f
                    ),
                    overflow = TextOverflow.Ellipsis,
                    softWrap = true,
                    maxLines = 2
                )
            },
            supportingContent = supportingContent,
            leadingContent = if (hasLeadingContent) {
                {
                   NetworkImage(
                       imageUrl = photoUrl,
                       modifier = Modifier.size(52.dp).clip(CircleShape),
                       type = NetworkImageType.PROFILE
                   )
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
    } else {
        NetworkImage(
            imageUrl = photoUrl,
            modifier = modifier
                .clip(CircleShape)
                .size(size.dp),
            type = NetworkImageType.PROFILE
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

@Composable
fun Header(
    title: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = title,
        style = MaterialTheme.typography.headlineMedium,
        modifier = modifier
            .padding(horizontal = 16.dp, vertical = 12.dp)

    )
}