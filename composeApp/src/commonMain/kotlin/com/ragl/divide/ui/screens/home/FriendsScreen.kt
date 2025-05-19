package com.ragl.divide.ui.screens.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.ragl.divide.data.models.User
import com.ragl.divide.ui.utils.FriendItem
import com.ragl.divide.ui.utils.Header
import dividemultiplatform.composeapp.generated.resources.Res
import dividemultiplatform.composeapp.generated.resources.bar_item_friends_text
import dividemultiplatform.composeapp.generated.resources.you_have_no_friends
import org.jetbrains.compose.resources.stringResource


@Composable
fun FriendsBody(
    friends: List<User>
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            item {
                Header(
                    title = stringResource(Res.string.bar_item_friends_text)
                )
            }
            if (friends.isEmpty()) {
                item {
                    Text(
                        text = stringResource(Res.string.you_have_no_friends),
                        style = MaterialTheme.typography.labelSmall,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            } else items(friends, key = { it.uuid }) { friend ->
                FriendItem(
                    headline = friend.name,
                    photoUrl = friend.photoUrl,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            }

        }
    }
}