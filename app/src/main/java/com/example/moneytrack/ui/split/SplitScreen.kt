package com.example.moneytrack.ui.split

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
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
    var managingGroup   by remember { mutableStateOf<SplitGroup?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("分账计算器") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, "新建分账",
                    tint = MaterialTheme.colorScheme.onPrimary)
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        if (groups.isEmpty()) {
            Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("暂无分账记录",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.outline)
                    Text("点击右下角 + 新建",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline)
                }
            }
        } else {
            LazyColumn(
                Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(groups, key = { it.id }) { group ->
                    SplitGroupCard(
                        group        = group,
                        participants = vm.participantList(group),
                        onClick      = { onOpenGroup(group.id) },
                        onManageParticipants = { managingGroup = group },
                        onDelete     = { vm.deleteGroup(group) }
                    )
                }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }

    if (showCreateDialog) {
        CreateGroupDialog(
            historicalParticipants = historicalParticipants,
            onDismiss = { showCreateDialog = false },
            onSave = { title, note, initialParticipants ->
                vm.addGroup(
                    SplitGroup(
                        title        = title,
                        date         = System.currentTimeMillis(),
                        note         = note,
                        participants = initialParticipants.joinToString(",")
                    ),
                    onCreated = { id -> onOpenGroup(id) }
                )
                showCreateDialog = false
            }
        )
    }

    managingGroup?.let { mg ->
        val latestGroup = groups.firstOrNull { it.id == mg.id } ?: mg
        ManageParticipantsDialog(
            group        = latestGroup,
            participants = vm.participantList(latestGroup),
            onAdd        = { vm.addParticipant(latestGroup, it) },
            onRemove     = { vm.removeParticipant(latestGroup, it) },
            onDismiss    = { managingGroup = null }
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
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick  = onClick,
        shape    = RoundedCornerShape(20.dp),
        colors   = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // 标题 + 操作按钮行
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        group.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        formatDateFull(group.date),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
                IconButton(onClick = onManageParticipants) {
                    Icon(Icons.Default.GroupAdd, "管理参与人",
                        tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error)
                }
                Icon(Icons.Default.ChevronRight, null,
                    tint = MaterialTheme.colorScheme.outlineVariant)
            }

            // 参与人展示
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
                    verticalArrangement   = Arrangement.spacedBy(4.dp)
                ) {
                    participants.forEach { name ->
                        SuggestionChip(
                            onClick = {},
                            label   = { Text(name, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                    AssistChip(
                        onClick = onManageParticipants,
                        label   = { Text("+", style = MaterialTheme.typography.labelSmall) },
                        colors  = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
            }
        }
    }
}

// ─── 新建分账对话框 ────────────────────────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CreateGroupDialog(
    historicalParticipants: List<String>,
    onDismiss: () -> Unit,
    onSave: (title: String, note: String, participants: List<String>) -> Unit
) {
    var title     by remember { mutableStateOf("") }
    var note      by remember { mutableStateOf("") }
    val selected  = remember { mutableStateListOf<String>() }
    var inputName by remember { mutableStateOf("") }

    val unselected = historicalParticipants.filter { it !in selected }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("新建分账") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("分账名称（如：周末聚餐）") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("备注（选填）") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                if (historicalParticipants.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("从历史参与人选取",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.outline)
                            if (unselected.isNotEmpty()) {
                                TextButton(
                                    onClick = { selected.addAll(unselected) },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                                ) { Text("全选") }
                            }
                        }
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement   = Arrangement.spacedBy(6.dp)
                        ) {
                            historicalParticipants.forEach { name ->
                                val isSelected = name in selected
                                FilterChip(
                                    selected = isSelected,
                                    onClick  = {
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
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
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

                if (selected.isNotEmpty()) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement   = Arrangement.spacedBy(4.dp)
                    ) {
                        selected.forEach { name ->
                            InputChip(
                                selected = false,
                                onClick  = { selected.remove(name) },
                                label    = { Text(name) },
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
                onClick  = { onSave(title.trim(), note.trim(), selected.toList()) },
                enabled  = title.isNotBlank()
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
        title = { Text("参与人 — ${group.title}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = inputName,
                        onValueChange = { inputName = it },
                        label = { Text("添加参与人") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    FilledTonalIconButton(
                        onClick = { onAdd(inputName.trim()); inputName = "" },
                        enabled = inputName.isNotBlank()
                    ) { Icon(Icons.Default.Add, null) }
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                if (participants.isEmpty()) {
                    Text("暂无参与人",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline)
                } else {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement   = Arrangement.spacedBy(6.dp)
                    ) {
                        participants.forEach { name ->
                            InputChip(
                                selected = false,
                                onClick  = { onRemove(name) },
                                label    = { Text(name) },
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
