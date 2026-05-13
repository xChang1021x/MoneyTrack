package com.example.moneytrack.ui.chart

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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.moneytrack.ui.common.formatAmount
import com.example.moneytrack.viewmodel.CategoryStat
import com.example.moneytrack.viewmodel.ChartViewModel
import com.example.moneytrack.viewmodel.ViewModelFactory
import kotlin.math.min

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChartScreen(factory: ViewModelFactory) {
    val viewModel: ChartViewModel = viewModel(factory = factory)
    val year by viewModel.selectedYear.collectAsStateWithLifecycle()
    val month by viewModel.selectedMonth.collectAsStateWithLifecycle()
    val income by viewModel.monthlyIncome.collectAsStateWithLifecycle()
    val expense by viewModel.monthlyExpense.collectAsStateWithLifecycle()
    val stats by viewModel.expenseCategoryStats.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { TopAppBar(title = { Text("统计图表") }) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 月份切换
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { viewModel.previousMonth() }) {
                        Icon(Icons.Default.ChevronLeft, "上个月")
                    }
                    Text(
                        text = "${year}年${month}月",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    IconButton(onClick = { viewModel.nextMonth() }) {
                        Icon(Icons.Default.ChevronRight, "下个月")
                    }
                }
            }

            // 收支汇总
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SummaryChip("收入", income, Color(0xFF4CAF50), Modifier.weight(1f))
                    SummaryChip("支出", expense, MaterialTheme.colorScheme.error, Modifier.weight(1f))
                }
            }

            // 支出饼图
            if (stats.isNotEmpty()) {
                item {
                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("支出分类", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(16.dp))
                            DonutChart(stats = stats, totalExpense = expense)
                        }
                    }
                }
                // 分类列表
                items(stats) { stat ->
                    CategoryStatRow(stat = stat, total = expense)
                }
            } else {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("本月暂无支出数据", color = MaterialTheme.colorScheme.outline)
                    }
                }
            }
        }
    }
}

@Composable
fun SummaryChip(label: String, amount: Double, color: Color, modifier: Modifier = Modifier) {
    Card(modifier = modifier, shape = RoundedCornerShape(12.dp)) {
        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, style = MaterialTheme.typography.labelMedium)
            Text(formatAmount(amount), color = color, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
fun DonutChart(stats: List<CategoryStat>, totalExpense: Double) {
    if (totalExpense <= 0) return
    val colors = stats.map { Color(it.color) }
    val sweeps = stats.map { (it.amount / totalExpense * 360f).toFloat() }

    Box(
        modifier = Modifier.fillMaxWidth().height(200.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(180.dp)) {
            var startAngle = -90f
            val stroke = size.width * 0.2f
            val inset = stroke / 2
            val arcSize = Size(size.width - stroke, size.height - stroke)
            val topLeft = Offset(inset, inset)
            sweeps.forEachIndexed { i, sweep ->
                drawArc(
                    color = colors.getOrElse(i) { Color.Gray },
                    startAngle = startAngle,
                    sweepAngle = sweep - 2f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = stroke)
                )
                startAngle += sweep
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("总支出", style = MaterialTheme.typography.labelSmall)
            Text(formatAmount(totalExpense), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
        }
    }
}

@Composable
fun CategoryStatRow(stat: CategoryStat, total: Double) {
    val percentage = if (total > 0) (stat.amount / total * 100).toInt() else 0
    val color = Color(stat.color)

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(12.dp).clip(CircleShape).background(color)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(stat.categoryName, modifier = Modifier.weight(1f))
        Text("$percentage%", color = MaterialTheme.colorScheme.outline, style = MaterialTheme.typography.labelMedium)
        Spacer(modifier = Modifier.width(12.dp))
        Text(formatAmount(stat.amount), fontWeight = FontWeight.SemiBold)
    }
}
