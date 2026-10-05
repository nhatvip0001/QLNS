package vn.personal.qlcongviec.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkOrderDao {
    @Query("SELECT * FROM work_orders ORDER BY scheduledAt DESC, id DESC")
    fun observeAll(): Flow<List<WorkOrderEntity>>

    @Query("SELECT COUNT(*) FROM work_orders")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(order: WorkOrderEntity)

    @Update
    suspend fun update(order: WorkOrderEntity)
}

@Dao
interface TeamMemberDao {
    @Query("SELECT * FROM team_members ORDER BY name COLLATE NOCASE")
    fun observeAll(): Flow<List<TeamMemberEntity>>

    @Query("SELECT COUNT(*) FROM team_members")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(member: TeamMemberEntity): Long
}
