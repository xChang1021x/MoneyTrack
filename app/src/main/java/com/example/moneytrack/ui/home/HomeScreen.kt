package com.example.moneytrack.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.moneytrack.data.model.TransactionType
import com.example.moneytrack.data.model.TransactionWithCategory
import com.example.moneytrack.ui.common.formatAmount
import com.example.moneytrack.ui.common.formatDate
import com.example.moneytrack.ui.theme.expenseColor
import com.example.moneytrack.ui.theme.incomeColor
import com.example.moneytrack.viewmodel.HomeViewModel
import com.example.moneytrack.viewmodel.ViewModelFactory
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    factory: ViewModelFactory,
    onAddClick: () -> Unit,
    onSearchClick: () -> Unit,
    onEditTransaction: (Long) -> Unit = {}
) {
    val viewModel: HomeViewModel = viewModel(factory = factory)
    val income  by viewModel.monthlyIncome.collectAsStateWithLifecycle()
    val expense by viewModel.monthlyExpense.collectAsStateWithLifecycle()
    val balance by viewModel.balance.collectAsStateWithLifecycle()
    val recentList by viewModel.recentTransactions.collectAsStateWithLifecycle()

    val cal   = remember { Calendar.getInstance() }
    val year  = cal.get(Calendar.YEAR)
    val month = cal.get(Calendar.MONTH) + 1

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("MoneyTrack", style = MaterialTheme.typography.titleLarge) },
                actions = {
                    IconButton(onClick = onSearchClick) {
                        Icon(Icons.Default.Search, "搜索")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            LargeFloatingActionButton(
                onClick = onAddClick,
                shape   = CircleShape,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, "记一笔", Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.onPrimary)
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // ── Hero 卡片 ─────────────────────────────────────────
            item {
                HeroCard(
                    year = year, month = month,
                    income = income, expense = expense, balance = balance
                )
                Spacer(Modifier.height(24.dp))
            }

            // ── 本月账单标题 ──────────────────────────────────────
            item {
                Text(
                    "本月账单",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }

            if (recentList.isEmpty()) {
                item {
                    Box(
                        Modifier.fillMaxWidth().padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally,
                               verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("暂无账单",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.outline)
                            Text("点击右下角 + 开始记账",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline)
                        }
                    }
                }
            } else {
                // 把所有账单放进一个 Card 容器，视觉上更统一
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        elevation = CardDefaults.cardElevation(0.dp)
                    ) {
                        Column(Modifier.padding(vertical = 4.dp)) {
                            recentList.forEachIndexed { index, item ->
                                TransactionItem(
                                    item    = item,
                                    onClick = { onEditTransaction(item.transaction.id) }
                                )
                                if (index < recentList.lastIndex) {
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
        }
    }
}

// ─── Hero 卡片 ─────────────────────────────────────────────────────────────

@Composable
fun HeroCard(year: Int, month: Int, income: Double, expense: Double, balance: Double) {
    val isPositive = balance >= 0
    val gradient = Brush.linearGradient(
        listOf(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.85f),
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(gradient)
            .padding(horizontal = 24.dp, vertical = 28.dp)
    ) {
        Column {
            // 月份标签
            Text(
                "${year}年${month}月",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
            )
            Spacer(Modifier.height(12.dp))

            // 结余金额（主视觉）
            Text(
                formatAmount(balance),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Black,
                color = if (isPositive) MaterialTheme.colorScheme.onPrimaryContainer
                        else MaterialTheme.colorScheme.expenseColor
            )
            Text(
                "本月结余",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
            )

            Spacer(Modifier.height(24.dp))

            // 收入 / 支出 行
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                IncomeExpensePill(
                    label = "收入",
                    amount = income,
                    isIncome = true,
                    modifier = Modifier.weight(1f)
                )
                IncomeExpensePill(
                    label = "支出",
                    amount = expense,
                    isIncome = false,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun IncomeExpensePill(
    label: String,
    amount: Double,
    isIncome: Boolean,
    modifier: Modifier = Modifier
) {
    val color = if (isIncome) MaterialTheme.colorScheme.incomeColor
                else MaterialTheme.colorScheme.expenseColor
    val icon  = if (isIncome) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.background.copy(alpha = 0.25f))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, Modifier.size(14.dp), tint = color)
        }
        Column {
            Text(label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f))
            Text(formatAmount(amount),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = color)
        }
    }
}

// ─── 账单条目（共用于 Home / History / Search）────────────────────────────

@Composable
fun TransactionItem(
    item: TransactionWithCategory,
    onClick: (() -> Unit)? = null
) {
    val t   = item.transaction
    val cat = item.category
    val isExpense = t.type == TransactionType.EXPENSE
    val catColor  = Color(cat?.color ?: 0xFF78909C)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 分类圆形色块
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(catColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = cat?.name?.take(1) ?: "?",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = catColor
            )
        }

        Spacer(Modifier.width(14.dp))

        // 分类名 + 备注
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(cat?.name ?: "未知分类",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium)
            if (t.note.isNotBlank()) {
                Text(t.note,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1)
            }
        }

        // 金额 + 日期
        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = "${if (isExpense) "-" else "+"}${formatAmount(t.amount)}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = if (isExpense) MaterialTheme.colorScheme.expenseColor
                        else MaterialTheme.colorScheme.incomeColor
            )
            Text(
                text = formatDate(t.date),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
