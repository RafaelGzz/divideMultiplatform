package com.ragl.divide.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import com.ragl.divide.ui.theme.alternate.backgroundDark
import com.ragl.divide.ui.theme.alternate.backgroundDarkHighContrast
import com.ragl.divide.ui.theme.alternate.backgroundDarkMediumContrast
import com.ragl.divide.ui.theme.alternate.backgroundLight
import com.ragl.divide.ui.theme.alternate.backgroundLightHighContrast
import com.ragl.divide.ui.theme.alternate.backgroundLightMediumContrast
import com.ragl.divide.ui.theme.alternate.errorContainerDark
import com.ragl.divide.ui.theme.alternate.errorContainerDarkHighContrast
import com.ragl.divide.ui.theme.alternate.errorContainerDarkMediumContrast
import com.ragl.divide.ui.theme.alternate.errorContainerLight
import com.ragl.divide.ui.theme.alternate.errorContainerLightHighContrast
import com.ragl.divide.ui.theme.alternate.errorContainerLightMediumContrast
import com.ragl.divide.ui.theme.alternate.errorDark
import com.ragl.divide.ui.theme.alternate.errorDarkHighContrast
import com.ragl.divide.ui.theme.alternate.errorDarkMediumContrast
import com.ragl.divide.ui.theme.alternate.errorLight
import com.ragl.divide.ui.theme.alternate.errorLightHighContrast
import com.ragl.divide.ui.theme.alternate.errorLightMediumContrast
import com.ragl.divide.ui.theme.alternate.inverseOnSurfaceDark
import com.ragl.divide.ui.theme.alternate.inverseOnSurfaceDarkHighContrast
import com.ragl.divide.ui.theme.alternate.inverseOnSurfaceDarkMediumContrast
import com.ragl.divide.ui.theme.alternate.inverseOnSurfaceLight
import com.ragl.divide.ui.theme.alternate.inverseOnSurfaceLightHighContrast
import com.ragl.divide.ui.theme.alternate.inverseOnSurfaceLightMediumContrast
import com.ragl.divide.ui.theme.alternate.inversePrimaryDark
import com.ragl.divide.ui.theme.alternate.inversePrimaryDarkHighContrast
import com.ragl.divide.ui.theme.alternate.inversePrimaryDarkMediumContrast
import com.ragl.divide.ui.theme.alternate.inversePrimaryLight
import com.ragl.divide.ui.theme.alternate.inversePrimaryLightHighContrast
import com.ragl.divide.ui.theme.alternate.inversePrimaryLightMediumContrast
import com.ragl.divide.ui.theme.alternate.inverseSurfaceDark
import com.ragl.divide.ui.theme.alternate.inverseSurfaceDarkHighContrast
import com.ragl.divide.ui.theme.alternate.inverseSurfaceDarkMediumContrast
import com.ragl.divide.ui.theme.alternate.inverseSurfaceLight
import com.ragl.divide.ui.theme.alternate.inverseSurfaceLightHighContrast
import com.ragl.divide.ui.theme.alternate.inverseSurfaceLightMediumContrast
import com.ragl.divide.ui.theme.alternate.onBackgroundDark
import com.ragl.divide.ui.theme.alternate.onBackgroundDarkHighContrast
import com.ragl.divide.ui.theme.alternate.onBackgroundDarkMediumContrast
import com.ragl.divide.ui.theme.alternate.onBackgroundLight
import com.ragl.divide.ui.theme.alternate.onBackgroundLightHighContrast
import com.ragl.divide.ui.theme.alternate.onBackgroundLightMediumContrast
import com.ragl.divide.ui.theme.alternate.onErrorContainerDark
import com.ragl.divide.ui.theme.alternate.onErrorContainerDarkHighContrast
import com.ragl.divide.ui.theme.alternate.onErrorContainerDarkMediumContrast
import com.ragl.divide.ui.theme.alternate.onErrorContainerLight
import com.ragl.divide.ui.theme.alternate.onErrorContainerLightHighContrast
import com.ragl.divide.ui.theme.alternate.onErrorContainerLightMediumContrast
import com.ragl.divide.ui.theme.alternate.onErrorDark
import com.ragl.divide.ui.theme.alternate.onErrorDarkHighContrast
import com.ragl.divide.ui.theme.alternate.onErrorDarkMediumContrast
import com.ragl.divide.ui.theme.alternate.onErrorLight
import com.ragl.divide.ui.theme.alternate.onErrorLightHighContrast
import com.ragl.divide.ui.theme.alternate.onErrorLightMediumContrast
import com.ragl.divide.ui.theme.alternate.onPrimaryContainerDark
import com.ragl.divide.ui.theme.alternate.onPrimaryContainerDarkHighContrast
import com.ragl.divide.ui.theme.alternate.onPrimaryContainerDarkMediumContrast
import com.ragl.divide.ui.theme.alternate.onPrimaryContainerLight
import com.ragl.divide.ui.theme.alternate.onPrimaryContainerLightHighContrast
import com.ragl.divide.ui.theme.alternate.onPrimaryContainerLightMediumContrast
import com.ragl.divide.ui.theme.alternate.onPrimaryDark
import com.ragl.divide.ui.theme.alternate.onPrimaryDarkHighContrast
import com.ragl.divide.ui.theme.alternate.onPrimaryDarkMediumContrast
import com.ragl.divide.ui.theme.alternate.onPrimaryLight
import com.ragl.divide.ui.theme.alternate.onPrimaryLightHighContrast
import com.ragl.divide.ui.theme.alternate.onPrimaryLightMediumContrast
import com.ragl.divide.ui.theme.alternate.onSecondaryContainerDark
import com.ragl.divide.ui.theme.alternate.onSecondaryContainerDarkHighContrast
import com.ragl.divide.ui.theme.alternate.onSecondaryContainerDarkMediumContrast
import com.ragl.divide.ui.theme.alternate.onSecondaryContainerLight
import com.ragl.divide.ui.theme.alternate.onSecondaryContainerLightHighContrast
import com.ragl.divide.ui.theme.alternate.onSecondaryContainerLightMediumContrast
import com.ragl.divide.ui.theme.alternate.onSecondaryDark
import com.ragl.divide.ui.theme.alternate.onSecondaryDarkHighContrast
import com.ragl.divide.ui.theme.alternate.onSecondaryDarkMediumContrast
import com.ragl.divide.ui.theme.alternate.onSecondaryLight
import com.ragl.divide.ui.theme.alternate.onSecondaryLightHighContrast
import com.ragl.divide.ui.theme.alternate.onSecondaryLightMediumContrast
import com.ragl.divide.ui.theme.alternate.onSurfaceDark
import com.ragl.divide.ui.theme.alternate.onSurfaceDarkHighContrast
import com.ragl.divide.ui.theme.alternate.onSurfaceDarkMediumContrast
import com.ragl.divide.ui.theme.alternate.onSurfaceLight
import com.ragl.divide.ui.theme.alternate.onSurfaceLightHighContrast
import com.ragl.divide.ui.theme.alternate.onSurfaceLightMediumContrast
import com.ragl.divide.ui.theme.alternate.onSurfaceVariantDark
import com.ragl.divide.ui.theme.alternate.onSurfaceVariantDarkHighContrast
import com.ragl.divide.ui.theme.alternate.onSurfaceVariantDarkMediumContrast
import com.ragl.divide.ui.theme.alternate.onSurfaceVariantLight
import com.ragl.divide.ui.theme.alternate.onSurfaceVariantLightHighContrast
import com.ragl.divide.ui.theme.alternate.onSurfaceVariantLightMediumContrast
import com.ragl.divide.ui.theme.alternate.onTertiaryContainerDark
import com.ragl.divide.ui.theme.alternate.onTertiaryContainerDarkHighContrast
import com.ragl.divide.ui.theme.alternate.onTertiaryContainerDarkMediumContrast
import com.ragl.divide.ui.theme.alternate.onTertiaryContainerLight
import com.ragl.divide.ui.theme.alternate.onTertiaryContainerLightHighContrast
import com.ragl.divide.ui.theme.alternate.onTertiaryContainerLightMediumContrast
import com.ragl.divide.ui.theme.alternate.onTertiaryDark
import com.ragl.divide.ui.theme.alternate.onTertiaryDarkHighContrast
import com.ragl.divide.ui.theme.alternate.onTertiaryDarkMediumContrast
import com.ragl.divide.ui.theme.alternate.onTertiaryLight
import com.ragl.divide.ui.theme.alternate.onTertiaryLightHighContrast
import com.ragl.divide.ui.theme.alternate.onTertiaryLightMediumContrast
import com.ragl.divide.ui.theme.alternate.outlineDark
import com.ragl.divide.ui.theme.alternate.outlineDarkHighContrast
import com.ragl.divide.ui.theme.alternate.outlineDarkMediumContrast
import com.ragl.divide.ui.theme.alternate.outlineLight
import com.ragl.divide.ui.theme.alternate.outlineLightHighContrast
import com.ragl.divide.ui.theme.alternate.outlineLightMediumContrast
import com.ragl.divide.ui.theme.alternate.outlineVariantDark
import com.ragl.divide.ui.theme.alternate.outlineVariantDarkHighContrast
import com.ragl.divide.ui.theme.alternate.outlineVariantDarkMediumContrast
import com.ragl.divide.ui.theme.alternate.outlineVariantLight
import com.ragl.divide.ui.theme.alternate.outlineVariantLightHighContrast
import com.ragl.divide.ui.theme.alternate.outlineVariantLightMediumContrast
import com.ragl.divide.ui.theme.alternate.primaryContainerDark
import com.ragl.divide.ui.theme.alternate.primaryContainerDarkHighContrast
import com.ragl.divide.ui.theme.alternate.primaryContainerDarkMediumContrast
import com.ragl.divide.ui.theme.alternate.primaryContainerLight
import com.ragl.divide.ui.theme.alternate.primaryContainerLightHighContrast
import com.ragl.divide.ui.theme.alternate.primaryContainerLightMediumContrast
import com.ragl.divide.ui.theme.alternate.primaryDark
import com.ragl.divide.ui.theme.alternate.primaryDarkHighContrast
import com.ragl.divide.ui.theme.alternate.primaryDarkMediumContrast
import com.ragl.divide.ui.theme.alternate.primaryLight
import com.ragl.divide.ui.theme.alternate.primaryLightHighContrast
import com.ragl.divide.ui.theme.alternate.primaryLightMediumContrast
import com.ragl.divide.ui.theme.alternate.scrimDark
import com.ragl.divide.ui.theme.alternate.scrimDarkHighContrast
import com.ragl.divide.ui.theme.alternate.scrimDarkMediumContrast
import com.ragl.divide.ui.theme.alternate.scrimLight
import com.ragl.divide.ui.theme.alternate.scrimLightHighContrast
import com.ragl.divide.ui.theme.alternate.scrimLightMediumContrast
import com.ragl.divide.ui.theme.alternate.secondaryContainerDark
import com.ragl.divide.ui.theme.alternate.secondaryContainerDarkHighContrast
import com.ragl.divide.ui.theme.alternate.secondaryContainerDarkMediumContrast
import com.ragl.divide.ui.theme.alternate.secondaryContainerLight
import com.ragl.divide.ui.theme.alternate.secondaryContainerLightHighContrast
import com.ragl.divide.ui.theme.alternate.secondaryContainerLightMediumContrast
import com.ragl.divide.ui.theme.alternate.secondaryDark
import com.ragl.divide.ui.theme.alternate.secondaryDarkHighContrast
import com.ragl.divide.ui.theme.alternate.secondaryDarkMediumContrast
import com.ragl.divide.ui.theme.alternate.secondaryLight
import com.ragl.divide.ui.theme.alternate.secondaryLightHighContrast
import com.ragl.divide.ui.theme.alternate.secondaryLightMediumContrast
import com.ragl.divide.ui.theme.alternate.surfaceBrightDark
import com.ragl.divide.ui.theme.alternate.surfaceBrightDarkHighContrast
import com.ragl.divide.ui.theme.alternate.surfaceBrightDarkMediumContrast
import com.ragl.divide.ui.theme.alternate.surfaceBrightLight
import com.ragl.divide.ui.theme.alternate.surfaceBrightLightHighContrast
import com.ragl.divide.ui.theme.alternate.surfaceBrightLightMediumContrast
import com.ragl.divide.ui.theme.alternate.surfaceContainerDark
import com.ragl.divide.ui.theme.alternate.surfaceContainerDarkHighContrast
import com.ragl.divide.ui.theme.alternate.surfaceContainerDarkMediumContrast
import com.ragl.divide.ui.theme.alternate.surfaceContainerHighDark
import com.ragl.divide.ui.theme.alternate.surfaceContainerHighDarkHighContrast
import com.ragl.divide.ui.theme.alternate.surfaceContainerHighDarkMediumContrast
import com.ragl.divide.ui.theme.alternate.surfaceContainerHighLight
import com.ragl.divide.ui.theme.alternate.surfaceContainerHighLightHighContrast
import com.ragl.divide.ui.theme.alternate.surfaceContainerHighLightMediumContrast
import com.ragl.divide.ui.theme.alternate.surfaceContainerHighestDark
import com.ragl.divide.ui.theme.alternate.surfaceContainerHighestDarkHighContrast
import com.ragl.divide.ui.theme.alternate.surfaceContainerHighestDarkMediumContrast
import com.ragl.divide.ui.theme.alternate.surfaceContainerHighestLight
import com.ragl.divide.ui.theme.alternate.surfaceContainerHighestLightHighContrast
import com.ragl.divide.ui.theme.alternate.surfaceContainerHighestLightMediumContrast
import com.ragl.divide.ui.theme.alternate.surfaceContainerLight
import com.ragl.divide.ui.theme.alternate.surfaceContainerLightHighContrast
import com.ragl.divide.ui.theme.alternate.surfaceContainerLightMediumContrast
import com.ragl.divide.ui.theme.alternate.surfaceContainerLowDark
import com.ragl.divide.ui.theme.alternate.surfaceContainerLowDarkHighContrast
import com.ragl.divide.ui.theme.alternate.surfaceContainerLowDarkMediumContrast
import com.ragl.divide.ui.theme.alternate.surfaceContainerLowLight
import com.ragl.divide.ui.theme.alternate.surfaceContainerLowLightHighContrast
import com.ragl.divide.ui.theme.alternate.surfaceContainerLowLightMediumContrast
import com.ragl.divide.ui.theme.alternate.surfaceContainerLowestDark
import com.ragl.divide.ui.theme.alternate.surfaceContainerLowestDarkHighContrast
import com.ragl.divide.ui.theme.alternate.surfaceContainerLowestDarkMediumContrast
import com.ragl.divide.ui.theme.alternate.surfaceContainerLowestLight
import com.ragl.divide.ui.theme.alternate.surfaceContainerLowestLightHighContrast
import com.ragl.divide.ui.theme.alternate.surfaceContainerLowestLightMediumContrast
import com.ragl.divide.ui.theme.alternate.surfaceDark
import com.ragl.divide.ui.theme.alternate.surfaceDarkHighContrast
import com.ragl.divide.ui.theme.alternate.surfaceDarkMediumContrast
import com.ragl.divide.ui.theme.alternate.surfaceDimDark
import com.ragl.divide.ui.theme.alternate.surfaceDimDarkHighContrast
import com.ragl.divide.ui.theme.alternate.surfaceDimDarkMediumContrast
import com.ragl.divide.ui.theme.alternate.surfaceDimLight
import com.ragl.divide.ui.theme.alternate.surfaceDimLightHighContrast
import com.ragl.divide.ui.theme.alternate.surfaceDimLightMediumContrast
import com.ragl.divide.ui.theme.alternate.surfaceLight
import com.ragl.divide.ui.theme.alternate.surfaceLightHighContrast
import com.ragl.divide.ui.theme.alternate.surfaceLightMediumContrast
import com.ragl.divide.ui.theme.alternate.surfaceVariantDark
import com.ragl.divide.ui.theme.alternate.surfaceVariantDarkHighContrast
import com.ragl.divide.ui.theme.alternate.surfaceVariantDarkMediumContrast
import com.ragl.divide.ui.theme.alternate.surfaceVariantLight
import com.ragl.divide.ui.theme.alternate.surfaceVariantLightHighContrast
import com.ragl.divide.ui.theme.alternate.surfaceVariantLightMediumContrast
import com.ragl.divide.ui.theme.alternate.tertiaryContainerDark
import com.ragl.divide.ui.theme.alternate.tertiaryContainerDarkHighContrast
import com.ragl.divide.ui.theme.alternate.tertiaryContainerDarkMediumContrast
import com.ragl.divide.ui.theme.alternate.tertiaryContainerLight
import com.ragl.divide.ui.theme.alternate.tertiaryContainerLightHighContrast
import com.ragl.divide.ui.theme.alternate.tertiaryContainerLightMediumContrast
import com.ragl.divide.ui.theme.alternate.tertiaryDark
import com.ragl.divide.ui.theme.alternate.tertiaryDarkHighContrast
import com.ragl.divide.ui.theme.alternate.tertiaryDarkMediumContrast
import com.ragl.divide.ui.theme.alternate.tertiaryLight
import com.ragl.divide.ui.theme.alternate.tertiaryLightHighContrast
import com.ragl.divide.ui.theme.alternate.tertiaryLightMediumContrast

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

