package bg6hxj.amatureradiohelper.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import bg6hxj.amatureradiohelper.data.dao.QuestionDao
import bg6hxj.amatureradiohelper.data.dao.StudyRecordDao
import bg6hxj.amatureradiohelper.data.dao.ExamRecordDao
import bg6hxj.amatureradiohelper.data.dao.ContactLogDao
import bg6hxj.amatureradiohelper.data.model.Question
import bg6hxj.amatureradiohelper.data.model.StudyRecord
import bg6hxj.amatureradiohelper.data.model.ExamRecord
import bg6hxj.amatureradiohelper.data.entity.ContactLog
import bg6hxj.amatureradiohelper.R

/**
 * 应用数据库
 * 包含题库数据和学习记录数据
 */
@Database(
    entities = [
        Question::class,
        StudyRecord::class,
        ExamRecord::class,
        ContactLog::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun questionDao(): QuestionDao
    abstract fun studyRecordDao(): StudyRecordDao
    abstract fun examRecordDao(): ExamRecordDao
    abstract fun contactLogDao(): ContactLogDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        private const val DATABASE_NAME = "questions.db"
        
        /**
         * 数据库迁移: 版本 1 -> 2
         * 添加随机练习相关字段
         */
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // 添加 randomPracticeOrder 字段
                database.execSQL(
                    "ALTER TABLE study_records ADD COLUMN randomPracticeOrder INTEGER NOT NULL DEFAULT -1"
                )
                // 添加 randomPracticeDone 字段
                database.execSQL(
                    "ALTER TABLE study_records ADD COLUMN randomPracticeDone INTEGER NOT NULL DEFAULT 0"
                )
            }
        }

        /**
         * 数据库迁移: 版本 2 -> 3
         * 添加通联日志表
         */
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `contact_logs` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                        `contactTime` INTEGER NOT NULL, 
                        `mode` TEXT NOT NULL, 
                        `frequency` TEXT NOT NULL, 
                        `cqZone` TEXT, 
                        `myCallsign` TEXT NOT NULL, 
                        `theirCallsign` TEXT NOT NULL, 
                        `rstSent` TEXT NOT NULL, 
                        `rstReceived` TEXT NOT NULL, 
                        `myPower` TEXT, 
                        `theirPower` TEXT, 
                        `theirQth` TEXT, 
                        `equipment` TEXT, 
                        `antenna` TEXT, 
                        `notes` TEXT
                    )
                """.trimIndent())
            }
        }
        
        /**
         * 获取数据库实例（单例模式）
         */
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = buildDatabase(context)
                INSTANCE = instance
                instance
            }
        }
        
        /**
         * 构建数据库
         * 如果数据库不存在，从 assets 复制预置数据库
         */
        private fun buildDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                DATABASE_NAME
            )
                .createFromInputStream {
                    context.resources.openRawResource(R.raw.questions)
                }
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3) // 添加迁移策略
                .fallbackToDestructiveMigration() // 版本升级时允许破坏性迁移
                .build()
        }
        
        /**
         * 关闭数据库连接
         */
        fun closeDatabase() {
            INSTANCE?.close()
            INSTANCE = null
        }
    }
}
