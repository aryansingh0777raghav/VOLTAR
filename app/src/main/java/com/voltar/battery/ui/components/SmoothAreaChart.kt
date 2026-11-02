package com.voltar.battery.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.voltar.battery.ui.theme.EmeraldDark
import com.voltar.battery.ui.theme.EmeraldPrimary
import com.voltar.battery.ui.theme.VoltarTheme

data class ChartPoint(
    val timestamp: Long,
    val value: Float,
    val label: String = ""
)

@Composable
fun SmoothAreaChart(
    points: List<ChartPoint>,
    modifier: Modifier = Modifier,
    lineColor: Color = EmeraldPrimary,
    minY: Float? = null,
    maxY: Float? = null,
    valueSuffix: String = "%",
    timeLabels: List<String> = listOf("24h", "7d", "14d", "30d")
) {
    val colors = VoltarTheme.colors
    val effectivePoints = if (points.size >= 2) {
        points
    } else if (points.size == 1) {
        listOf(points[0].copy(value = points[0].value), points[0])
    } else {
        emptyList()
    }

    Column(modifier = modifier.fillMaxWidth()) {
        if (effectivePoints.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Awaiting continuous observations",
                    color = colors.textSecondary,
                    fontSize = 13.sp
                )
            }
        } else {
            val computedMin = minY ?: (effectivePoints.minOf { it.value } * 0.95f)
            val computedMax = maxY ?: (effectivePoints.maxOf { it.value } * 1.05f).coerceAtLeast(computedMin + 1f)
            val yRange = (computedMax - computedMin).coerceAtLeast(0.01f)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
            ) {
                // Canvas Line and Area
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    val width = size.width
                    val height = size.height
                    val spacing = width / (effectivePoints.size - 1).coerceAtLeast(1)

                    val coords = effectivePoints.mapIndexed { index, point ->
                        val x = index * spacing
                        val normalizedY = ((point.value - computedMin) / yRange).coerceIn(0f, 1f)
                        val y = height - (normalizedY * height)
                        Offset(x, y)
                    }

                    // Build Cubic Bezier Path
                    val strokePath = Path().apply {
                        if (coords.isNotEmpty()) {
                            moveTo(coords.first().x, coords.first().y)
                            for (i in 0 until coords.size - 1) {
                                val current = coords[i]
                                val next = coords[i + 1]
                                val control1 = Offset((current.x + next.x) / 2f, current.y)
                                val control2 = Offset((current.x + next.x) / 2f, next.y)
                                cubicTo(control1.x, control1.y, control2.x, control2.y, next.x, next.y)
                            }
                        }
                    }

                    // Build Gradient Fill Path
                    val fillPath = Path().apply {
                        addPath(strokePath)
                        if (coords.isNotEmpty()) {
                            lineTo(coords.last().x, height)
                            lineTo(coords.first().x, height)
                            close()
                        }
                    }

                    // Draw gradient fill
                    val gradient = Brush.verticalGradient(
                        colors = listOf(
                            lineColor.copy(alpha = 0.35f),
                            lineColor.copy(alpha = 0.02f)
                        ),
                        startY = 0f,
                        endY = height
                    )
                    drawPath(path = fillPath, brush = gradient)

                    // Draw smooth curve stroke
                    drawPath(
                        path = strokePath,
                        color = lineColor,
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // Draw data dots
                    coords.forEachIndexed { idx, offset ->
                        if (idx == 0 || idx == coords.size - 1 || idx % 2 == 0) {
                            drawCircle(
                                color = colors.cardBackground,
                                radius = 4.dp.toPx(),
                                center = offset
                            )
                            drawCircle(
                                color = lineColor,
                                radius = 2.5.dp.toPx(),
                                center = offset
                            )
                        }
                    }
                }

                // Value badge for the latest point (aligned at right)
                val lastPoint = effectivePoints.lastOrNull()
                if (lastPoint != null) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(end = 8.dp)
                            .background(
                                color = lineColor,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "${lastPoint.value.toInt()}$valueSuffix",
                            color = Color.Black,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Time labels row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            timeLabels.forEach { label ->
                Text(
                    text = label,
                    style = VoltarTheme.typography.bodySmall ?: VoltarTheme.typography.bodyMedium,
                    color = colors.textSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
