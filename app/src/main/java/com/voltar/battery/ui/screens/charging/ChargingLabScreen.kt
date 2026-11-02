package com.voltar.battery.ui.screens.charging

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ElectricBolt
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Power
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.Thermostat
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.voltar.battery.core.model.MetricSource
import com.voltar.battery.ui.components.GlassmorphicCard
import com.voltar.battery.ui.components.MetricCard
import com.voltar.battery.ui.theme.EmeraldPrimary
import com.voltar.battery.ui.theme.HealthFair
import com.voltar.battery.ui.theme.HealthGood
import com.voltar.battery.ui.theme.HealthPoor
import com.voltar.battery.ui.theme.VoltarTheme

@Composable
fun ChargingLabScreen(
    viewModel: ChargingLabViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val colors = VoltarTheme.colors
    val live = uiState.liveObservation
    val isCharging = uiState.isCharging

    val infiniteTransition = rememberInfiniteTransition(label = "Pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseScale"
    )

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
                text = "Charging Lab",
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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // Main Charging Status Hero Card
            GlassmorphicCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(26.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(12.dp))

                    // Pulsing Bolt Icon
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .scale(if (isCharging) pulseScale else 1f)
                            .clip(CircleShape)
                            .background(
                                if (isCharging) {
                                    (if (colors.isDark) EmeraldPrimary else VoltarTheme.colors.accent).copy(alpha = 0.16f)
                                } else {
                                    colors.surfaceVariant
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Bolt,
                            contentDescription = null,
                            tint = if (isCharging) {
                                if (colors.isDark) EmeraldPrimary else VoltarTheme.colors.accent
                            } else {
                                colors.textSecondary
                            },
                            modifier = Modifier.size(46.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = if (isCharging) "Charging" else "Not Charging",
                        style = VoltarTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary,
                        fontSize = 22.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = live?.plugType ?: if (isCharging) "Connected" else "Discharging / Idle",
                        style = VoltarTheme.typography.bodyMedium,
                        color = if (isCharging) {
                            if (colors.isDark) EmeraldPrimary else VoltarTheme.colors.accent
                        } else {
                            colors.textSecondary
                        },
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2x2 Metric Grid: Voltage, Current, Power, Temperature
            val voltage = live?.voltageVolts ?: 4.18f
            val currentMa = live?.currentMilliAmperes
            val currentText = if (currentMa != null && currentMa != 0f) {
                String.format(java.util.Locale.US, "%.2f A", kotlin.math.abs(currentMa) / 1000f)
            } else {
                "Unavailable"
            }

            val powerWatts = uiState.chargingPowerWatts
            val powerText = if (powerWatts != null && powerWatts > 0.1f) {
                String.format(java.util.Locale.US, "%.1f W", powerWatts)
            } else {
                "Unavailable"
            }

            val tempCelsius = live?.temperatureCelsius ?: 32f
            val (tempStatus, tempColor) = when {
                tempCelsius < 15f -> "Cool" to HealthFair
                tempCelsius <= 37f -> "Normal" to (if (colors.isDark) EmeraldPrimary else VoltarTheme.colors.accent)
                tempCelsius <= 42f -> "Warm" to HealthFair
                else -> "Throttling" to HealthPoor
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                MetricCard(
                    title = "Voltage",
                    value = String.format(java.util.Locale.US, "%.2f V", voltage),
                    subtitle = "Voltage",
                    icon = Icons.Outlined.ElectricBolt,
                    modifier = Modifier.weight(1f)
                )

                MetricCard(
                    title = "Current",
                    value = currentText,
                    subtitle = if (isCharging) "Charging current" else "Current",
                    icon = Icons.Outlined.Speed,
                    source = if (currentMa != null) MetricSource.HARDWARE_REPORTED else MetricSource.UNAVAILABLE,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                MetricCard(
                    title = "Power",
                    value = powerText,
                    subtitle = if (powerWatts != null) "Calculated V × I" else "Not charging",
                    icon = Icons.Outlined.Power,
                    source = if (powerWatts != null) MetricSource.CALCULATED else MetricSource.UNAVAILABLE,
                    modifier = Modifier.weight(1f)
                )

                MetricCard(
                    title = "Temperature",
                    value = "${tempCelsius.toInt()}°C",
                    subtitle = tempStatus,
                    icon = Icons.Outlined.Thermostat,
                    statusColor = tempColor,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Active Session Details
            GlassmorphicCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Session Progress",
                        style = VoltarTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary,
                        fontSize = 15.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    SessionDetailRow(
                        title = "Session Duration",
                        value = uiState.sessionDurationFormatted,
                        icon = Icons.Outlined.Timer
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    SessionDetailRow(
                        title = "Battery Level Gained",
                        value = "+${uiState.percentageGained}%",
                        icon = Icons.Outlined.Speed
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    val energyText = if (uiState.energyGainedMah != null && uiState.energyGainedMah!! > 0f) {
                        "+${uiState.energyGainedMah!!.toInt()} mAh"
                    } else {
                        "Analyzing counter..."
                    }

                    SessionDetailRow(
                        title = "Accumulated Energy",
                        value = energyText,
                        icon = Icons.Outlined.ElectricBolt
                    )
                }
            }

            Spacer(modifier = Modifier.height(90.dp))
        }
    }
}

@Composable
private fun SessionDetailRow(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    val colors = VoltarTheme.colors

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (colors.isDark) EmeraldPrimary else VoltarTheme.colors.accent,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                color = colors.textSecondary,
                fontSize = 13.sp
            )
        }

        Text(
            text = value,
            color = colors.textPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
        )
    }
}
