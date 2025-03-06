package com.ragl.divide

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform