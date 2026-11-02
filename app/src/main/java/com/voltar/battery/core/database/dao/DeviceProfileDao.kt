package com.voltar.battery.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.voltar.battery.core.database.entity.DeviceProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DeviceProfileDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: DeviceProfileEntity)

    @Query("SELECT * FROM device_profile WHERE id = 1 LIMIT 1")
    fun getProfileFlow(): Flow<DeviceProfileEntity?>

    @Query("SELECT * FROM device_profile WHERE id = 1 LIMIT 1")
    suspend fun getProfileSync(): DeviceProfileEntity?
}
