package com.ragl.divide.ui.screens.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ragl.divide.data.models.Group
import com.ragl.divide.ui.components.NetworkImage
import com.ragl.divide.ui.components.NetworkImageType
import com.ragl.divide.ui.utils.Header
import com.ragl.divide.ui.utils.WindowWidthSizeClass
import com.ragl.divide.ui.utils.getWindowWidthSizeClass
import dividemultiplatform.composeapp.generated.resources.Res
import dividemultiplatform.composeapp.generated.resources.you_have_no_groups
import dividemultiplatform.composeapp.generated.resources.your_groups
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun GroupsContent(
    groups: List<Group>,
    onGroupClick: (String) -> Unit,
    onAddGroupClick: () -> Unit
) {
    // Estados para controlar las animaciones escalonadas
    var showHeader by remember { mutableStateOf(true) }
    var showEmptyState by remember { mutableStateOf(true) }
    var visibleGroups by remember { mutableStateOf(groups.size + 1) }
    var windowSizeClass = getWindowWidthSizeClass()

    // Efecto para iniciar las animaciones escalonadas
    LaunchedEffect(groups) {
        showHeader = true
        delay(50)

        if (groups.isEmpty()) {
            showEmptyState = true
        } else {
            // Mostrar grupos uno por uno (incluyendo el botón de agregar)
//            for (i in 0..groups.size) { // +1 para incluir el botón de agregar
//                visibleGroups = i + 1
//                delay(20)
//            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = showHeader,
            enter = slideInVertically(
                animationSpec = tween(500),
                initialOffsetY = { -it / 2 }
            ) + fadeIn(animationSpec = tween(500))
        ) {
            Header(
                title = stringResource(Res.string.your_groups)
            )
        }

        if (groups.isEmpty()) {
            AnimatedVisibility(
                visible = showEmptyState,
                enter = slideInVertically(
                    animationSpec = tween(500),
                    initialOffsetY = { -it / 2 }
                ) + fadeIn(animationSpec = tween(500))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(Res.string.you_have_no_groups),
                        style = MaterialTheme.typography.labelMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                columns = GridCells.Fixed(
                    when (windowSizeClass) {
                        WindowWidthSizeClass.Compact -> 2
                        WindowWidthSizeClass.Medium -> 3
                        WindowWidthSizeClass.Expanded -> 4
                    }
                ),
                modifier = Modifier.padding(16.dp)
            ) {
                // Botón de agregar grupo (índice 0)
//                item {
//                    AnimatedVisibility(
//                        visible = visibleGroups > 0,
//                        enter = slideInVertically(
//                            animationSpec = tween(400),
//                            initialOffsetY = { -it / 2 }
//                        ) + fadeIn(animationSpec = tween(400))
//                    ) {
//                        Box(
//                            modifier = Modifier
//                                .size(150.dp)
//                                .clip(ShapeDefaults.Medium)
//                                .background(MaterialTheme.colorScheme.secondaryContainer)
//                                .clickable { onAddGroupClick() },
//                            contentAlignment = Alignment.Center
//                        ) {
//                            Icon(
//                                Icons.Default.Add,
//                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
//                                contentDescription = null,
//                                modifier = Modifier.size(24.dp)
//                            )
//                        }
//                    }
//                }

                // Grupos (índices 1 en adelante)
                items(groups.size) { index ->
                    val group = groups[index]
                    val groupIndex = index + 1 // +1 porque el botón de agregar es el índice 0

                    AnimatedVisibility(
                        visible = visibleGroups > groupIndex,
                        enter = slideInVertically(
                            animationSpec = tween(400),
                            initialOffsetY = { -it / 2 }
                        ) + fadeIn(animationSpec = tween(400))
                    ) {
                        GroupCard(
                            group = group,
                            modifier = Modifier
                                .size(150.dp)
                                .clip(ShapeDefaults.Medium)
                        ) { onGroupClick(group.id) }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun GroupCard(
    group: Group,
    modifier: Modifier = Modifier,
    onGroupClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clickable { onGroupClick() }
    ) {
        // Imagen de fondo
        NetworkImage(
            imageUrl = group.image,
            modifier = Modifier.fillMaxSize(),
            type = NetworkImageType.GROUP
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.5f)
                        ),
                        startY = 250f,
                    )
                )
        )

        Text(
            text = group.name,
            style = MaterialTheme.typography.bodyMedium.copy(
                textAlign = TextAlign.Center,
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp)
        )
    }
}