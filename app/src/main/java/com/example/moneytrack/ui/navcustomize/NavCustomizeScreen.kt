package com.example.moneytrack.ui.navcustomize

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.moneytrack.data.preferences.NavPreferences
import com.example.moneytrack.viewmodel.NavViewModel

// ─── 可配置导航页元信息 ────────────────────────────────────────────────────

data class NavOption(val route: String, val label: String, val icon: ImageVector)

val ALL_NAV_OPTIONS = listOf(
    NavOption("home",     "首页",     Icons.Default.Home),
    NavOption("history",  "账单",     Icons.Default.List),
    NavOption("add",      "记账",     Icons.Default.Add),
    NavOption("chart",    "统计",     Icons.Default.PieChart),
    NavOption("debt",     "借贷记录", Icons.Default.CurrencyExchange),
    NavOption("split",    "分账",     Icons.Default.Calculate),
    NavOption("category", "分类管理", Icons.Default.Category),
    NavOption("budget",   "预算管理", Icons.Default.AccountBalance),
    NavOption("search",   "搜索",     Icons.Default.Search),
)

private val routeToOption = ALL_NAV_OPTIONS.associateBy { it.route }

// ─── 主界面 ────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun NavCustomizeScreen(navVm: NavViewModel, onBack: () -> Unit) {

    val savedRoutes by navVm.routes.collectAsStateWithLifecycle()
    val selected = remember(savedRoutes) { savedRoutes.toMutableStateList() }
    val unselected = ALL_NAV_OPTIONS.filter { it.route !in selected }
    val isFull = selected.size >= NavPreferences.MAX_SLOTS

    LaunchedEffect(selected.toList()) { navVm.updateRoutes(selected.toList()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("自定义导航栏") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) }
                },
                actions = {
                    TextButton(onClick = {
                        selected.clear()
                        selected.addAll(NavPreferences.DEFAULT_ROUTES.split(","))
                    }) { Text("重置") }
                },
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

            // ── 提示横幅 ──────────────────────────────────────────
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    elevation = CardDefaults.cardElevation(0.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Info, null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer)
                        Text(
                            "已选 ${selected.size} / ${NavPreferences.MAX_SLOTS} 个快捷功能，" +
                                    "「更多」始终固定在末位",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            // ── 导航栏预览 ────────────────────────────────────────
            item {
                NavPreview(routes = selected)
            }

            // ── 已选功能 ──────────────────────────────────────────
            item {
                Text(
                    "已选功能",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }

            if (selected.isEmpty()) {
                item {
                    Text("至少添加一个功能",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(start = 4.dp))
                }
            }

            itemsIndexed(selected) { index, route ->
                val option = routeToOption[route] ?: return@itemsIndexed
                SelectedItemRow(
                    option      = option,
                    canMoveUp   = index > 0,
                    canMoveDown = index < selected.lastIndex,
                    onMoveUp    = { selected.apply { add(index - 1, removeAt(index)) } },
                    onMoveDown  = { selected.apply { add(index + 1, removeAt(index)) } },
                    onRemove    = { selected.removeAt(index) }
                )
            }

            // 固定的 Profile 行
            item { FixedItemRow(label = "更多", icon = Icons.Default.MoreHoriz) }

            // ── 可添加功能 ────────────────────────────────────────
            if (unselected.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        if (isFull) "可添加功能（已满 ${NavPreferences.MAX_SLOTS} 个，请先移除再添加）"
                        else "可添加功能",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
                item {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement   = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        unselected.forEach { option ->
                            FilterChip(
                                selected = false,
                                onClick  = { selected.add(option.route) },
                                enabled  = !isFull,
                                label    = { Text(option.label) },
                                leadingIcon = { Icon(option.icon, null, Modifier.size(16.dp)) }
                            )
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(32.dp)) }
        }
    }
}

// ─── 底部导航栏迷你预览 ────────────────────────────────────────────────────

@Composable
private fun NavPreview(routes: List<String>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text("预览",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline)
            Row(
                Modifier.fillMaxWidth().padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                routes.forEach { route ->
                    val opt = routeToOption[route]
                    if (opt != null) {
                        PreviewNavItem(
                            icon     = opt.icon,
                            label    = opt.label,
                            selected = route == routes.firstOrNull()
                        )
                    }
                }
                PreviewNavItem(icon = Icons.Default.MoreHoriz, label = "更多", selected = false)
            }
        }
    }
}

@Composable
private fun PreviewNavItem(icon: ImageVector, label: String, selected: Boolean) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Icon(
            icon, null,
            Modifier.size(22.dp),
            tint = if (selected) MaterialTheme.colorScheme.primary
                   else MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = if (selected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ─── 已选功能行 ────────────────────────────────────────────────────────────

@Composable
private fun SelectedItemRow(
    option: NavOption,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(option.icon, null,
                Modifier.size(22.dp),
                tint = MaterialTheme.colorScheme.primary)
            Text(
                option.label,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            IconButton(
                onClick = onMoveUp,
                enabled = canMoveUp,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    Icons.Default.KeyboardArrowUp, "上移",
                    tint = if (canMoveUp) MaterialTheme.colorScheme.onSurface
                           else MaterialTheme.colorScheme.outlineVariant
                )
            }
            IconButton(
                onClick = onMoveDown,
                enabled = canMoveDown,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    Icons.Default.KeyboardArrowDown, "下移",
                    tint = if (canMoveDown) MaterialTheme.colorScheme.onSurface
                           else MaterialTheme.colorScheme.outlineVariant
                )
            }
            IconButton(onClick = onRemove, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Default.Close, "移除", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

// ─── 固定末位 Profile 行 ───────────────────────────────────────────────────

@Composable
private fun FixedItemRow(label: String, icon: ImageVector) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(icon, null, Modifier.size(22.dp), tint = MaterialTheme.colorScheme.outline)
            Text(
                label,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.weight(1f)
            )
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Text(
                    "固定",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}
