package vn.personal.qlcongviec.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [TeamMemberEntity::class, WorkOrderEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun workOrderDao(): WorkOrderDao
    abstract fun teamMemberDao(): TeamMemberDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun get(context: Context): AppDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "ql-cong-viec.db"
            ).build().also { instance = it }
        }
    }
}
