package com.example.moneytrack.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CallMerge
import androidx.compose.material.icons.filled.CallSplit
import androidx.compose.material.icons.filled.Refresh
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
import com.example.moneytrack.data.model.SUPPORTED_CURRENCIES
import com.example.moneytrack.data.model.TransactionType
import com.example.moneytrack.data.model.TransactionWithCategory
import com.example.moneytrack.data.model.convertCurrency
import com.example.moneytrack.data.model.currencySymbol
import com.example.moneytrack.data.model.formatAmountCurrency
import com.example.moneytrack.ui.common.formatDate
import com.example.moneytrack.ui.theme.expenseColor
import com.example.moneytrack.ui.theme.incomeColor
import com.example.moneytrack.viewmodel.CurrencyViewModel
import com.example.moneytrack.viewmodel.HomeViewModel
import com.example.moneytrack.viewmodel.ViewModelFactory
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    factory: ViewModelFactory,
    currencyVm: CurrencyViewModel,
    onAddClick: () -> Unit,
    onSearchClick: () -> Unit,
    onEditTransaction: (Long) -> Unit = {}
) {
    val viewModel: HomeViewModel = viewModel(factory = factory)
    val recentList   by viewModel.recentTransactions.collectAsStateWithLifecycle()
    val allMonthly   by viewModel.allMonthlyTransactions.collectAsStateWithLifecycle()

    val displayCurrency   by currencyVm.displayCurrency.collectAsStateWithLifecycle()
    val exchangeRates     by currencyVm.exchangeRates.collectAsStateWithLifecycle()
    val isLoading         by currencyVm.isLoading.collectAsStateWithLifecycle()
    val ratesDate         by currencyVm.ratesDate.collectAsStateWithLifecycle()
    val isMergeMode       by currencyVm.isMergeMode.collectAsStateWithLifecycle()
    val enabledCurrencies by currencyVm.enabledCurrencies.collectAsStateWithLifecycle()

    val cal   = remember { Calendar.getInstance() }
    val year  = cal.get(Calendar.YEAR)
    val month = cal.get(Calendar.MONTH) + 1

    // 根据模式过滤账单列表
    val filteredMonthly = remember(allMonthly, isMergeMode, displayCurrency) {
        if (isMergeMode) allMonthly
        else allMonthly.filter { it.transaction.currency == displayCurrency }
    }
    val filteredRecent = remember(recentList, isMergeMode, displayCurrency) {
        if (isMergeMode) recentList
        else recentList.filter { it.transaction.currency == displayCurrency }
    }

    // 汇总（合并模式下换算，分开模式下直接累加同币种）
    val convertedIncome = remember(filteredMonthly, isMergeMode, displayCurrency, exchangeRates) {
        filteredMonthly.filter { it.transaction.type == TransactionType.INCOME }.sumOf {
            if (isMergeMode) convertCurrency(it.transaction.amount, it.transaction.currency, displayCurrency, exchangeRates)
            else it.transaction.amount
        }
    }
    val convertedExpense = remember(filteredMonthly, isMergeMode, displayCurrency, exchangeRates) {
        filteredMonthly.filter { it.transaction.type == TransactionType.EXPENSE }.sumOf {
            if (isMergeMode) convertCurrency(it.transaction.amount, it.transaction.currency, displayCurrency, exchangeRates)
            else it.transaction.amount
        }
    }
    val convertedBalance = convertedIncome - convertedExpense

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("MoneyTrack", style = MaterialTheme.typography.titleLarge) },
                actions = {
                    IconButton(onClick = onSearchClick) { Icon(Icons.Default.Search, "搜索") }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            LargeFloatingActionButton(
                onClick = onAddClick,
                shape = CircleShape,
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
            item {
                HeroCard(
                    year             = year,
                    month            = month,
                    income           = convertedIncome,
                    expense          = convertedExpense,
                    balance          = convertedBalance,
                    displayCurrency  = displayCurrency,
                    ratesDate        = ratesDate,
                    isLoading        = isLoading,
                    isMergeMode      = isMergeMode,
                    enabledCurrencies = enabledCurrencies,
                    onRefreshRates   = { currencyVm.refreshRates() },
                    onCurrencySelect = { currencyVm.setDisplayCurrency(it) },
                    onToggleMode     = { currencyVm.toggleMergeMode() }
                )
                Spacer(Modifier.height(24.dp))
            }

            item {
                Text(
                    if (isMergeMode) "本月账单" else "本月 ${currencySymbol(displayCurrency)} $displayCurrency 账单",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }

            if (filteredRecent.isEmpty()) {
                item {
                    Box(Modifier.fillMaxWidth().padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally,
                               verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("暂无账单",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.outline)
                            Text(if (isMergeMode) "点击右下角 + 开始记账"
                                 else "切换到合并模式可查看所有货币账单",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline)
                        }
                    }
                }
            } else {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        elevation = CardDefaults.cardElevation(0.dp)
                    ) {
                        Column(Modifier.padding(vertical = 4.dp)) {
                            filteredRecent.forEachIndexed { index, item ->
                                TransactionItem(
                                    item    = item,
                                    onClick = { onEditTransaction(item.transaction.id) }
                                )
                                if (index < filteredRecent.lastIndex) {
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
fun HeroCard(
    year: Int,
    month: Int,
    income: Double,
    expense: Double,
    balance: Double,
    displayCurrency: String,
    ratesDate: String,
    isLoading: Boolean,
    isMergeMode: Boolean,
    enabledCurrencies: Set<String>,
    onRefreshRates: () -> Unit,
    onCurrencySelect: (String) -> Unit,
    onToggleMode: () -> Unit
) {
    val isPositive = balance >= 0
    val gradient = Brush.linearGradient(
        listOf(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.85f),
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        )
    )

    val visibleCurrencies = remember(enabledCurrencies) {
        SUPPORTED_CURRENCIES.filter { it.code in enabledCurrencies }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(gradient)
            .padding(horizontal = 24.dp, vertical = 20.dp)
    ) {
        Column {
            // 顶栏：月份 + 合并/分开模式切换 + 刷新
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "${year}年${month}月",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    // 合并 / 分开 切换按钮
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.background.copy(alpha = 0.2f),
                        modifier = Modifier.clickable(onClick = onToggleMode)
                    ) {
                        Row(
                            Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(
                                if (isMergeMode) Icons.Default.CallMerge else Icons.Default.CallSplit,
                                contentDescription = null,
                                modifier = Modifier.size(13.dp),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                            Text(
                                if (isMergeMode) "合并" else "分开",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }
                    if (isMergeMode) {
                        if (ratesDate.isNotEmpty()) {
                            Text(
                                ratesDate,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.45f)
                            )
                        }
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(14.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                            )
                        } else {
                            IconButton(
                                onClick = onRefreshRates,
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    Icons.Default.Refresh, "刷新汇率",
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(4.dp))

            // 货币切换 Chips（仅显示启用的货币）
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                visibleCurrencies.forEach { cur ->
                    val isSelected = displayCurrency == cur.code
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.background.copy(alpha = 0.25f)
                            )
                            .clickable { onCurrencySelect(cur.code) }
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "${cur.symbol} ${cur.code}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                    else MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f)
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // 结余金额
            Text(
                formatAmountCurrency(balance, displayCurrency),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Black,
                color = if (isPositive) MaterialTheme.colorScheme.onPrimaryContainer
                        else MaterialTheme.colorScheme.expenseColor
            )
            Text(
                if (isMergeMode)
                    "本月结余（已换算至 ${currencySymbol(displayCurrency)} $displayCurrency）"
                else
                    "本月结余（仅 ${currencySymbol(displayCurrency)} $displayCurrency 账单）",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
            )

            Spacer(Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                IncomeExpensePill("收入", income, displayCurrency, true,  Modifier.weight(1f))
                IncomeExpensePill("支出", expense, displayCurrency, false, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun IncomeExpensePill(
    label: String, amount: Double, currency: String,
    isIncome: Boolean, modifier: Modifier = Modifier
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
            modifier = Modifier.size(28.dp).clip(CircleShape).background(color.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) { Icon(icon, null, Modifier.size(14.dp), tint = color) }
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f))
            Text(formatAmountCurrency(amount, currency),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold, color = color)
        }
    }
}

// ─── 账单条目（共用于 Home / History / Search）────────────────────────────

@Composable
fun TransactionItem(item: TransactionWithCategory, onClick: (() -> Unit)? = null) {
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
        Box(
            modifier = Modifier.size(44.dp).clip(CircleShape)
                .background(catColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text(cat?.name?.take(1) ?: "?",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold, color = catColor)
        }

        Spacer(Modifier.width(14.dp))

        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(cat?.name ?: "未知分类",
                style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            if (t.note.isNotBlank()) {
                Text(t.note, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
            }
        }

        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                if (t.currency != "CNY") {
                    Text(
                        t.currency,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                }
                Text(
                    text = "${if (isExpense) "-" else "+"}${formatAmountCurrency(t.amount, t.currency)}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isExpense) MaterialTheme.colorScheme.expenseColor
                            else MaterialTheme.colorScheme.incomeColor
                )
            }
            Text(formatDate(t.date),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
