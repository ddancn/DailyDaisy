package com.ddancn.dailydaisy.data.repository

import com.ddancn.dailydaisy.data.database.HabitDao
import com.ddancn.dailydaisy.data.database.CheckinDao
import com.ddancn.dailydaisy.data.entity.Habit
import com.ddancn.dailydaisy.data.entity.Checkin
import com.ddancn.dailydaisy.data.model.HabitWithData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
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
    
    // 获取习惯及其今日数据
    fun getHabitsWithTodayData(): Flow<List<HabitWithData>> {
        return habitDao.getActiveHabits().combine(
            getTodayCheckinCounts()
        ) { habits, checkinCounts ->
            habits.map { habit ->
                val todayCheckinCount = checkinCounts[habit.id] ?: 0
                
                HabitWithData(
                    habit = habit,
                    isCheckedToday = todayCheckinCount > 0,
                    todayCheckinCount = todayCheckinCount
                )
            }
        }
    }
    
    // 获取今日打卡次数
    private fun getTodayCheckinCounts(): Flow<Map<Long, Int>> {
        // 这里需要实现获取今日打卡次数的逻辑
        // 暂时返回空Map，后续实现
        return kotlinx.coroutines.flow.flowOf(emptyMap())
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