package bg6hxj.amatureradiohelper.data.dao

import androidx.room.*
import bg6hxj.amatureradiohelper.data.model.ExamRecord
import kotlinx.coroutines.flow.Flow

/**
 * 考试记录数据访问对象
 */
@Dao
interface ExamRecordDao {
    
    // ==================== 基础操作 ====================
    
    /**
     * 插入考试记录
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: ExamRecord): Long
    
    /**
     * 更新考试记录
     */
    @Update
    suspend fun update(record: ExamRecord)
    
    /**
     * 删除考试记录
     */
    @Delete
    suspend fun delete(record: ExamRecord)
    
    /**
     * 根据 ID 删除记录
     */
    @Query("DELETE FROM exam_records WHERE id = :id")
    suspend fun deleteById(id: Long)
    
    // ==================== 查询 ====================
    
    /**
     * 根据 ID 获取考试记录
     */
    @Query("SELECT * FROM exam_records WHERE id = :id")
    suspend fun getExamRecordById(id: Long): ExamRecord?
    
    /**
     * 获取所有考试记录
     */
    @Query("SELECT * FROM exam_records ORDER BY startTime DESC")
    suspend fun getAllExamRecords(): List<ExamRecord>
    
    /**
     * 获取所有考试记录（Flow）
     */
    @Query("SELECT * FROM exam_records ORDER BY startTime DESC")
    fun getAllExamRecordsFlow(): Flow<List<ExamRecord>>
    
    /**
     * 获取指定等级的考试记录
     */
    @Query("SELECT * FROM exam_records WHERE level = :level ORDER BY startTime DESC")
    suspend fun getExamRecordsByLevel(level: String): List<ExamRecord>
    
    /**
     * 获取指定等级的考试记录（Flow）
     */
    @Query("SELECT * FROM exam_records WHERE level = :level ORDER BY startTime DESC")
    fun getExamRecordsByLevelFlow(level: String): Flow<List<ExamRecord>>
    
    /**
     * 获取最近 N 条考试记录
     */
    @Query("SELECT * FROM exam_records ORDER BY startTime DESC LIMIT :limit")
    suspend fun getRecentExamRecords(limit: Int = 10): List<ExamRecord>
    
    // ==================== 统计查询 ====================
    
    /**
     * 获取指定等级的考试总次数
     */
    @Query("SELECT COUNT(*) FROM exam_records WHERE level = :level")
    suspend fun getExamCountByLevel(level: String): Int
    
    /**
     * 获取指定等级的考试总次数（Flow）
     */
    @Query("SELECT COUNT(*) FROM exam_records WHERE level = :level")
    fun getExamCountByLevelFlow(level: String): Flow<Int>
    
    /**
     * 获取指定等级的及格次数
     */
    @Query("SELECT COUNT(*) FROM exam_records WHERE level = :level AND isPassed = 1")
    suspend fun getPassedCountByLevel(level: String): Int
    
    /**
     * 获取指定等级的最高分
     */
    @Query("SELECT MAX(score) FROM exam_records WHERE level = :level")
    suspend fun getHighestScoreByLevel(level: String): Float?
    
    /**
     * 获取指定等级的平均分
     */
    @Query("SELECT AVG(score) FROM exam_records WHERE level = :level")
    suspend fun getAverageScoreByLevel(level: String): Float?
    
    /**
     * 获取指定等级的最快通过时间（秒）
     */
    @Query("SELECT MIN(endTime - startTime) / 1000 FROM exam_records WHERE level = :level AND isPassed = 1")
    suspend fun getFastestPassTimeByLevel(level: String): Long?
    
    // ==================== 按时间筛选 ====================
    
    /**
     * 获取指定时间范围内的考试记录
     */
    @Query("SELECT * FROM exam_records WHERE startTime BETWEEN :startTime AND :endTime ORDER BY startTime DESC")
    suspend fun getExamRecordsByTimeRange(startTime: Long, endTime: Long): List<ExamRecord>
    
    /**
     * 获取今天的考试记录
     */
    @Query("SELECT * FROM exam_records WHERE startTime >= :todayStart ORDER BY startTime DESC")
    suspend fun getTodayExamRecords(todayStart: Long): List<ExamRecord>
    
    // ==================== 批量操作 ====================
    
    /**
     * 批量插入考试记录
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(records: List<ExamRecord>)
    
    /**
     * 清空指定等级的考试记录
     */
    @Query("DELETE FROM exam_records WHERE level = :level")
    suspend fun clearRecordsByLevel(level: String)
    
    /**
     * 清空所有考试记录
     */
    @Query("DELETE FROM exam_records")
    suspend fun clearAllRecords()
    
    /**
     * 删除指定时间之前的考试记录
     */
    @Query("DELETE FROM exam_records WHERE startTime < :timestamp")
    suspend fun deleteRecordsBeforeTime(timestamp: Long)
}
