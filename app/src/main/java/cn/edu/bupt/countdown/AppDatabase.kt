package cn.edu.bupt.countdown

import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import android.content.Context
import cn.edu.bupt.countdown.bean.EventBean
import cn.edu.bupt.countdown.bean.SingleAppWidgetBean
import cn.edu.bupt.countdown.dao.EventDao
import cn.edu.bupt.countdown.dao.SingleWidgetDao

@Database(
    entities = [EventBean::class, SingleAppWidgetBean::class],
    version = 1, exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    companion object {
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            if (INSTANCE == null) {
                synchronized(AppDatabase::class.java) {
                    if (INSTANCE == null) {
                        INSTANCE = Room.databaseBuilder(
                            context.applicationContext,
                            AppDatabase::class.java, "countdown.db"
                        ).build()
                    }
                }
            }
            return INSTANCE!!
        }
    }

    abstract fun eventDao(): EventDao
    abstract fun singleWidgetDao(): SingleWidgetDao
}