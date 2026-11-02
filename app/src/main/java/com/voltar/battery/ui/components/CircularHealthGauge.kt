package com.voltar.battery.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BatteryStd
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.voltar.battery.core.model.BatteryHealthCategory
import com.voltar.battery.ui.theme.HealthFair
import com.voltar.battery.ui.theme.HealthGood
import com.voltar.battery.ui.theme.HealthPoor
import com.voltar.battery.ui.theme.HealthUnknown
import com.voltar.battery.ui.theme.HealthWeak
import com.voltar.battery.ui.theme.VoltarTheme

@Composable
fun CircularHealthGauge(
    healthPercentage: Float?,
    category: BatteryHealthCategory,
    estimatedCapacityMah: Float?,
    designCapacityMah: Float,
    isHardwareVerified: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = 230.dp,
    strokeWidth: Dp = 14.dp
) {
    val colors = VoltarTheme.colors
    val targetProgress = (healthPercentage ?: 0f) / 100f

    val animatedProgress by animateFloatAsState(
        targetValue = targetProgress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "HealthProgress"
    )

    val (statusColor, statusBgColor) = when (category) {
        BatteryHealthCategory.GOOD -> HealthGood to HealthGood.copy(alpha = 0.16f)
        BatteryHealthCategory.FAIR -> HealthFair to HealthFair.copy(alpha = 0.16f)
        BatteryHealthCategory.WEAK -> HealthWeak to HealthWeak.copy(alpha = 0.16f)
        BatteryHealthCategory.POOR -> HealthPoor to HealthPoor.copy(alpha = 0.16f)
        BatteryHealthCategory.INSUFFICIENT_DATA -> HealthUnknown to HealthUnknown.copy(alpha = 0.16f)
    }

    val trackColor = if (colors.isDark) Color(0xFF222222) else Color(0xFFE8ECEF)

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        // Background track and progress arc
        Canvas(modifier = Modifier.fillMaxSize().padding(strokeWidth / 2)) {
            // Full background circle
            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            )

            // Progress arc
            if (animatedProgress > 0f) {
                drawArc(
                    color = statusColor,
                    startAngle = -90f,
                    sweepAngle = 360f * animatedProgress,
                    useCenter = false,
                    style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
                )
            }
        }

        // Center Information
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        ) {
            // Icon
            Icon(
                imageVector = Icons.Rounded.BatteryStd,
                contentDescription = null,
                tint = statusColor,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Battery Health",
                style = VoltarTheme.typography.titleMedium,
                color = colors.textSecondary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(2.dp))

            // Percentage
            Text(
                text = if (healthPercentage != null && healthPercentage > 0) "${healthPercentage.toInt()}%" else "--%",
                style = VoltarTheme.typography.displayMedium,
                color = colors.textPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 40.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Health Status Pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(statusBgColor)
                    .padding(horizontal = 14.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = category.label,
                    color = statusColor,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Capacity Details
            val capacityText = if (estimatedCapacityMah != null) {
                "${estimatedCapacityMah.toInt()} mAh / ${designCapacityMah.toInt()} mAh"
            } else {
                "Target: ${designCapacityMah.toInt()} mAh"
            }
            val statusPrefix = if (isHardwareVerified) "Verified Capacity" else "Estimated Capacity"

            Text(
                text = statusPrefix,
                color = colors.textSecondary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = capacityText,
                color = colors.textPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
