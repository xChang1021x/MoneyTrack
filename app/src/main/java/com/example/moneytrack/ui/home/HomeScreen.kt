package com.example.moneytrack.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.moneytrack.data.model.TransactionType
import com.example.moneytrack.data.model.TransactionWithCategory
import com.example.moneytrack.ui.common.formatAmount
import com.example.moneytrack.ui.common.formatDate
import com.example.moneytrack.viewmodel.HomeViewModel
import com.example.moneytrack.viewmodel.ViewModelFactory
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    factory: ViewModelFactory,
    onAddClick: () -> Unit,
    onSearchClick: () -> Unit
) {
    val viewModel: HomeViewModel = viewModel(factory = factory)
    val income by viewModel.monthlyIncome.collectAsStateWithLifecycle()
    val expense by viewModel.monthlyExpense.collectAsStateWithLifecycle()
    val balance by viewModel.balance.collectAsStateWithLifecycle()
    val recentList by viewModel.recentTransactions.collectAsStateWithLifecycle()

    val month = Calendar.getInstance().get(Calendar.MONTH) + 1

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("MoneyTrack") },
                actions = {
                    IconButton(onClick = onSearchClick) {
                        Icon(Icons.Default.Search, "搜索")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddClick) {
                Icon(Icons.Default.Add, "记一笔")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 月度总览卡片
            item {
                SummaryCard(month = month, income = income, expense = expense, balance = balance)
            }
            // 最近账单标题
            item {
                Text(
                    text = "本月账单",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            if (recentList.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("暂无账单，点击右下角 + 开始记账", color = MaterialTheme.colorScheme.outline)
                    }
                }
            } else {
                items(recentList, key = { it.transaction.id }) { item ->
                    TransactionItem(item)
                }
            }
        }
    }
}

@Composable
fun SummaryCard(month: Int, income: Double, expense: Double, balance: Double) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("${month}月概览", style = MaterialTheme.typography.labelLarge)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = formatAmount(balance),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = if (balance >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )
            Text("本月结余", style = MaterialTheme.typography.labelMedium)
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                SummaryItem(label = "收入", amount = income, color = Color(0xFF4CAF50))
                SummaryItem(label = "支出", amount = expense, color = Color(0xFFEF5350))
            }
        }
    }
}

@Composable
fun SummaryItem(label: String, amount: Double, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = formatAmount(amount), fontWeight = FontWeight.SemiBold, color = color, fontSize = 18.sp)
        Text(text = label, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
fun TransactionItem(item: TransactionWithCategory) {
    val t = item.transaction
    val cat = item.category
    val isExpense = t.type == TransactionType.EXPENSE
    val catColor = Color(cat?.color ?: 0xFF78909C)

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 分类色块
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(catColor.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = cat?.name?.take(1) ?: "?", color = catColor, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = cat?.name ?: "未知分类", style = MaterialTheme.typography.bodyLarge)
            if (t.note.isNotBlank()) {
                Text(text = t.note, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "${if (isExpense) "-" else "+"}${formatAmount(t.amount)}",
                color = if (isExpense) MaterialTheme.colorScheme.error else Color(0xFF4CAF50),
                fontWeight = FontWeight.SemiBold
            )
            Text(text = formatDate(t.date), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
        }
    }
}
