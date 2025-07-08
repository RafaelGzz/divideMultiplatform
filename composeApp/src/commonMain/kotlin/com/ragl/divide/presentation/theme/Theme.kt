package com.ragl.divide.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import com.ragl.divide.presentation.theme.alternate.backgroundDark
import com.ragl.divide.presentation.theme.alternate.backgroundDarkHighContrast
import com.ragl.divide.presentation.theme.alternate.backgroundDarkMediumContrast
import com.ragl.divide.presentation.theme.alternate.backgroundLight
import com.ragl.divide.presentation.theme.alternate.backgroundLightHighContrast
import com.ragl.divide.presentation.theme.alternate.backgroundLightMediumContrast
import com.ragl.divide.presentation.theme.alternate.errorContainerDark
import com.ragl.divide.presentation.theme.alternate.errorContainerDarkHighContrast
import com.ragl.divide.presentation.theme.alternate.errorContainerDarkMediumContrast
import com.ragl.divide.presentation.theme.alternate.errorContainerLight
import com.ragl.divide.presentation.theme.alternate.errorContainerLightHighContrast
import com.ragl.divide.presentation.theme.alternate.errorContainerLightMediumContrast
import com.ragl.divide.presentation.theme.alternate.errorDark
import com.ragl.divide.presentation.theme.alternate.errorDarkHighContrast
import com.ragl.divide.presentation.theme.alternate.errorDarkMediumContrast
import com.ragl.divide.presentation.theme.alternate.errorLight
import com.ragl.divide.presentation.theme.alternate.errorLightHighContrast
import com.ragl.divide.presentation.theme.alternate.errorLightMediumContrast
import com.ragl.divide.presentation.theme.alternate.inverseOnSurfaceDark
import com.ragl.divide.presentation.theme.alternate.inverseOnSurfaceDarkHighContrast
import com.ragl.divide.presentation.theme.alternate.inverseOnSurfaceDarkMediumContrast
import com.ragl.divide.presentation.theme.alternate.inverseOnSurfaceLight
import com.ragl.divide.presentation.theme.alternate.inverseOnSurfaceLightHighContrast
import com.ragl.divide.presentation.theme.alternate.inverseOnSurfaceLightMediumContrast
import com.ragl.divide.presentation.theme.alternate.inversePrimaryDark
import com.ragl.divide.presentation.theme.alternate.inversePrimaryDarkHighContrast
import com.ragl.divide.presentation.theme.alternate.inversePrimaryDarkMediumContrast
import com.ragl.divide.presentation.theme.alternate.inversePrimaryLight
import com.ragl.divide.presentation.theme.alternate.inversePrimaryLightHighContrast
import com.ragl.divide.presentation.theme.alternate.inversePrimaryLightMediumContrast
import com.ragl.divide.presentation.theme.alternate.inverseSurfaceDark
import com.ragl.divide.presentation.theme.alternate.inverseSurfaceDarkHighContrast
import com.ragl.divide.presentation.theme.alternate.inverseSurfaceDarkMediumContrast
import com.ragl.divide.presentation.theme.alternate.inverseSurfaceLight
import com.ragl.divide.presentation.theme.alternate.inverseSurfaceLightHighContrast
import com.ragl.divide.presentation.theme.alternate.inverseSurfaceLightMediumContrast
import com.ragl.divide.presentation.theme.alternate.onBackgroundDark
import com.ragl.divide.presentation.theme.alternate.onBackgroundDarkHighContrast
import com.ragl.divide.presentation.theme.alternate.onBackgroundDarkMediumContrast
import com.ragl.divide.presentation.theme.alternate.onBackgroundLight
import com.ragl.divide.presentation.theme.alternate.onBackgroundLightHighContrast
import com.ragl.divide.presentation.theme.alternate.onBackgroundLightMediumContrast
import com.ragl.divide.presentation.theme.alternate.onErrorContainerDark
import com.ragl.divide.presentation.theme.alternate.onErrorContainerDarkHighContrast
import com.ragl.divide.presentation.theme.alternate.onErrorContainerDarkMediumContrast
import com.ragl.divide.presentation.theme.alternate.onErrorContainerLight
import com.ragl.divide.presentation.theme.alternate.onErrorContainerLightHighContrast
import com.ragl.divide.presentation.theme.alternate.onErrorContainerLightMediumContrast
import com.ragl.divide.presentation.theme.alternate.onErrorDark
import com.ragl.divide.presentation.theme.alternate.onErrorDarkHighContrast
import com.ragl.divide.presentation.theme.alternate.onErrorDarkMediumContrast
import com.ragl.divide.presentation.theme.alternate.onErrorLight
import com.ragl.divide.presentation.theme.alternate.onErrorLightHighContrast
import com.ragl.divide.presentation.theme.alternate.onErrorLightMediumContrast
import com.ragl.divide.presentation.theme.alternate.onPrimaryContainerDark
import com.ragl.divide.presentation.theme.alternate.onPrimaryContainerDarkHighContrast
import com.ragl.divide.presentation.theme.alternate.onPrimaryContainerDarkMediumContrast
import com.ragl.divide.presentation.theme.alternate.onPrimaryContainerLight
import com.ragl.divide.presentation.theme.alternate.onPrimaryContainerLightHighContrast
import com.ragl.divide.presentation.theme.alternate.onPrimaryContainerLightMediumContrast
import com.ragl.divide.presentation.theme.alternate.onPrimaryDark
import com.ragl.divide.presentation.theme.alternate.onPrimaryDarkHighContrast
import com.ragl.divide.presentation.theme.alternate.onPrimaryDarkMediumContrast
import com.ragl.divide.presentation.theme.alternate.onPrimaryLight
import com.ragl.divide.presentation.theme.alternate.onPrimaryLightHighContrast
import com.ragl.divide.presentation.theme.alternate.onPrimaryLightMediumContrast
import com.ragl.divide.presentation.theme.alternate.onSecondaryContainerDark
import com.ragl.divide.presentation.theme.alternate.onSecondaryContainerDarkHighContrast
import com.ragl.divide.presentation.theme.alternate.onSecondaryContainerDarkMediumContrast
import com.ragl.divide.presentation.theme.alternate.onSecondaryContainerLight
import com.ragl.divide.presentation.theme.alternate.onSecondaryContainerLightHighContrast
import com.ragl.divide.presentation.theme.alternate.onSecondaryContainerLightMediumContrast
import com.ragl.divide.presentation.theme.alternate.onSecondaryDark
import com.ragl.divide.presentation.theme.alternate.onSecondaryDarkHighContrast
import com.ragl.divide.presentation.theme.alternate.onSecondaryDarkMediumContrast
import com.ragl.divide.presentation.theme.alternate.onSecondaryLight
import com.ragl.divide.presentation.theme.alternate.onSecondaryLightHighContrast
import com.ragl.divide.presentation.theme.alternate.onSecondaryLightMediumContrast
import com.ragl.divide.presentation.theme.alternate.onSurfaceDark
import com.ragl.divide.presentation.theme.alternate.onSurfaceDarkHighContrast
import com.ragl.divide.presentation.theme.alternate.onSurfaceDarkMediumContrast
import com.ragl.divide.presentation.theme.alternate.onSurfaceLight
import com.ragl.divide.presentation.theme.alternate.onSurfaceLightHighContrast
import com.ragl.divide.presentation.theme.alternate.onSurfaceLightMediumContrast
import com.ragl.divide.presentation.theme.alternate.onSurfaceVariantDark
import com.ragl.divide.presentation.theme.alternate.onSurfaceVariantDarkHighContrast
import com.ragl.divide.presentation.theme.alternate.onSurfaceVariantDarkMediumContrast
import com.ragl.divide.presentation.theme.alternate.onSurfaceVariantLight
import com.ragl.divide.presentation.theme.alternate.onSurfaceVariantLightHighContrast
import com.ragl.divide.presentation.theme.alternate.onSurfaceVariantLightMediumContrast
import com.ragl.divide.presentation.theme.alternate.onTertiaryContainerDark
import com.ragl.divide.presentation.theme.alternate.onTertiaryContainerDarkHighContrast
import com.ragl.divide.presentation.theme.alternate.onTertiaryContainerDarkMediumContrast
import com.ragl.divide.presentation.theme.alternate.onTertiaryContainerLight
import com.ragl.divide.presentation.theme.alternate.onTertiaryContainerLightHighContrast
import com.ragl.divide.presentation.theme.alternate.onTertiaryContainerLightMediumContrast
import com.ragl.divide.presentation.theme.alternate.onTertiaryDark
import com.ragl.divide.presentation.theme.alternate.onTertiaryDarkHighContrast
import com.ragl.divide.presentation.theme.alternate.onTertiaryDarkMediumContrast
import com.ragl.divide.presentation.theme.alternate.onTertiaryLight
import com.ragl.divide.presentation.theme.alternate.onTertiaryLightHighContrast
import com.ragl.divide.presentation.theme.alternate.onTertiaryLightMediumContrast
import com.ragl.divide.presentation.theme.alternate.outlineDark
import com.ragl.divide.presentation.theme.alternate.outlineDarkHighContrast
import com.ragl.divide.presentation.theme.alternate.outlineDarkMediumContrast
import com.ragl.divide.presentation.theme.alternate.outlineLight
import com.ragl.divide.presentation.theme.alternate.outlineLightHighContrast
import com.ragl.divide.presentation.theme.alternate.outlineLightMediumContrast
import com.ragl.divide.presentation.theme.alternate.outlineVariantDark
import com.ragl.divide.presentation.theme.alternate.outlineVariantDarkHighContrast
import com.ragl.divide.presentation.theme.alternate.outlineVariantDarkMediumContrast
import com.ragl.divide.presentation.theme.alternate.outlineVariantLight
import com.ragl.divide.presentation.theme.alternate.outlineVariantLightHighContrast
import com.ragl.divide.presentation.theme.alternate.outlineVariantLightMediumContrast
import com.ragl.divide.presentation.theme.alternate.primaryContainerDark
import com.ragl.divide.presentation.theme.alternate.primaryContainerDarkHighContrast
import com.ragl.divide.presentation.theme.alternate.primaryContainerDarkMediumContrast
import com.ragl.divide.presentation.theme.alternate.primaryContainerLight
import com.ragl.divide.presentation.theme.alternate.primaryContainerLightHighContrast
import com.ragl.divide.presentation.theme.alternate.primaryContainerLightMediumContrast
import com.ragl.divide.presentation.theme.alternate.primaryDark
import com.ragl.divide.presentation.theme.alternate.primaryDarkHighContrast
import com.ragl.divide.presentation.theme.alternate.primaryDarkMediumContrast
import com.ragl.divide.presentation.theme.alternate.primaryLight
import com.ragl.divide.presentation.theme.alternate.primaryLightHighContrast
import com.ragl.divide.presentation.theme.alternate.primaryLightMediumContrast
import com.ragl.divide.presentation.theme.alternate.scrimDark
import com.ragl.divide.presentation.theme.alternate.scrimDarkHighContrast
import com.ragl.divide.presentation.theme.alternate.scrimDarkMediumContrast
import com.ragl.divide.presentation.theme.alternate.scrimLight
import com.ragl.divide.presentation.theme.alternate.scrimLightHighContrast
import com.ragl.divide.presentation.theme.alternate.scrimLightMediumContrast
import com.ragl.divide.presentation.theme.alternate.secondaryContainerDark
import com.ragl.divide.presentation.theme.alternate.secondaryContainerDarkHighContrast
import com.ragl.divide.presentation.theme.alternate.secondaryContainerDarkMediumContrast
import com.ragl.divide.presentation.theme.alternate.secondaryContainerLight
import com.ragl.divide.presentation.theme.alternate.secondaryContainerLightHighContrast
import com.ragl.divide.presentation.theme.alternate.secondaryContainerLightMediumContrast
import com.ragl.divide.presentation.theme.alternate.secondaryDark
import com.ragl.divide.presentation.theme.alternate.secondaryDarkHighContrast
import com.ragl.divide.presentation.theme.alternate.secondaryDarkMediumContrast
import com.ragl.divide.presentation.theme.alternate.secondaryLight
import com.ragl.divide.presentation.theme.alternate.secondaryLightHighContrast
import com.ragl.divide.presentation.theme.alternate.secondaryLightMediumContrast
import com.ragl.divide.presentation.theme.alternate.surfaceBrightDark
import com.ragl.divide.presentation.theme.alternate.surfaceBrightDarkHighContrast
import com.ragl.divide.presentation.theme.alternate.surfaceBrightDarkMediumContrast
import com.ragl.divide.presentation.theme.alternate.surfaceBrightLight
import com.ragl.divide.presentation.theme.alternate.surfaceBrightLightHighContrast
import com.ragl.divide.presentation.theme.alternate.surfaceBrightLightMediumContrast
import com.ragl.divide.presentation.theme.alternate.surfaceContainerDark
import com.ragl.divide.presentation.theme.alternate.surfaceContainerDarkHighContrast
import com.ragl.divide.presentation.theme.alternate.surfaceContainerDarkMediumContrast
import com.ragl.divide.presentation.theme.alternate.surfaceContainerHighDark
import com.ragl.divide.presentation.theme.alternate.surfaceContainerHighDarkHighContrast
import com.ragl.divide.presentation.theme.alternate.surfaceContainerHighDarkMediumContrast
import com.ragl.divide.presentation.theme.alternate.surfaceContainerHighLight
import com.ragl.divide.presentation.theme.alternate.surfaceContainerHighLightHighContrast
import com.ragl.divide.presentation.theme.alternate.surfaceContainerHighLightMediumContrast
import com.ragl.divide.presentation.theme.alternate.surfaceContainerHighestDark
import com.ragl.divide.presentation.theme.alternate.surfaceContainerHighestDarkHighContrast
import com.ragl.divide.presentation.theme.alternate.surfaceContainerHighestDarkMediumContrast
import com.ragl.divide.presentation.theme.alternate.surfaceContainerHighestLight
import com.ragl.divide.presentation.theme.alternate.surfaceContainerHighestLightHighContrast
import com.ragl.divide.presentation.theme.alternate.surfaceContainerHighestLightMediumContrast
import com.ragl.divide.presentation.theme.alternate.surfaceContainerLight
import com.ragl.divide.presentation.theme.alternate.surfaceContainerLightHighContrast
import com.ragl.divide.presentation.theme.alternate.surfaceContainerLightMediumContrast
import com.ragl.divide.presentation.theme.alternate.surfaceContainerLowDark
import com.ragl.divide.presentation.theme.alternate.surfaceContainerLowDarkHighContrast
import com.ragl.divide.presentation.theme.alternate.surfaceContainerLowDarkMediumContrast
import com.ragl.divide.presentation.theme.alternate.surfaceContainerLowLight
import com.ragl.divide.presentation.theme.alternate.surfaceContainerLowLightHighContrast
import com.ragl.divide.presentation.theme.alternate.surfaceContainerLowLightMediumContrast
import com.ragl.divide.presentation.theme.alternate.surfaceContainerLowestDark
import com.ragl.divide.presentation.theme.alternate.surfaceContainerLowestDarkHighContrast
import com.ragl.divide.presentation.theme.alternate.surfaceContainerLowestDarkMediumContrast
import com.ragl.divide.presentation.theme.alternate.surfaceContainerLowestLight
import com.ragl.divide.presentation.theme.alternate.surfaceContainerLowestLightHighContrast
import com.ragl.divide.presentation.theme.alternate.surfaceContainerLowestLightMediumContrast
import com.ragl.divide.presentation.theme.alternate.surfaceDark
import com.ragl.divide.presentation.theme.alternate.surfaceDarkHighContrast
import com.ragl.divide.presentation.theme.alternate.surfaceDarkMediumContrast
import com.ragl.divide.presentation.theme.alternate.surfaceDimDark
import com.ragl.divide.presentation.theme.alternate.surfaceDimDarkHighContrast
import com.ragl.divide.presentation.theme.alternate.surfaceDimDarkMediumContrast
import com.ragl.divide.presentation.theme.alternate.surfaceDimLight
import com.ragl.divide.presentation.theme.alternate.surfaceDimLightHighContrast
import com.ragl.divide.presentation.theme.alternate.surfaceDimLightMediumContrast
import com.ragl.divide.presentation.theme.alternate.surfaceLight
import com.ragl.divide.presentation.theme.alternate.surfaceLightHighContrast
import com.ragl.divide.presentation.theme.alternate.surfaceLightMediumContrast
import com.ragl.divide.presentation.theme.alternate.surfaceVariantDark
import com.ragl.divide.presentation.theme.alternate.surfaceVariantDarkHighContrast
import com.ragl.divide.presentation.theme.alternate.surfaceVariantDarkMediumContrast
import com.ragl.divide.presentation.theme.alternate.surfaceVariantLight
import com.ragl.divide.presentation.theme.alternate.surfaceVariantLightHighContrast
import com.ragl.divide.presentation.theme.alternate.surfaceVariantLightMediumContrast
import com.ragl.divide.presentation.theme.alternate.tertiaryContainerDark
import com.ragl.divide.presentation.theme.alternate.tertiaryContainerDarkHighContrast
import com.ragl.divide.presentation.theme.alternate.tertiaryContainerDarkMediumContrast
import com.ragl.divide.presentation.theme.alternate.tertiaryContainerLight
import com.ragl.divide.presentation.theme.alternate.tertiaryContainerLightHighContrast
import com.ragl.divide.presentation.theme.alternate.tertiaryContainerLightMediumContrast
import com.ragl.divide.presentation.theme.alternate.tertiaryDark
import com.ragl.divide.presentation.theme.alternate.tertiaryDarkHighContrast
import com.ragl.divide.presentation.theme.alternate.tertiaryDarkMediumContrast
import com.ragl.divide.presentation.theme.alternate.tertiaryLight
import com.ragl.divide.presentation.theme.alternate.tertiaryLightHighContrast
import com.ragl.divide.presentation.theme.alternate.tertiaryLightMediumContrast

private val lightScheme = lightColorScheme(
    primary = primaryLight,
    onPrimary = onPrimaryLight,
    primaryContainer = primaryContainerLight,
    onPrimaryContainer = onPrimaryContainerLight,
    secondary = secondaryLight,
    onSecondary = onSecondaryLight,
    secondaryContainer = secondaryContainerLight,
    onSecondaryContainer = onSecondaryContainerLight,
    tertiary = tertiaryLight,
    onTertiary = onTertiaryLight,
    tertiaryContainer = tertiaryContainerLight,
    onTertiaryContainer = onTertiaryContainerLight,
    error = errorLight,
    onError = onErrorLight,
    errorContainer = errorContainerLight,
    onErrorContainer = onErrorContainerLight,
    background = backgroundLight,
    onBackground = onBackgroundLight,
    surface = surfaceLight,
    onSurface = onSurfaceLight,
    surfaceVariant = surfaceVariantLight,
    onSurfaceVariant = onSurfaceVariantLight,
    outline = outlineLight,
    outlineVariant = outlineVariantLight,
    scrim = scrimLight,
    inverseSurface = inverseSurfaceLight,
    inverseOnSurface = inverseOnSurfaceLight,
    inversePrimary = inversePrimaryLight,
    surfaceDim = surfaceDimLight,
    surfaceBright = surfaceBrightLight,
    surfaceContainerLowest = surfaceContainerLowestLight,
    surfaceContainerLow = surfaceContainerLowLight,
    surfaceContainer = surfaceContainerLight,
    surfaceContainerHigh = surfaceContainerHighLight,
    surfaceContainerHighest = surfaceContainerHighestLight,
)

private val darkScheme = darkColorScheme(
    primary = primaryDark,
    onPrimary = onPrimaryDark,
    primaryContainer = primaryContainerDark,
    onPrimaryContainer = onPrimaryContainerDark,
    secondary = secondaryDark,
    onSecondary = onSecondaryDark,
    secondaryContainer = secondaryContainerDark,
    onSecondaryContainer = onSecondaryContainerDark,
    tertiary = tertiaryDark,
    onTertiary = onTertiaryDark,
    tertiaryContainer = tertiaryContainerDark,
    onTertiaryContainer = onTertiaryContainerDark,
    error = errorDark,
    onError = onErrorDark,
    errorContainer = errorContainerDark,
    onErrorContainer = onErrorContainerDark,
    background = backgroundDark,
    onBackground = onBackgroundDark,
    surface = surfaceDark,
    onSurface = onSurfaceDark,
    surfaceVariant = surfaceVariantDark,
    onSurfaceVariant = onSurfaceVariantDark,
    outline = outlineDark,
    outlineVariant = outlineVariantDark,
    scrim = scrimDark,
    inverseSurface = inverseSurfaceDark,
    inverseOnSurface = inverseOnSurfaceDark,
    inversePrimary = inversePrimaryDark,
    surfaceDim = surfaceDimDark,
    surfaceBright = surfaceBrightDark,
    surfaceContainerLowest = surfaceContainerLowestDark,
    surfaceContainerLow = surfaceContainerLowDark,
    surfaceContainer = surfaceContainerDark,
    surfaceContainerHigh = surfaceContainerHighDark,
    surfaceContainerHighest = surfaceContainerHighestDark,
)

private val mediumContrastLightColorScheme = lightColorScheme(
    primary = primaryLightMediumContrast,
    onPrimary = onPrimaryLightMediumContrast,
    primaryContainer = primaryContainerLightMediumContrast,
    onPrimaryContainer = onPrimaryContainerLightMediumContrast,
    secondary = secondaryLightMediumContrast,
    onSecondary = onSecondaryLightMediumContrast,
    secondaryContainer = secondaryContainerLightMediumContrast,
    onSecondaryContainer = onSecondaryContainerLightMediumContrast,
    tertiary = tertiaryLightMediumContrast,
    onTertiary = onTertiaryLightMediumContrast,
    tertiaryContainer = tertiaryContainerLightMediumContrast,
    onTertiaryContainer = onTertiaryContainerLightMediumContrast,
    error = errorLightMediumContrast,
    onError = onErrorLightMediumContrast,
    errorContainer = errorContainerLightMediumContrast,
    onErrorContainer = onErrorContainerLightMediumContrast,
    background = backgroundLightMediumContrast,
    onBackground = onBackgroundLightMediumContrast,
    surface = surfaceLightMediumContrast,
    onSurface = onSurfaceLightMediumContrast,
    surfaceVariant = surfaceVariantLightMediumContrast,
    onSurfaceVariant = onSurfaceVariantLightMediumContrast,
    outline = outlineLightMediumContrast,
    outlineVariant = outlineVariantLightMediumContrast,
    scrim = scrimLightMediumContrast,
    inverseSurface = inverseSurfaceLightMediumContrast,
    inverseOnSurface = inverseOnSurfaceLightMediumContrast,
    inversePrimary = inversePrimaryLightMediumContrast,
    surfaceDim = surfaceDimLightMediumContrast,
    surfaceBright = surfaceBrightLightMediumContrast,
    surfaceContainerLowest = surfaceContainerLowestLightMediumContrast,
    surfaceContainerLow = surfaceContainerLowLightMediumContrast,
    surfaceContainer = surfaceContainerLightMediumContrast,
    surfaceContainerHigh = surfaceContainerHighLightMediumContrast,
    surfaceContainerHighest = surfaceContainerHighestLightMediumContrast,
)

private val highContrastLightColorScheme = lightColorScheme(
    primary = primaryLightHighContrast,
    onPrimary = onPrimaryLightHighContrast,
    primaryContainer = primaryContainerLightHighContrast,
    onPrimaryContainer = onPrimaryContainerLightHighContrast,
    secondary = secondaryLightHighContrast,
    onSecondary = onSecondaryLightHighContrast,
    secondaryContainer = secondaryContainerLightHighContrast,
    onSecondaryContainer = onSecondaryContainerLightHighContrast,
    tertiary = tertiaryLightHighContrast,
    onTertiary = onTertiaryLightHighContrast,
    tertiaryContainer = tertiaryContainerLightHighContrast,
    onTertiaryContainer = onTertiaryContainerLightHighContrast,
    error = errorLightHighContrast,
    onError = onErrorLightHighContrast,
    errorContainer = errorContainerLightHighContrast,
    onErrorContainer = onErrorContainerLightHighContrast,
    background = backgroundLightHighContrast,
    onBackground = onBackgroundLightHighContrast,
    surface = surfaceLightHighContrast,
    onSurface = onSurfaceLightHighContrast,
    surfaceVariant = surfaceVariantLightHighContrast,
    onSurfaceVariant = onSurfaceVariantLightHighContrast,
    outline = outlineLightHighContrast,
    outlineVariant = outlineVariantLightHighContrast,
    scrim = scrimLightHighContrast,
    inverseSurface = inverseSurfaceLightHighContrast,
    inverseOnSurface = inverseOnSurfaceLightHighContrast,
    inversePrimary = inversePrimaryLightHighContrast,
    surfaceDim = surfaceDimLightHighContrast,
    surfaceBright = surfaceBrightLightHighContrast,
    surfaceContainerLowest = surfaceContainerLowestLightHighContrast,
    surfaceContainerLow = surfaceContainerLowLightHighContrast,
    surfaceContainer = surfaceContainerLightHighContrast,
    surfaceContainerHigh = surfaceContainerHighLightHighContrast,
    surfaceContainerHighest = surfaceContainerHighestLightHighContrast,
)

private val mediumContrastDarkColorScheme = darkColorScheme(
    primary = primaryDarkMediumContrast,
    onPrimary = onPrimaryDarkMediumContrast,
    primaryContainer = primaryContainerDarkMediumContrast,
    onPrimaryContainer = onPrimaryContainerDarkMediumContrast,
    secondary = secondaryDarkMediumContrast,
    onSecondary = onSecondaryDarkMediumContrast,
    secondaryContainer = secondaryContainerDarkMediumContrast,
    onSecondaryContainer = onSecondaryContainerDarkMediumContrast,
    tertiary = tertiaryDarkMediumContrast,
    onTertiary = onTertiaryDarkMediumContrast,
    tertiaryContainer = tertiaryContainerDarkMediumContrast,
    onTertiaryContainer = onTertiaryContainerDarkMediumContrast,
    error = errorDarkMediumContrast,
    onError = onErrorDarkMediumContrast,
    errorContainer = errorContainerDarkMediumContrast,
    onErrorContainer = onErrorContainerDarkMediumContrast,
    background = backgroundDarkMediumContrast,
    onBackground = onBackgroundDarkMediumContrast,
    surface = surfaceDarkMediumContrast,
    onSurface = onSurfaceDarkMediumContrast,
    surfaceVariant = surfaceVariantDarkMediumContrast,
    onSurfaceVariant = onSurfaceVariantDarkMediumContrast,
    outline = outlineDarkMediumContrast,
    outlineVariant = outlineVariantDarkMediumContrast,
    scrim = scrimDarkMediumContrast,
    inverseSurface = inverseSurfaceDarkMediumContrast,
    inverseOnSurface = inverseOnSurfaceDarkMediumContrast,
    inversePrimary = inversePrimaryDarkMediumContrast,
    surfaceDim = surfaceDimDarkMediumContrast,
    surfaceBright = surfaceBrightDarkMediumContrast,
    surfaceContainerLowest = surfaceContainerLowestDarkMediumContrast,
    surfaceContainerLow = surfaceContainerLowDarkMediumContrast,
    surfaceContainer = surfaceContainerDarkMediumContrast,
    surfaceContainerHigh = surfaceContainerHighDarkMediumContrast,
    surfaceContainerHighest = surfaceContainerHighestDarkMediumContrast,
)

private val highContrastDarkColorScheme = darkColorScheme(
    primary = primaryDarkHighContrast,
    onPrimary = onPrimaryDarkHighContrast,
    primaryContainer = primaryContainerDarkHighContrast,
    onPrimaryContainer = onPrimaryContainerDarkHighContrast,
    secondary = secondaryDarkHighContrast,
    onSecondary = onSecondaryDarkHighContrast,
    secondaryContainer = secondaryContainerDarkHighContrast,
    onSecondaryContainer = onSecondaryContainerDarkHighContrast,
    tertiary = tertiaryDarkHighContrast,
    onTertiary = onTertiaryDarkHighContrast,
    tertiaryContainer = tertiaryContainerDarkHighContrast,
    onTertiaryContainer = onTertiaryContainerDarkHighContrast,
    error = errorDarkHighContrast,
    onError = onErrorDarkHighContrast,
    errorContainer = errorContainerDarkHighContrast,
    onErrorContainer = onErrorContainerDarkHighContrast,
    background = backgroundDarkHighContrast,
    onBackground = onBackgroundDarkHighContrast,
    surface = surfaceDarkHighContrast,
    onSurface = onSurfaceDarkHighContrast,
    surfaceVariant = surfaceVariantDarkHighContrast,
    onSurfaceVariant = onSurfaceVariantDarkHighContrast,
    outline = outlineDarkHighContrast,
    outlineVariant = outlineVariantDarkHighContrast,
    scrim = scrimDarkHighContrast,
    inverseSurface = inverseSurfaceDarkHighContrast,
    inverseOnSurface = inverseOnSurfaceDarkHighContrast,
    inversePrimary = inversePrimaryDarkHighContrast,
    surfaceDim = surfaceDimDarkHighContrast,
    surfaceBright = surfaceBrightDarkHighContrast,
    surfaceContainerLowest = surfaceContainerLowestDarkHighContrast,
    surfaceContainerLow = surfaceContainerLowDarkHighContrast,
    surfaceContainer = surfaceContainerDarkHighContrast,
    surfaceContainerHigh = surfaceContainerHighDarkHighContrast,
    surfaceContainerHighest = surfaceContainerHighestDarkHighContrast,
)

@Composable
fun DivideTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> darkScheme
        else -> lightScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = DivideFontFamily(),
        content = content
    )
}

