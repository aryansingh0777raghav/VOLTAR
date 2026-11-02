package com.voltar.battery.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.voltar.battery.core.database.entity.HealthAnalysisEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HealthAnalysisDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnalysis(analysis: HealthAnalysisEntity): Long

    @Query("SELECT * FROM health_analyses ORDER BY timestamp DESC LIMIT 1")
    fun getLatestAnalysis(): Flow<HealthAnalysisEntity?>

    @Query("SELECT * FROM health_analyses ORDER BY timestamp ASC")
    fun getAllAnalyses(): Flow<List<HealthAnalysisEntity>>

    @Query("SELECT * FROM health_analyses WHERE timestamp >= :sinceTimestamp ORDER BY timestamp ASC")
    fun getAnalysesSince(sinceTimestamp: Long): Flow<List<HealthAnalysisEntity>>

    @Query("DELETE FROM health_analyses")
    suspend fun deleteAll()
}
