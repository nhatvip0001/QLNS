package vn.personal.qlcongviec

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.QueryStats
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import vn.personal.qlcongviec.data.TeamMemberEntity
import vn.personal.qlcongviec.data.WorkOrderEntity

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { QLCongViecTheme { WorkApp() } }
    }
}

private enum class AppTab(val label: String, val icon: ImageVector) {
    Overview("Tổng quan", Icons.Outlined.Home),
    Orders("Công việc", Icons.Outlined.Assignment),
    Schedule("Lịch", Icons.Outlined.CalendarMonth),
    Kpi("KPI", Icons.Outlined.QueryStats),
    Map("Bản đồ", Icons.Outlined.Map)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WorkApp(workViewModel: WorkViewModel = viewModel()) {
    val orders by workViewModel.orders.collectAsStateWithLifecycle()
    val members by workViewModel.members.collectAsStateWithLifecycle()
    var selectedTab by rememberSaveable { mutableStateOf(AppTab.Overview) }
    var showNewOrder by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { workViewModel.seedIfNeeded() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(selectedTab.label, fontWeight = FontWeight.SemiBold)
                        Text("QL CÔNG VIỆC", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                AppTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        icon = { androidx.compose.material3.Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label) }
                    )
                }
            }
        },
        floatingActionButton = {
            if (selectedTab == AppTab.Orders) {
                FloatingActionButton(onClick = { showNewOrder = true }) {
                    Text("+", style = MaterialTheme.typography.headlineMedium)
                }
            }
        }
    ) { contentPadding ->
        when (selectedTab) {
            AppTab.Overview -> OverviewScreen(orders, members, Modifier.padding(contentPadding))
            AppTab.Orders -> OrdersScreen(orders, members, workViewModel, Modifier.padding(contentPadding))
            AppTab.Schedule -> ScheduleScreen(orders, members, workViewModel, Modifier.padding(contentPadding))
            AppTab.Kpi -> KpiScreen(orders, members, Modifier.padding(contentPadding))
            AppTab.Map -> MapScreen(orders, members, Modifier.padding(contentPadding))
        }
    }

    if (showNewOrder) {
        NewOrderDialog(
            members = members,
            onDismiss = { showNewOrder = false },
            onSave = { title, description, member, date, priority, location, checklist, materials ->
                workViewModel.addOrder(title, description, member, date, priority, location, checklist, materials)
                showNewOrder = false
            }
        )
    }
}

@Composable
private fun OverviewScreen(
    orders: List<WorkOrderEntity>,
    members: List<TeamMemberEntity>,
    modifier: Modifier = Modifier
) {
    val completed = orders.count { it.status == "Hoàn tất" }
    val active = orders.count { it.status == "Đang làm" }
    val completion = if (orders.isEmpty()) 0 else completed * 100 / orders.size
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Hiệu suất tổ", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text("Tổng hợp từ các lệnh việc lưu trên thiết bị", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard("Tổng WO", orders.size.toString(), Modifier.weight(1f))
                StatCard("Đang làm", active.toString(), Modifier.weight(1f))
                StatCard("Hoàn tất", "$completion%", Modifier.weight(1f))
            }
        }
        item {
            Text("Việc sắp tới", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        }
        if (orders.isEmpty()) {
            item { EmptyState("Chưa có lệnh việc", "Tạo WO đầu tiên trong mục Công việc.") }
        } else {
            items(orders.sortedBy { it.scheduledAt }.take(4), key = { it.id }) { order ->
                WorkOrderCard(order, members, onAdvance = {})
            }
        }
        item {
            Text("Nhân sự: ${members.size} người", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun OrdersScreen(
    orders: List<WorkOrderEntity>,
    members: List<TeamMemberEntity>,
    workViewModel: WorkViewModel,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Lệnh công việc", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text("${orders.size} WO · Chạm nút trạng thái để cập nhật tiến độ", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (orders.isEmpty()) {
            item { EmptyState("Danh sách đang trống", "Tạo WO mới bằng nút +.") }
        } else {
            items(orders, key = { it.id }) { order ->
                WorkOrderCard(order, members, onAdvance = { workViewModel.advance(order) })
            }
        }
    }
}

@Composable
private fun ScheduleScreen(
    orders: List<WorkOrderEntity>,
    members: List<TeamMemberEntity>,
    workViewModel: WorkViewModel,
    modifier: Modifier = Modifier
) {
    val dateFormat = remember { SimpleDateFormat("EEEE, dd/MM", Locale("vi", "VN")) }
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item { Text("Lịch công việc", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }
        if (orders.isEmpty()) {
            item { EmptyState("Chưa có lịch", "Các WO sẽ xuất hiện theo ngày thực hiện.") }
        } else {
            items(orders.sortedBy { it.scheduledAt }, key = { it.id }) { order ->
                Text(dateFormat.format(Date(order.scheduledAt)).replaceFirstChar { it.titlecase(Locale("vi", "VN")) }, fontWeight = FontWeight.SemiBold)
                WorkOrderCard(order, members, onAdvance = { workViewModel.advance(order) })
            }
        }
    }
}

@Composable
private fun KpiScreen(orders: List<WorkOrderEntity>, members: List<TeamMemberEntity>, modifier: Modifier = Modifier) {
    val completed = orders.count { it.status == "Hoàn tất" }
    val completionRate = if (orders.isEmpty()) 0 else completed * 100 / orders.size
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("KPI & năng suất", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text("Tính trực tiếp từ số WO và trạng thái hiện tại", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard("Toàn đội", "$completionRate%", Modifier.weight(1f))
                StatCard("Đã xong", "$completed/${orders.size}", Modifier.weight(1f))
            }
        }
        item { Text("Theo nhân sự", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold) }
        if (members.isEmpty()) {
            item { EmptyState("Chưa có thành viên", "Thêm dữ liệu thành viên để xem KPI.") }
        } else {
            items(members, key = { it.id }) { member ->
                val assigned = orders.filter { it.memberId == member.id }
                val done = assigned.count { it.status == "Hoàn tất" }
                val rate = if (assigned.isEmpty()) 0 else done * 100 / assigned.size
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(member.name, modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
                            Text("$rate%", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        }
                        Text("${member.role} · $done/${assigned.size} WO hoàn tất", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${assigned.count { it.status == "Đang làm" }} đang làm · ${assigned.count { it.status == "Mới" }} mới")
                    }
                }
            }
        }
    }
}

@Composable
private fun MapScreen(orders: List<WorkOrderEntity>, members: List<TeamMemberEntity>, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Địa điểm WO", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text("Vị trí được mở bằng ứng dụng bản đồ trên máy", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        val mappedOrders = orders.filter { it.location.isNotBlank() }
        if (mappedOrders.isEmpty()) {
            item { EmptyState("Chưa có vị trí", "Thêm tọa độ hoặc địa chỉ vào WO để mở bản đồ.") }
        } else {
            items(mappedOrders, key = { it.id }) { order ->
                val memberName = members.firstOrNull { it.id == order.memberId }?.name ?: "Chưa phân công"
                Card {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(order.title, fontWeight = FontWeight.SemiBold)
                        Text("$memberName · ${order.location}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        OutlinedButton(onClick = {
                            val uri = Uri.parse("geo:0,0?q=${Uri.encode(order.location)}")
                            runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, uri)) }
                        }) { Text("Mở bản đồ") }
                    }
                }
            }
        }
    }
}

@Composable
private fun WorkOrderCard(order: WorkOrderEntity, members: List<TeamMemberEntity>, onAdvance: () -> Unit) {
    val memberName = members.firstOrNull { it.id == order.memberId }?.name ?: "Chưa phân công"
    val date = remember(order.scheduledAt) { SimpleDateFormat("dd/MM/yyyy", Locale("vi", "VN")).format(Date(order.scheduledAt)) }
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(order.title, modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                StatusLabel(order.status)
            }
            Text("$memberName · $date · Ưu tiên ${order.priority.lowercase(Locale("vi", "VN"))}", color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (order.description.isNotBlank()) Text(order.description)
            if (order.checklist.isNotBlank()) Text("Checklist: ${order.checklist.lines().joinToString(" · ")}", style = MaterialTheme.typography.bodySmall)
            if (order.materials.isNotBlank()) Text("Vật tư: ${order.materials.lines().joinToString(" · ")}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (order.status != "Hoàn tất") {
                OutlinedButton(onClick = onAdvance) { Text(if (order.status == "Mới") "Bắt đầu" else "Đánh dấu hoàn tất") }
            }
        }
    }
}

@Composable
private fun StatusLabel(status: String) {
    val color = when (status) {
        "Hoàn tất" -> Color(0xFF2E7654)
        "Đang làm" -> Color(0xFFB66B20)
        else -> MaterialTheme.colorScheme.secondary
    }
    Surface(color = color.copy(alpha = 0.12f), shape = MaterialTheme.shapes.small) {
        Text(status, modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp), color = color, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
        }
    }
}

@Composable
private fun EmptyState(title: String, detail: String) {
    Column(Modifier.fillMaxWidth().padding(vertical = 28.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(title, fontWeight = FontWeight.SemiBold)
        Text(detail, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun NewOrderDialog(
    members: List<TeamMemberEntity>,
    onDismiss: () -> Unit,
    onSave: (String, String, TeamMemberEntity, Long, String, String, String, String) -> Unit
) {
    var title by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var dateText by rememberSaveable { mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())) }
    var location by rememberSaveable { mutableStateOf("") }
    var checklist by rememberSaveable { mutableStateOf("") }
    var materials by rememberSaveable { mutableStateOf("") }
    var priority by rememberSaveable { mutableStateOf("Vừa") }
    var memberId by remember(members) { mutableLongStateOf(members.firstOrNull()?.id ?: 0L) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tạo lệnh công việc") },
        text = {
            Column(
                Modifier.heightIn(max = 540.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(title, { title = it }, label = { Text("Tên công việc *") }, singleLine = true)
                OutlinedTextField(description, { description = it }, label = { Text("Mô tả") }, minLines = 2)
                Text("Người phụ trách", style = MaterialTheme.typography.labelLarge)
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    members.forEach { member ->
                        FilterChip(
                            selected = member.id == memberId,
                            onClick = { memberId = member.id },
                            label = { Text(member.name) }
                        )
                    }
                }
                OutlinedTextField(dateText, { dateText = it }, label = { Text("Ngày thực hiện (yyyy-MM-dd)") }, singleLine = true)
                Text("Mức ưu tiên", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("Thấp", "Vừa", "Cao").forEach { value ->
                        FilterChip(selected = priority == value, onClick = { priority = value }, label = { Text(value) })
                    }
                }
                OutlinedTextField(location, { location = it }, label = { Text("Địa chỉ hoặc tọa độ") }, singleLine = true)
                OutlinedTextField(checklist, { checklist = it }, label = { Text("Checklist (mỗi dòng một mục)") }, minLines = 2)
                OutlinedTextField(materials, { materials = it }, label = { Text("Vật tư (mỗi dòng một mục)") }, minLines = 2)
            }
        },
        confirmButton = {
            TextButton(
                enabled = title.isNotBlank() && members.any { it.id == memberId },
                onClick = {
                    val selectedMember = members.first { it.id == memberId }
                    val scheduledAt = runCatching {
                        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateText)?.time
                    }.getOrNull() ?: System.currentTimeMillis()
                    onSave(title, description, selectedMember, scheduledAt, priority, location, checklist, materials)
                }
            ) { Text("Lưu WO") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Hủy") } }
    )
}

@Composable
private fun QLCongViecTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = androidx.compose.material3.lightColorScheme(
            primary = Color(0xFF17614D),
            onPrimary = Color.White,
            secondary = Color(0xFFB85C3B),
            background = Color(0xFFF3F5F1),
            surface = Color(0xFFFCFDFB),
            primaryContainer = Color(0xFFDDEDE4),
            onPrimaryContainer = Color(0xFF123D31)
        ),
        content = content
    )
}
