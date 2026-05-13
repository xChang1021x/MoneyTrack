package com.example.moneytrack.ui.split

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.moneytrack.data.model.SplitGroup
import com.example.moneytrack.data.model.SplitItem
import com.example.moneytrack.viewmodel.SplitViewModel
import com.example.moneytrack.viewmodel.ViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SplitDetailScreen(
    groupId: Long,
    factory: ViewModelFactory,
    onBack: () -> Unit
) {
    val vm: SplitViewModel = viewModel(factory = factory)

    LaunchedEffect(groupId) { vm.loadItems(groupId) }

    val group   by vm.currentGroup.collectAsStateWithLifecycle()
    val items   by vm.currentItems.collectAsStateWithLifecycle()

    var showAddItemDialog by remember { mutableStateOf(false) }
    var showSettlement    by remember { mutableStateOf(false) }

    val settlement  = remember(items) { vm.calcSettlement(items) }
    val totalAmount = items.sumOf { it.price }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(group?.title ?: "分账明细") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) }
                },
                actions = {
                    if (items.isNotEmpty()) {
                        IconButton(onClick = { showSettlement = true }) {
                            Icon(Icons.Default.Calculate, "查看结算")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddItemDialog = true }) {
                Icon(Icons.Default.Add, "添加条目")
            }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            // 汇总栏
            if (items.isNotEmpty()) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("共 ${items.size} 笔", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            "合计 ¥%.2f".format(totalAmount),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        TextButton(onClick = { showSettlement = true }) { Text("结算") }
                    }
                }
            }

            if (items.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("暂无消费条目，点击 + 添加", color = MaterialTheme.colorScheme.outline)
                }
            } else {
                LazyColumn(
                    Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(items, key = { it.id }) { item ->
                        SplitItemCard(item = item, onDelete = { vm.deleteItem(item) })
                    }
                }
            }
        }
    }

    // 添加条目对话框（需要当前组对象才能弹出，避免空 participants）
    if (showAddItemDialog && group != null) {
        AddItemDialog(
            group = group!!,
            onDismiss = { showAddItemDialog = false },
            onAddParticipant = { vm.addParticipant(group!!, it) },
            onSave = { item ->
                vm.addItem(item.copy(groupId = groupId))
                showAddItemDialog = false
            }
        )
    }

    // 结算弹窗
    if (showSettlement) {
        if (settlement.isNotEmpty()) {
            SettlementDialog(settlement = settlement, onDismiss = { showSettlement = false })
        } else {
            AlertDialog(
                onDismissRequest = { showSettlement = false },
                title = { Text("结算结果") },
                text = { Text("大家已各自均摊，无需额外转账 🎉") },
                confirmButton = { TextButton(onClick = { showSettlement = false }) { Text("好的") } }
            )
        }
    }
}

// ─── 条目卡片 ──────────────────────────────────────────────────────────────

@Composable
private fun SplitItemCard(item: SplitItem, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(item.itemName, style = MaterialTheme.typography.titleSmall)
                Text(
                    "出资：${item.payer}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    "参与：${item.participants.replace(",", "、")}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
            Text(
                "¥%.2f".format(item.price),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.width(4.dp))
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, null,
                    Modifier.size(18.dp), tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

// ─── 添加条目对话框 ────────────────────────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AddItemDialog(
    group: SplitGroup,
    onDismiss: () -> Unit,
    onAddParticipant: (String) -> Unit,   // 持久化新参与人到组
    onSave: (SplitItem) -> Unit
) {
    // 从 group 读取参与人（函数每次 recompose 都会拿最新 group.participants）
    val groupParticipants = group.participants
        .split(",").map { it.trim() }.filter { it.isNotBlank() }

    var itemName  by remember { mutableStateOf("") }
    var priceText by remember { mutableStateOf("") }
    var payer     by remember { mutableStateOf("") }          // 出资人（单选）
    val selected  = remember { mutableStateListOf<String>() } // 参与人（多选）
    var newName   by remember { mutableStateOf("") }          // 现场新增

    // group 参与人变化时，如果 payer/selected 中有人不在新列表里也不影响（允许不在列表里的老数据）

    fun isValid() = itemName.isNotBlank() &&
            priceText.toDoubleOrNull() != null &&
            payer.isNotBlank() &&
            selected.isNotEmpty()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("添加消费条目") },
        text = {
            Column(
                Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // ── 基本信息 ──────────────────────────────
                OutlinedTextField(
                    value = itemName,
                    onValueChange = { itemName = it },
                    label = { Text("商品 / 项目名称") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = priceText,
                    onValueChange = { priceText = it },
                    label = { Text("金额") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // ── 出资人（卡片式单选）──────────────────
                Text("出资人", style = MaterialTheme.typography.labelLarge)
                if (groupParticipants.isEmpty()) {
                    Text("请先在分账主页面添加参与人，或在下方现场添加",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline)
                } else {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        groupParticipants.forEach { name ->
                            PayerCard(
                                name = name,
                                selected = payer == name,
                                onClick = { payer = if (payer == name) "" else name }
                            )
                        }
                    }
                }

                // ── 参与人（芯片多选）────────────────────
                HorizontalDivider()
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("参与人", style = MaterialTheme.typography.labelLarge)
                    if (groupParticipants.isNotEmpty()) {
                        TextButton(
                            onClick = {
                                selected.clear()
                                selected.addAll(groupParticipants)
                            }
                        ) { Text("全选") }
                    }
                }
                if (groupParticipants.isEmpty()) {
                    Text("请先添加参与人",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline)
                } else {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        groupParticipants.forEach { name ->
                            FilterChip(
                                selected = name in selected,
                                onClick = {
                                    if (name in selected) selected.remove(name)
                                    else selected.add(name)
                                },
                                label = { Text(name) }
                            )
                        }
                    }
                }

                // ── 现场新增参与人（自动持久化到组）──────
                HorizontalDivider()
                Text("现场添加参与人", style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.outline)
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text("姓名") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    FilledTonalIconButton(
                        onClick = {
                            val n = newName.trim()
                            if (n.isNotBlank()) {
                                onAddParticipant(n)   // 持久化到 split_group
                                if (n !in selected) selected.add(n)
                                newName = ""
                            }
                        },
                        enabled = newName.isNotBlank()
                    ) { Icon(Icons.Default.PersonAdd, null) }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onSave(
                        SplitItem(
                            groupId = 0L,
                            itemName = itemName.trim(),
                            price = priceText.toDouble(),
                            payer = payer,
                            participants = selected.joinToString(",")
                        )
                    )
                },
                enabled = isValid()
            ) { Text("添加") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } }
    )
}

// ─── 出资人卡片（单选样式）────────────────────────────────────────────────

@Composable
private fun PayerCard(name: String, selected: Boolean, onClick: () -> Unit) {
    val containerColor = if (selected)
        MaterialTheme.colorScheme.primaryContainer
    else
        MaterialTheme.colorScheme.surfaceVariant

    val borderColor = if (selected)
        MaterialTheme.colorScheme.primary
    else
        MaterialTheme.colorScheme.outlineVariant

    val contentColor = if (selected)
        MaterialTheme.colorScheme.onPrimaryContainer
    else
        MaterialTheme.colorScheme.onSurfaceVariant

    Surface(
        color = containerColor,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(12.dp)
            )
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (selected) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
            } else {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(16.dp)
                )
            }
            Text(
                name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                color = contentColor
            )
        }
    }
}

// ─── 结算结果弹窗 ──────────────────────────────────────────────────────────

@Composable
private fun SettlementDialog(
    settlement: List<Triple<String, String, Double>>,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("结算结果") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "最少 ${settlement.size} 笔转账即可结清",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
                HorizontalDivider()
                settlement.forEach { (from, to, amount) ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
                        )
                    ) {
                        Row(
                            Modifier.fillMaxWidth().padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(from,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold)
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(Icons.Default.ArrowForward, null,
                                        Modifier.size(12.dp),
                                        tint = MaterialTheme.colorScheme.outline)
                                    Text(to,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.outline)
                                }
                            }
                            Text(
                                "¥%.2f".format(amount),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("关闭") } }
    )
}
