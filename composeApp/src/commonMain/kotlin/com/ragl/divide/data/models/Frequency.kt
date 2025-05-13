package com.ragl.divide.data.models

import dividemultiplatform.composeapp.generated.resources.*
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.StringResource

@Serializable
enum class Frequency(val resId: StringResource) {
    ONCE(Res.string.once),
    DAILY(Res.string.daily),
    WEEKLY(Res.string.weekly),
    BIWEEKLY(Res.string.biweekly),
    MONTHLY(Res.string.monthly),
    BIMONTHLY(Res.string.bimonthly),
    QUARTERLY(Res.string.quarterly),
    SEMIANNUALLY(Res.string.semiannually),
    ANNUALLY(Res.string.annually)
}

fun Frequency.getInMillis(): Long{
    return when(this){
        Frequency.ONCE -> 0L
        Frequency.DAILY -> 86400000L
        Frequency.WEEKLY -> 604800000L
        Frequency.BIWEEKLY -> 1210000000L
        Frequency.MONTHLY -> 2592000000L
        Frequency.BIMONTHLY -> 5184000000L
        Frequency.QUARTERLY -> 7776000000L
        Frequency.SEMIANNUALLY -> 15552000000L
        Frequency.ANNUALLY -> 31104000000L
    }
}