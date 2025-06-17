package com.ragl.divide.data.models

import androidx.compose.runtime.Composable
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
import dividemultiplatform.composeapp.generated.resources.Res
import dividemultiplatform.composeapp.generated.resources.category_clothing
import dividemultiplatform.composeapp.generated.resources.category_education
import dividemultiplatform.composeapp.generated.resources.category_electronics
import dividemultiplatform.composeapp.generated.resources.category_entertainment
import dividemultiplatform.composeapp.generated.resources.category_food
import dividemultiplatform.composeapp.generated.resources.category_general
import dividemultiplatform.composeapp.generated.resources.category_gifts
import dividemultiplatform.composeapp.generated.resources.category_health
import dividemultiplatform.composeapp.generated.resources.category_housing
import dividemultiplatform.composeapp.generated.resources.category_transport
import dividemultiplatform.composeapp.generated.resources.category_travel
import dividemultiplatform.composeapp.generated.resources.category_utilities
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.stringResource

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
    GIFTS;

    @Composable
    fun getCategoryName(): String {
        return when (this) {
            GENERAL -> stringResource(Res.string.category_general)
            ELECTRONICS -> stringResource(Res.string.category_electronics)
            FOOD -> stringResource(Res.string.category_food)
            TRANSPORT -> stringResource(Res.string.category_transport)
            HOUSING -> stringResource(Res.string.category_housing)
            ENTERTAINMENT -> stringResource(Res.string.category_entertainment)
            HEALTH -> stringResource(Res.string.category_health)
            EDUCATION -> stringResource(Res.string.category_education)
            CLOTHING -> stringResource(Res.string.category_clothing)
            TRAVEL -> stringResource(Res.string.category_travel)
            UTILITIES -> stringResource(Res.string.category_utilities)
            GIFTS -> stringResource(Res.string.category_gifts)
        }
    }
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