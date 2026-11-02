package com.voltar.battery.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.voltar.battery.core.database.entity.BatteryObservationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BatteryObservationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertObservation(observation: BatteryObservationEntity): Long

    @Query("SELECT * FROM battery_observations ORDER BY timestamp DESC LIMIT 1")
    fun getLatestObservation(): Flow<BatteryObservationEntity?>

    @Query("SELECT * FROM battery_observations WHERE timestamp >= :sinceTimestamp ORDER BY timestamp ASC")
    fun getObservationsSince(sinceTimestamp: Long): Flow<List<BatteryObservationEntity>>

    @Query("SELECT * FROM battery_observations ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentObservations(limit: Int): Flow<List<BatteryObservationEntity>>

    @Query("SELECT COUNT(*) FROM battery_observations WHERE isValid = 1")
    fun getValidObservationCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM battery_observations")
    suspend fun getTotalCount(): Int

    @Query("SELECT MIN(timestamp) FROM battery_observations")
    suspend fun getOldestObservationTimestamp(): Long?

    @Query("DELETE FROM battery_observations WHERE timestamp < :timestamp")
    suspend fun deleteOlderThan(timestamp: Long): Int

    @Query("DELETE FROM battery_observations")
    suspend fun deleteAll()
}
