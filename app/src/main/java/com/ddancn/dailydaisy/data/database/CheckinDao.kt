package com.ddancn.dailydaisy.data.database

import androidx.room.*
import com.ddancn.dailydaisy.data.entity.Checkin
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * 打卡记录数据访问对象
 */
@Dao
interface CheckinDao {
    
    @Query("SELECT * FROM checkins WHERE habitId = :habitId ORDER BY checkinTime DESC")
    fun getCheckinsByHabitId(habitId: Long): Flow<List<Checkin>>
    
    @Query("SELECT * FROM checkins WHERE habitId = :habitId AND date(checkinTime) = date(:date)")
    suspend fun getCheckinsByHabitIdAndDate(habitId: Long, date: LocalDate): List<Checkin>
    
    @Query("SELECT COUNT(*) FROM checkins WHERE habitId = :habitId AND date(checkinTime) = date(:date)")
    suspend fun getCheckinCountByHabitIdAndDate(habitId: Long, date: LocalDate): Int
    
    @Query("SELECT SUM(count) FROM checkins WHERE habitId = :habitId AND date(checkinTime) = date(:date)")
    suspend fun getTotalCountByHabitIdAndDate(habitId: Long, date: LocalDate): Int?
    
    // 获取习惯的总打卡次数
    @Query("SELECT SUM(count) FROM checkins WHERE habitId = :habitId")
    suspend fun getTotalCountByHabitId(habitId: Long): Int?
    
    // 获取所有打卡记录（用于监听总次数变化）
    @Query("SELECT * FROM checkins")
    fun getAllCheckins(): Flow<List<Checkin>>
    
    // 获取今日所有打卡记录（用于监听变化）
    @Query("SELECT * FROM checkins WHERE date(checkinTime) = date(:date)")
    fun getTodayCheckins(date: LocalDate): Flow<List<Checkin>>
    
    // 获取本周所有打卡记录（用于监听变化）
    @Query("SELECT * FROM checkins WHERE checkinTime >= :startDate AND checkinTime < :endDate")
    fun getWeekCheckins(startDate: LocalDateTime, endDate: LocalDateTime): Flow<List<Checkin>>
    
    // 获取本月所有打卡记录（用于监听变化）
    @Query("SELECT * FROM checkins WHERE checkinTime >= :startDate AND checkinTime < :endDate")
    fun getMonthCheckins(startDate: LocalDateTime, endDate: LocalDateTime): Flow<List<Checkin>>
    
    // 获取指定时间段内的打卡总次数
    @Query("SELECT SUM(count) FROM checkins WHERE habitId = :habitId AND checkinTime >= :startDate AND checkinTime < :endDate")
    suspend fun getTotalCountByHabitIdAndDateRange(habitId: Long, startDate: LocalDateTime, endDate: LocalDateTime): Int?
    
    // 获取指定时间段内最新的打卡记录
    @Query("SELECT * FROM checkins WHERE habitId = :habitId AND checkinTime >= :startDate AND checkinTime < :endDate ORDER BY checkinTime DESC LIMIT 1")
    suspend fun getLatestCheckinByHabitIdAndDateRange(habitId: Long, startDate: LocalDateTime, endDate: LocalDateTime): Checkin?
    
    @Query("SELECT * FROM checkins WHERE habitId = :habitId AND checkinTime >= :startDate AND checkinTime <= :endDate ORDER BY checkinTime DESC")
    suspend fun getCheckinsByHabitIdAndDateRange(habitId: Long, startDate: LocalDateTime, endDate: LocalDateTime): List<Checkin>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCheckin(checkin: Checkin): Long
    
    @Update
    suspend fun updateCheckin(checkin: Checkin)
    
    @Delete
    suspend fun deleteCheckin(checkin: Checkin)
    
    @Query("DELETE FROM checkins WHERE id = :checkinId")
    suspend fun deleteCheckinById(checkinId: Long)
    
    @Query("DELETE FROM checkins WHERE habitId = :habitId")
    suspend fun deleteCheckinsByHabitId(habitId: Long)
} 