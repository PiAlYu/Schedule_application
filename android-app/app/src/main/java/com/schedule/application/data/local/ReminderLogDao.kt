package com.schedule.application.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ReminderLogDao {
    @Query("SELECT EXISTS(SELECT 1 FROM reminder_logs WHERE id = :id)")
    suspend fun exists(id: String): Boolean

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entity: ReminderLogEntity)

    @Query("DELETE FROM reminder_logs WHERE created_at < :threshold")
    suspend fun deleteOlderThan(threshold: Long)
}
