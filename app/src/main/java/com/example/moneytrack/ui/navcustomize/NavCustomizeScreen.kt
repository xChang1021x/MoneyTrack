package com.example.moneytrack.ui.navcustomize

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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

// ─── 所有可配置的导航页元信息（不含 Profile，它始终固定）───────────────────

data class NavOption(val route: String, val label: String, val icon: ImageVector)

val ALL_NAV_OPTIONS = listOf(
    NavOption("home",    "首页",     Icons.Default.Home),
    NavOption("history", "账单",     Icons.Default.List),
    NavOption("add",     "记账",     Icons.Default.Add),
    NavOption("chart",   "统计",     Icons.Default.PieChart),
    NavOption("debt",    "借贷记录", Icons.Default.CurrencyExchange),
    NavOption("split",   "分账",     Icons.Default.Calculate),
    NavOption("category","分类管理", Icons.Default.Category),
    NavOption("budget",  "预算管理", Icons.Default.AccountBalance),
    NavOption("search",  "搜索",     Icons.Default.Search),
)

private val routeToOption = ALL_NAV_OPTIONS.associateBy { it.route }

// ─── 主界面 ────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun NavCustomizeScreen(navVm: NavViewModel, onBack: () -> Unit) {

    val savedRoutes by navVm.routes.collectAsStateWithLifecycle()

    // 本地编辑状态：用户操作都先改这里，离开时才说明已保存（实时保存）
    val selected = remember(savedRoutes) { savedRoutes.toMutableStateList() }

    val unselected = ALL_NAV_OPTIONS.filter { it.route !in selected }
    val isFull = selected.size >= NavPreferences.MAX_SLOTS

    // 每次 selected 变动立即持久化
    LaunchedEffect(selected.toList()) {
        navVm.updateRoutes(selected.toList())
    }

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
                }
            )
        }
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // ── 提示卡片 ──────────────────────────────────────────
            item {
                InfoBanner(selectedCount = selected.size)
            }

            // ── 导航栏预览 ────────────────────────────────────────
            item {
                NavPreview(routes = selected)
            }

            // ── 已选功能 ──────────────────────────────────────────
            item {
                Text(
                    "已选功能",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            if (selected.isEmpty()) {
                item {
                    Text(
                        "至少添加一个功能",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            itemsIndexed(selected) { index, route ->
                val option = routeToOption[route] ?: return@itemsIndexed
                SelectedItemRow(
                    option = option,
                    canMoveUp   = index > 0,
                    canMoveDown = index < selected.lastIndex,
                    onMoveUp    = { selected.apply { add(index - 1, removeAt(index)) } },
                    onMoveDown  = { selected.apply { add(index + 1, removeAt(index)) } },
                    onRemove    = { selected.removeAt(index) }
                )
            }

            // 固定的 Profile 行（不可移除）
            item {
                FixedItemRow(label = "更多", icon = Icons.Default.MoreHoriz)
            }

            // ── 可添加功能 ────────────────────────────────────────
            if (unselected.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        if (isFull) "可添加功能（已满 ${NavPreferences.MAX_SLOTS} 个，请先移除再添加）"
                        else "可添加功能",
                        style = MaterialTheme.typography.titleSmall,
                        color = if (isFull) MaterialTheme.colorScheme.outline
                        else MaterialTheme.colorScheme.outline
                    )
                }
                item {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        unselected.forEach { option ->
                            AddableChip(
                                option = option,
                                enabled = !isFull,
                                onClick = { selected.add(option.route) }
                            )
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(32.dp)) }
        }
    }
}

// ─── 子组件 ────────────────────────────────────────────────────────────────

@Composable
private fun InfoBanner(selectedCount: Int) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Info, null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer)
            Text(
                "已选 $selectedCount / ${NavPreferences.MAX_SLOTS} 个快捷功能，" +
                        "「更多」始终固定在末位",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

/** 底部导航栏的迷你预览 */
@Composable
private fun NavPreview(routes: List<String>) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("预览", style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline)
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // 用户选的项
                    routes.forEach { route ->
                        val opt = routeToOption[route]
                        if (opt != null) {
                            PreviewNavItem(icon = opt.icon, label = opt.label,
                                selected = route == routes.firstOrNull())
                        }
                    }
                    // 固定的更多
                    PreviewNavItem(icon = Icons.Default.MoreHoriz, label = "更多", selected = false)
                }
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

/** 已选功能行：显示图标+名称，以及上移/下移/删除按钮 */
@Composable
private fun SelectedItemRow(
    option: NavOption,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onRemove: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                option.icon, null,
                Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                option.label,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            // 上移
            IconButton(onClick = onMoveUp, enabled = canMoveUp, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Default.KeyboardArrowUp, "上移",
                    tint = if (canMoveUp) MaterialTheme.colorScheme.onSurface
                           else MaterialTheme.colorScheme.outlineVariant)
            }
            // 下移
            IconButton(onClick = onMoveDown, enabled = canMoveDown, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Default.KeyboardArrowDown, "下移",
                    tint = if (canMoveDown) MaterialTheme.colorScheme.onSurface
                           else MaterialTheme.colorScheme.outlineVariant)
            }
            // 删除
            IconButton(onClick = onRemove, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Default.Close, "移除", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

/** 固定末位的 Profile 行（置灰，不可交互） */
@Composable
private fun FixedItemRow(label: String, icon: ImageVector) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(icon, null, Modifier.size(24.dp), tint = MaterialTheme.colorScheme.outline)
            Text(label, style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.outline, modifier = Modifier.weight(1f))
            Surface(
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Text("固定", modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer)
            }
        }
    }
}

/** 可添加的功能芯片 */
@Composable
private fun AddableChip(option: NavOption, enabled: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = false,
        onClick = onClick,
        enabled = enabled,
        label = { Text(option.label) },
        leadingIcon = { Icon(option.icon, null, Modifier.size(16.dp)) }
    )
}
