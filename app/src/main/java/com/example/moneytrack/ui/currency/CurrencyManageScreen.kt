package com.example.moneytrack.ui.currency

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.moneytrack.data.model.SUPPORTED_CURRENCIES
import com.example.moneytrack.viewmodel.CurrencyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencyManageScreen(
    currencyVm: CurrencyViewModel,
    onBack: () -> Unit
) {
    val displayCurrency   by currencyVm.displayCurrency.collectAsStateWithLifecycle()
    val enabledCurrencies by currencyVm.enabledCurrencies.collectAsStateWithLifecycle()

    // 当前已开启数量
    val enabledCount = enabledCurrencies.size

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("货币管理") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "返回") }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    "选择在首页货币切换栏中显示的货币。\n已选货币可在首页顶部快速切换，以查看对应的账单或换算金额。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Column(Modifier.padding(vertical = 4.dp)) {
                        SUPPORTED_CURRENCIES.forEachIndexed { index, cur ->
                            val isEnabled = cur.code in enabledCurrencies
                            val isCurrent = cur.code == displayCurrency

                            // 以下情况不可关闭：
                            //   a) 是当前展示货币
                            //   b) 是仅剩的最后一个开启货币
                            val isLocked = isCurrent || (isEnabled && enabledCount <= 1)

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // 货币符号徽章
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            cur.symbol,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                                Spacer(Modifier.width(14.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        cur.name,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Medium
                                    )
                                    val subtitle = when {
                                        isCurrent            -> cur.code + "  · 当前展示货币"
                                        isLocked && isEnabled -> cur.code + "  · 至少保留一个"
                                        else                 -> cur.code
                                    }
                                    Text(
                                        subtitle,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = when {
                                            isCurrent -> MaterialTheme.colorScheme.primary
                                            isLocked  -> MaterialTheme.colorScheme.tertiary
                                            else      -> MaterialTheme.colorScheme.onSurfaceVariant
                                        }
                                    )
                                }
                                Switch(
                                    checked = isEnabled,
                                    onCheckedChange = { checked ->
                                        if (!isLocked) {
                                            val next = if (checked)
                                                enabledCurrencies + cur.code
                                            else
                                                enabledCurrencies - cur.code
                                            currencyVm.setEnabledCurrencies(next)
                                        }
                                    },
                                    enabled = !isLocked
                                )
                            }
                            if (index < SUPPORTED_CURRENCIES.lastIndex) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(start = 70.dp, end = 16.dp),
                                    color = MaterialTheme.colorScheme.outlineVariant,
                                    thickness = 0.5.dp
                                )
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    "当前展示货币始终保持启用；至少需保留一个货币开启。",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        }
    }
}
