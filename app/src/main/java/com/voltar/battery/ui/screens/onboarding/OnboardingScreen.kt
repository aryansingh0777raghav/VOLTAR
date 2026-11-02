package com.voltar.battery.ui.screens.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.outlined.ElectricBolt
import androidx.compose.material.icons.outlined.QueryBuilder
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.voltar.battery.ui.theme.VoltarTheme

@Composable
fun OnboardingScreen(
    onStartClicked: () -> Unit
) {
    val colors = VoltarTheme.colors

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Spacer(modifier = Modifier.height(24.dp))

            // Brand Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (colors.isDark) Color(0xFF1B1B1B) else Color(0xFF111111)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ElectricBolt,
                        contentDescription = null,
                        tint = EmeraldPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "VOLTAR",
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 2.sp,
                        color = colors.textPrimary,
                        fontSize = 18.sp
                    )
                    Text(
                        text = "Real Battery Intelligence",
                        color = colors.textSecondary,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Understanding\nYour Battery Health",
                style = VoltarTheme.typography.headlineLarge,
                color = colors.textPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
                lineHeight = 34.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "No fabricated numbers. VOLTAR provides transparent, data-driven battery diagnostics based strictly on actual hardware measurements.",
                style = VoltarTheme.typography.bodyLarge,
                color = colors.textSecondary,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Feature 1: Percentage vs Health
            OnboardingPointCard(
                icon = Icons.Outlined.Speed,
                title = "Battery Level vs. Health",
                description = "Battery Level (0–100%) indicates current stored energy. Battery Health measures permanent capacity degradation compared to factory design."
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Feature 2: Progressive Analysis
            OnboardingPointCard(
                icon = Icons.Outlined.QueryBuilder,
                title = "Progressive Intelligence (24h / 7d / 14d)",
                description = "Immediate baseline on Day 1. Repeated charge-counter analysis over 7–14 days filters transient noise and dramatically improves accuracy."
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Feature 3: Hardware Transparency & Privacy
            OnboardingPointCard(
                icon = Icons.Outlined.Security,
                title = "Hardware Transparency & 100% Local",
                description = "Never requires root. Explains manufacturer API differences honestly. All diagnostic data remains safely stored on your device."
            )
        }

        Column(modifier = Modifier.padding(vertical = 24.dp)) {
            Button(
                onClick = onStartClicked,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (colors.isDark) EmeraldPrimary else VoltarTheme.colors.accent,
                    contentColor = if (colors.isDark) Color.Black else Color.White
                )
            ) {
                Text(
                    text = "Get Started",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun OnboardingPointCard(
    icon: ImageVector,
    title: String,
    description: String
) {
    val colors = VoltarTheme.colors

    GlassmorphicCard(
        shape = RoundedCornerShape(18.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(
                        (if (colors.isDark) EmeraldPrimary else VoltarTheme.colors.accent).copy(alpha = 0.12f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (colors.isDark) EmeraldPrimary else VoltarTheme.colors.accent,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = VoltarTheme.typography.titleMedium,
                    color = colors.textPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = VoltarTheme.typography.bodyMedium,
                    color = colors.textSecondary,
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )
            }
        }
    }
}
