package com.ddancn.dailydaisy.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ddancn.dailydaisy.data.entity.Habit
import com.ddancn.dailydaisy.data.entity.HabitFrequency
import com.ddancn.dailydaisy.data.model.HabitWithData
import com.ddancn.dailydaisy.data.repository.HabitRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import java.time.LocalDateTime

/**
 * 习惯ViewModel
 * 管理习惯相关的UI状态和数据操作
 */
class HabitViewModel(
    private val habitRepository: HabitRepository
) : ViewModel() {

    // 当前选择的日期
    private val _selectedDate = MutableStateFlow(java.time.LocalDate.now())
    val selectedDate: StateFlow<java.time.LocalDate> = _selectedDate.asStateFlow()

    // 习惯列表状态（根据选择的日期）
    val habitsWithData: Flow<List<HabitWithData>> = _selectedDate.flatMapLatest { date ->
        habitRepository.getHabitsWithCurrentPeriodData(date)
    }
    
    /**
     * 设置选择的日期
     */
    fun setSelectedDate(date: java.time.LocalDate) {
        _selectedDate.value = date
    }
    
    /**
     * 选择前一天
     */
    fun selectPreviousDay() {
        _selectedDate.value = _selectedDate.value.minusDays(1)
    }
    
    /**
     * 选择后一天
     */
    fun selectNextDay() {
        _selectedDate.value = _selectedDate.value.plusDays(1)
    }
    
    /**
     * 重置到今天
     */
    fun resetToToday() {
        _selectedDate.value = java.time.LocalDate.now()
    }

    // 新建习惯的状态
    private val _uiState = MutableStateFlow(NewHabitUiState())
    val uiState: StateFlow<NewHabitUiState> = _uiState.asStateFlow()

    /**
     * 更新习惯名称
     */
    fun updateName(name: String) {
        _uiState.value = _uiState.value.copy(name = name)
    }

    /**
     * 更新习惯描述
     */
    fun updateDescription(description: String) {
        _uiState.value = _uiState.value.copy(description = description)
    }

    /**
     * 更新习惯频率
     */
    fun updateFrequency(frequency: HabitFrequency) {
        _uiState.value = _uiState.value.copy(frequency = frequency)
    }

    /**
     * 更新目标次数
     */
    fun updateTargetCount(count: Int) {
        _uiState.value = _uiState.value.copy(targetCount = count)
    }

    /**
     * 更新习惯颜色
     */
    fun updateColor(color: Int?) {
        _uiState.value = _uiState.value.copy(color = color)
    }

    /**
     * 更新开始日期
     */
    fun updateStartDate(date: LocalDateTime) {
        _uiState.value = _uiState.value.copy(startDate = date)
    }

    /**
     * 重置UI状态
     */
    fun resetState() {
        _uiState.value = NewHabitUiState()
    }

    /**
     * 加载习惯到编辑状态
     */
    fun loadHabitForEdit(habit: Habit) {
        _uiState.value = NewHabitUiState(
            name = habit.name,
            description = habit.description ?: "",
            frequency = habit.frequency,
            targetCount = habit.targetCount,
            startDate = habit.startDate,
            color = habit.color
        )
    }

    /**
     * 保存习惯
     */
    suspend fun saveHabit(): Result<Long> {
        val state = _uiState.value
        
        // 验证输入
        if (state.name.isBlank()) {
            return Result.failure(IllegalArgumentException("习惯名称不能为空"))
        }
        
        if (state.targetCount <= 0) {
            return Result.failure(IllegalArgumentException("目标次数必须大于0"))
        }

        val habit = Habit(
            name = state.name.trim(),
            description = state.description.takeIf { it.isNotBlank() },
            icon = null,
            color = state.color,
            frequency = state.frequency,
            targetCount = state.targetCount,
            isActive = true,
            startDate = state.startDate,
            endDate = null,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        return try {
            val id = habitRepository.insertHabit(habit)
            resetState()
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 更新习惯
     */
    suspend fun updateHabit(habitId: Long): Result<Unit> {
        val state = _uiState.value
        
        // 验证输入
        if (state.name.isBlank()) {
            return Result.failure(IllegalArgumentException("习惯名称不能为空"))
        }
        
        if (state.targetCount <= 0) {
            return Result.failure(IllegalArgumentException("目标次数必须大于0"))
        }

        return try {
            // 先获取原有习惯
            val existingHabit = habitRepository.getHabitById(habitId)
            if (existingHabit == null) {
                return Result.failure(IllegalArgumentException("习惯不存在"))
            }

            val updatedHabit = existingHabit.copy(
                name = state.name.trim(),
                description = state.description.takeIf { it.isNotBlank() },
                color = state.color,
                frequency = state.frequency,
                targetCount = state.targetCount,
                updatedAt = LocalDateTime.now()
            )
            
            habitRepository.updateHabit(updatedHabit)
            resetState()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 删除习惯
     */
    suspend fun deleteHabit(habitId: Long): Result<Unit> {
        return try {
            val habit = habitRepository.getHabitById(habitId)
            if (habit == null) {
                return Result.failure(IllegalArgumentException("习惯不存在"))
            }
            habitRepository.deleteHabit(habit)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 打卡
     */
    fun checkin(habitId: Long, count: Int = 1, note: String? = null) {
        viewModelScope.launch {
            val selectedDate = _selectedDate.value
            val checkinTime = selectedDate.atTime(java.time.LocalTime.now())
            habitRepository.checkin(habitId, checkinTime, count, note)
        }
    }
    
    /**
     * 取消打卡
     */
    fun cancelCheckin(habitId: Long) {
        viewModelScope.launch {
            val selectedDate = _selectedDate.value
            habitRepository.cancelCheckin(habitId, selectedDate)
        }
    }
}

/**
 * 新建习惯的UI状态
 */
data class NewHabitUiState(
    val name: String = "",
    val description: String = "",
    val frequency: HabitFrequency = HabitFrequency.DAILY,
    val targetCount: Int = 1,
    val startDate: LocalDateTime = LocalDateTime.now(),
    val color: Int? = null
)
