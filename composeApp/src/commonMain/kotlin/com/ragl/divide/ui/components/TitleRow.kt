package com.ragl.divide.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource


@Composable
fun TitleRow(
    buttonStringResource: StringResource,
    labelStringResource: StringResource,
    modifier: Modifier = Modifier,
    onAddClick: () -> Unit = {}
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Text(
            text = stringResource(labelStringResource),
            style = MaterialTheme.typography.titleMedium
        )
        Box(modifier = Modifier
            .clip(ShapeDefaults.Small)
            .background(MaterialTheme.colorScheme.primary)
            .wrapContentWidth()
            .clickable { onAddClick() }) {
            Text(
                text = stringResource(buttonStringResource),
                style = MaterialTheme.typography.titleSmall.copy(color = MaterialTheme.colorScheme.onPrimary),
                modifier = Modifier
                    .padding(horizontal = 28.dp, vertical = 8.dp)
            )
        }
    }
}