package com.example.moneytrack.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 分账会话中的一条消费记录。
 * participants 以英文逗号分隔存储，例如 "张三,李四,王五"
 */
@Entity(
    tableName = "split_item",
    foreignKeys = [ForeignKey(
        entity = SplitGroup::class,
        parentColumns = ["id"],
        childColumns = ["groupId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("groupId")]
)
data class SplitItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val groupId: Long,
    val itemName: String,
    val price: Double,
    val payer: String,             // 出资人
    val participants: String       // 参与人（逗号分隔）
)
