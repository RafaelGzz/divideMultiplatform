package com.ragl.divide.presentation.screens.eventPayment

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.ragl.divide.domain.services.AppStateService
import com.ragl.divide.presentation.components.FriendItem
import com.ragl.divide.presentation.screens.eventPaymentProperties.EventPaymentPropertiesScreen
import com.ragl.divide.presentation.utils.Strings
import com.ragl.divide.presentation.utils.formatCurrency
import com.ragl.divide.presentation.utils.formatDate
import dividemultiplatform.composeapp.generated.resources.Res
import dividemultiplatform.composeapp.generated.resources.added_on
import dividemultiplatform.composeapp.generated.resources.cancel
import dividemultiplatform.composeapp.generated.resources.delete
import dividemultiplatform.composeapp.generated.resources.delete_payment_confirm
import dividemultiplatform.composeapp.generated.resources.from
import dividemultiplatform.composeapp.generated.resources.loan
import dividemultiplatform.composeapp.generated.resources.payment
import dividemultiplatform.composeapp.generated.resources.to
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

class EventPaymentScreen(
    private val groupId: String,
    private val paymentId: String,
    private val eventId: String,
    private val isEventSettled: Boolean = false
) : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val vm = koinScreenModel<EventPaymentViewModel>()
        val strings: Strings = koinInject()
        val appStateService: AppStateService = koinInject()

        var showDeleteDialog by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            vm.setPayment(groupId, paymentId, eventId)
        }

        val payment by vm.payment.collectAsState()

        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(stringResource(if (payment.isLoan) Res.string.loan else Res.string.payment))
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        scrolledContainerColor = Color.Transparent,
                        navigationIconContentColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = Color.Unspecified,
                        actionIconContentColor = MaterialTheme.colorScheme.primary
                    ),
                    navigationIcon = {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            if (!isEventSettled)
                                navigator.push(
                                    EventPaymentPropertiesScreen(
                                        groupId = groupId,
                                        paymentId = paymentId,
                                        eventId = eventId
                                    )
                                )
                            else
                                appStateService.handleError(strings.getCannotModifyWhileEventSettled())
                        }) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit"
                            )
                        }
                        IconButton(
                            onClick = {
                                if (!isEventSettled)
                                    showDeleteDialog = true
                                else
                                    appStateService.handleError(strings.getCannotModifyWhileEventSettled())
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete"
                            )
                        }
                    }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                if (showDeleteDialog) {
                    AlertDialog(
                        onDismissRequest = { showDeleteDialog = false },
                        title = {
                            Text(
                                stringResource(Res.string.delete),
                                style = MaterialTheme.typography.titleMedium
                            )
                        },
                        text = {
                            Text(
                                stringResource(Res.string.delete_payment_confirm),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        },
                        confirmButton = {
                            TextButton(onClick = {
                                appStateService.showLoading()
                                showDeleteDialog = false
                                vm.deletePayment(
                                    onSuccess = {
                                        appStateService.hideLoading()
                                        navigator.pop()
                                    },
                                    onError = {
                                        appStateService.hideLoading()
                                        appStateService.handleError(it)
                                    }
                                )
                            }) {
                                Text(stringResource(Res.string.delete))
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDeleteDialog = false }) {
                                Text(stringResource(Res.string.cancel))
                            }
                        }
                    )
                }

                // Amount and date
                Text(
                    text = formatCurrency(payment.amount, "es-MX"),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.headlineLarge.copy(color = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.fillMaxWidth()
                )

                if (payment.description.isNotEmpty())
                    Text(
                        text = payment.description,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                    )

                Text(
                    text = stringResource(Res.string.added_on, formatDate(payment.createdAt)),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // From
                Text(
                    text = stringResource(Res.string.from),
                    textAlign = TextAlign.Start,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, bottom = 8.dp)
                )

                FriendItem(
                    headline = vm.fromUser.name,
                    photoUrl = vm.fromUser.photoUrl,
                    modifier = Modifier.fillMaxWidth().clip(ShapeDefaults.Medium)
                )

                // To
                Text(
                    text = stringResource(Res.string.to),
                    textAlign = TextAlign.Start,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, bottom = 8.dp)
                )

                FriendItem(
                    headline = vm.toUser.name,
                    photoUrl = vm.toUser.photoUrl,
                    modifier = Modifier.fillMaxWidth().clip(ShapeDefaults.Medium)
                )
            }
        }
    }
} 