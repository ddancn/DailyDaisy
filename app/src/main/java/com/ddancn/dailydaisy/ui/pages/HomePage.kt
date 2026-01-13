package com.ddancn.dailydaisy.ui.pages

import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ddancn.dailydaisy.HabitViewModelFactory
import com.ddancn.dailydaisy.data.entity.HabitFrequency
import com.ddancn.dailydaisy.data.model.HabitWithData
import com.ddancn.dailydaisy.ui.viewmodel.HabitViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomePage(
    viewModel: HabitViewModel = viewModel(factory = HabitViewModelFactory)
) {
    var showNewHabitPage by remember { mutableStateOf(false) }
    var editingHabit by remember { mutableStateOf<HabitWithData?>(null) }
    val habitsWithData by viewModel.habitsWithData.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val today = LocalDate.now()
    val isToday = selectedDate == today
    val context = LocalContext.current

    // 格式化日期显示
    val dateFormatter = DateTimeFormatter.ofPattern("M月d日")
    val weekDayFormatter = DateTimeFormatter.ofPattern("E", java.util.Locale("zh", "CN"))
    val dateText = if (isToday) {
        "今日"
    } else {
        "${selectedDate.format(dateFormatter)} ${selectedDate.format(weekDayFormatter)}"
    }

    when {
        editingHabit != null -> {
            val currentHabit = editingHabit!!
            EditHabitPage(
                habitWithData = currentHabit,
                onBack = { editingHabit = null },
                onSuccess = { message ->
                    editingHabit = null
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                },
                viewModel = viewModel
            )
        }

        showNewHabitPage -> {
            NewHabitPage(
                onBack = { showNewHabitPage = false },
                onSuccess = { message ->
                    showNewHabitPage = false
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                },
                viewModel = viewModel
            )
        }

        else -> {
            Scaffold(
                topBar = {
                    TopAppBar(
                        modifier = Modifier.statusBarsPadding(),
                        title = { Text("打卡", fontWeight = FontWeight.Bold) },
                        actions = {
                            IconButton(onClick = { showNewHabitPage = true }) {
                                Icon(Icons.Filled.Add, contentDescription = "新建习惯")
                            }
                        }
                    )
                }
            ) { paddingValues ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    // 日期选择器
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                            .background(
                                color = MaterialTheme.colorScheme.inverseOnSurface,
                                shape = MaterialTheme.shapes.small
                            )
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { viewModel.selectPreviousDay() }
                        ) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "前一天")
                        }
                        Text(
                            text = dateText,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(
                            onClick = { viewModel.selectNextDay() },
                            enabled = selectedDate.isBefore(today)
                        ) {
                            Icon(Icons.Filled.ArrowForward, contentDescription = "后一天")
                        }
                    }

                    if (habitsWithData.isEmpty()) {
                        // 空状态
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "还没有习惯",
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "点击右上角的 + 按钮添加你的第一个习惯",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        // 打卡列表
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            modifier = Modifier
                                .fillMaxSize()
                                .weight(1f),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(habitsWithData) { habitWithData ->
                                CheckinCard(
                                    habitWithData = habitWithData,
                                    onCheckin = { viewModel.checkin(habitWithData.habit.id) },
                                    onCancelCheckin = { viewModel.cancelCheckin(habitWithData.habit.id) },
                                    onEdit = { editingHabit = habitWithData }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckinCard(
    habitWithData: HabitWithData,
    onCheckin: () -> Unit,
    onCancelCheckin: () -> Unit = {},
    onEdit: () -> Unit = {}
) {
    // 使用动画来平滑过渡进度条
    val animatedProgress by animateFloatAsState(
        targetValue = habitWithData.getTodayCompletionRate(),
        animationSpec = tween(durationMillis = 500),
        label = "progress_animation"
    )
    
    // 获取习惯的主题色，如果没有则使用默认主题色
    val habitColor = habitWithData.habit.color?.let { Color(it) } 
        ?: MaterialTheme.colorScheme.primary
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = habitColor.copy(alpha = 0.1f)
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = 0.5.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = habitWithData.habit.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = habitColor,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        Icons.Filled.Edit,
                        contentDescription = "编辑",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    )
                }
            }
            Column(modifier = Modifier.fillMaxWidth()) {
                habitWithData.habit.description?.let { description ->
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "累计 ${habitWithData.totalCheckinCount} 次",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 按钮区域：水平排列
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // 打卡按钮：一直显示
                Button(
                    onClick = onCheckin,
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(vertical = 8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = habitColor
                    )
                ) {
                    Text("打卡", style = MaterialTheme.typography.bodySmall)
                }
                
                // 撤销按钮：只有在有打卡记录时显示
                if (habitWithData.isCheckedToday) {
                    OutlinedButton(
                        onClick = onCancelCheckin,
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = 8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("撤销", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 进度信息
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = when (habitWithData.habit.frequency) {
                            HabitFrequency.DAILY -> "今日"
                            HabitFrequency.WEEKLY -> "本周"
                            HabitFrequency.MONTHLY -> "本月"
                            HabitFrequency.CUSTOM -> "当前"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = "${habitWithData.todayCheckinCount}/${habitWithData.habit.targetCount}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = if (habitWithData.isTodayCompleted())
                            habitColor
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // 进度条（使用动画值）
                LinearProgressIndicator(
                    progress = animatedProgress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    color = habitColor,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }
    }
} 