package com.example.moneytrack

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.moneytrack.data.db.MoneyTrackDatabase
import com.example.moneytrack.data.preferences.NavPreferences
import com.example.moneytrack.data.repository.MoneyRepository
import com.example.moneytrack.ui.navigation.MoneyTrackNavGraph
import com.example.moneytrack.ui.theme.MoneyTrackTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database   = MoneyTrackDatabase.getInstance(applicationContext)
        val navPrefs   = NavPreferences(applicationContext)
        val repository = MoneyRepository(
            transactionDao = database.transactionDao(),
            categoryDao = database.categoryDao(),
            budgetDao = database.budgetDao(),
            debtDao = database.debtDao(),
            splitDao = database.splitDao()
        )

        setContent {
            MoneyTrackTheme {
                MoneyTrackNavGraph(repository = repository, navPrefs = navPrefs)
            }
        }
    }
}
