package com.example.moneytrack.ui.add

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.moneytrack.data.model.Category
import com.example.moneytrack.data.model.SUPPORTED_CURRENCIES
import com.example.moneytrack.data.model.TransactionType
import com.example.moneytrack.data.model.currencySymbol
import com.example.moneytrack.ui.common.formatDateFull
import com.example.moneytrack.viewmodel.AddTransactionViewModel
import com.example.moneytrack.viewmodel.CurrencyViewModel
import com.example.moneytrack.viewmodel.ViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    factory: ViewModelFactory,
    currencyVm: CurrencyViewModel,
    transactionId: Long = -1L,
    onBack: () -> Unit
) {
    val viewModel: AddTransactionViewModel = viewModel(factory = factory)
    val isEditMode = transactionId >= 0L

    var selectedType       by remember { mutableStateOf(TransactionType.EXPENSE) }
    var amountText         by remember { mutableStateOf("") }
    var note               by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableStateOf<Long?>(null) }
    var selectedDate       by remember { mutableStateOf(System.currentTimeMillis()) }
    var selectedCurrency   by remember { mutableStateOf(currencyVm.getLastUsedCurrency()) }
    var showDatePicker     by remember { mutableStateOf(false) }
    var amountError        by remember { mutableStateOf(false) }
    var categoryError      by remember { mutableStateOf(false) }
    var showDeleteConfirm  by remember { mutableStateOf(false) }

    val loadedTransaction by viewModel.loadedTransaction.collectAsStateWithLifecycle()
    var initialized        by remember { mutableStateOf(false) }

    LaunchedEffect(transactionId) {
        if (isEditMode) viewModel.loadTransaction(transactionId)
    }

    LaunchedEffect(loadedTransaction) {
        if (isEditMode && !initialized && loadedTransaction != null) {
            val t = loadedTransaction!!
            selectedType       = t.type
            amountText         = if (t.amount == t.amount.toLong().toDouble())
                                     t.amount.toLong().toString()
                                 else t.amount.toString()
            note               = t.note
            selectedDate       = t.date
            selectedCategoryId = t.categoryId
            selectedCurrency   = t.currency
            initialized        = true
        }
    }

    val expenseCategories by viewModel.expenseCategories.collectAsStateWithLifecycle()
    val incomeCategories  by viewModel.incomeCategories.collectAsStateWithLifecycle()
    val categories = if (selectedType == TransactionType.EXPENSE) expenseCategories else incomeCategories

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDate)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { selectedDate = it }
                    showDatePicker = false
                }) { Text("确定") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("取消") } }
        ) { DatePicker(state = datePickerState) }
    }

    if (showDeleteConfirm && loadedTransaction != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("删除账单") },
            text  = { Text("确定删除这条账单记录吗？此操作不可撤销。") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteTransaction(loadedTransaction!!) { onBack() }
                    showDeleteConfirm = false
                }) { Text("删除", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { showDeleteConfirm = false }) { Text("取消") } }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditMode) "修改记账" else "记一笔") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "返回") }
                },
                actions = {
                    if (isEditMode) {
                        IconButton(onClick = { showDeleteConfirm = true }) {
                            Icon(Icons.Default.Delete, "删除",
                                tint = MaterialTheme.colorScheme.error)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.height(4.dp))

            Surface(
                shape = RoundedCornerShape(50.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(Modifier.fillMaxWidth().padding(4.dp)) {
                    listOf(TransactionType.EXPENSE to "支出", TransactionType.INCOME to "收入")
                        .forEach { (type, label) ->
                            val isSelected = selectedType == type
                            Box(
                                Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(46.dp))
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primary
                                        else Color.Transparent
                                    )
                                    .clickable {
                                        if (selectedType != type) {
                                            selectedType = type
                                            selectedCategoryId = null
                                        }
                                    }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    label,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                            else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    "货币",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SUPPORTED_CURRENCIES.forEach { cur ->
                        val isSelected = selectedCurrency == cur.code
                        FilterChip(
                            selected = isSelected,
                            onClick  = { selectedCurrency = cur.code },
                            label    = {
                                Text(
                                    "${cur.symbol} ${cur.code}",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = amountText,
                onValueChange = {
                    amountText = it.filter { c -> c.isDigit() || c == '.' }
                    amountError = false
                },
                label = { Text("金额") },
                prefix = {
                    Text(
                        currencySymbol(selectedCurrency),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                isError = amountError,
                supportingText = if (amountError) {{ Text("请输入有效金额") }} else null,
                singleLine = true,
                textStyle = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                shape = RoundedCornerShape(16.dp)
            )

            OutlinedTextField(
                value = formatDateFull(selectedDate),
                onValueChange = {},
                label = { Text("日期") },
                modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true },
                enabled = false,
                trailingIcon = {
                    Icon(Icons.Default.DateRange, "选择日期",
                        tint = MaterialTheme.colorScheme.primary)
                },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor         = MaterialTheme.colorScheme.onSurface,
                    disabledBorderColor       = MaterialTheme.colorScheme.outlineVariant,
                    disabledLabelColor        = MaterialTheme.colorScheme.onSurfaceVariant,
                    disabledTrailingIconColor = MaterialTheme.colorScheme.primary
                )
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "选择分类",
                        style = MaterialTheme.typography.labelLarge,
                        color = if (categoryError) MaterialTheme.colorScheme.error
                                else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(4),
                        modifier = Modifier.fillMaxWidth().heightIn(max = 210.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(categories, key = { it.id }) { cat ->
                            CategoryChip(
                                category   = cat,
                                isSelected = selectedCategoryId == cat.id,
                                onClick    = { selectedCategoryId = cat.id; categoryError = false }
                            )
                        }
                    }
                    if (categoryError) {
                        Text("请选择分类",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error)
                    }
                }
            }

            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("备注（可选）") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(16.dp)
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull()
                    if (amount == null || amount <= 0) { amountError = true; return@Button }
                    if (selectedCategoryId == null) { categoryError = true; return@Button }
                    currencyVm.setLastUsedCurrency(selectedCurrency)
                    if (isEditMode && loadedTransaction != null) {
                        viewModel.updateTransaction(
                            original   = loadedTransaction!!,
                            amount     = amount,
                            type       = selectedType,
                            categoryId = selectedCategoryId!!,
                            note       = note.trim(),
                            date       = selectedDate,
                            currency   = selectedCurrency,
                            onSuccess  = onBack
                        )
                    } else {
                        viewModel.saveTransaction(
                            amount     = amount,
                            type       = selectedType,
                            categoryId = selectedCategoryId!!,
                            note       = note.trim(),
                            date       = selectedDate,
                            currency   = selectedCurrency,
                            onSuccess  = onBack
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    if (isEditMode) "保存修改" else "保存",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
fun CategoryChip(category: Category, isSelected: Boolean, onClick: () -> Unit) {
    val catColor = Color(category.color)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(if (isSelected) catColor else catColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = category.name.take(1),
                color = if (isSelected) Color.White else catColor,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = category.name,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1,
            color = if (isSelected) catColor else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
