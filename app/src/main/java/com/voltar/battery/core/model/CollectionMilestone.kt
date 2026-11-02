package com.voltar.battery.core.model

enum class CollectionMilestone(
    val title: String,
    val description: String,
    val minObservationDays: Int,
    val minSessionsRequired: Int
) {
    INITIAL_24H(
        title = "First 24 Hours",
        description = "Initial battery analysis is being prepared. More measurements will help improve the estimate.",
        minObservationDays = 1,
        minSessionsRequired = 1
    ),
    SEVEN_DAYS(
        title = "7-Day Analysis",
        description = "More reliable estimate after additional charging sessions and consistency validation.",
        minObservationDays = 7,
        minSessionsRequired = 3
    ),
    FOURTEEN_DAYS(
        title = "14-Day Analysis",
        description = "Longer-term measurement, capacity trend validation, and highest confidence score.",
        minObservationDays = 14,
        minSessionsRequired = 6
    );

    companion object {
        fun currentMilestone(elapsedDays: Int, validSessions: Int): CollectionMilestone {
            return when {
                elapsedDays >= 14 && validSessions >= 6 -> FOURTEEN_DAYS
                elapsedDays >= 7 && validSessions >= 3 -> SEVEN_DAYS
                else -> INITIAL_24H
            }
        }
    }
}
