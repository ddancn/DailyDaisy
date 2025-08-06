# 打卡App数据层设计

## 实体类设计

### 1. Habit (习惯实体)
**核心实体，表示用户创建的一个习惯**

主要字段：
- `id`: 主键
- `name`: 习惯名称
- `description`: 习惯描述
- `icon`: 习惯图标
- `color`: 习惯颜色
- `frequency`: 习惯频率（每日/每周/每月/自定义）
- `targetCount`: 目标次数
- `isActive`: 是否激活
- `startDate`: 开始日期
- `endDate`: 结束日期

### 2. Checkin (打卡记录实体)
**记录每次打卡的详细信息**

主要字段：
- `id`: 主键
- `habitId`: 关联的习惯ID
- `checkinTime`: 打卡时间
- `count`: 打卡次数
- `note`: 打卡备注



## 数据模型

### HabitWithData
**习惯及其数据的组合类，用于UI显示**

包含：
- 习惯基本信息
- 今日统计数据
- 今日是否已打卡
- 今日打卡次数
- 完成率计算方法
- 剩余次数计算方法

## 枚举类型

### HabitFrequency (习惯频率)
- `DAILY`: 每日
- `WEEKLY`: 每周
- `MONTHLY`: 每月
- `CUSTOM`: 自定义





## 数据库关系

1. **Habit** ←→ **Checkin**: 一对多关系

当习惯被删除时，相关的打卡记录也会被删除。

## 使用场景

1. **创建习惯**: 用户创建新习惯，设置基本信息
2. **打卡**: 用户进行打卡，记录打卡时间和备注
3. **查看记录**: 查看习惯的打卡历史

## Room数据库配置

### 依赖配置
在`app/build.gradle.kts`中添加以下依赖：

```kotlin
plugins {
    id("kotlin-kapt")
}

dependencies {
    // Room数据库
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")
}
```

### 数据库结构
- **AppDatabase**: 主数据库类，包含所有实体和DAO
- **Converters**: 类型转换器，处理LocalDateTime等时间类型
- **DAO接口**: 数据访问对象，定义数据库操作
- **Repository**: 仓库类，封装业务逻辑

### 主要组件
1. **HabitDao**: 习惯数据操作
2. **CheckinDao**: 打卡记录操作
3. **HabitRepository**: 习惯仓库，封装业务逻辑

### 使用示例
```kotlin
// 获取数据库实例
val database = AppDatabase.getDatabase(context)

// 获取仓库
val repository = HabitRepository(
    database.habitDao(),
    database.checkinDao()
)

// 创建习惯
val habit = Habit(
    name = "喝水",
    description = "每天喝8杯水",
    frequency = HabitFrequency.DAILY,
    targetCount = 8,
    startDate = LocalDateTime.now()
)
val habitId = repository.insertHabit(habit)

// 打卡
repository.checkin(habitId, count = 1, note = "早上第一杯")
``` 