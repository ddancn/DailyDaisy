package com.ddancn.dailydaisy.data.database

import androidx.room.*
import com.ddancn.dailydaisy.data.entity.Habit
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/**
 * 习惯数据访问对象
 */
@Dao
interface HabitDao {
    
    @Query("SELECT * FROM habits ORDER BY createdAt DESC")
    fun getAllHabits(): Flow<List<Habit>>
    
    @Query("SELECT * FROM habits WHERE isActive = 1 ORDER BY createdAt DESC")
    fun getActiveHabits(): Flow<List<Habit>>
    
    @Query("SELECT * FROM habits WHERE id = :habitId")
    suspend fun getHabitById(habitId: Long): Habit?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: Habit): Long
    
    @Update
    suspend fun updateHabit(habit: Habit)
    
    @Delete
    suspend fun deleteHabit(habit: Habit)
    
    @Query("DELETE FROM habits WHERE id = :habitId")
    suspend fun deleteHabitById(habitId: Long)
    
    @Query("UPDATE habits SET isActive = :isActive WHERE id = :habitId")
    suspend fun updateHabitActiveStatus(habitId: Long, isActive: Boolean)
} 