package com.ragl.divide.presentation.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import dividemultiplatform.composeapp.generated.resources.*
import org.jetbrains.compose.resources.Font

@Composable
fun DisplayFontFamily() = FontFamily(
    Font(Res.font.grtskpeta_regular, FontWeight.Normal),
    Font(Res.font.grtskpeta_thin, FontWeight.Thin),
    Font(Res.font.grtskpeta_thinitalic, FontWeight.Thin, FontStyle.Italic),
    Font(Res.font.grtskpeta_italic, FontWeight.Normal, FontStyle.Italic),
    Font(Res.font.grtskpeta_bold, FontWeight.Bold),
    Font(Res.font.grtskpeta_bolditalic, FontWeight.Bold, FontStyle.Italic),
    Font(Res.font.grtskpeta_semibold, FontWeight.SemiBold),
    Font(Res.font.grtskpeta_semibolditalic, FontWeight.SemiBold, FontStyle.Italic),
    Font(Res.font.grtskpeta_light, FontWeight.Light),
    Font(Res.font.grtskpeta_lightitalic, FontWeight.Light, FontStyle.Italic),
    Font(Res.font.grtskpeta_mediumitalic, FontWeight.Medium, FontStyle.Italic),
    Font(Res.font.grtskpeta_medium, FontWeight.Medium),
    Font(Res.font.grtskpeta_extralightitalic, FontWeight.ExtraLight, FontStyle.Italic),
    Font(Res.font.grtskpeta_extralight, FontWeight.ExtraLight)
)

// Default Material 3 typography values
@Composable
fun DivideFontFamily() = Typography().run {
    val fontFamily = DisplayFontFamily()
    copy(
        displayLarge = displayLarge.copy(fontFamily = fontFamily),
        displayMedium = displayMedium.copy(fontFamily = fontFamily),
        displaySmall = displaySmall.copy(fontFamily = fontFamily),
        headlineLarge = headlineLarge.copy(fontFamily = fontFamily),
        headlineMedium = headlineMedium.copy(fontFamily = fontFamily),
        headlineSmall = headlineSmall.copy(fontFamily = fontFamily),
        titleLarge = titleLarge.copy(fontFamily = fontFamily),
        titleMedium = titleMedium.copy(fontFamily = fontFamily),
        titleSmall = titleSmall.copy(fontFamily = fontFamily),

        bodyLarge = bodyLarge.copy(fontFamily = fontFamily),
        bodyMedium = bodyMedium.copy(fontFamily = fontFamily),
        bodySmall = bodySmall.copy(fontFamily = fontFamily),
        labelLarge = labelLarge.copy(fontFamily = fontFamily),
        labelMedium = labelMedium.copy(fontFamily = fontFamily),
        labelSmall = labelSmall.copy(fontFamily = fontFamily),
    )

}