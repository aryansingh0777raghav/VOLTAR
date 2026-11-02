package com.voltar.battery.core.model

enum class BatteryHealthCategory(val label: String) {
    GOOD("Good"),
    FAIR("Fair"),
    WEAK("Weak"),
    POOR("Poor"),
    INSUFFICIENT_DATA("Insufficient Data");

    companion object {
        fun fromPercentage(percentage: Float?): BatteryHealthCategory {
            if (percentage == null || percentage <= 0f) return INSUFFICIENT_DATA
            return when {
                percentage >= 85f -> GOOD
                percentage >= 75f -> FAIR
                percentage >= 60f -> WEAK
                else -> POOR
            }
        }
    }
}
