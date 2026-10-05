package vn.personal.qlcongviec.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "team_members")
data class TeamMemberEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val role: String = "Kỹ thuật viên"
)

@Entity(
    tableName = "work_orders",
    foreignKeys = [
        ForeignKey(
            entity = TeamMemberEntity::class,
            parentColumns = ["id"],
            childColumns = ["memberId"],
            onDelete = ForeignKey.RESTRICT,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [Index("memberId")]
)
data class WorkOrderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String = "",
    val memberId: Long,
    val scheduledAt: Long,
    val completedAt: Long? = null,
    val priority: String = "Vừa",
    val status: String = "Mới",
    val location: String = "",
    val checklist: String = "",
    val materials: String = ""
)
