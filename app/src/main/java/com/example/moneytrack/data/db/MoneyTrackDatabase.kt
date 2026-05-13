package com.example.moneytrack.data.db

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.moneytrack.data.model.Budget
import com.example.moneytrack.data.model.Category
import com.example.moneytrack.data.model.Debt
import com.example.moneytrack.data.model.SplitGroup
import com.example.moneytrack.data.model.SplitItem
import com.example.moneytrack.data.model.Transaction
import com.example.moneytrack.data.model.TransactionType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [Transaction::class, Category::class, Budget::class,
                Debt::class, SplitGroup::class, SplitItem::class],
    version = 3,
    exportSchema = true,
    autoMigrations = [AutoMigration(from = 1, to = 2)]
)
@TypeConverters(Converters::class)
abstract class MoneyTrackDatabase : RoomDatabase() {

    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun budgetDao(): BudgetDao
    abstract fun debtDao(): DebtDao
    abstract fun splitDao(): SplitDao

    companion object {
        @Volatile
        private var INSTANCE: MoneyTrackDatabase? = null

        fun getInstance(context: Context): MoneyTrackDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }
        }

        /** v2 → v3：split_group 新增 participants 列 */
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE split_group ADD COLUMN participants TEXT NOT NULL DEFAULT ''"
                )
            }
        }

        private fun buildDatabase(context: Context): MoneyTrackDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                MoneyTrackDatabase::class.java,
                "moneytrack.db"
            )
                .addMigrations(MIGRATION_2_3)
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // 首次创建时在后台插入预置分类
                        CoroutineScope(Dispatchers.IO).launch {
                            getInstance(context).categoryDao()
                                .insertCategories(DEFAULT_CATEGORIES)
                        }
                    }
                })
                .build()
        }

        // 预置分类
        val DEFAULT_CATEGORIES = listOf(
            // 支出分类
            Category(name = "餐饮", icon = "restaurant", color = 0xFFEF5350, type = TransactionType.EXPENSE, isDefault = true),
            Category(name = "交通", icon = "directions_car", color = 0xFF42A5F5, type = TransactionType.EXPENSE, isDefault = true),
            Category(name = "购物", icon = "shopping_bag", color = 0xFFAB47BC, type = TransactionType.EXPENSE, isDefault = true),
            Category(name = "娱乐", icon = "sports_esports", color = 0xFF26A69A, type = TransactionType.EXPENSE, isDefault = true),
            Category(name = "居家", icon = "home", color = 0xFFFF7043, type = TransactionType.EXPENSE, isDefault = true),
            Category(name = "医疗", icon = "local_hospital", color = 0xFF66BB6A, type = TransactionType.EXPENSE, isDefault = true),
            Category(name = "教育", icon = "school", color = 0xFFFFCA28, type = TransactionType.EXPENSE, isDefault = true),
            Category(name = "其他支出", icon = "more_horiz", color = 0xFF78909C, type = TransactionType.EXPENSE, isDefault = true),
            // 收入分类
            Category(name = "工资", icon = "payments", color = 0xFF4CAF50, type = TransactionType.INCOME, isDefault = true),
            Category(name = "奖金", icon = "card_giftcard", color = 0xFFFFB300, type = TransactionType.INCOME, isDefault = true),
            Category(name = "理财", icon = "trending_up", color = 0xFF29B6F6, type = TransactionType.INCOME, isDefault = true),
            Category(name = "其他收入", icon = "more_horiz", color = 0xFF78909C, type = TransactionType.INCOME, isDefault = true),
        )
    }
}
