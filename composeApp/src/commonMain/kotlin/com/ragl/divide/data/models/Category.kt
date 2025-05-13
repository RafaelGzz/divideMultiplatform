package com.ragl.divide.data.models

import androidx.compose.ui.graphics.vector.ImageVector
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.Bolt
import compose.icons.fontawesomeicons.solid.ShoppingCart
import kotlinx.serialization.Serializable

@Serializable
enum class Category {
    GENERAL,
    ELECTRONICS,
}

fun getCategoryIcon(category: Category): ImageVector {
    return when (category) {
        Category.GENERAL -> FontAwesomeIcons.Solid.ShoppingCart
        Category.ELECTRONICS -> FontAwesomeIcons.Solid.Bolt
    }
}