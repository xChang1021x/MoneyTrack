package com.example.moneytrack.ui.budget

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.moneytrack.data.model.Budget
import com.example.moneytrack.data.model.Category
import com.example.moneytrack.ui.common.formatAmount
import com.example.moneytrack.viewmodel.BudgetViewModel
import com.example.moneytrack.viewmodel.ViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetScreen(factory: ViewModelFactory, onBack: () -> Unit) {
    val viewModel: BudgetViewModel = viewModel(factory = factory)
    val budgets           by viewModel.budgets.collectAsStateWithLifecycle()
    val expenseCategories by viewModel.expenseCategories.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }

    if (showAddDialog) {
        AddBudgetDialog(
            categories = expenseCategories,
            existingBudgetCategoryIds = budgets.map { it.categoryId }.toSet(),
            onConfirm = { categoryId, amount ->
                viewModel.saveBudget(categoryId, amount)
                showAddDialog = false
            },
            onDismiss = { showAddDialog = false }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("预算管理 · ${viewModel.currentYear}年${viewModel.currentMonth}月")
                },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "返回") }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, "设置预算",
                    tint = MaterialTheme.colorScheme.onPrimary)
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        if (budgets.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("暂未设置预算",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.outline)
                    Text("点击右下角 + 开始设置",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(budgets, key = { it.id }) { budget ->
                    val category  = expenseCategories.find { it.id == budget.categoryId }
                    val spentFlow = viewModel.getSpentFlow(budget.categoryId)
                    val spent by spentFlow.collectAsStateWithLifecycle(initialValue = 0.0)
                    BudgetCard(
                        budget   = budget,
                        category = category,
                        spent    = spent,
                        onDelete = { viewModel.deleteBudget(budget) }
                    )
                }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }
}

// ─── 预算卡片 ──────────────────────────────────────────────────────────────

@Composable
fun BudgetCard(budget: Budget, category: Category?, spent: Double, onDelete: () -> Unit) {
    val progress    = if (budget.amount > 0) (spent / budget.amount).toFloat().coerceIn(0f, 1f) else 0f
    val isOverBudget = spent > budget.amount
    val progressColor = when {
        isOverBudget   -> MaterialTheme.colorScheme.error
        progress > 0.8f -> Color(0xFFFFA726)   // 橙色预警
        else           -> MaterialTheme.colorScheme.primary
    }
    val catColor = category?.let { Color(it.color) } ?: MaterialTheme.colorScheme.onSurface

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {

            // 分类名 + 删除按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = catColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            category?.name?.take(1) ?: "?",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = catColor
                        )
                    }
                    Text(
                        category?.name ?: "未知分类",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = catColor
                    )
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Delete, "删除",
                        Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.outline)
                }
            }

            // 进度条
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(6.dp),
                color = progressColor,
                trackColor = MaterialTheme.colorScheme.outlineVariant
            )

            // 已用 / 预算
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "已用 ${formatAmount(spent)}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isOverBudget) MaterialTheme.colorScheme.error
                            else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    "预算 ${formatAmount(budget.amount)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            // 超出 / 剩余提示
            val statusText = if (isOverBudget)
                "⚠ 已超出预算 ${formatAmount(spent - budget.amount)}"
            else
                "剩余 ${formatAmount(budget.amount - spent)}"

            Text(
                statusText,
                style = MaterialTheme.typography.labelSmall,
                color = if (isOverBudget) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.outline
            )
        }
    }
}

// ─── 添加预算弹窗 ──────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBudgetDialog(
    categories: List<Category>,
    existingBudgetCategoryIds: Set<Long>,
    onConfirm: (Long, Double) -> Unit,
    onDismiss: () -> Unit
) {
    val availableCategories = categories.filter { it.id !in existingBudgetCategoryIds }
    var selectedCategory by remember { mutableStateOf(availableCategories.firstOrNull()) }
    var amountText       by remember { mutableStateOf("") }
    var amountError      by remember { mutableStateOf(false) }
    var expanded         by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("设置预算") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (availableCategories.isEmpty()) {
                    Text("所有支出分类均已设置预算",
                        color = MaterialTheme.colorScheme.outline)
                } else {
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        OutlinedTextField(
                            value = selectedCategory?.name ?: "选择分类",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("分类") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                            },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            availableCategories.forEach { cat ->
                                DropdownMenuItem(
                                    text    = { Text(cat.name) },
                                    onClick = { selectedCategory = cat; expanded = false }
                                )
                            }
                        }
                    }
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = {
                            amountText = it.filter { c -> c.isDigit() || c == '.' }
                            amountError = false
                        },
                        label = { Text("月度预算金额") },
                        prefix = { Text("¥") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        isError = amountError,
                        supportingText = if (amountError) {{ Text("请输入有效金额") }} else null,
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (availableCategories.isEmpty()) { onDismiss(); return@TextButton }
                val cat = selectedCategory ?: return@TextButton
                val amount = amountText.toDoubleOrNull()
                if (amount == null || amount <= 0) { amountError = true; return@TextButton }
                onConfirm(cat.id, amount)
            }) { Text("确定") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } }
    )
}
