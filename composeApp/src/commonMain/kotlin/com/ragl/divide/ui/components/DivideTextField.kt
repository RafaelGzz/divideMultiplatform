package com.ragl.divide.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.Eye
import compose.icons.fontawesomeicons.solid.EyeSlash

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
    characterLimit: Int? = null,
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
        // Row para error y contador de caracteres
        if (!error.isNullOrEmpty() || characterLimit != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (!error.isNullOrEmpty()) {
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }

                if (characterLimit != null) {
                    val currentLength = value.trim().length
                    val counterText = "$currentLength/$characterLimit"
                    val isOverLimit = currentLength > characterLimit
                    Text(
                        text = counterText,
                        color = if (isOverLimit) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        } else {
            Spacer(modifier = Modifier.padding(top = 4.dp))
        }
    }
}