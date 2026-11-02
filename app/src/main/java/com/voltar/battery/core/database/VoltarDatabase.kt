package com.voltar.battery.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.voltar.battery.core.database.dao.BatteryObservationDao
import com.voltar.battery.core.database.dao.ChargingSessionDao
import com.voltar.battery.core.database.dao.DeviceProfileDao
import com.voltar.battery.core.database.dao.HealthAnalysisDao
import com.voltar.battery.core.database.entity.BatteryObservationEntity
import com.voltar.battery.core.database.entity.ChargingSessionEntity
import com.voltar.battery.core.database.entity.DeviceProfileEntity
import com.voltar.battery.core.database.entity.HealthAnalysisEntity

@Database(
    entities = [
        BatteryObservationEntity::class,
        ChargingSessionEntity::class,
        HealthAnalysisEntity::class,
        DeviceProfileEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class VoltarDatabase : RoomDatabase() {
    abstract fun batteryObservationDao(): BatteryObservationDao
    abstract fun chargingSessionDao(): ChargingSessionDao
    abstract fun healthAnalysisDao(): HealthAnalysisDao
    abstract fun deviceProfileDao(): DeviceProfileDao

    companion object {
        @Volatile
        private var INSTANCE: VoltarDatabase? = null

        fun getInstance(context: Context): VoltarDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    VoltarDatabase::class.java,
                    "voltar_battery.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
