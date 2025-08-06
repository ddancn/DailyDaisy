package com.ddancn.dailydaisy.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.ddancn.dailydaisy.data.converter.Converters
import com.ddancn.dailydaisy.data.entity.Habit
import com.ddancn.dailydaisy.data.entity.Checkin

/**
 * 应用数据库
 */
@Database(
    entities = [
        Habit::class,
        Checkin::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun habitDao(): HabitDao
    abstract fun checkinDao(): CheckinDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "daily_daisy_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
} 