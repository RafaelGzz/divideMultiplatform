package com.ragl.divide.data.models

import dividemultiplatform.composeapp.generated.resources.*
import org.jetbrains.compose.resources.StringResource

enum class Method(val resId: StringResource) {
    EQUALLY(Res.string.equally),
    PERCENTAGES(Res.string.percentages),
    CUSTOM(Res.string.custom)
}