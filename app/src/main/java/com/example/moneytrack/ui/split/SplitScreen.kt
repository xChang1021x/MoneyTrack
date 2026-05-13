package com.example.moneytrack.ui.split

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.moneytrack.data.model.SplitGroup
import com.example.moneytrack.ui.common.formatDateFull
import com.example.moneytrack.viewmodel.SplitViewModel
import com.example.moneytrack.viewmodel.ViewModelFactory

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SplitScreen(
    factory: ViewModelFactory,
    onBack: () -> Unit,
    onOpenGroup: (Long) -> Unit
) {
    val vm: SplitViewModel = viewModel(factory = factory)
    val groups by vm.allGroups.collectAsStateWithLifecycle()

    val historicalParticipants by vm.historicalParticipants.collectAsStateWithLifecycle()

    var showCreateDialog by remember { mutableStateOf(false) }
    /** 当前正在管理参与人的组，非 null 时弹出管理弹窗 */
    var managingGroup by remember { mutableStateOf<SplitGroup?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("分账计算器") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreateDialog = true }) {
                Icon(Icons.Default.Add, "新建分账")
            }
        }
    ) { padding ->
        if (groups.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("暂无分账记录，点击 + 新建", color = MaterialTheme.colorScheme.outline)
            }
        } else {
            LazyColumn(
                Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(groups, key = { it.id }) { group ->
                    SplitGroupCard(
                        group = group,
                        participants = vm.participantList(group),
                        onClick = { onOpenGroup(group.id) },
                        onManageParticipants = { managingGroup = group },
                        onDelete = { vm.deleteGroup(group) }
                    )
                }
            }
        }
    }

    // 新建分账对话框
    if (showCreateDialog) {
        CreateGroupDialog(
            historicalParticipants = historicalParticipants,
            onDismiss = { showCreateDialog = false },
            onSave = { title, note, initialParticipants ->
                vm.addGroup(
                    SplitGroup(
                        title = title,
                        date = System.currentTimeMillis(),
                        note = note,
                        participants = initialParticipants.joinToString(",")
                    ),
                    onCreated = { id -> onOpenGroup(id) }
                )
                showCreateDialog = false
            }
        )
    }

    // 管理参与人弹窗（使用最新的 group 对象保证实时更新）
    managingGroup?.let { mg ->
        val latestGroup = groups.firstOrNull { it.id == mg.id } ?: mg
        ManageParticipantsDialog(
            group = latestGroup,
            participants = vm.participantList(latestGroup),
            onAdd = { vm.addParticipant(latestGroup, it) },
            onRemove = { vm.removeParticipant(latestGroup, it) },
            onDismiss = { managingGroup = null }
        )
    }
}

// ─── 分账组卡片 ─────────────────────────────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SplitGroupCard(
    group: SplitGroup,
    participants: List<String>,
    onClick: () -> Unit,
    onManageParticipants: () -> Unit,
    onDelete: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth(), onClick = onClick) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // 标题行
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(group.title, style = MaterialTheme.typography.titleMedium)
                    Text(formatDateFull(group.date), style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline)
                }
                // 管理参与人
                IconButton(onClick = onManageParticipants) {
                    Icon(Icons.Default.GroupAdd, "管理参与人",
                        tint = MaterialTheme.colorScheme.primary)
                }
                // 删除
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error)
                }
                Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.outline)
            }

            // 参与人芯片行
            if (participants.isEmpty()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(Icons.Default.PersonAdd, null,
                        Modifier.size(14.dp), tint = MaterialTheme.colorScheme.outline)
                    Text("点击 👥 添加参与人",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline)
                }
            } else {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    participants.forEach { name ->
                        SuggestionChip(
                            onClick = {},
                            label = { Text(name, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                    // 提示可继续添加
                    AssistChip(
                        onClick = onManageParticipants,
                        label = { Text("+", style = MaterialTheme.typography.labelSmall) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
            }
        }
    }
}

// ─── 新建分账对话框（含初始参与人设置）────────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CreateGroupDialog(
    historicalParticipants: List<String>,
    onDismiss: () -> Unit,
    onSave: (title: String, note: String, participants: List<String>) -> Unit
) {
    var title     by remember { mutableStateOf("") }
    var note      by remember { mutableStateOf("") }
    val selected  = remember { mutableStateListOf<String>() }   // 本次已选参与人
    var inputName by remember { mutableStateOf("") }

    // 历史参与人里未被选中的，作为"可选"候选
    val unselected = historicalParticipants.filter { it !in selected }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("新建分账") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // ── 基本信息 ──────────────────────────────────
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("分账名称（如：周末聚餐）") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("备注（选填）") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                HorizontalDivider()

                // ── 历史参与人快速选取 ────────────────────────
                if (historicalParticipants.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "从历史参与人选取",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.outline
                            )
                            if (unselected.isNotEmpty()) {
                                TextButton(
                                    onClick = { selected.addAll(unselected) },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                                ) { Text("全选") }
                            }
                        }
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            historicalParticipants.forEach { name ->
                                val isSelected = name in selected
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        if (isSelected) selected.remove(name)
                                        else selected.add(name)
                                    },
                                    label = { Text(name) },
                                    leadingIcon = if (isSelected) {
                                        { Icon(Icons.Default.Check, null, Modifier.size(14.dp)) }
                                    } else null
                                )
                            }
                        }
                    }
                }

                // ── 手动输入新参与人 ──────────────────────────
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        if (historicalParticipants.isEmpty()) "添加参与人（选填，也可之后再加）"
                        else "或手动添加新参与人",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = inputName,
                            onValueChange = { inputName = it },
                            label = { Text("姓名") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        FilledTonalIconButton(
                            onClick = {
                                val n = inputName.trim()
                                if (n.isNotBlank() && n !in selected) selected.add(n)
                                inputName = ""
                            },
                            enabled = inputName.isNotBlank()
                        ) { Icon(Icons.Default.Add, null) }
                    }
                }

                // ── 已选列表预览 ──────────────────────────────
                if (selected.isNotEmpty()) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        selected.forEach { name ->
                            InputChip(
                                selected = false,
                                onClick = { selected.remove(name) },
                                label = { Text(name) },
                                trailingIcon = {
                                    Icon(Icons.Default.Close, null, Modifier.size(14.dp))
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(title.trim(), note.trim(), selected.toList()) },
                enabled = title.isNotBlank()
            ) { Text("创建") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } }
    )
}

// ─── 管理参与人弹窗 ────────────────────────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ManageParticipantsDialog(
    group: SplitGroup,
    participants: List<String>,
    onAdd: (String) -> Unit,
    onRemove: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var inputName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("管理参与人 — ${group.title}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // 添加输入框
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = inputName,
                        onValueChange = { inputName = it },
                        label = { Text("添加参与人") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    FilledTonalIconButton(onClick = {
                        onAdd(inputName.trim())
                        inputName = ""
                    }, enabled = inputName.isNotBlank()) {
                        Icon(Icons.Default.Add, null)
                    }
                }
                HorizontalDivider()
                if (participants.isEmpty()) {
                    Text("暂无参与人", style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline)
                } else {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        participants.forEach { name ->
                            InputChip(
                                selected = false,
                                onClick = { onRemove(name) },
                                label = { Text(name) },
                                trailingIcon = {
                                    Icon(Icons.Default.Close, "移除", Modifier.size(14.dp))
                                }
                            )
                        }
                    }
                    Text("点击芯片可移除参与人",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline)
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("完成") } }
    )
}
