package com.ragl.divide.data.models

import androidx.compose.ui.graphics.vector.ImageVector
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.Bolt
import compose.icons.fontawesomeicons.solid.Car
import compose.icons.fontawesomeicons.solid.Film
import compose.icons.fontawesomeicons.solid.Gift
import compose.icons.fontawesomeicons.solid.GraduationCap
import compose.icons.fontawesomeicons.solid.Heartbeat
import compose.icons.fontawesomeicons.solid.Home
import compose.icons.fontawesomeicons.solid.Lightbulb
import compose.icons.fontawesomeicons.solid.Plane
import compose.icons.fontawesomeicons.solid.ShoppingCart
import compose.icons.fontawesomeicons.solid.Tshirt
import compose.icons.fontawesomeicons.solid.Utensils
import kotlinx.serialization.Serializable

@Serializable
enum class Category {
    GENERAL,
    ELECTRONICS,
    FOOD,
    TRANSPORT,
    HOUSING,
    ENTERTAINMENT,
    HEALTH,
    EDUCATION,
    CLOTHING,
    TRAVEL,
    UTILITIES,
    GIFTS
}

fun getCategoryIcon(category: Category): ImageVector {
    return when (category) {
        Category.GENERAL -> FontAwesomeIcons.Solid.ShoppingCart
        Category.ELECTRONICS -> FontAwesomeIcons.Solid.Bolt
        Category.FOOD -> FontAwesomeIcons.Solid.Utensils
        Category.TRANSPORT -> FontAwesomeIcons.Solid.Car
        Category.HOUSING -> FontAwesomeIcons.Solid.Home
        Category.ENTERTAINMENT -> FontAwesomeIcons.Solid.Film
        Category.HEALTH -> FontAwesomeIcons.Solid.Heartbeat
        Category.EDUCATION -> FontAwesomeIcons.Solid.GraduationCap
        Category.CLOTHING -> FontAwesomeIcons.Solid.Tshirt
        Category.TRAVEL -> FontAwesomeIcons.Solid.Plane
        Category.UTILITIES -> FontAwesomeIcons.Solid.Lightbulb
        Category.GIFTS -> FontAwesomeIcons.Solid.Gift
    }
}