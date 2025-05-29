package com.ragl.divide.ui.screens.group

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinNavigatorScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.ragl.divide.data.models.Group
import com.ragl.divide.data.models.GroupEvent
import com.ragl.divide.ui.components.NetworkImage
import com.ragl.divide.ui.components.NetworkImageType
import com.ragl.divide.ui.components.TitleRow
import com.ragl.divide.ui.screens.UserViewModel
import com.ragl.divide.ui.screens.event.EventScreen
import com.ragl.divide.ui.screens.eventProperties.EventPropertiesScreen
import com.ragl.divide.ui.screens.groupProperties.GroupPropertiesScreen
import com.ragl.divide.ui.utils.formatDate
import dividemultiplatform.composeapp.generated.resources.Res
import dividemultiplatform.composeapp.generated.resources.add_event
import dividemultiplatform.composeapp.generated.resources.events

class GroupScreen(
    private val groupId: String
) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = navigator.koinNavigatorScreenModel<GroupViewModel>()
        val userViewModel = navigator.koinNavigatorScreenModel<UserViewModel>()

        LaunchedEffect(Unit) {
            val group = userViewModel.getGroupById(groupId)
            val members = userViewModel.getGroupMembers(groupId)
            viewModel.setGroup(group, members)
        }

        val uuid = remember(groupId) { userViewModel.getUUID() }
        val groupState by viewModel.group.collectAsState()
        val hasExpensesOrPayments =
            remember(groupState) { groupState.expenses.isNotEmpty() || groupState.payments.isNotEmpty() }

        Scaffold(topBar = {
            GroupDetailsAppBar(
                group = groupState,
                onBackClick = { navigator.pop() },
                onEditClick = { navigator.push(GroupPropertiesScreen(groupId)) })
        }) { paddingValues ->

            Column(
                modifier = Modifier.padding(paddingValues).padding(horizontal = 16.dp)
                    .fillMaxSize().background(MaterialTheme.colorScheme.background)
            ) {
                EventsSection(
                    events = viewModel.events,
                    onEventClick = { eventId ->
                        navigator.push(EventScreen(groupId, eventId))
                    },
                    onAddEventClick = {
                        navigator.push(
                            EventPropertiesScreen(
                                groupId
                            )
                        )
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GroupDetailsAppBar(
    onBackClick: () -> Unit, onEditClick: () -> Unit, group: Group
) {
    TopAppBar(
        colors = TopAppBarDefaults.largeTopAppBarColors(
            scrolledContainerColor = Color.Transparent,
            containerColor = Color.Transparent,
            navigationIconContentColor = MaterialTheme.colorScheme.primary
        ), title = { GroupImageAndTitleRow(group) }, navigationIcon = {
            IconButton(
                onClick = onBackClick
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }

        }, actions = {
            IconButton(onClick = onEditClick) {
                Icon(Icons.Filled.Settings, contentDescription = "Edit")
            }
        })
}

@Composable
private fun GroupImageAndTitleRow(group: Group, modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
    ) {
        NetworkImage(
            imageUrl = group.image,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape),
            type = NetworkImageType.GROUP
        )
        Text(
            group.name,
            style = MaterialTheme.typography.titleLarge,
            softWrap = true,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun EventsSection(
    events: List<GroupEvent>,
    onEventClick: (String) -> Unit,
    onAddEventClick: () -> Unit
) {
    Column {
        TitleRow(
            buttonStringResource = Res.string.add_event,
            labelStringResource = Res.string.events,
            onAddClick = onAddEventClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp, bottom = 10.dp)
        )

        if (events.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No hay eventos creados",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyVerticalGrid(
                modifier = Modifier
                    .fillMaxWidth(),
                columns = GridCells.Fixed(2),
            ) {
                items(events) { event ->
                    EventItem(
                        event = event,
                        onClick = { onEventClick(event.id) },
                        modifier = Modifier.padding(4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun EventItem(
    event: GroupEvent,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = MaterialTheme.colorScheme.onPrimary
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(containerColor)
            .clickable(onClick = onClick)
            .padding(12.dp),
    ) {
        Icon(
            imageVector = Icons.Default.DateRange,
            contentDescription = null,
            tint = containerColor,
            modifier = Modifier
                .size(40.dp)
                .background(
                    contentColor,
                    CircleShape
                )
                .padding(8.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = event.title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = contentColor
        )
        Text(
            text = formatDate(event.createdAt, "dd MMM yyyy"),
            style = MaterialTheme.typography.bodySmall,
            color = contentColor.copy(alpha = 0.5f)
        )

        if (event.settled) {
            Text(
                text = "Liquidado",
                style = MaterialTheme.typography.bodySmall,
                color = containerColor,
                modifier = Modifier
                    .background(
                        contentColor.copy(alpha = 0.1f),
                        RoundedCornerShape(4.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}
