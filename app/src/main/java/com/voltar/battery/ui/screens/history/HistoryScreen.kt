package com.voltar.battery.ui.screens.history

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ElectricBolt
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Power
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.voltar.battery.ui.components.GlassmorphicCard
import com.voltar.battery.ui.theme.EmeraldPrimary
import com.voltar.battery.ui.theme.HealthFair
import com.voltar.battery.ui.theme.HealthGood
import com.voltar.battery.ui.theme.VoltarTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val colors = VoltarTheme.colors
    val sdf = SimpleDateFormat("MMM dd, hh:mm a", Locale.US)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .statusBarsPadding()
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "History",
                style = VoltarTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary,
                fontSize = 24.sp
            )

            Surface(
                shape = CircleShape,
                color = colors.surfaceVariant,
                modifier = Modifier.size(38.dp)
            ) {
                IconButton(onClick = {}) {
                    Icon(
                        imageVector = Icons.Outlined.Notifications,
                        contentDescription = "Notifications",
                        tint = colors.textPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Filter Tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(colors.surfaceVariant)
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            HistoryFilter.values().forEach { filter ->
                val isSelected = uiState.filter == filter
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (isSelected) colors.cardBackground else Color.Transparent
                        )
                        .clickable { viewModel.setFilter(filter) }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = filter.label,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) colors.textPrimary else colors.textSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // History List
        if (uiState.items.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 80.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No history recorded yet.\nConnect charger or run tests to generate logs.",
                    color = colors.textSecondary,
                    fontSize = 14.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    lineHeight = 20.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(uiState.items) { item ->
                    when (item) {
                        is HistoryItem.Charging -> {
                            val session = item.session
                            val gain = session.percentageGain
                            val gainText = if (gain > 0) "+$gain%" else ""
                            HistoryCard(
                                icon = Icons.Outlined.Power,
                                iconTint = if (colors.isDark) EmeraldPrimary else VoltarTheme.colors.accent,
                                title = "Charging Session $gainText",
                                subtitle = sdf.format(Date(session.startTime)),
                                value = if (session.isComplete) "Completed" else "In Progress",
                                onClick = { viewModel.selectItem(item) }
                            )
                        }
                        is HistoryItem.Health -> {
                            val analysis = item.analysis
                            val pctText = analysis.healthPercentage?.let { "${it.toInt()}%" } ?: "--%"
                            HistoryCard(
                                icon = Icons.Outlined.CheckCircle,
                                iconTint = HealthGood,
                                title = "Health Analysis ($pctText)",
                                subtitle = sdf.format(Date(analysis.timestamp)),
                                value = analysis.milestone.title,
                                onClick = { viewModel.selectItem(item) }
                            )
                        }
                        is HistoryItem.Observation -> {
                            val obs = item.obs
                            HistoryCard(
                                icon = Icons.Outlined.Speed,
                                iconTint = colors.textSecondary,
                                title = "Battery Log (${obs.batteryLevel}%)",
                                subtitle = sdf.format(Date(obs.timestamp)),
                                value = "${obs.temperatureCelsius.toInt()}°C • ${String.format(Locale.US, "%.2f V", obs.voltageVolts)}",
                                onClick = { viewModel.selectItem(item) }
                            )
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(90.dp))
                }
            }
        }
    }

    // Detail Inspection Dialog
    val selected = uiState.selectedItem
    if (selected != null) {
        AlertDialog(
            onDismissRequest = { viewModel.selectItem(null) },
            title = {
                Text(
                    text = when (selected) {
                        is HistoryItem.Charging -> "Charging Session Details"
                        is HistoryItem.Health -> "Health Analysis Details"
                        is HistoryItem.Observation -> "Battery Log Details"
                    },
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary,
                    fontSize = 18.sp
                )
            },
            text = {
                Column {
                    when (selected) {
                        is HistoryItem.Charging -> {
                            val s = selected.session
                            DetailRow("Start Level", "${s.startLevel}%")
                            DetailRow("End Level", "${s.endLevel ?: s.startLevel}%")
                            DetailRow("Temperature Range", "${s.minTemperatureCelsius.toInt()}°C - ${s.maxTemperatureCelsius.toInt()}°C")
                            DetailRow("Average Voltage", String.format(Locale.US, "%.2f V", s.avgVoltageVolts))
                            s.estimatedCapacityMahi?.let {
                                DetailRow("Estimated Capacity", "${it.toInt()} mAh")
                            }
                            DetailRow("Plug Type", s.plugType)
                            DetailRow("Valid for Health", if (s.isValidForHealthEstimation) "Yes" else "No (${s.qualityRejectReason ?: "Incomplete"})")
                        }
                        is HistoryItem.Health -> {
                            val h = selected.analysis
                            DetailRow("Health Percentage", h.healthPercentage?.let { "${it}%" } ?: "Insufficient Data")
                            DetailRow("Category", h.category.label)
                            DetailRow("Confidence", h.confidenceLabel)
                            DetailRow("Methodology", h.methodologyNote)
                        }
                        is HistoryItem.Observation -> {
                            val o = selected.obs
                            DetailRow("Battery Level", "${o.batteryLevel}%")
                            DetailRow("Temperature", "${o.temperatureCelsius}°C")
                            DetailRow("Voltage", "${o.voltageVolts} V")
                            DetailRow("Charging State", if (o.isCharging) "Charging" else "Not Charging")
                            DetailRow("Current", o.currentMilliAmperes?.let { "${it.toInt()} mA" } ?: "Unavailable")
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.selectItem(null) }) {
                    Text("Close", color = if (colors.isDark) EmeraldPrimary else VoltarTheme.colors.accent)
                }
            },
            containerColor = colors.cardBackground
        )
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    val colors = VoltarTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = colors.textSecondary, fontSize = 12.sp)
        Text(text = value, color = colors.textPrimary, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
    }
}

@Composable
private fun HistoryCard(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    value: String,
    onClick: () -> Unit
) {
    val colors = VoltarTheme.colors

    GlassmorphicCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(iconTint.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = title,
                        style = VoltarTheme.typography.titleMedium,
                        color = colors.textPrimary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        style = VoltarTheme.typography.bodySmall ?: VoltarTheme.typography.bodyMedium,
                        color = colors.textSecondary,
                        fontSize = 11.sp
                    )
                }
            }

            Text(
                text = value,
                style = VoltarTheme.typography.labelSmall,
                color = colors.textPrimary,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp
            )
        }
    }
}
