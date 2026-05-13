package com.example.moneytrack.ui.history

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.moneytrack.data.model.TransactionType
import com.example.moneytrack.data.model.TransactionWithCategory
import com.example.moneytrack.ui.common.formatAmount
import com.example.moneytrack.ui.common.formatDateFull
import com.example.moneytrack.ui.home.TransactionItem
import com.example.moneytrack.viewmodel.HistoryViewModel
import com.example.moneytrack.viewmodel.ViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(factory: ViewModelFactory) {
    val viewModel: HistoryViewModel = viewModel(factory = factory)
    val allTransactions by viewModel.allTransactions.collectAsStateWithLifecycle()

    // 按日期分组
    val grouped = allTransactions.groupBy { formatDateFull(it.transaction.date) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("账单历史") })
        }
    ) { padding ->
        if (allTransactions.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("暂无账单记录", color = MaterialTheme.colorScheme.outline)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                grouped.forEach { (date, items) ->
                    // 日期分组标题
                    item(key = date) {
                        val dayIncome = items.filter { it.transaction.type == TransactionType.INCOME }
                            .sumOf { it.transaction.amount }
                        val dayExpense = items.filter { it.transaction.type == TransactionType.EXPENSE }
                            .sumOf { it.transaction.amount }
                        DayHeader(date = date, income = dayIncome, expense = dayExpense)
                    }
                    // 当日账单列表（支持左滑删除）
                    items(items, key = { it.transaction.id }) { item ->
                        SwipeToDismissTransactionItem(
                            item = item,
                            onDismiss = { viewModel.deleteTransaction(item.transaction) }
                        )
                    }
                    item { Divider(color = MaterialTheme.colorScheme.outlineVariant) }
                }
            }
        }
    }
}

@Composable
fun DayHeader(date: String, income: Double, expense: Double) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = date, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (income > 0) Text("+${formatAmount(income)}", color = Color(0xFF4CAF50), style = MaterialTheme.typography.labelMedium)
            if (expense > 0) Text("-${formatAmount(expense)}", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeToDismissTransactionItem(
    item: TransactionWithCategory,
    onDismiss: () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = {
            if (it == SwipeToDismissBoxValue.EndToStart) { onDismiss(); true }
            else false
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            val color by animateColorAsState(
                targetValue = if (dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart)
                    MaterialTheme.colorScheme.errorContainer else Color.Transparent,
                label = "swipe_bg"
            )
            Box(
                modifier = Modifier.fillMaxSize().background(color).padding(end = 16.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(Icons.Default.Delete, contentDescription = "删除", tint = MaterialTheme.colorScheme.error)
            }
        }
    ) {
        Surface(modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surface) {
            Box(modifier = Modifier.padding(vertical = 8.dp)) {
                TransactionItem(item = item)
            }
        }
    }
}
