package com.voltar.battery.ui.screens.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.voltar.battery.ui.theme.EmeraldPrimary
import com.voltar.battery.ui.theme.VoltarTheme
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {
    val colors = VoltarTheme.colors
    val scale = remember { Animatable(0.85f) }
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        alpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
        )
        scale.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
        )
        delay(1200)
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .scale(scale.value)
                .alpha(alpha.value)
        ) {
            // Minimalist Brand Emblem
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(if (colors.isDark) Color(0xFF161616) else Color(0xFF111111)),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(46.dp)) {
                    val path = Path().apply {
                        val w = size.width
                        val h = size.height
                        moveTo(w * 0.54f, h * 0.16f)
                        lineTo(w * 0.30f, h * 0.52f)
                        lineTo(w * 0.52f, h * 0.52f)
                        lineTo(w * 0.44f, h * 0.84f)
                        lineTo(w * 0.72f, h * 0.46f)
                        lineTo(w * 0.50f, h * 0.46f)
                        close()
                    }
                    drawPath(path, color = EmeraldPrimary)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // App Name
            Text(
                text = "VOLTAR",
                style = VoltarTheme.typography.displayMedium,
                fontWeight = FontWeight.Black,
                letterSpacing = 4.sp,
                color = colors.textPrimary,
                fontSize = 32.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Brand Positioning
            Text(
                text = "ADVANCED RELIABILITY",
                style = VoltarTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 2.sp,
                color = if (colors.isDark) EmeraldPrimary else VoltarTheme.colors.accent,
                fontSize = 11.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Core Promise
            Text(
                text = "Real Battery Intelligence. No Fake Numbers.",
                style = VoltarTheme.typography.bodyMedium,
                color = colors.textSecondary,
                fontSize = 13.sp
            )
        }
    }
}
