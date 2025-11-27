package com.ddancn.dailydaisy.data.repository

import com.ddancn.dailydaisy.data.database.HabitDao
import com.ddancn.dailydaisy.data.database.CheckinDao
import com.ddancn.dailydaisy.data.entity.Habit
import com.ddancn.dailydaisy.data.entity.Checkin
import com.ddancn.dailydaisy.data.model.HabitWithData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * 习惯仓库类
 * 封装习惯相关的数据操作
 */
class HabitRepository(
    private val habitDao: HabitDao,
    private val checkinDao: CheckinDao
) {
    
    // 获取所有习惯
    fun getAllHabits(): Flow<List<Habit>> = habitDao.getAllHabits()
    
    // 获取激活的习惯
    fun getActiveHabits(): Flow<List<Habit>> = habitDao.getActiveHabits()
    
    // 根据ID获取习惯
    suspend fun getHabitById(habitId: Long): Habit? {
        return habitDao.getHabitById(habitId)
    }
    
    // 获取习惯及其今日数据
    fun getHabitsWithTodayData(): Flow<List<HabitWithData>> {
        val today = LocalDate.now()
        // 同时监听习惯列表和今日打卡记录的变化
        return combine(
            habitDao.getActiveHabits(),
            checkinDao.getTodayCheckins(today)
        ) { habits, todayCheckins ->
            // 创建习惯ID到打卡次数的映射
            val checkinCountMap = todayCheckins
                .groupBy { it.habitId }
                .mapValues { (_, checkins) -> checkins.sumOf { it.count } }
            
            habits.map { habit ->
                val todayCheckinCount = checkinCountMap[habit.id] ?: 0
                HabitWithData(
                    habit = habit,
                    isCheckedToday = todayCheckinCount > 0,
                    todayCheckinCount = todayCheckinCount
                )
            }
        }
    }
    
    // 插入习惯
    suspend fun insertHabit(habit: Habit): Long {
        return habitDao.insertHabit(habit)
    }
    
    // 更新习惯
    suspend fun updateHabit(habit: Habit) {
        habitDao.updateHabit(habit)
    }
    
    // 删除习惯
    suspend fun deleteHabit(habit: Habit) {
        habitDao.deleteHabit(habit)
    }
    
    // 打卡
    suspend fun checkin(habitId: Long, count: Int = 1, note: String? = null) {
        val checkin = Checkin(
            habitId = habitId,
            checkinTime = LocalDateTime.now(),
            count = count,
            note = note
        )
        
        checkinDao.insertCheckin(checkin)
    }
    
    // 获取习惯的打卡记录
    fun getCheckinsByHabitId(habitId: Long): Flow<List<Checkin>> {
        return checkinDao.getCheckinsByHabitId(habitId)
    }
    
    // 获取今日打卡次数
    suspend fun getTodayCheckinCount(habitId: Long): Int {
        return checkinDao.getCheckinCountByHabitIdAndDate(habitId, LocalDate.now())
    }
    
    // 获取今日总次数
    suspend fun getTodayTotalCount(habitId: Long): Int {
        return checkinDao.getTotalCountByHabitIdAndDate(habitId, LocalDate.now()) ?: 0
    }
    

} 