package com.example.moneytrack.ui.debt

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.moneytrack.data.model.Debt
import com.example.moneytrack.data.model.DebtType
import com.example.moneytrack.ui.common.formatDate
import com.example.moneytrack.viewmodel.DebtViewModel
import com.example.moneytrack.viewmodel.ViewModelFactory
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebtScreen(factory: ViewModelFactory, onBack: () -> Unit) {
    val vm: DebtViewModel = viewModel(factory = factory)
    val debts by vm.allDebts.collectAsStateWithLifecycle()

    var showDialog by remember { mutableStateOf(false) }
    var editingDebt by remember { mutableStateOf<Debt?>(null) }

    // 过滤分组
    val unsettled = debts.filter { !it.isSettled }
    val settled   = debts.filter { it.isSettled }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("借贷记录") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { editingDebt = null; showDialog = true }) {
                Icon(Icons.Default.Add, "添加借贷")
            }
        }
    ) { padding ->
        if (debts.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("暂无借贷记录", color = MaterialTheme.colorScheme.outline)
            }
        } else {
            LazyColumn(
                Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (unsettled.isNotEmpty()) {
                    item {
                        Text(
                            "未结清（${unsettled.size}）",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                    items(unsettled, key = { it.id }) { debt ->
                        DebtCard(
                            debt = debt,
                            onSettle = { vm.settleDebt(debt) },
                            onEdit   = { editingDebt = debt; showDialog = true },
                            onDelete = { vm.deleteDebt(debt) }
                        )
                    }
                }
                if (settled.isNotEmpty()) {
                    item {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "已结清",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                    items(settled, key = { it.id }) { debt ->
                        DebtCard(
                            debt = debt,
                            onSettle = {},
                            onEdit   = { editingDebt = debt; showDialog = true },
                            onDelete = { vm.deleteDebt(debt) }
                        )
                    }
                }
            }
        }
    }

    if (showDialog) {
        DebtDialog(
            initial = editingDebt,
            onDismiss = { showDialog = false },
            onSave = { debt ->
                if (editingDebt == null) vm.addDebt(debt)
                else vm.updateDebt(debt)
                showDialog = false
            }
        )
    }
}

@Composable
private fun DebtCard(
    debt: Debt,
    onSettle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val now = System.currentTimeMillis()
    val isOverdue = !debt.isSettled && debt.dueDate != null && debt.dueDate < now

    val cardColor = when {
        debt.isSettled -> MaterialTheme.colorScheme.surfaceVariant
        isOverdue      -> MaterialTheme.colorScheme.errorContainer
        else           -> MaterialTheme.colorScheme.surface
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                // 借/贷标签 + 姓名
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = if (debt.type == DebtType.LENT)
                            MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.tertiaryContainer
                    ) {
                        Text(
                            if (debt.type == DebtType.LENT) "借出" else "借入",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                    Text(debt.personName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                Text(
                    "¥%.2f".format(debt.amount),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (debt.type == DebtType.LENT)
                        MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.error
                )
            }

            Text(
                "记录：${formatDate(debt.date)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
            if (debt.dueDate != null) {
                val color = if (isOverdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline
                Text(
                    "到期：${formatDate(debt.dueDate)}" + if (isOverdue) "（已逾期）" else "",
                    style = MaterialTheme.typography.bodySmall,
                    color = color
                )
            }
            if (debt.note.isNotBlank()) {
                Text(debt.note, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                if (!debt.isSettled) {
                    TextButton(onClick = onSettle) { Text("标记结清") }
                } else {
                    Text(
                        "已结清",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.padding(end = 8.dp).align(Alignment.CenterVertically)
                    )
                }
                IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, null, Modifier.size(18.dp)) }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

// ─── Dialog ────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DebtDialog(
    initial: Debt?,
    onDismiss: () -> Unit,
    onSave: (Debt) -> Unit
) {
    var personName by remember { mutableStateOf(initial?.personName ?: "") }
    var amountText by remember { mutableStateOf(if (initial != null) initial.amount.toString() else "") }
    var type by remember { mutableStateOf(initial?.type ?: DebtType.LENT) }
    var note by remember { mutableStateOf(initial?.note ?: "") }

    // 到期日
    val sdf = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    var dueDateText by remember {
        mutableStateOf(if (initial?.dueDate != null) sdf.format(Date(initial.dueDate)) else "")
    }

    fun isValid() = personName.isNotBlank() && amountText.toDoubleOrNull() != null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "新增借贷" else "编辑借贷") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // 类型切换
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = type == DebtType.LENT,
                        onClick = { type = DebtType.LENT },
                        label = { Text("借出（别人欠我）") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = type == DebtType.BORROWED,
                        onClick = { type = DebtType.BORROWED },
                        label = { Text("借入（我欠别人）") },
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = personName,
                    onValueChange = { personName = it },
                    label = { Text("对方姓名") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("金额") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = dueDateText,
                    onValueChange = { dueDateText = it },
                    label = { Text("到期日（选填，格式 yyyy-MM-dd）") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("备注（选填）") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val dueDate = if (dueDateText.isNotBlank()) {
                        runCatching { sdf.parse(dueDateText)?.time }.getOrNull()
                    } else null
                    val debt = Debt(
                        id = initial?.id ?: 0L,
                        personName = personName.trim(),
                        amount = amountText.toDouble(),
                        type = type,
                        date = initial?.date ?: System.currentTimeMillis(),
                        dueDate = dueDate,
                        note = note.trim(),
                        isSettled = initial?.isSettled ?: false
                    )
                    onSave(debt)
                },
                enabled = isValid()
            ) { Text("保存") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } }
    )
}
