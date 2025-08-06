package com.ddancn.dailydaisy.data.converter

import androidx.room.TypeConverter
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * Room数据库类型转换器
 * 用于处理LocalDateTime、LocalTime、LocalDate等时间类型
 */
class Converters {
    
    @TypeConverter
    fun fromLocalDateTime(value: LocalDateTime?): String? {
        return value?.toString()
    }
    
    @TypeConverter
    fun toLocalDateTime(value: String?): LocalDateTime? {
        return value?.let { LocalDateTime.parse(it) }
    }
    
    @TypeConverter
    fun fromLocalDate(value: LocalDate?): String? {
        return value?.toString()
    }
    
    @TypeConverter
    fun toLocalDate(value: String?): LocalDate? {
        return value?.let { LocalDate.parse(it) }
    }
    
    @TypeConverter
    fun fromLocalTime(value: LocalTime?): String? {
        return value?.toString()
    }
    
    @TypeConverter
    fun toLocalTime(value: String?): LocalTime? {
        return value?.let { LocalTime.parse(it) }
    }
    
    @TypeConverter
    fun fromDayOfWeekList(value: List<DayOfWeek>?): String? {
        return value?.joinToString(",") { it.name }
    }
    
    @TypeConverter
    fun toDayOfWeekList(value: String?): List<DayOfWeek>? {
        return value?.split(",")?.map { DayOfWeek.valueOf(it) }
    }
} 