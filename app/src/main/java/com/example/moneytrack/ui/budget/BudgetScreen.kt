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
import kotlinx.coroutines.flow.Flow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetScreen(factory: ViewModelFactory, onBack: () -> Unit) {
    val viewModel: BudgetViewModel = viewModel(factory = factory)
    val budgets by viewModel.budgets.collectAsStateWithLifecycle()
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
                title = { Text("预算管理 · ${viewModel.currentYear}年${viewModel.currentMonth}月") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "返回") } }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, "设置预算")
            }
        }
    ) { padding ->
        if (budgets.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("暂未设置预算，点击 + 开始设置", color = MaterialTheme.colorScheme.outline)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(budgets, key = { it.id }) { budget ->
                    val category = expenseCategories.find { it.id == budget.categoryId }
                    val spentFlow = viewModel.getSpentFlow(budget.categoryId)
                    val spent by spentFlow.collectAsStateWithLifecycle(initialValue = 0.0)
                    BudgetCard(
                        budget = budget,
                        category = category,
                        spent = spent,
                        onDelete = { viewModel.deleteBudget(budget) }
                    )
                }
            }
        }
    }
}

@Composable
fun BudgetCard(budget: Budget, category: Category?, spent: Double, onDelete: () -> Unit) {
    val progress = if (budget.amount > 0) (spent / budget.amount).toFloat().coerceIn(0f, 1f) else 0f
    val isOverBudget = spent > budget.amount
    val progressColor = when {
        isOverBudget -> MaterialTheme.colorScheme.error
        progress > 0.8f -> Color(0xFFFFA726)
        else -> MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = category?.name ?: "未知分类",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = category?.let { Color(it.color) } ?: MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Delete, "删除", tint = MaterialTheme.colorScheme.outline)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(8.dp),
                color = progressColor
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "已用 ${formatAmount(spent)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isOverBudget) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "预算 ${formatAmount(budget.amount)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
            if (isOverBudget) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "⚠ 已超出预算 ${formatAmount(spent - budget.amount)}",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelSmall
                )
            } else {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "剩余 ${formatAmount(budget.amount - spent)}",
                    color = MaterialTheme.colorScheme.outline,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

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
    var amountText by remember { mutableStateOf("") }
    var amountError by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("设置预算") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (availableCategories.isEmpty()) {
                    Text("所有支出分类均已设置预算", color = MaterialTheme.colorScheme.outline)
                } else {
                    // 分类下拉选择
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        OutlinedTextField(
                            value = selectedCategory?.name ?: "选择分类",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("分类") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            availableCategories.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat.name) },
                                    onClick = { selectedCategory = cat; expanded = false }
                                )
                            }
                        }
                    }
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = it.filter { c -> c.isDigit() || c == '.' }; amountError = false },
                        label = { Text("月度预算金额") },
                        prefix = { Text("¥") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        isError = amountError,
                        supportingText = if (amountError) {{ Text("请输入有效金额") }} else null,
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
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
