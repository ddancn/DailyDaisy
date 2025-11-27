package com.ddancn.dailydaisy.data.model

import com.ddancn.dailydaisy.data.entity.Habit
import com.ddancn.dailydaisy.data.entity.Checkin
import java.time.LocalDate

/**
 * 习惯及其数据的组合类
 * 用于在UI中显示习惯的完整信息
 */
data class HabitWithData(
    val habit: Habit,
    val isCheckedToday: Boolean = false,
    val todayCheckinCount: Int = 0,
    val totalCheckinCount: Int = 0
) {
    /**
     * 获取今日完成率
     */
    fun getTodayCompletionRate(): Float {
        return if (habit.targetCount > 0) {
            (todayCheckinCount.toFloat() / habit.targetCount.toFloat()).coerceAtMost(1f)
        } else 0f
    }
    
    /**
     * 是否完成今日目标
     */
    fun isTodayCompleted(): Boolean {
        return todayCheckinCount >= habit.targetCount
    }
    
    /**
     * 获取今日剩余次数
     */
    fun getRemainingCount(): Int {
        return (habit.targetCount - todayCheckinCount).coerceAtLeast(0)
    }
}

/**
 * 习惯列表项数据类
 */
data class HabitListItem(
    val habit: Habit,
    val todayCheckins: List<CheckinWithTime>,
    val isCheckedToday: Boolean = false
)

/**
 * 打卡记录与时间组合
 */
data class CheckinWithTime(
    val checkin: Checkin,
    val displayTime: String // 格式化的显示时间
) 