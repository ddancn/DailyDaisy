package com.ddancn.dailydaisy.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDateTime

/**
 * 习惯实体类
 * 表示用户创建的一个习惯
 */
@Entity(
    tableName = "habits",
    indices = [
        Index(value = ["isActive"]),
        Index(value = ["createdAt"])
    ]
)
data class Habit(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    // 习惯基本信息
    val name: String,                    // 习惯名称
    val description: String? = null,     // 习惯描述
    val icon: String? = null,           // 习惯图标（可以是图标名称或路径）
    val color: Int? = null,             // 习惯颜色
    
    // 习惯设置
    val frequency: HabitFrequency,       // 习惯频率（每日、每周等）
    val targetCount: Int = 1,           // 目标次数（如每天喝8杯水）
    
    // 习惯状态
    val isActive: Boolean = true,       // 是否激活
    val startDate: LocalDateTime,       // 开始日期
    val endDate: LocalDateTime? = null, // 结束日期（可选）
    
    // 时间戳
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

/**
 * 习惯频率枚举
 */
enum class HabitFrequency {
    DAILY,      // 每日
    WEEKLY,     // 每周
    MONTHLY,    // 每月
    CUSTOM      // 自定义
} 