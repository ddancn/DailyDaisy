package com.ddancn.dailydaisy.data.repository

import com.ddancn.dailydaisy.data.database.HabitDao
import com.ddancn.dailydaisy.data.database.CheckinDao
import com.ddancn.dailydaisy.data.entity.Habit
import com.ddancn.dailydaisy.data.entity.Checkin
import com.ddancn.dailydaisy.data.model.HabitWithData
import com.ddancn.dailydaisy.data.entity.HabitFrequency
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.TemporalAdjusters

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

    // 获取习惯及其当前周期数据（根据频率统计不同时间段）
    fun getHabitsWithCurrentPeriodData(): Flow<List<HabitWithData>> {
        return getHabitsWithCurrentPeriodData(LocalDate.now())
    }

    // 获取习惯及其指定日期的周期数据（根据频率统计不同时间段）
    fun getHabitsWithCurrentPeriodData(targetDate: LocalDate): Flow<List<HabitWithData>> {
        
        // 计算指定日期所在周的开始和结束时间（周一到周日）
        val weekStart = targetDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).atStartOfDay()
        val weekEnd = weekStart.plusDays(7)
        
        // 计算指定日期所在月的开始和结束时间
        val monthStart = targetDate.with(TemporalAdjusters.firstDayOfMonth()).atStartOfDay()
        val monthEnd = monthStart.plusMonths(1)
        
        // 同时监听习惯列表和不同时间段的打卡记录
        return combine(
            habitDao.getActiveHabits(),
            checkinDao.getTodayCheckins(targetDate),
            checkinDao.getWeekCheckins(weekStart, weekEnd),
            checkinDao.getMonthCheckins(monthStart, monthEnd),
            checkinDao.getAllCheckins()
        ) { habits, targetDateCheckins, weekCheckins, monthCheckins, allCheckins ->
            // 创建习惯ID到打卡次数的映射（根据频率）
            val dailyCountMap = targetDateCheckins
                .groupBy { it.habitId }
                .mapValues { (_, checkins) -> checkins.sumOf { it.count } }
            
            val weeklyCountMap = weekCheckins
                .groupBy { it.habitId }
                .mapValues { (_, checkins) -> checkins.sumOf { it.count } }
            
            val monthlyCountMap = monthCheckins
                .groupBy { it.habitId }
                .mapValues { (_, checkins) -> checkins.sumOf { it.count } }
            
            // 创建习惯ID到总打卡次数的映射
            val totalCountMap = allCheckins
                .groupBy { it.habitId }
                .mapValues { (_, checkins) -> checkins.sumOf { it.count } }
            
            habits.map { habit ->
                val checkinCount = when (habit.frequency) {
                    HabitFrequency.DAILY -> dailyCountMap[habit.id] ?: 0
                    HabitFrequency.WEEKLY -> weeklyCountMap[habit.id] ?: 0
                    HabitFrequency.MONTHLY -> monthlyCountMap[habit.id] ?: 0
                    HabitFrequency.CUSTOM -> dailyCountMap[habit.id] ?: 0 // 自定义暂时按日统计
                }
                
                val totalCount = totalCountMap[habit.id] ?: 0
                
                HabitWithData(
                    habit = habit,
                    isCheckedToday = checkinCount > 0,
                    todayCheckinCount = checkinCount,
                    totalCheckinCount = totalCount
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
        checkin(habitId, LocalDateTime.now(), count, note)
    }

    // 打卡（指定时间）
    suspend fun checkin(
        habitId: Long,
        checkinTime: LocalDateTime,
        count: Int = 1,
        note: String? = null
    ) {
        val checkin = Checkin(
            habitId = habitId,
            checkinTime = checkinTime,
            count = count,
            note = note
        )
        
        checkinDao.insertCheckin(checkin)
    }

    // 取消打卡（删除当前周期内最新的打卡记录）
    suspend fun cancelCheckin(habitId: Long, frequency: HabitFrequency) {
        cancelCheckin(habitId, frequency, LocalDate.now())
    }

    // 取消打卡（指定日期）
    suspend fun cancelCheckin(habitId: Long, frequency: HabitFrequency, targetDate: LocalDate) {
        // 获取习惯对象以获取自定义周期信息
        val habit = habitDao.getHabitById(habitId) ?: return

        val (startDate, endDate) = when (frequency) {
            HabitFrequency.DAILY -> {
                val dayStart = targetDate.atStartOfDay()
                val dayEnd = dayStart.plusDays(1)
                Pair(dayStart, dayEnd)
            }

            HabitFrequency.WEEKLY -> {
                val weekStart =
                    targetDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                        .atStartOfDay()
                val weekEnd = weekStart.plusDays(7)
                Pair(weekStart, weekEnd)
            }

            HabitFrequency.MONTHLY -> {
                val monthStart =
                    targetDate.with(TemporalAdjusters.firstDayOfMonth()).atStartOfDay()
                val monthEnd = monthStart.plusMonths(1)
                Pair(monthStart, monthEnd)
            }

            HabitFrequency.CUSTOM -> {
                // 自定义暂时按日处理
                val dayStart = targetDate.atStartOfDay()
                val dayEnd = dayStart.plusDays(1)
                Pair(dayStart, dayEnd)
            }
        }

        val latestCheckin =
            checkinDao.getLatestCheckinByHabitIdAndDateRange(habitId, startDate, endDate)
        latestCheckin?.let {
            checkinDao.deleteCheckin(it)
        }
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