package com.example.moneytrack.ui.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onCategoryClick: () -> Unit,
    onBudgetClick: () -> Unit,
    onDebtClick: () -> Unit,
    onSplitClick: () -> Unit,
    onNavCustomizeClick: () -> Unit
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("更多") }) }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("功能管理", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.outline)
            MenuRow(icon = Icons.Default.Category, title = "分类管理", subtitle = "自定义收支分类", onClick = onCategoryClick)
            MenuRow(icon = Icons.Default.AccountBalance, title = "预算管理", subtitle = "设置月度分类预算", onClick = onBudgetClick)
            MenuRow(icon = Icons.Default.CurrencyExchange, title = "借贷记录", subtitle = "记录借出 / 借入，逾期高亮提醒", onClick = onDebtClick)
            MenuRow(icon = Icons.Default.Calculate, title = "分账计算器", subtitle = "多人消费一键结算", onClick = onSplitClick)
            Spacer(modifier = Modifier.height(8.dp))
            Text("个性化", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.outline)
            MenuRow(icon = Icons.Default.Tune, title = "自定义导航栏", subtitle = "选择底部快捷入口及其顺序", onClick = onNavCustomizeClick)
            Spacer(modifier = Modifier.height(8.dp))
            Text("关于", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.outline)
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("MoneyTrack", style = MaterialTheme.typography.titleMedium)
                    Text("简洁好用的个人记账应用", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                    Text("版本 1.0.0", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                }
            }
        }
    }
}

@Composable
fun MenuRow(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = title, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyLarge)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
            }
            Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.outline)
        }
    }
}
