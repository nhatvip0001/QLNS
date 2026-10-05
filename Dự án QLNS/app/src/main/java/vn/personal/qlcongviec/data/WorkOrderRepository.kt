package vn.personal.qlcongviec.data

import kotlinx.coroutines.flow.Flow
import java.util.Calendar

class WorkOrderRepository(private val database: AppDatabase) {
    val orders: Flow<List<WorkOrderEntity>> = database.workOrderDao().observeAll()
    val members: Flow<List<TeamMemberEntity>> = database.teamMemberDao().observeAll()

    suspend fun seedIfEmpty() {
        val memberDao = database.teamMemberDao()
        if (memberDao.count() == 0) {
            val an = memberDao.insert(TeamMemberEntity(name = "Nguyễn Minh An", role = "Tổ trưởng"))
            val linh = memberDao.insert(TeamMemberEntity(name = "Trần Thu Linh"))
            val khang = memberDao.insert(TeamMemberEntity(name = "Lê Quốc Khang"))
            val today = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 9)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
            }.timeInMillis
            database.workOrderDao().insert(
                WorkOrderEntity(
                    title = "Kiểm tra tuyến cáp khu vực A",
                    description = "Đo suy hao và cập nhật hiện trạng tuyến.",
                    memberId = an,
                    scheduledAt = today,
                    priority = "Cao",
                    status = "Đang làm",
                    location = "21.0285, 105.8542",
                    checklist = "Kiểm tra đầu nối\nĐo công suất quang\nChụp ảnh hiện trạng",
                    materials = "Dây nhảy quang x2\nĐầu nối SC/APC x4"
                )
            )
            database.workOrderDao().insert(
                WorkOrderEntity(
                    title = "Bảo trì tủ phối quang",
                    description = "Vệ sinh và kiểm tra nhãn port.",
                    memberId = linh,
                    scheduledAt = today + 86_400_000,
                    priority = "Vừa",
                    location = "Khu vực B",
                    checklist = "Vệ sinh tủ\nĐối chiếu nhãn port",
                    materials = "Khăn lau chuyên dụng"
                )
            )
            database.workOrderDao().insert(
                WorkOrderEntity(
                    title = "Nghiệm thu nhánh thuê bao",
                    memberId = khang,
                    scheduledAt = today - 86_400_000,
                    completedAt = today - 86_000_000,
                    priority = "Thấp",
                    status = "Hoàn tất",
                    location = "21.0301, 105.8508"
                )
            )
        }
    }

    suspend fun add(order: WorkOrderEntity) = database.workOrderDao().insert(order)

    suspend fun update(order: WorkOrderEntity) = database.workOrderDao().update(order)
}
