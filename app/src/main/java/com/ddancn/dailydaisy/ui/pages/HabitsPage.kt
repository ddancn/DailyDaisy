package com.ddancn.dailydaisy.ui.pages

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ddancn.dailydaisy.HabitViewModelFactory
import com.ddancn.dailydaisy.data.entity.HabitFrequency
import com.ddancn.dailydaisy.data.model.HabitWithData
import com.ddancn.dailydaisy.ui.viewmodel.HabitViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitsPage(
    viewModel: HabitViewModel = viewModel(factory = HabitViewModelFactory)
) {
    var showNewHabitPage by remember { mutableStateOf(false) }
    var editingHabit by remember { mutableStateOf<HabitWithData?>(null) }
    val habitsWithData by viewModel.habitsWithData.collectAsState(initial = emptyList())
    val context = LocalContext.current

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
                        title = { Text("我的习惯", fontWeight = FontWeight.Bold) },
                        actions = {
                            IconButton(onClick = { showNewHabitPage = true }) {
                                Icon(Icons.Filled.Add, contentDescription = "添加习惯")
                            }
                        }
                    )
                }
            ) { paddingValues ->
                if (habitsWithData.isEmpty()) {
                    // 空状态
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
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
                    // 习惯列表
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(habitsWithData) { habitWithData ->
                            HabitCard(
                                habitWithData = habitWithData,
                                onClick = { editingHabit = habitWithData }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitCard(
    habitWithData: HabitWithData,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = habitWithData.habit.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            habitWithData.habit.description?.let { description ->
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = when (habitWithData.habit.frequency) {
                        HabitFrequency.DAILY -> "每日"
                        HabitFrequency.WEEKLY -> "每周"
                        HabitFrequency.MONTHLY -> "每月"
                        HabitFrequency.CUSTOM -> "自定义"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "目标: ${habitWithData.habit.targetCount}次",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
