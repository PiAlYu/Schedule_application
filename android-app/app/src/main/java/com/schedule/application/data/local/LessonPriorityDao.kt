package com.schedule.application.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LessonPriorityDao {
    @Query(
        """
        SELECT *
        FROM lesson_priorities
        WHERE group_name = :groupName AND day_of_week = :dayOfWeek
        ORDER BY pair_number
        """
    )
    fun observeDayPriorities(groupName: String, dayOfWeek: Int): Flow<List<LessonPriorityEntity>>

    @Query(
        """
        SELECT *
        FROM lesson_priorities
        WHERE group_name = :groupName
        """
    )
    suspend fun getAllForGroup(groupName: String): List<LessonPriorityEntity>

    @Query(
        """
        SELECT *
        FROM lesson_priorities
        WHERE group_name = :groupName AND day_of_week = :dayOfWeek AND pair_number = :pairNumber
        """
    )
    suspend fun getPriority(groupName: String, dayOfWeek: Int, pairNumber: Int): LessonPriorityEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: LessonPriorityEntity)
}
