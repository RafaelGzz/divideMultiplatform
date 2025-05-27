package com.ragl.divide.data.models

import kotlinx.serialization.Serializable

@Serializable
data class UserInfo(
    val uuid: String = "",
    val name: String = "",
    val photoUrl: String = ""
)