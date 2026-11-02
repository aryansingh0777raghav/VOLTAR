package com.voltar.battery.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.voltar.battery.core.database.entity.ChargingSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChargingSessionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: ChargingSessionEntity): Long

    @Update
    suspend fun updateSession(session: ChargingSessionEntity)

    @Query("SELECT * FROM charging_sessions WHERE isComplete = 0 ORDER BY startTime DESC LIMIT 1")
    suspend fun getActiveSession(): ChargingSessionEntity?

    @Query("SELECT * FROM charging_sessions WHERE isComplete = 0 ORDER BY startTime DESC LIMIT 1")
    fun getActiveSessionFlow(): Flow<ChargingSessionEntity?>

    @Query("SELECT * FROM charging_sessions ORDER BY startTime DESC")
    fun getAllSessions(): Flow<List<ChargingSessionEntity>>

    @Query("SELECT * FROM charging_sessions WHERE isComplete = 1 ORDER BY startTime DESC")
    fun getCompletedSessions(): Flow<List<ChargingSessionEntity>>

    @Query("SELECT * FROM charging_sessions WHERE isValidForHealthEstimation = 1 ORDER BY startTime DESC")
    suspend fun getValidSessionsForHealth(): List<ChargingSessionEntity>

    @Query("SELECT * FROM charging_sessions WHERE isValidForHealthEstimation = 1 ORDER BY startTime DESC")
    fun getValidSessionsForHealthFlow(): Flow<List<ChargingSessionEntity>>

    @Query("SELECT COUNT(*) FROM charging_sessions WHERE isValidForHealthEstimation = 1")
    fun getValidSessionCount(): Flow<Int>

    @Query("SELECT * FROM charging_sessions WHERE id = :id LIMIT 1")
    suspend fun getSessionById(id: Long): ChargingSessionEntity?

    @Query("DELETE FROM charging_sessions")
    suspend fun deleteAll()
}
