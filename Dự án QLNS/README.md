# QL Công việc

Ứng dụng Android cá nhân để theo dõi lệnh công việc theo thành viên, tiến độ và năng suất. Dữ liệu được lưu cục bộ bằng Room; bản MVP không cần tài khoản, máy chủ hay API key.

## MVP hiện có

- Tổng quan số WO, trạng thái đang làm và tỷ lệ hoàn tất.
- Tạo WO với người phụ trách, ngày, ưu tiên, địa chỉ/tọa độ, checklist và vật tư.
- Cập nhật tiến độ qua các trạng thái Mới → Đang làm → Hoàn tất.
- Lịch WO, KPI toàn đội và số liệu theo từng thành viên.
- Mở địa chỉ/tọa độ trong ứng dụng bản đồ đã cài trên thiết bị.
- Một số dữ liệu mẫu được thêm tự động khi cơ sở dữ liệu mới khởi chạy lần đầu.

## Yêu cầu

- Android Studio phiên bản ổn định có hỗ trợ Android Gradle Plugin 8.7.3.
- JDK 17.
- Android SDK Platform 35; thiết bị/emulator Android 8.0 (API 26) trở lên.
- Gradle Wrapper 8.9 đã có trong dự án. Android Studio dùng wrapper khi đồng bộ; chọn Gradle JDK 17.

## Build và chạy

1. Mở thư mục workspace hiện tại trong Android Studio (thư mục chứa `settings.gradle.kts`).
2. Chờ Gradle đồng bộ và tải các dependency.
3. Chọn thiết bị Android API 26 trở lên hoặc tạo emulator.
4. Chạy cấu hình `app`.

Build debug từ terminal sau khi JDK 17 và Android SDK Platform 35 được cài đặt:

```powershell
.\gradlew.bat :app:assembleDebug
```

APK debug nằm tại `app/build/outputs/apk/debug/app-debug.apk`.

## Ghi chú dữ liệu

Cơ sở dữ liệu `ql-cong-viec.db` chỉ nằm trên thiết bị. Gỡ ứng dụng sẽ xóa dữ liệu. KPI hiện tính từ tổng số WO và trạng thái hiện tại, chưa có mục tiêu KPI theo kỳ, xác thực, đồng bộ nhiều thiết bị, đính kèm ảnh hoặc định tuyến bản đồ bên trong ứng dụng.
