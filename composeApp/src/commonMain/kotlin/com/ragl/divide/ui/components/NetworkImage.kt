package com.ragl.divide.ui.components

import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.coil3.CoilImage
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.Users
import dividemultiplatform.composeapp.generated.resources.Res
import dividemultiplatform.composeapp.generated.resources.ic_divide
import org.jetbrains.compose.resources.painterResource

/**
 * Componente que unifica la carga de imágenes desde la red con estados de carga y error
 * 
 * @param imageUrl URL de la imagen a cargar
 * @param modifier Modifier para personalizar el composable
 * @param contentScale Escala de contenido de la imagen (por defecto Crop)
 * @param shape Forma para recortar la imagen (por defecto RectangleShape)
 * @param type Tipo de imagen (determina el icono de fallback)
 */
@Composable
fun NetworkImage(
    imageUrl: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    type: NetworkImageType = NetworkImageType.DEFAULT
) {
    Box(
        modifier = modifier
    ) {
        if (!imageUrl.isNullOrEmpty()) {
            CoilImage(
                imageModel = { imageUrl },
                imageOptions = ImageOptions(
                    contentScale = contentScale
                ),
                loading = {
                    ShimmerLoading(modifier = Modifier.fillMaxSize())
                },
                failure = {
                    FallbackImage(type = type, modifier = Modifier.fillMaxSize())
                },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            FallbackImage(type = type, modifier = Modifier.fillMaxSize())
        }
    }
}

/**
 * Tipo de imagen para determinar el icono de fallback
 */
enum class NetworkImageType {
    PROFILE,
    GROUP,
    DEFAULT
}

/**
 * Efecto de carga shimmer
 */
@Composable
private fun ShimmerLoading(modifier: Modifier = Modifier) {
    val shimmerColors = listOf(
        MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
        MaterialTheme.colorScheme.surface.copy(alpha = 0.2f),
        MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
    )
    
    val transition = rememberInfiniteTransition("ShimmerTransition")
    val translateAnimation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(800)
        ),
        label = "ShimmerAnimation"
    )
    
    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(translateAnimation, translateAnimation),
        end = Offset(translateAnimation + 100f, translateAnimation + 100f)
    )
    
    Box(
        modifier = modifier
            .background(brush)
    )
}

/**
 * Imagen de fallback basada en el tipo
 */
@Composable
private fun FallbackImage(type: NetworkImageType, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(MaterialTheme.colorScheme.primary),
        contentAlignment = Alignment.Center
    ) {
        when (type) {
            NetworkImageType.PROFILE -> {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }
            NetworkImageType.GROUP -> {
                Icon(
                    imageVector = FontAwesomeIcons.Solid.Users,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }
            NetworkImageType.DEFAULT -> {
                Icon(
                    painter = painterResource(Res.drawable.ic_divide),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
} 