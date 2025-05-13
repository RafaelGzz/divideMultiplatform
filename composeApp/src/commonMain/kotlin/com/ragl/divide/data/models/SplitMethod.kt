package com.ragl.divide.data.models

import dividemultiplatform.composeapp.generated.resources.Res
import dividemultiplatform.composeapp.generated.resources.custom
import dividemultiplatform.composeapp.generated.resources.equally
import dividemultiplatform.composeapp.generated.resources.percentages
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.StringResource

@Serializable
enum class SplitMethod(val resId: StringResource) {
    EQUALLY(Res.string.equally),
    PERCENTAGES(Res.string.percentages),
    CUSTOM(Res.string.custom)
}