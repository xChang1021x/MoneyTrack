# MoneyTrack

一款使用 Jetpack Compose 构建的个人记账 Android 应用，采用深色 Teal 设计语言，支持多维度的收支管理、预算提醒、借贷记录与分账功能。

---

## 功能概览

### 记账核心
- **首页**：当月收入 / 支出 / 结余 Hero 卡片，本月账单列表，预算超限实时提醒
- **账单历史**：按日期分组展示所有账单，支持左滑删除、点击编辑
- **记账 / 修改**：选择分类、金额、日期、备注；编辑模式支持删除记录
- **统计图表**：按月查看收支分类饼图与趋势

### 预算管理
- 为每个支出分类单独设置月度预算
- 首页在使用率 ≥ 80% 时自动显示提醒卡片（橙色接近 / 红色超出）

### 搜索
- 同时搜索**备注关键字**和**分类名称**
- 可按收入 / 支出类型筛选结果

### 分类管理
- 支出 & 收入分类分页管理
- 添加自定义分类（名称 + 颜色）
- 删除任意分类（含预置分类）
- ↑ ↓ 按钮调整分类排列顺序

### 借贷记录
- 记录"借出"与"借入"两种类型
- 记录日期和到期日均通过日历选择器填写
- 到期自动标红 + 逾期提示
- 支持标记结清、编辑、删除

### 分账
- 创建分账群组，添加参与人及各自金额
- 查看群组分账明细与汇总

### 导航自定义
- 自由选择底部导航栏显示哪些页面（最多 4 项 + 固定"更多"）

---

## 技术架构

```
app/src/main/java/com/example/moneytrack/
├── data/
│   ├── db/           # Room DAO、Database、TypeConverters
│   ├── model/        # 数据实体（Category、Transaction、Budget、Debt、Split…）
│   ├── preferences/  # 导航偏好（DataStore）
│   └── repository/   # MoneyRepository 统一数据访问层
├── ui/
│   ├── add/          # 记账 / 编辑页
│   ├── budget/       # 预算设置页
│   ├── category/     # 分类管理页
│   ├── chart/        # 统计图表页
│   ├── common/       # 通用格式工具（formatAmount、formatDate…）
│   ├── debt/         # 借贷记录页
│   ├── history/      # 账单历史页
│   ├── home/         # 首页
│   ├── navcustomize/ # 导航自定义页
│   ├── navigation/   # NavGraph、Screen 路由定义
│   ├── profile/      # 更多页（功能入口）
│   ├── search/       # 搜索页
│   ├── split/        # 分账列表 & 明细页
│   └── theme/        # 颜色、字体、深色主题
└── viewmodel/        # 各页面 ViewModel + ViewModelFactory
```

### 主要技术栈

| 组件 | 说明 |
|------|------|
| UI | Jetpack Compose + Material 3 |
| 导航 | Navigation Compose |
| 数据库 | Room（KSP 注解处理） |
| 状态管理 | StateFlow + `collectAsStateWithLifecycle` |
| 架构模式 | MVVM + Repository |
| 偏好存储 | DataStore Preferences |
| 最低 SDK | API 26（Android 8.0） |
| 目标 SDK | API 36 |
| 版本 | 1.1（versionCode 2） |

### 数据库版本历史

| 版本 | 变更内容 |
|------|---------|
| v1 | 初始表结构（transactions、categories、budgets） |
| v2 | 自动迁移（AutoMigration） |
| v3 | `split_group` 表新增 `participants` 列 |
| v4 | `categories` 表新增 `sortOrder` 列，支持分类排序 |

---

## 设计风格

- **深色优先**：主背景 `#0C1A1D`，卡片使用 `surfaceVariant`，零阴影 + 大圆角（20 dp）
- **主色**：Teal 300（`#4DB6AC`）
- **收入色**：绿色 Tertiary；**支出色**：红色 Error
- 分组数据统一收入圆角 Card，内部用 0.5 dp 细线分隔条目

---

## 运行方式

1. 使用 **Android Studio Hedgehog** 或更高版本打开项目根目录
2. 同步 Gradle 依赖
3. 连接设备或启动模拟器（API 26+），点击 Run

无需任何额外配置，首次启动会自动写入预置分类数据。
