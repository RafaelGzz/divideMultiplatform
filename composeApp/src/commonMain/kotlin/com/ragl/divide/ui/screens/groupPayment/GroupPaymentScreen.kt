package com.ragl.divide.ui.screens.groupPayment

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinNavigatorScreenModel
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.ragl.divide.ui.screens.UserViewModel
import com.ragl.divide.ui.screens.groupPaymentProperties.GroupPaymentPropertiesScreen
import com.ragl.divide.ui.utils.FriendItem
import com.ragl.divide.ui.utils.formatCurrency
import com.ragl.divide.ui.utils.formatDate
import dividemultiplatform.composeapp.generated.resources.Res
import dividemultiplatform.composeapp.generated.resources.added_on
import dividemultiplatform.composeapp.generated.resources.cancel
import dividemultiplatform.composeapp.generated.resources.delete
import dividemultiplatform.composeapp.generated.resources.delete_payment_confirm
import dividemultiplatform.composeapp.generated.resources.from
import dividemultiplatform.composeapp.generated.resources.to
import org.jetbrains.compose.resources.stringResource

class GroupPaymentScreen(
    private val groupId: String,
    private val paymentId: String,
    private val eventId: String? = null
) : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val vm = koinScreenModel<GroupPaymentViewModel>()
        val userViewModel = navigator.koinNavigatorScreenModel<UserViewModel>()
        
        var showDeleteDialog by remember { mutableStateOf(false) }
        
        LaunchedEffect(groupId, paymentId) {
            val payment = userViewModel.getGroupPaymentById(groupId, paymentId, eventId)
            val members = userViewModel.getGroupMembers(groupId)
            vm.setPayment(payment, groupId, members)
        }
        
        val payment by vm.payment.collectAsState()
        
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { },
                    colors = TopAppBarDefaults.largeTopAppBarColors(
                        scrolledContainerColor = Color.Transparent,
                        containerColor = Color.Transparent,
                        navigationIconContentColor = MaterialTheme.colorScheme.primary
                    ),
                    navigationIcon = {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        if(!payment.settled) {
                            IconButton(onClick = {
                                navigator.push(
                                    GroupPaymentPropertiesScreen(
                                        groupId = groupId,
                                        paymentId = paymentId,
                                        eventId = eventId
                                    )
                                )
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit"
                                )
                            }
                        }
                        IconButton(onClick = { showDeleteDialog = true }) {
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
                        title = { Text(stringResource(Res.string.delete), style = MaterialTheme.typography.titleMedium) },
                        text = { Text(stringResource(Res.string.delete_payment_confirm), style = MaterialTheme.typography.bodyMedium) },
                        confirmButton = {
                            TextButton(onClick = {
                                userViewModel.showLoading()
                                showDeleteDialog = false
                                vm.deletePayment(
                                    onSuccess = {
                                        userViewModel.deleteGroupPayment(groupId, it)
                                        userViewModel.hideLoading()
                                        navigator.pop()
                                    },
                                    onError = {
                                        userViewModel.hideLoading()
                                        userViewModel.handleError(it)
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
                
                Text(
                    text = stringResource(Res.string.added_on, formatDate(payment.date)),
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
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Start,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, bottom = 8.dp)
                )
                
                FriendItem(
                    headline = vm.fromUser.name,
                    photoUrl = vm.fromUser.photoUrl,
                    modifier = Modifier.fillMaxWidth()
                )
                
                // To
                Text(
                    text = stringResource(Res.string.to),
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Start,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, bottom = 8.dp)
                )
                
                FriendItem(
                    headline = vm.toUser.name,
                    photoUrl = vm.toUser.photoUrl,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
} 