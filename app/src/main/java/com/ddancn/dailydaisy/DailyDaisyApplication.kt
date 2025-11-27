package com.ddancn.dailydaisy

import android.app.Application
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.ddancn.dailydaisy.data.database.AppDatabase
import com.ddancn.dailydaisy.data.repository.HabitRepository
import com.ddancn.dailydaisy.ui.viewmodel.HabitViewModel

/**
 * 应用程序类
 * 管理数据库和Repository的初始化
 */
class DailyDaisyApplication : Application() {
    
    val database by lazy {
        AppDatabase.getDatabase(this)
    }
    
    val habitRepository by lazy {
        HabitRepository(
            habitDao = database.habitDao(),
            checkinDao = database.checkinDao()
        )
    }
}

/**
 * ViewModel工厂
 */
val HabitViewModelFactory = viewModelFactory {
    initializer {
        val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as DailyDaisyApplication)
        HabitViewModel(application.habitRepository)
    }
}
