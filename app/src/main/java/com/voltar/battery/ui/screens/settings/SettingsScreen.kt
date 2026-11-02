package com.voltar.battery.ui.screens.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ColorLens
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Sensors
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.RadioButtonChecked
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.voltar.battery.R
import com.voltar.battery.core.datastore.ThemeMode
import com.voltar.battery.ui.components.CreatorProfileBottomSheet
import com.voltar.battery.ui.components.CreatorSignatureButton
import com.voltar.battery.ui.components.GlassmorphicCard
import com.voltar.battery.ui.theme.EmeraldPrimary
import com.voltar.battery.ui.theme.HealthPoor
import com.voltar.battery.ui.theme.VoltarTheme

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val colors = VoltarTheme.colors
    val context = LocalContext.current
    val prefs = uiState.userPreferences
    val profile = uiState.deviceProfile

    var customCapacityInput by remember {
        mutableStateOf(profile?.designCapacityMah?.toInt()?.toString() ?: "5000")
    }

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
                text = "Settings",
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

            // 1. Appearance Section
            SettingsSectionHeader(title = "APPEARANCE")

            GlassmorphicCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    ThemeOptionRow(
                        title = "Light",
                        description = "Clean, bright interface (Default)",
                        isSelected = prefs.themeMode == ThemeMode.LIGHT,
                        onClick = { viewModel.setThemeMode(ThemeMode.LIGHT) }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    ThemeOptionRow(
                        title = "Dark",
                        description = "Premium minimalist dark interface",
                        isSelected = prefs.themeMode == ThemeMode.DARK,
                        onClick = { viewModel.setThemeMode(ThemeMode.DARK) }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    ThemeOptionRow(
                        title = "System",
                        description = "Follow device system theme",
                        isSelected = prefs.themeMode == ThemeMode.SYSTEM,
                        onClick = { viewModel.setThemeMode(ThemeMode.SYSTEM) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 2. Monitoring Section
            SettingsSectionHeader(title = "BACKGROUND MONITORING")

            GlassmorphicCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Periodic Data Collection",
                                style = VoltarTheme.typography.titleMedium,
                                color = colors.textPrimary,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Captures low-power periodic battery snapshots with WorkManager.",
                                style = VoltarTheme.typography.bodySmall ?: VoltarTheme.typography.bodyMedium,
                                color = colors.textSecondary,
                                fontSize = 12.sp
                            )
                        }

                        Switch(
                            checked = prefs.isMonitoringEnabled,
                            onCheckedChange = { viewModel.setMonitoringEnabled(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = if (colors.isDark) EmeraldPrimary else VoltarTheme.colors.accent
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Collection Interval Selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Collection Interval",
                            style = VoltarTheme.typography.bodyMedium,
                            color = colors.textPrimary,
                            fontSize = 13.sp
                        )

                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(colors.surfaceVariant)
                                .padding(2.dp)
                        ) {
                            listOf(15, 30, 60).forEach { interval ->
                                val isSelected = prefs.collectionIntervalMinutes == interval
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) colors.cardBackground else Color.Transparent)
                                        .clickable { viewModel.setCollectionInterval(interval) }
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${interval}m",
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) colors.textPrimary else colors.textSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 2b. Homescreen Widgets Section
            SettingsSectionHeader(title = "HOMESCREEN WIDGET (ONE UI 9)")

            GlassmorphicCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Glass Blur and Translucency",
                        style = VoltarTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Adjust the frosted glass blur intensity and background opacity for all homescreen widgets (One UI 9 inspired).",
                        color = colors.textSecondary,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            25 to "25% Clear",
                            50 to "50% Trans",
                            75 to "75% Frost",
                            100 to "100% Solid"
                        ).forEach { (pct, label) ->
                            val isSelected = prefs.widgetGlassOpacity == pct
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (isSelected) {
                                            if (colors.isDark) EmeraldPrimary else VoltarTheme.colors.accent
                                        } else colors.surfaceVariant
                                    )
                                    .clickable { viewModel.setWidgetGlassOpacity(context, pct) }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else colors.textPrimary
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 3. Hardware & Device Profile Section
            SettingsSectionHeader(title = "DEVICE HARDWARE SPECS")

            GlassmorphicCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    HardwareSpecRow(
                        label = "Device Model",
                        value = "${profile?.manufacturer} ${profile?.model}"
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    HardwareSpecRow(
                        label = "Android Version",
                        value = "Android ${profile?.androidVersion} (API ${profile?.apiLevel})"
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Design Capacity",
                                color = colors.textSecondary,
                                fontSize = 12.sp
                            )
                            Text(
                                text = "${profile?.designCapacityMah?.toInt() ?: 5000} mAh" +
                                        if (profile?.isCustomDesignCapacity == true) " (Custom)" else if (profile?.isDesignCapacityVerified == true) " (Verified)" else " (Standard)",
                                color = colors.textPrimary,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                        }

                        TextButton(onClick = { viewModel.showCustomCapacityDialog(true) }) {
                            Text(
                                text = "Edit",
                                color = if (colors.isDark) EmeraldPrimary else VoltarTheme.colors.accent,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    HardwareSpecRow(
                        label = "Hardware Charge-Counter",
                        value = if (profile?.supportsChargeCounter == true) "Supported" else "Emulated / Fallback"
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    HardwareSpecRow(
                        label = "Instant Current Sensor",
                        value = if (profile?.supportsInstantCurrent == true) "Supported" else "Unavailable on OEM"
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 4. Data Management Section
            SettingsSectionHeader(title = "DATA MANAGEMENT")

            GlassmorphicCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    ActionRow(
                        icon = Icons.Outlined.FileDownload,
                        title = "Export Diagnostics as CSV",
                        subtitle = "Tabular records of battery levels, voltages, and temperatures",
                        onClick = { viewModel.exportData(context, asCsv = true) }
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    ActionRow(
                        icon = Icons.Outlined.FileDownload,
                        title = "Export Diagnostics as JSON",
                        subtitle = "Structured JSON file of device profile and sessions",
                        onClick = { viewModel.exportData(context, asCsv = false) }
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    ActionRow(
                        icon = Icons.Outlined.Refresh,
                        title = "Reset Battery Health Analysis",
                        subtitle = "Clears computed health models and starts fresh recalculation",
                        onClick = { viewModel.showResetAnalysisDialog(true) }
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    ActionRow(
                        icon = Icons.Outlined.DeleteOutline,
                        title = "Clear All Local Data",
                        subtitle = "Permanently erases all observations, sessions, and preferences",
                        titleColor = HealthPoor,
                        onClick = { viewModel.showClearHistoryDialog(true) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 5. Creator Signature & Profile
            SettingsSectionHeader(title = "ARCHITECT & CREATOR")

            GlassmorphicCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                onClick = { viewModel.showCreatorSheet(true) }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .border(
                                    width = 1.5.dp,
                                    color = if (colors.isDark) EmeraldPrimary else VoltarTheme.colors.accent,
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.creator_aryan_singh),
                                contentDescription = "Aryan Singh",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = "Aryan Singh",
                                style = VoltarTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "Creator & Lead Platform Architect",
                                color = if (colors.isDark) EmeraldPrimary else VoltarTheme.colors.accent,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    CreatorSignatureButton(
                        onClick = { viewModel.showCreatorSheet(true) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 6. About App Section
            SettingsSectionHeader(title = "ABOUT VOLTAR")

            GlassmorphicCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "VOLTAR 1.0.0 (Build 1)",
                        style = VoltarTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Real Battery Intelligence. No Fake Numbers.",
                        color = if (colors.isDark) EmeraldPrimary else VoltarTheme.colors.accent,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Privacy First: All database operations, health modeling, and charging analysis execute strictly on this device. No network telemetry. No accounts required.",
                        color = colors.textSecondary,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(100.dp))
        }
    }

    // Custom Design Capacity Dialog
    if (uiState.isCustomCapacityDialogVisible) {
        AlertDialog(
            onDismissRequest = { viewModel.showCustomCapacityDialog(false) },
            title = {
                Text(
                    text = "Configure Factory Design Capacity",
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
            },
            text = {
                Column {
                    Text(
                        text = "Enter your phone manufacturer's official rated battery capacity (mAh) to ensure precise health calculations.",
                        color = colors.textSecondary,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = customCapacityInput,
                        onValueChange = { customCapacityInput = it },
                        label = { Text("Capacity in mAh") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val cap = customCapacityInput.toFloatOrNull()
                        if (cap != null && cap in 1000f..12000f) {
                            viewModel.setCustomDesignCapacity(cap)
                        }
                        viewModel.showCustomCapacityDialog(false)
                    }
                ) {
                    Text("Save", color = if (colors.isDark) EmeraldPrimary else VoltarTheme.colors.accent)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.showCustomCapacityDialog(false) }) {
                    Text("Cancel", color = colors.textSecondary)
                }
            },
            containerColor = colors.cardBackground
        )
    }

    // Reset Analysis Confirmation Dialog
    if (uiState.isResetAnalysisDialogVisible) {
        AlertDialog(
            onDismissRequest = { viewModel.showResetAnalysisDialog(false) },
            title = {
                Text(
                    text = "Reset Health Analysis?",
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
            },
            text = {
                Text(
                    text = "This will recalculate your battery health models using all recorded valid charging sessions. Your underlying logs will be preserved.",
                    color = colors.textSecondary,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.resetAnalysisOnly() }) {
                    Text("Reset Analysis", color = if (colors.isDark) EmeraldPrimary else VoltarTheme.colors.accent)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.showResetAnalysisDialog(false) }) {
                    Text("Cancel", color = colors.textSecondary)
                }
            },
            containerColor = colors.cardBackground
        )
    }

    // Clear All History Dialog
    if (uiState.isClearHistoryDialogVisible) {
        AlertDialog(
            onDismissRequest = { viewModel.showClearHistoryDialog(false) },
            title = {
                Text(
                    text = "Clear All Local Data?",
                    fontWeight = FontWeight.Bold,
                    color = HealthPoor
                )
            },
            text = {
                Text(
                    text = "Are you sure? This will delete all charging sessions, battery logs, and computed health history. This action cannot be undone.",
                    color = colors.textSecondary,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.clearAllData() }) {
                    Text("Delete Everything", color = HealthPoor, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.showClearHistoryDialog(false) }) {
                    Text("Cancel", color = colors.textSecondary)
                }
            },
            containerColor = colors.cardBackground
        )
    }

    // Creator Profile Bottom Sheet
    if (uiState.isCreatorSheetVisible) {
        CreatorProfileBottomSheet(
            onDismissRequest = { viewModel.showCreatorSheet(false) }
        )
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    val colors = VoltarTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 4.dp, bottom = 8.dp)
    ) {
        Text(
            text = title,
            style = VoltarTheme.typography.labelSmall,
            color = colors.textSecondary,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
    }
}

@Composable
private fun ThemeOptionRow(
    title: String,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val colors = VoltarTheme.colors

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
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
                text = description,
                style = VoltarTheme.typography.bodySmall ?: VoltarTheme.typography.bodyMedium,
                color = colors.textSecondary,
                fontSize = 12.sp
            )
        }

        Icon(
            imageVector = if (isSelected) Icons.Rounded.RadioButtonChecked else Icons.Rounded.RadioButtonUnchecked,
            contentDescription = null,
            tint = if (isSelected) (if (colors.isDark) EmeraldPrimary else VoltarTheme.colors.accent) else colors.textSecondary,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun HardwareSpecRow(label: String, value: String) {
    val colors = VoltarTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = colors.textSecondary, fontSize = 12.sp)
        Text(text = value, color = colors.textPrimary, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
    }
}

@Composable
private fun ActionRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    titleColor: Color? = null,
    onClick: () -> Unit
) {
    val colors = VoltarTheme.colors

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(colors.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = titleColor ?: (if (colors.isDark) EmeraldPrimary else VoltarTheme.colors.accent),
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = VoltarTheme.typography.titleMedium,
                color = titleColor ?: colors.textPrimary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp
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
}
