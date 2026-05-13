package com.example.moneytrack.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moneytrack.data.model.SplitGroup
import com.example.moneytrack.data.model.SplitItem
import com.example.moneytrack.data.repository.MoneyRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SplitViewModel(private val repository: MoneyRepository) : ViewModel() {

    // ─── 所有分账组 ────────────────────────────────────────────────

    val allGroups = repository.getAllSplitGroups()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** 所有历史分账组中出现过的参与人，去重排序，供新建时快速复选 */
    val historicalParticipants = allGroups
        .map { groups ->
            groups.flatMap { g ->
                g.participants.split(",").map { it.trim() }.filter { it.isNotBlank() }
            }.distinct().sorted()
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ─── 当前打开的组 ──────────────────────────────────────────────

    private val _currentGroupId = MutableStateFlow(-1L)

    /** 当前组对象，随 DB 变化自动更新（从 allGroups 派生，无需额外查询） */
    val currentGroup: StateFlow<SplitGroup?> = _currentGroupId
        .combine(allGroups) { id, groups -> groups.firstOrNull { it.id == id } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // ─── 当前组的条目 ──────────────────────────────────────────────

    private val _currentItems = MutableStateFlow<List<SplitItem>>(emptyList())
    val currentItems: StateFlow<List<SplitItem>> = _currentItems.asStateFlow()

    private var loadJob: Job? = null

    fun loadItems(groupId: Long) {
        if (_currentGroupId.value == groupId) return
        _currentGroupId.value = groupId
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            repository.getSplitItemsByGroup(groupId).collect { _currentItems.value = it }
        }
    }

    // ─── Group CRUD ────────────────────────────────────────────────

    fun addGroup(group: SplitGroup, onCreated: (Long) -> Unit = {}) = viewModelScope.launch {
        val id = repository.insertSplitGroup(group)
        onCreated(id)
    }

    fun deleteGroup(group: SplitGroup) = viewModelScope.launch {
        repository.deleteSplitGroup(group)
    }

    // ─── 参与人管理（持久化到 split_group.participants）─────────────

    /** 获取某组的参与人列表（去空过滤） */
    fun participantList(group: SplitGroup): List<String> =
        group.participants.split(",").map { it.trim() }.filter { it.isNotBlank() }

    /** 向组添加一个新参与人（已存在则跳过） */
    fun addParticipant(group: SplitGroup, name: String) {
        val current = participantList(group)
        if (name.isBlank() || name in current) return
        val updated = (current + name.trim()).joinToString(",")
        viewModelScope.launch { repository.updateSplitGroup(group.copy(participants = updated)) }
    }

    /** 从组删除一个参与人 */
    fun removeParticipant(group: SplitGroup, name: String) {
        val updated = participantList(group).filter { it != name }.joinToString(",")
        viewModelScope.launch { repository.updateSplitGroup(group.copy(participants = updated)) }
    }

    // ─── Item CRUD ─────────────────────────────────────────────────

    fun addItem(item: SplitItem) = viewModelScope.launch { repository.insertSplitItem(item) }
    fun deleteItem(item: SplitItem) = viewModelScope.launch { repository.deleteSplitItem(item) }

    // ─── Settlement calculation ────────────────────────────────────

    /**
     * 贪心最小化转账次数算法。
     * 返回 Triple(付款人, 收款人, 金额)。
     */
    fun calcSettlement(items: List<SplitItem>): List<Triple<String, String, Double>> {
        val balance = mutableMapOf<String, Double>()
        for (item in items) {
            val parts = item.participants.split(",").map { it.trim() }.filter { it.isNotBlank() }
            if (parts.isEmpty()) continue
            val share = item.price / parts.size
            balance[item.payer] = (balance[item.payer] ?: 0.0) + item.price
            for (p in parts) balance[p] = (balance[p] ?: 0.0) - share
        }

        val creditors = balance.filter { it.value >  0.001 }
            .map { (k, v) -> Pair(k, v) }.sortedByDescending { it.second }.toMutableList()
        val debtors   = balance.filter { it.value < -0.001 }
            .map { (k, v) -> Pair(k, v) }.sortedBy { it.second }.toMutableList()

        val result = mutableListOf<Triple<String, String, Double>>()
        var ci = 0; var di = 0
        while (ci < creditors.size && di < debtors.size) {
            val (cName, cAmt) = creditors[ci]
            val (dName, dAmt) = debtors[di]
            val transfer = minOf(cAmt, -dAmt)
            result.add(Triple(dName, cName, transfer))
            creditors[ci] = Pair(cName, cAmt - transfer)
            debtors[di]   = Pair(dName, dAmt + transfer)
            if (creditors[ci].second < 0.001) ci++
            if (-debtors[di].second  < 0.001) di++
        }
        return result
    }
}
