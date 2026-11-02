package com.voltar.battery.core.model

enum class MetricSource(val displayName: String) {
    HARDWARE_REPORTED("Hardware Reported"),
    CALCULATED("Calculated"),
    ESTIMATED("Estimated"),
    UNAVAILABLE("Unavailable")
}
