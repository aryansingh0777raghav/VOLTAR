package com.voltar.battery

import com.voltar.battery.core.model.BatteryHealthCategory
import com.voltar.battery.core.model.CollectionMilestone
import com.voltar.battery.core.model.MetricSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BatteryDiagnosticsTest {

    @Test
    fun `BatteryHealthCategory maps percentage correctly`() {
        assertEquals(BatteryHealthCategory.GOOD, BatteryHealthCategory.fromPercentage(95f))
        assertEquals(BatteryHealthCategory.GOOD, BatteryHealthCategory.fromPercentage(85f))
        assertEquals(BatteryHealthCategory.FAIR, BatteryHealthCategory.fromPercentage(84f))
        assertEquals(BatteryHealthCategory.FAIR, BatteryHealthCategory.fromPercentage(75f))
        assertEquals(BatteryHealthCategory.WEAK, BatteryHealthCategory.fromPercentage(74f))
        assertEquals(BatteryHealthCategory.WEAK, BatteryHealthCategory.fromPercentage(60f))
        assertEquals(BatteryHealthCategory.POOR, BatteryHealthCategory.fromPercentage(59f))
        assertEquals(BatteryHealthCategory.INSUFFICIENT_DATA, BatteryHealthCategory.fromPercentage(null))
        assertEquals(BatteryHealthCategory.INSUFFICIENT_DATA, BatteryHealthCategory.fromPercentage(0f))
    }

    @Test
    fun `CollectionMilestone progression checks`() {
        // Less than 7 days
        assertEquals(
            CollectionMilestone.INITIAL_24H,
            CollectionMilestone.currentMilestone(elapsedDays = 3, validSessions = 2)
        )

        // 7 days and 3 sessions
        assertEquals(
            CollectionMilestone.SEVEN_DAYS,
            CollectionMilestone.currentMilestone(elapsedDays = 7, validSessions = 3)
        )

        // 14 days and 6 sessions
        assertEquals(
            CollectionMilestone.FOURTEEN_DAYS,
            CollectionMilestone.currentMilestone(elapsedDays = 14, validSessions = 6)
        )

        // 14 days but only 2 sessions -> stays at Initial/Lower
        assertEquals(
            CollectionMilestone.INITIAL_24H,
            CollectionMilestone.currentMilestone(elapsedDays = 14, validSessions = 2)
        )
    }

    @Test
    fun `MetricSource display names are distinct and accurate`() {
        assertEquals("Hardware Reported", MetricSource.HARDWARE_REPORTED.displayName)
        assertEquals("Calculated", MetricSource.CALCULATED.displayName)
        assertEquals("Estimated", MetricSource.ESTIMATED.displayName)
        assertEquals("Unavailable", MetricSource.UNAVAILABLE.displayName)
    }

    @Test
    fun `BottomNavDestination items are properly initialized and non-null`() {
        val items = com.voltar.battery.ui.components.BottomNavDestination.items
        assertEquals(5, items.size)
        items.forEach { dest ->
            org.junit.Assert.assertNotNull(dest)
            org.junit.Assert.assertNotNull(dest.route)
            org.junit.Assert.assertNotNull(dest.title)
            org.junit.Assert.assertNotNull(dest.icon)
            assertTrue(dest.route.isNotEmpty())
        }
    }
}
