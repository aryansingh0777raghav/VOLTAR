package com.voltar.battery.ui.screens.home

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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ElectricBolt
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Power
import androidx.compose.material.icons.outlined.Thermostat
import androidx.compose.material.icons.outlined.Timeline
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Loop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.voltar.battery.core.model.BatteryHealthCategory
import com.voltar.battery.core.model.CollectionMilestone
import com.voltar.battery.core.model.MetricSource
import com.voltar.battery.ui.components.CircularHealthGauge
import com.voltar.battery.ui.components.CreatorProfileBottomSheet
import com.voltar.battery.ui.components.CreatorSignatureButton
import com.voltar.battery.ui.components.GlassmorphicCard
import com.voltar.battery.ui.components.MetricCard
import com.voltar.battery.ui.components.SmoothAreaChart
import com.voltar.battery.ui.components.VoltarTopBar
import com.voltar.battery.ui.theme.EmeraldDark
import com.voltar.battery.ui.theme.EmeraldPrimary
import com.voltar.battery.ui.theme.HealthFair
import com.voltar.battery.ui.theme.HealthGood
import com.voltar.battery.ui.theme.HealthPoor
import com.voltar.battery.ui.theme.HealthUnknown
import com.voltar.battery.ui.theme.HealthWeak
import com.voltar.battery.ui.theme.VoltarTheme

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val colors = VoltarTheme.colors

    val live = uiState.liveObservation
    val health = uiState.healthAnalysis
    val profile = uiState.deviceProfile

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .statusBarsPadding()
    ) {
        VoltarTopBar(
            onInfoClick = { viewModel.showInfoDialog(true) },
            onNotificationClick = { viewModel.showInfoDialog(true) }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // 1. Main Battery Health Card
            GlassmorphicCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(26.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Card Top Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = health?.milestone?.title ?: "Data Collection",
                                style = VoltarTheme.typography.labelSmall,
                                color = colors.textSecondary,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 0.5.sp
                            )
                        }

                        IconButton(
                            onClick = { viewModel.showInfoDialog(true) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Info,
                                contentDescription = "Info",
                                tint = colors.textSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Circular Progress Health Gauge
                    CircularHealthGauge(
                        healthPercentage = health?.healthPercentage,
                        category = health?.category ?: BatteryHealthCategory.INSUFFICIENT_DATA,
                        estimatedCapacityMah = health?.estimatedCapacityMah,
                        designCapacityMah = health?.designCapacityMah ?: profile?.designCapacityMah ?: 5000f,
                        isHardwareVerified = !(health?.isPreliminary ?: true) && (profile?.isDesignCapacityVerified ?: false),
                        size = 220.dp,
                        strokeWidth = 14.dp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Measurement Note & Confidence Badge
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(colors.surfaceVariant)
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = health?.confidenceLabel ?: "Collecting Session Data",
                            style = VoltarTheme.typography.bodySmall ?: VoltarTheme.typography.bodyMedium,
                            color = colors.textPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Secondary Metrics 2x2 Grid
            val tempCelsius = live?.temperatureCelsius ?: 32.0f
            val (tempStatus, tempColor) = when {
                tempCelsius < 15f -> "Cool" to HealthFair
                tempCelsius <= 37f -> "Normal" to (if (colors.isDark) EmeraldPrimary else VoltarTheme.colors.accent)
                tempCelsius <= 42f -> "Warm" to HealthFair
                else -> "Hot" to HealthPoor
            }

            val voltage = live?.voltageVolts ?: 4.18f
            val voltageStatus = when {
                voltage >= 4.35f -> "High"
                voltage >= 3.80f -> "Stable"
                else -> "Low"
            }

            val isCharging = live?.isCharging ?: false
            val chargingStatusText = if (isCharging) live?.plugType ?: "Charging" else "Not Charging"
            val chargingSubtitle = if (isCharging) "Active Charge" else "Idle"

            val cycleCount = profile?.hardwareCycleCount
            val cyclesText = cycleCount?.toString() ?: "Unavailable"
            val cyclesSubtitle = if (cycleCount != null) "Hardware reported" else "Not exposed by OS"

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                MetricCard(
                    title = "Temperature",
                    value = "${tempCelsius.toInt()}°C",
                    subtitle = tempStatus,
                    icon = Icons.Outlined.Thermostat,
                    statusColor = tempColor,
                    modifier = Modifier.weight(1f)
                )

                MetricCard(
                    title = "Voltage",
                    value = String.format(java.util.Locale.US, "%.2f V", voltage),
                    subtitle = voltageStatus,
                    icon = Icons.Outlined.ElectricBolt,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                MetricCard(
                    title = "Charging",
                    value = chargingStatusText,
                    subtitle = chargingSubtitle,
                    icon = Icons.Outlined.Power,
                    modifier = Modifier.weight(1f)
                )

                MetricCard(
                    title = "Charge Cycles",
                    value = cyclesText,
                    subtitle = cyclesSubtitle,
                    icon = Icons.Rounded.Loop,
                    source = if (cycleCount != null) MetricSource.HARDWARE_REPORTED else MetricSource.UNAVAILABLE,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 3. Health Trend Card
            GlassmorphicCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Health Trend",
                            style = VoltarTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary,
                            fontSize = 16.sp
                        )

                        // Time Range Selector Tabs (24h, 7d, 14d, 30d)
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(colors.surfaceVariant)
                                .padding(2.dp)
                        ) {
                            listOf("24h", "7d", "14d", "30d").forEach { range ->
                                val isSelected = uiState.selectedTrendRange == range
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(
                                            if (isSelected) colors.cardBackground else Color.Transparent
                                        )
                                        .clickable { viewModel.setSelectedTrendRange(range) }
                                        .padding(horizontal = 10.dp, vertical = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = range,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) colors.textPrimary else colors.textSecondary
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Smooth Area Curve Chart
                    SmoothAreaChart(
                        points = uiState.trendChartPoints,
                        lineColor = if (colors.isDark) EmeraldPrimary else VoltarTheme.colors.accent,
                        minY = 70f,
                        maxY = 100f,
                        valueSuffix = "%"
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 4. Creator Signature Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                CreatorSignatureButton(
                    onClick = { viewModel.showCreatorSheet(true) }
                )
            }

            Spacer(modifier = Modifier.height(90.dp)) // Space for bottom navigation bar
        }
    }

    // Creator Profile Bottom Sheet
    if (uiState.isCreatorSheetVisible) {
        CreatorProfileBottomSheet(
            onDismissRequest = { viewModel.showCreatorSheet(false) }
        )
    }

    // Info Dialog
    if (uiState.isInfoDialogVisible) {
        AlertDialog(
            onDismissRequest = { viewModel.showInfoDialog(false) },
            title = {
                Text(
                    text = "Real Battery Intelligence",
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
            },
            text = {
                Column {
                    Text(
                        text = "VOLTAR calculates genuine battery wear using repeated charge-counter differences over multiple charging sessions.",
                        color = colors.textPrimary,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "• First 24 Hours: Baseline capture and initial session estimation.\n• 7-Day Analysis: Multi-session validation and outlier rejection.\n• 14-Day Analysis: Highest confidence rating with longitudinal trend evaluation.",
                        color = colors.textSecondary,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.showInfoDialog(false) }) {
                    Text("Understood", color = if (colors.isDark) EmeraldPrimary else VoltarTheme.colors.accent)
                }
            },
            containerColor = colors.cardBackground
        )
    }
}
