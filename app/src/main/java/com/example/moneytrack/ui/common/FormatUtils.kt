package com.example.moneytrack.ui.common

import java.text.SimpleDateFormat
import java.util.*

fun formatAmount(amount: Double): String = "¥%.2f".format(amount)

fun formatDate(ms: Long, pattern: String = "MM-dd"): String =
    SimpleDateFormat(pattern, Locale.CHINESE).format(Date(ms))

fun formatDateFull(ms: Long): String =
    SimpleDateFormat("yyyy年MM月dd日", Locale.CHINESE).format(Date(ms))

fun formatMonthHeader(ms: Long): String =
    SimpleDateFormat("yyyy年MM月dd日 EEEE", Locale.CHINESE).format(Date(ms))
