package com.schedule.application.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.schedule.application.data.model.NotificationPriorityConfig
import com.schedule.application.data.model.PriorityLevel
import com.schedule.application.data.model.ThemeMode

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    contentPadding: PaddingValues,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val settings = state.settings

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
    ) {
        item {
            Card {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text("Connection", style = MaterialTheme.typography.titleMedium)

                    OutlinedTextField(
                        value = settings.serverUrl,
                        onValueChange = viewModel::updateServerUrl,
                        label = { Text("Server URL") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = settings.readKey,
                        onValueChange = viewModel::updateReadKey,
                        label = { Text("Read key") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = settings.submitKey,
                        onValueChange = viewModel::updateSubmitKey,
                        label = { Text("Submit key") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = settings.selectedGroup,
                        onValueChange = viewModel::updateGroup,
                        label = { Text("Student group") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }

        item {
            ThemeSection(
                selected = settings.themeMode,
                onSelected = viewModel::updateTheme,
            )
        }

        item {
            Text("Notifications by priority", style = MaterialTheme.typography.titleMedium)
        }

        items(PriorityLevel.entries) { priority ->
            val config = settings.notificationConfig[priority] ?: NotificationPriorityConfig()
            NotificationPriorityCard(
                priority = priority,
                config = config,
                onConfigChange = { enabled, minutes ->
                    viewModel.updatePriorityNotification(priority, enabled, minutes)
                },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ThemeSection(
    selected: ThemeMode,
    onSelected: (ThemeMode) -> Unit,
) {
    Card {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text("Theme", style = MaterialTheme.typography.titleMedium)

            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                ThemeMode.entries.forEachIndexed { index, mode ->
                    SegmentedButton(
                        selected = selected == mode,
                        onClick = { onSelected(mode) },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = ThemeMode.entries.size),
                    ) {
                        Text(
                            when (mode) {
                                ThemeMode.SYSTEM -> "System"
                                ThemeMode.LIGHT -> "Light"
                                ThemeMode.DARK -> "Dark"
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationPriorityCard(
    priority: PriorityLevel,
    config: NotificationPriorityConfig,
    onConfigChange: (Boolean, Int) -> Unit,
) {
    Card {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(priorityTitle(priority), style = MaterialTheme.typography.titleMedium)
                Switch(
                    checked = config.enabled,
                    onCheckedChange = { enabled -> onConfigChange(enabled, config.minutesBefore) },
                )
            }

            OutlinedTextField(
                value = config.minutesBefore.toString(),
                onValueChange = { raw ->
                    val parsed = raw.toIntOrNull() ?: config.minutesBefore
                    onConfigChange(config.enabled, parsed.coerceIn(0, 180))
                },
                label = { Text("Minutes before lesson") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

private fun priorityTitle(priority: PriorityLevel): String {
    return when (priority) {
        PriorityLevel.RED -> "Red (highest)"
        PriorityLevel.ORANGE -> "Orange"
        PriorityLevel.YELLOW -> "Yellow"
        PriorityLevel.DARK_GREEN -> "Dark green"
        PriorityLevel.LIGHT_GREEN -> "Light green"
    }
}
