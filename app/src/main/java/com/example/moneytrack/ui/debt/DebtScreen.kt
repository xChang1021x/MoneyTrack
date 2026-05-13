package com.example.moneytrack.ui.debt

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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

    var showDialog  by remember { mutableStateOf(false) }
    var editingDebt by remember { mutableStateOf<Debt?>(null) }

    val unsettled = debts.filter { !it.isSettled }
    val settled   = debts.filter { it.isSettled }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("借贷记录") },
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
                onClick = { editingDebt = null; showDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, "添加借贷",
                    tint = MaterialTheme.colorScheme.onPrimary)
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        if (debts.isEmpty()) {
            Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("暂无借贷记录，点击 + 新增",
                    color = MaterialTheme.colorScheme.outline,
                    style = MaterialTheme.typography.bodyMedium)
            }
        } else {
            LazyColumn(
                Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (unsettled.isNotEmpty()) {
                    item { DebtSectionHeader("未结清", unsettled.size) }
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
                        DebtSectionHeader("已结清", null)
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
                item { Spacer(Modifier.height(80.dp)) }
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

// ─── 分组标题（带主色左边条）──────────────────────────────────────────────

@Composable
private fun DebtSectionHeader(label: String, count: Int?) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Box(
            Modifier
                .width(3.dp)
                .height(16.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(MaterialTheme.colorScheme.primary)
        )
        Text(
            if (count != null) "$label（$count）" else label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ─── 借贷卡片 ──────────────────────────────────────────────────────────────

@Composable
private fun DebtCard(
    debt: Debt,
    onSettle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val now = System.currentTimeMillis()
    val isOverdue = !debt.isSettled && debt.dueDate != null && debt.dueDate < now

    val containerColor = when {
        debt.isSettled -> MaterialTheme.colorScheme.surfaceVariant
        isOverdue      -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f)
        else           -> MaterialTheme.colorScheme.surfaceVariant
    }

    val isLent = debt.type == DebtType.LENT

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {

            // 顶部行：标签 + 姓名 + 金额
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // 类型标签
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (isLent) MaterialTheme.colorScheme.primaryContainer
                                else MaterialTheme.colorScheme.tertiaryContainer
                    ) {
                        Text(
                            if (isLent) "借出" else "借入",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isLent) MaterialTheme.colorScheme.onPrimaryContainer
                                    else MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                    // 人名
                    Text(
                        debt.personName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                // 金额
                Text(
                    "¥%.2f".format(debt.amount),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isLent) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.error
                )
            }

            // 日期信息行
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "记录：${formatDate(debt.date)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
                if (debt.dueDate != null) {
                    val dueDateColor = if (isOverdue) MaterialTheme.colorScheme.error
                                      else MaterialTheme.colorScheme.outline
                    Text(
                        "到期：${formatDate(debt.dueDate)}" + if (isOverdue) " ⚠ 已逾期" else "",
                        style = MaterialTheme.typography.bodySmall,
                        color = dueDateColor,
                        fontWeight = if (isOverdue) FontWeight.SemiBold else FontWeight.Normal
                    )
                }
            }

            if (debt.note.isNotBlank()) {
                Text(
                    debt.note,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // 操作按钮行
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant,
                thickness = 0.5.dp,
                modifier = Modifier.padding(top = 2.dp)
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                if (!debt.isSettled) {
                    TextButton(onClick = onSettle) {
                        Text("标记结清", color = MaterialTheme.colorScheme.primary)
                    }
                } else {
                    Text(
                        "已结清",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .align(Alignment.CenterVertically)
                    )
                }
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, null,
                        Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, null,
                        Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

// ─── 新增 / 编辑弹窗 ───────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DebtDialog(
    initial: Debt?,
    onDismiss: () -> Unit,
    onSave: (Debt) -> Unit
) {
    var personName  by remember { mutableStateOf(initial?.personName ?: "") }
    var amountText  by remember { mutableStateOf(if (initial != null) initial.amount.toString() else "") }
    var type        by remember { mutableStateOf(initial?.type ?: DebtType.LENT) }
    var note        by remember { mutableStateOf(initial?.note ?: "") }

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
                        onClick  = { type = DebtType.LENT },
                        label    = { Text("借出（别人欠我）") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = type == DebtType.BORROWED,
                        onClick  = { type = DebtType.BORROWED },
                        label    = { Text("借入（我欠别人）") },
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = personName,
                    onValueChange = { personName = it },
                    label = { Text("对方姓名") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("金额") },
                    prefix = { Text("¥") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = dueDateText,
                    onValueChange = { dueDateText = it },
                    label = { Text("到期日（选填，格式 yyyy-MM-dd）") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("备注（选填）") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val dueDate = if (dueDateText.isNotBlank())
                        runCatching { sdf.parse(dueDateText)?.time }.getOrNull()
                    else null
                    val debt = Debt(
                        id         = initial?.id ?: 0L,
                        personName = personName.trim(),
                        amount     = amountText.toDouble(),
                        type       = type,
                        date       = initial?.date ?: System.currentTimeMillis(),
                        dueDate    = dueDate,
                        note       = note.trim(),
                        isSettled  = initial?.isSettled ?: false
                    )
                    onSave(debt)
                },
                enabled = isValid()
            ) { Text("保存") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } }
    )
}
