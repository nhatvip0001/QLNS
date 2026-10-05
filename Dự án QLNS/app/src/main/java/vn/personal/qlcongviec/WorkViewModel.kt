package vn.personal.qlcongviec

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import vn.personal.qlcongviec.data.AppDatabase
import vn.personal.qlcongviec.data.TeamMemberEntity
import vn.personal.qlcongviec.data.WorkOrderEntity
import vn.personal.qlcongviec.data.WorkOrderRepository

class WorkViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = WorkOrderRepository(AppDatabase.get(application))

    val orders = repository.orders.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val members = repository.members.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun seedIfNeeded() {
        viewModelScope.launch { repository.seedIfEmpty() }
    }

    fun addOrder(
        title: String,
        description: String,
        member: TeamMemberEntity,
        scheduledAt: Long,
        priority: String,
        location: String,
        checklist: String,
        materials: String
    ) {
        viewModelScope.launch {
            repository.add(
                WorkOrderEntity(
                    title = title.trim(),
                    description = description.trim(),
                    memberId = member.id,
                    scheduledAt = scheduledAt,
                    priority = priority,
                    location = location.trim(),
                    checklist = checklist.trim(),
                    materials = materials.trim()
                )
            )
        }
    }

    fun advance(order: WorkOrderEntity) {
        val nextStatus = when (order.status) {
            "Mới" -> "Đang làm"
            "Đang làm" -> "Hoàn tất"
            else -> "Mới"
        }
        viewModelScope.launch {
            repository.update(
                order.copy(
                    status = nextStatus,
                    completedAt = if (nextStatus == "Hoàn tất") System.currentTimeMillis() else null
                )
            )
        }
    }
}
