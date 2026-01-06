package com.ddancn.dailydaisy.ui.pages

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    val habitsWithData by viewModel.habitsWithData.collectAsState(initial = emptyList())
    val selectedDate by viewModel.selectedDate.collectAsState()
    val today = LocalDate.now()
    val isToday = selectedDate == today
    
    // 格式化日期显示
    val dateFormatter = DateTimeFormatter.ofPattern("M月d日")
    val weekDayFormatter = DateTimeFormatter.ofPattern("E", java.util.Locale("zh", "CN"))
    val dateText = if (isToday) {
        "今日"
    } else {
        "${selectedDate.format(dateFormatter)} ${selectedDate.format(weekDayFormatter)}"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier.statusBarsPadding(),
                title = { Text("打卡", fontWeight = FontWeight.Bold) }
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
                            text = "去习惯页面添加你的第一个习惯吧",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                // 打卡列表
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(habitsWithData) { habitWithData ->
                        CheckinCard(
                            habitWithData = habitWithData,
                            onCheckin = { viewModel.checkin(habitWithData.habit.id) },
                            onCancelCheckin = { viewModel.cancelCheckin(habitWithData.habit.id) }
                        )
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
    onCancelCheckin: () -> Unit = {}
) {
    // 使用动画来平滑过渡进度条
    val animatedProgress by animateFloatAsState(
        targetValue = habitWithData.getTodayCompletionRate(),
        animationSpec = tween(durationMillis = 500),
        label = "progress_animation"
    )
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = habitWithData.habit.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    habitWithData.habit.description?.let { description ->
                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "累计打卡 ${habitWithData.totalCheckinCount} 次",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // 按钮区域：两个按钮，撤销按钮只在有打卡记录时显示
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 打卡按钮：一直显示
                    Button(
                        onClick = onCheckin
                    ) {
                        Text("打卡")
                    }
                    
                    // 撤销按钮：只有在有打卡记录时显示
                    if (habitWithData.isCheckedToday) {
                        OutlinedButton(
                            onClick = onCancelCheckin,
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text("撤销")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 进度信息
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = when (habitWithData.habit.frequency) {
                        HabitFrequency.DAILY -> "今日进度: ${habitWithData.todayCheckinCount}/${habitWithData.habit.targetCount}"
                        HabitFrequency.WEEKLY -> "本周进度: ${habitWithData.todayCheckinCount}/${habitWithData.habit.targetCount}"
                        HabitFrequency.MONTHLY -> "本月进度: ${habitWithData.todayCheckinCount}/${habitWithData.habit.targetCount}"
                        HabitFrequency.CUSTOM -> "当前进度: ${habitWithData.todayCheckinCount}/${habitWithData.habit.targetCount}"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = "${(habitWithData.getTodayCompletionRate() * 100).toInt()}%",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = if (habitWithData.isTodayCompleted())
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // 进度条（使用动画值）
            LinearProgressIndicator(
                progress = animatedProgress,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
} 