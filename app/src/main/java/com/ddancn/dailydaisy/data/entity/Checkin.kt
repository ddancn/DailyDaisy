package com.ddancn.dailydaisy.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDateTime

/**
 * 打卡记录实体类
 * 记录用户每次打卡的详细信息
 */
@Entity(
    tableName = "checkins",
    foreignKeys = [
        ForeignKey(
            entity = Habit::class,
            parentColumns = ["id"],
            childColumns = ["habitId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["habitId"]),
        Index(value = ["checkinTime"])
    ]
)
data class Checkin(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val habitId: Long,                   // 关联的习惯ID
    val checkinTime: LocalDateTime,      // 打卡时间
    val count: Int = 1,                 // 打卡次数（如喝水8杯）
    val note: String? = null,           // 打卡备注
    
    // 时间戳
    val createdAt: LocalDateTime = LocalDateTime.now()
) 