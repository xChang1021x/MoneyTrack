package com.example.moneytrack.ui.history

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.example.moneytrack.ui.theme.expenseColor
import com.example.moneytrack.ui.theme.incomeColor
import com.example.moneytrack.viewmodel.HistoryViewModel
import com.example.moneytrack.viewmodel.ViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(factory: ViewModelFactory) {
    val viewModel: HistoryViewModel = viewModel(factory = factory)
    val allTransactions by viewModel.allTransactions.collectAsStateWithLifecycle()

    val grouped = allTransactions.groupBy { formatDateFull(it.transaction.date) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("账单历史") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        if (allTransactions.isEmpty()) {
            Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("暂无账单记录", color = MaterialTheme.colorScheme.outline)
            }
        } else {
            LazyColumn(
                Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                grouped.forEach { (date, items) ->
                    // 日期分组头
                    item(key = "header_$date") {
                        val dayIncome  = items.filter { it.transaction.type == TransactionType.INCOME }
                            .sumOf { it.transaction.amount }
                        val dayExpense = items.filter { it.transaction.type == TransactionType.EXPENSE }
                            .sumOf { it.transaction.amount }
                        Spacer(Modifier.height(12.dp))
                        DayHeader(date = date, income = dayIncome, expense = dayExpense)
                        Spacer(Modifier.height(6.dp))
                    }

                    // 当日账单 — 统一放在一个圆角 Card 中
                    item(key = "group_$date") {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            ),
                            elevation = CardDefaults.cardElevation(0.dp)
                        ) {
                            Column(Modifier.padding(vertical = 4.dp)) {
                                items.forEachIndexed { index, item ->
                                    SwipeToDismissTransactionItem(
                                        item = item,
                                        onDismiss = { viewModel.deleteTransaction(item.transaction) }
                                    )
                                    if (index < items.lastIndex) {
                                        HorizontalDivider(
                                            modifier = Modifier.padding(horizontal = 16.dp),
                                            color = MaterialTheme.colorScheme.outlineVariant,
                                            thickness = 0.5.dp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                item { Spacer(Modifier.height(16.dp)) }
            }
        }
    }
}

// ─── 日期分组标题（带主色左边条）─────────────────────────────────────────────

@Composable
fun DayHeader(date: String, income: Double, expense: Double) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // 主色左边条
            Box(
                Modifier
                    .width(3.dp)
                    .height(16.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.colorScheme.primary)
            )
            Text(
                date,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            if (income > 0) Text(
                "+${formatAmount(income)}",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.incomeColor
            )
            if (expense > 0) Text(
                "-${formatAmount(expense)}",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.expenseColor
            )
        }
    }
}

// ─── 滑动删除包装（背景为 errorContainer，内容透明以露出 Card）─────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeToDismissTransactionItem(
    item: TransactionWithCategory,
    onDismiss: () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = {
            if (it == SwipeToDismissBoxValue.EndToStart) { onDismiss(); true } else false
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            val color by animateColorAsState(
                targetValue = if (dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart)
                    MaterialTheme.colorScheme.errorContainer
                else Color.Transparent,
                label = "swipe_bg"
            )
            Box(
                Modifier.fillMaxSize().background(color).padding(end = 20.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                if (dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Delete, null,
                            tint = MaterialTheme.colorScheme.error)
                        Text("删除",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    ) {
        // 内容直接用 TransactionItem（背景透明，Card 背景已由上层 Card 提供）
        TransactionItem(item = item)
    }
}
