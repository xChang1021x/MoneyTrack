package com.example.moneytrack.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color

// ─── 深色主题 Teal 色板 ────────────────────────────────────────────────────
val Teal300       = Color(0xFF4DB6AC)
val Teal200       = Color(0xFF80CBC4)
val TealDark900   = Color(0xFF004A45)
val TealDark800   = Color(0xFF00382F)

val DarkBg        = Color(0xFF0C1A1D)   // 主背景
val DarkSurface   = Color(0xFF111E21)   // 卡片/Surface
val DarkVariant   = Color(0xFF192C30)   // surfaceVariant

val GreenIncomeDark  = Color(0xFF81C784)   // 收入（绿 300）
val RedExpenseDark   = Color(0xFFEF9A9A)   // 支出（红 200，深色下柔和）

// ─── 浅色主题 Teal 色板 ────────────────────────────────────────────────────
val Teal700       = Color(0xFF00695C)
val Teal100       = Color(0xFFB2DFDB)

val LightBg       = Color(0xFFF2FAFB)
val LightVariant  = Color(0xFFDEF0EF)

val GreenIncomeLight = Color(0xFF2E7D32)   // 收入（绿 800）
val RedExpenseLight  = Color(0xFFC62828)   // 支出（红 800）

// ─── 语义色扩展：在整个项目中统一引用收入/支出色 ──────────────────────────
val ColorScheme.incomeColor: Color
    get() = tertiary

val ColorScheme.expenseColor: Color
    get() = error
