package com.ddancn.dailydaisy.ui.pages

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ddancn.dailydaisy.HabitViewModelFactory
import com.ddancn.dailydaisy.data.entity.HabitFrequency
import com.ddancn.dailydaisy.ui.viewmodel.HabitViewModel
import kotlinx.coroutines.launch

/**
 * 新建习惯页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewHabitPage(
    onBack: () -> Unit,
    onSuccess: (String) -> Unit = {},
    viewModel: HabitViewModel = viewModel(factory = HabitViewModelFactory)
) {
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    // 进入新建页面时重置状态
    LaunchedEffect(Unit) {
        viewModel.resetState()
    }

    // 拦截返回键，返回到列表页面
    BackHandler(onBack = {
        viewModel.resetState()
        onBack()
    })

    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier.statusBarsPadding(),
                title = { Text("新建习惯", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.resetState()
                        onBack()
                    }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            scope.launch {
                                viewModel.saveHabit().fold(
                                    onSuccess = {
                                        // saveHabit 内部已经调用了 resetState
                                        onSuccess("习惯创建成功")
                                    },
                                    onFailure = { error ->
                                        errorMessage = error.message ?: "保存失败"
                                        showErrorDialog = true
                                    }
                                )
                            }
                        },
                        enabled = uiState.name.isNotBlank() && uiState.targetCount > 0
                    ) {
                        Text("保存")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 习惯名称
            OutlinedTextField(
                value = uiState.name,
                onValueChange = viewModel::updateName,
                label = { Text("习惯名称 *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = uiState.name.isBlank()
            )

            // 习惯描述
            OutlinedTextField(
                value = uiState.description,
                onValueChange = viewModel::updateDescription,
                label = { Text("习惯描述") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("描述一下这个习惯...") }
            )

            // 习惯频率
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "习惯频率",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HabitFrequency.values().forEach { frequency ->
                        FilterChip(
                            selected = uiState.frequency == frequency,
                            onClick = { viewModel.updateFrequency(frequency) },
                            label = {
                                Text(
                                    text = when (frequency) {
                                        HabitFrequency.DAILY -> "每日"
                                        HabitFrequency.WEEKLY -> "每周"
                                        HabitFrequency.MONTHLY -> "每月"
                                        HabitFrequency.CUSTOM -> "自定义"
                                    }
                                )
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // 目标次数
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "目标次数",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = when (uiState.frequency) {
                            HabitFrequency.DAILY -> "每天完成多少次这个习惯"
                            HabitFrequency.WEEKLY -> "每周完成多少次这个习惯"
                            HabitFrequency.MONTHLY -> "每月完成多少次这个习惯"
                            HabitFrequency.CUSTOM -> "完成多少次这个习惯"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                if (uiState.targetCount > 1) {
                                    viewModel.updateTargetCount(uiState.targetCount - 1)
                                }
                            },
                            enabled = uiState.targetCount > 1
                        ) {
                            Text("-", style = MaterialTheme.typography.headlineMedium)
                        }
                        Text(
                            text = "${uiState.targetCount}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Normal,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        IconButton(
                            onClick = {
                                viewModel.updateTargetCount(uiState.targetCount + 1)
                            }
                        ) {
                            Text("+", style = MaterialTheme.typography.headlineMedium)
                        }
                    }
                }
            }
        }
    }

    // 错误对话框
    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = { showErrorDialog = false },
            title = { Text("错误") },
            text = { Text(errorMessage) },
            confirmButton = {
                TextButton(onClick = { showErrorDialog = false }) {
                    Text("确定")
                }
            }
        )
    }
}
