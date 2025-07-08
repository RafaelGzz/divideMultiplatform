package com.ragl.divide.presentation.screens.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinNavigatorScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.ragl.divide.presentation.screens.main.MainScreen
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.Calculator
import compose.icons.fontawesomeicons.solid.MoneyBill
import compose.icons.fontawesomeicons.solid.Users
import dividemultiplatform.composeapp.generated.resources.Res
import dividemultiplatform.composeapp.generated.resources.get_started
import dividemultiplatform.composeapp.generated.resources.next
import dividemultiplatform.composeapp.generated.resources.onboarding_description_1
import dividemultiplatform.composeapp.generated.resources.onboarding_description_2
import dividemultiplatform.composeapp.generated.resources.onboarding_description_3
import dividemultiplatform.composeapp.generated.resources.onboarding_title_1
import dividemultiplatform.composeapp.generated.resources.onboarding_title_2
import dividemultiplatform.composeapp.generated.resources.onboarding_title_3
import dividemultiplatform.composeapp.generated.resources.skip
import dividemultiplatform.composeapp.generated.resources.welcome_to_divide
import org.jetbrains.compose.resources.stringResource

data class OnboardingPage(
    val title: String,
    val description: String,
    val icon: @Composable () -> Unit
)

class OnboardingScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = navigator.koinNavigatorScreenModel<OnboardingViewModel>()
        val state by viewModel.state.collectAsState()
        val pagerState = rememberPagerState(pageCount = { viewModel.totalPages })

        LaunchedEffect(state.currentPage) {
            pagerState.animateScrollToPage(state.currentPage)
        }

        LaunchedEffect(pagerState.currentPage) {
            viewModel.setPage(pagerState.currentPage)
        }

        val pages = listOf(
            OnboardingPage(
                title = stringResource(Res.string.onboarding_title_1),
                description = stringResource(Res.string.onboarding_description_1),
                icon = {
                    androidx.compose.material3.Icon(
                        imageVector = FontAwesomeIcons.Solid.Calculator,
                        contentDescription = null,
                        modifier = Modifier.size(100.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            ),
            OnboardingPage(
                title = stringResource(Res.string.onboarding_title_2),
                description = stringResource(Res.string.onboarding_description_2),
                icon = {
                    androidx.compose.material3.Icon(
                        imageVector = FontAwesomeIcons.Solid.Users,
                        contentDescription = null,
                        modifier = Modifier.size(100.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            ),
            OnboardingPage(
                title = stringResource(Res.string.onboarding_title_3),
                description = stringResource(Res.string.onboarding_description_3),
                icon = {
                    androidx.compose.material3.Icon(
                        imageVector = FontAwesomeIcons.Solid.MoneyBill,
                        contentDescription = null,
                        modifier = Modifier.size(100.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header con título de bienvenida
            Text(
                text = stringResource(Res.string.welcome_to_divide),
                style = MaterialTheme.typography.headlineLarge.copy(
                    color = MaterialTheme.colorScheme.primary
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 32.dp, bottom = 16.dp)
            )

            // Pager para las páginas del onboarding
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                OnboardingPageContent(
                    page = pages[page],
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Indicadores de página
            PageIndicators(
                totalPages = viewModel.totalPages,
                currentPage = state.currentPage,
                modifier = Modifier.padding(vertical = 24.dp)
            )

            // Botones de navegación
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Botón Skip
                TextButton(
                    onClick = {
                        viewModel.completeOnboarding()
                        navigator.replaceAll(MainScreen())
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Text(
                        text = stringResource(Res.string.skip),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                // Botón Next/Get Started
                if (state.currentPage < viewModel.totalPages - 1) {
                    Button(
                        onClick = {
                            viewModel.nextPage()
                        },
                        shape = RoundedCornerShape(25.dp),
                        modifier = Modifier.height(50.dp)
                    ) {
                        Text(
                            text = stringResource(Res.string.next),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                } else {
                    Button(
                        onClick = {
                            viewModel.completeOnboarding()
                            navigator.replaceAll(MainScreen())
                        },
                        shape = RoundedCornerShape(25.dp),
                        modifier = Modifier.height(50.dp)
                    ) {
                        Text(
                            text = stringResource(Res.string.get_started),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun OnboardingPageContent(
    page: OnboardingPage,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Icono
        Box(
            modifier = Modifier
                .size(200.dp)
                .background(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            page.icon()
        }

        Spacer(modifier = Modifier.height(48.dp))

        // Título
        Text(
            text = page.title,
            style = MaterialTheme.typography.headlineMedium.copy(
                color = MaterialTheme.colorScheme.onBackground
            ),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Descripción
        Text(
            text = page.description,
            style = MaterialTheme.typography.bodyLarge.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
    }
}

@Composable
private fun PageIndicators(
    totalPages: Int,
    currentPage: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(totalPages) { index ->
            val isSelected = index == currentPage
            Box(
                modifier = Modifier
                    .size(
                        width = if (isSelected) 24.dp else 8.dp,
                        height = 8.dp
                    )
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                        }
                    )
            )
        }
    }
} 