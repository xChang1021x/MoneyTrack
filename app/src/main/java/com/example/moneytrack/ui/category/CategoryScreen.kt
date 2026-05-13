package com.example.moneytrack.ui.category

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.moneytrack.data.model.Category
import com.example.moneytrack.data.model.TransactionType
import com.example.moneytrack.viewmodel.CategoryViewModel
import com.example.moneytrack.viewmodel.ViewModelFactory

val PRESET_COLORS = listOf(
    0xFFEF5350L, 0xFFE91E63L, 0xFF9C27B0L, 0xFF673AB7L,
    0xFF3F51B5L, 0xFF2196F3L, 0xFF03A9F4L, 0xFF00BCD4L,
    0xFF009688L, 0xFF4CAF50L, 0xFF8BC34AL, 0xFFFFEB3BL,
    0xFFFFC107L, 0xFFFF9800L, 0xFFFF5722L, 0xFF78909CL
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryScreen(factory: ViewModelFactory, onBack: () -> Unit) {
    val viewModel: CategoryViewModel = viewModel(factory = factory)
    val expenseCategories by viewModel.expenseCategories.collectAsStateWithLifecycle()
    val incomeCategories  by viewModel.incomeCategories.collectAsStateWithLifecycle()

    var selectedTab  by remember { mutableIntStateOf(0) }
    var showAddDialog by remember { mutableStateOf(false) }

    if (showAddDialog) {
        val type = if (selectedTab == 0) TransactionType.EXPENSE else TransactionType.INCOME
        AddCategoryDialog(
            type = type,
            onConfirm = { name, color ->
                viewModel.addCategory(name, "label", color, type)
                showAddDialog = false
            },
            onDismiss = { showAddDialog = false }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("分类管理") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "返回") }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, "添加分类",
                    tint = MaterialTheme.colorScheme.onPrimary)
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {

            // ── 标签页切换 ─────────────────────────────────────────
            Surface(
                color = MaterialTheme.colorScheme.background,
                modifier = Modifier.fillMaxWidth()
            ) {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor   = MaterialTheme.colorScheme.background,
                    contentColor     = MaterialTheme.colorScheme.primary
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick  = { selectedTab = 0 },
                        text     = { Text("支出分类") }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick  = { selectedTab = 1 },
                        text     = { Text("收入分类") }
                    )
                }
            }

            val currentList = if (selectedTab == 0) expenseCategories else incomeCategories

            if (currentList.isEmpty()) {
                Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("暂无分类，点击 + 添加",
                        color = MaterialTheme.colorScheme.outline,
                        style = MaterialTheme.typography.bodyMedium)
                }
            } else {
                // 所有分类放入一个圆角 Card
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            ),
                            elevation = CardDefaults.cardElevation(0.dp)
                        ) {
                            Column(Modifier.padding(vertical = 4.dp)) {
                                currentList.forEachIndexed { index, cat ->
                                    CategoryRow(
                                        category = cat,
                                        onDelete = { viewModel.deleteCategory(cat) }
                                    )
                                    if (index < currentList.lastIndex) {
                                        HorizontalDivider(
                                            modifier = Modifier.padding(start = 68.dp, end = 16.dp),
                                            color = MaterialTheme.colorScheme.outlineVariant,
                                            thickness = 0.5.dp
                                        )
                                    }
                                }
                            }
                        }
                    }
                    item { Spacer(Modifier.height(80.dp)) }
                }
            }
        }
    }
}

// ─── 分类条目行 ────────────────────────────────────────────────────────────

@Composable
fun CategoryRow(category: Category, onDelete: () -> Unit) {
    var showConfirm by remember { mutableStateOf(false) }

    if (showConfirm) {
        AlertDialog(
            onDismissRequest = { showConfirm = false },
            title = { Text("删除分类") },
            text  = { Text("确定要删除「${category.name}」吗？") },
            confirmButton = {
                TextButton(onClick = { onDelete(); showConfirm = false }) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { showConfirm = false }) { Text("取消") } }
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 分类色块
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color(category.color).copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                category.name.take(1),
                color = Color(category.color),
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleSmall
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            category.name,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )
        if (category.isDefault) {
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Text(
                    "预置",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        } else {
            IconButton(onClick = { showConfirm = true }) {
                Icon(Icons.Default.Delete, "删除",
                    Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

// ─── 添加分类弹窗 ──────────────────────────────────────────────────────────

@Composable
fun AddCategoryDialog(
    type: TransactionType,
    onConfirm: (String, Long) -> Unit,
    onDismiss: () -> Unit
) {
    var name           by remember { mutableStateOf("") }
    var selectedColor  by remember { mutableLongStateOf(PRESET_COLORS.first()) }
    var nameError      by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("添加${if (type == TransactionType.EXPENSE) "支出" else "收入"}分类")
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; nameError = false },
                    label = { Text("分类名称") },
                    isError = nameError,
                    supportingText = if (nameError) {{ Text("请输入分类名称") }} else null,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                Text("选择颜色", style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                LazyVerticalGrid(
                    columns = GridCells.Fixed(8),
                    modifier = Modifier.height(80.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement   = Arrangement.spacedBy(6.dp)
                ) {
                    items(PRESET_COLORS) { color ->
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Color(color))
                                .then(
                                    if (color == selectedColor)
                                        Modifier.border(3.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                                    else Modifier
                                )
                                .clickable { selectedColor = color }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (name.isBlank()) { nameError = true; return@TextButton }
                onConfirm(name.trim(), selectedColor)
            }) { Text("添加") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } }
    )
}
