package com.example.moneytrack.ui.chart

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.moneytrack.data.model.TransactionType
import com.example.moneytrack.data.model.convertCurrency
import com.example.moneytrack.data.model.currencySymbol
import com.example.moneytrack.data.model.formatAmountCurrency
import com.example.moneytrack.ui.theme.expenseColor
import com.example.moneytrack.ui.theme.incomeColor
import com.example.moneytrack.viewmodel.CategoryStat
import com.example.moneytrack.viewmodel.ChartViewModel
import com.example.moneytrack.viewmodel.CurrencyViewModel
import com.example.moneytrack.viewmodel.ViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChartScreen(factory: ViewModelFactory, currencyVm: CurrencyViewModel) {
    val viewModel: ChartViewModel = viewModel(factory = factory)
    val year  by viewModel.selectedYear.collectAsStateWithLifecycle()
    val month by viewModel.selectedMonth.collectAsStateWithLifecycle()
    val allTransactions by viewModel.monthlyTransactions.collectAsStateWithLifecycle()

    val displayCurrency by currencyVm.displayCurrency.collectAsStateWithLifecycle()
    val exchangeRates   by currencyVm.exchangeRates.collectAsStateWithLifecycle()
    val isMergeMode     by currencyVm.isMergeMode.collectAsStateWithLifecycle()

    // 根据合并/分开模式过滤账单
    val relevant = remember(allTransactions, isMergeMode, displayCurrency) {
        if (isMergeMode) allTransactions
        else allTransactions.filter { it.transaction.currency == displayCurrency }
    }

    // 货币感知的收入 / 支出汇总
    val income = remember(relevant, isMergeMode, displayCurrency, exchangeRates) {
        relevant.filter { it.transaction.type == TransactionType.INCOME }.sumOf {
            if (isMergeMode) convertCurrency(it.transaction.amount, it.transaction.currency, displayCurrency, exchangeRates)
            else it.transaction.amount
        }
    }
    val expense = remember(relevant, isMergeMode, displayCurrency, exchangeRates) {
        relevant.filter { it.transaction.type == TransactionType.EXPENSE }.sumOf {
            if (isMergeMode) convertCurrency(it.transaction.amount, it.transaction.currency, displayCurrency, exchangeRates)
            else it.transaction.amount
        }
    }

    // 货币感知的分类统计
    val stats = remember(relevant, isMergeMode, displayCurrency, exchangeRates) {
        relevant.filter { it.transaction.type == TransactionType.EXPENSE }
            .groupBy { it.category }
            .map { (cat, items) ->
                CategoryStat(
                    categoryName = cat?.name ?: "未知",
                    color = cat?.color ?: 0xFF78909C,
                    amount = items.sumOf {
                        if (isMergeMode) convertCurrency(it.transaction.amount, it.transaction.currency, displayCurrency, exchangeRates)
                        else it.transaction.amount
                    }
                )
            }
            .sortedByDescending { it.amount }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("统计") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 月份切换
            item {
                Surface(
                    shape = RoundedCornerShape(50.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { viewModel.previousMonth() }) {
                            Icon(Icons.Default.ChevronLeft, "上个月",
                                tint = MaterialTheme.colorScheme.primary)
                        }
                        Text("${year}年${month}月",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold)
                        IconButton(onClick = { viewModel.nextMonth() }) {
                            Icon(Icons.Default.ChevronRight, "下个月",
                                tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }

            // 货币说明标签
            item {
                val modeLabel = if (isMergeMode)
                    "合并模式 · 已换算至 ${currencySymbol(displayCurrency)} $displayCurrency"
                else
                    "分开模式 · 仅 ${currencySymbol(displayCurrency)} $displayCurrency 账单"
                Text(
                    modeLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            // 收入 / 支出卡片
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    IncomeExpenseCard("收入", income, displayCurrency, isIncome = true,  Modifier.weight(1f))
                    IncomeExpenseCard("支出", expense, displayCurrency, isIncome = false, Modifier.weight(1f))
                }
            }

            // 支出饼图
            if (stats.isNotEmpty()) {
                item {
                    Card(
                        Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        elevation = CardDefaults.cardElevation(0.dp)
                    ) {
                        Column(Modifier.padding(20.dp)) {
                            Text("支出分类",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(20.dp))
                            AnimatedDonutChart(stats = stats, totalExpense = expense,
                                displayCurrency = displayCurrency)
                        }
                    }
                }

                item {
                    Card(
                        Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        elevation = CardDefaults.cardElevation(0.dp)
                    ) {
                        Column(
                            Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Text("分类明细",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                            stats.forEach { stat ->
                                CategoryStatRow(stat = stat, total = expense,
                                    displayCurrency = displayCurrency)
                            }
                        }
                    }
                }
            } else {
                item {
                    Box(Modifier.fillMaxWidth().padding(vertical = 64.dp),
                        contentAlignment = Alignment.Center) {
                        Text("本月暂无支出数据",
                            color = MaterialTheme.colorScheme.outline,
                            style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

@Composable
fun IncomeExpenseCard(
    label: String, amount: Double, currency: String,
    isIncome: Boolean, modifier: Modifier = Modifier
) {
    val color = if (isIncome) MaterialTheme.colorScheme.incomeColor
                else MaterialTheme.colorScheme.expenseColor
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(Modifier.size(8.dp).clip(CircleShape).background(color))
                Text(label, style = MaterialTheme.typography.labelMedium, color = color)
            }
            Text(
                formatAmountCurrency(amount, currency),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
fun AnimatedDonutChart(
    stats: List<CategoryStat>,
    totalExpense: Double,
    displayCurrency: String
) {
    if (totalExpense <= 0) return

    val animProgress = remember { Animatable(0f) }
    LaunchedEffect(stats) {
        animProgress.snapTo(0f)
        animProgress.animateTo(1f, animationSpec = tween(900, easing = FastOutSlowInEasing))
    }

    val progress by animProgress.asState()
    val colors = stats.map { Color(it.color) }
    val sweeps = stats.map { (it.amount / totalExpense * 360f * progress).toFloat() }

    Box(Modifier.fillMaxWidth().height(220.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(200.dp)) {
            var startAngle = -90f
            val strokeWidth = size.width * 0.18f
            val inset = strokeWidth / 2f
            val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
            val topLeft = Offset(inset, inset)
            sweeps.forEachIndexed { i, sweep ->
                drawArc(
                    color = colors.getOrElse(i) { Color.Gray },
                    startAngle = startAngle, sweepAngle = sweep - 3f,
                    useCenter = false, topLeft = topLeft, size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
                startAngle += sweep
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("总支出", style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(formatAmountCurrency(totalExpense, displayCurrency),
                style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun CategoryStatRow(stat: CategoryStat, total: Double, displayCurrency: String) {
    val percentage = if (total > 0) (stat.amount / total * 100) else 0.0
    val color = Color(stat.color)

    val animWidth = remember { Animatable(0f) }
    LaunchedEffect(stat) {
        animWidth.animateTo((percentage / 100f).toFloat(),
            animationSpec = tween(700, easing = FastOutSlowInEasing))
    }

    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(10.dp).clip(CircleShape).background(color))
            Spacer(Modifier.width(8.dp))
            Text(stat.categoryName, Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium)
            Text("%.1f%%".format(percentage),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.width(12.dp))
            Text(
                formatAmountCurrency(stat.amount, displayCurrency),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold, color = color
            )
        }
        Box(
            Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp))
                .background(MaterialTheme.colorScheme.outlineVariant)
        ) {
            Box(
                Modifier.fillMaxWidth(animWidth.value).fillMaxHeight()
                    .clip(RoundedCornerShape(2.dp)).background(color)
            )
        }
    }
}
