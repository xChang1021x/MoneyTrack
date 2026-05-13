package com.example.moneytrack.ui.chart

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.moneytrack.ui.common.formatAmount
import com.example.moneytrack.ui.theme.expenseColor
import com.example.moneytrack.ui.theme.incomeColor
import com.example.moneytrack.viewmodel.CategoryStat
import com.example.moneytrack.viewmodel.ChartViewModel
import com.example.moneytrack.viewmodel.ViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChartScreen(factory: ViewModelFactory) {
    val viewModel: ChartViewModel = viewModel(factory = factory)
    val year    by viewModel.selectedYear.collectAsStateWithLifecycle()
    val month   by viewModel.selectedMonth.collectAsStateWithLifecycle()
    val income  by viewModel.monthlyIncome.collectAsStateWithLifecycle()
    val expense by viewModel.monthlyExpense.collectAsStateWithLifecycle()
    val stats   by viewModel.expenseCategoryStats.collectAsStateWithLifecycle()

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

            // ── 月份切换器 ────────────────────────────────────────
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
                        Text(
                            "${year}年${month}月",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = { viewModel.nextMonth() }) {
                            Icon(Icons.Default.ChevronRight, "下个月",
                                tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }

            // ── 收入 / 支出卡片 ───────────────────────────────────
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    IncomeExpenseCard("收入", income, isIncome = true, Modifier.weight(1f))
                    IncomeExpenseCard("支出", expense, isIncome = false, Modifier.weight(1f))
                }
            }

            // ── 支出饼图 ──────────────────────────────────────────
            if (stats.isNotEmpty()) {
                item {
                    Card(
                        Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        elevation = CardDefaults.cardElevation(0.dp)
                    ) {
                        Column(Modifier.padding(20.dp)) {
                            Text("支出分类",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(20.dp))
                            AnimatedDonutChart(stats = stats, totalExpense = expense)
                        }
                    }
                }

                // ── 分类明细列表 ──────────────────────────────────
                item {
                    Card(
                        Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        elevation = CardDefaults.cardElevation(0.dp)
                    ) {
                        Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                               verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            Text("分类明细",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                            stats.forEach { stat ->
                                CategoryStatRow(stat = stat, total = expense)
                            }
                        }
                    }
                }
            } else {
                item {
                    Box(
                        Modifier.fillMaxWidth().padding(vertical = 64.dp),
                        contentAlignment = Alignment.Center
                    ) {
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

// ─── 收入 / 支出卡片 ───────────────────────────────────────────────────────

@Composable
fun IncomeExpenseCard(
    label: String,
    amount: Double,
    isIncome: Boolean,
    modifier: Modifier = Modifier
) {
    val color = if (isIncome) MaterialTheme.colorScheme.incomeColor
                else MaterialTheme.colorScheme.expenseColor
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    Modifier.size(8.dp).clip(CircleShape).background(color)
                )
                Text(label,
                    style = MaterialTheme.typography.labelMedium,
                    color = color)
            }
            Text(formatAmount(amount),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color)
        }
    }
}

// ─── 带入场动画的甜甜圈图 ─────────────────────────────────────────────────

@Composable
fun AnimatedDonutChart(stats: List<CategoryStat>, totalExpense: Double) {
    if (totalExpense <= 0) return

    val animProgress = remember { Animatable(0f) }
    LaunchedEffect(stats) {
        animProgress.snapTo(0f)
        animProgress.animateTo(
            1f,
            animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing)
        )
    }

    val progress by animProgress.asState()
    val colors = stats.map { Color(it.color) }
    val sweeps = stats.map { (it.amount / totalExpense * 360f * progress).toFloat() }

    Box(
        Modifier.fillMaxWidth().height(220.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.size(200.dp)) {
            var startAngle = -90f
            val strokeWidth = size.width * 0.18f
            val inset = strokeWidth / 2f
            val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
            val topLeft = Offset(inset, inset)

            sweeps.forEachIndexed { i, sweep ->
                drawArc(
                    color     = colors.getOrElse(i) { Color.Gray },
                    startAngle = startAngle,
                    sweepAngle = sweep - 3f,
                    useCenter  = false,
                    topLeft    = topLeft,
                    size       = arcSize,
                    style      = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
                startAngle += sweep
            }
        }
        // 中心文字
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("总支出",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(formatAmount(totalExpense),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold)
        }
    }
}

// ─── 分类占比行（带进度条）────────────────────────────────────────────────

@Composable
fun CategoryStatRow(stat: CategoryStat, total: Double) {
    val percentage = if (total > 0) (stat.amount / total * 100) else 0.0
    val color = Color(stat.color)

    val animWidth = remember { Animatable(0f) }
    LaunchedEffect(stat) {
        animWidth.animateTo(
            (percentage / 100f).toFloat(),
            animationSpec = tween(700, easing = FastOutSlowInEasing)
        )
    }

    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(10.dp).clip(CircleShape).background(color)
            )
            Spacer(Modifier.width(8.dp))
            Text(stat.categoryName,
                Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium)
            Text(
                "%.1f%%".format(percentage),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.width(12.dp))
            Text(formatAmount(stat.amount),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = color)
        }
        // 进度条
        Box(
            Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(MaterialTheme.colorScheme.outlineVariant)
        ) {
            Box(
                Modifier
                    .fillMaxWidth(animWidth.value)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(2.dp))
                    .background(color)
            )
        }
    }
}
