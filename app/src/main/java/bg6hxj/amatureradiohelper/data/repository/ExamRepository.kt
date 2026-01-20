package bg6hxj.amatureradiohelper.data.repository

import bg6hxj.amatureradiohelper.data.dao.ExamRecordDao
import bg6hxj.amatureradiohelper.data.model.ExamRecord
import kotlinx.coroutines.flow.Flow

/**
 * 考试记录仓库
 */
class ExamRepository(
    private val examRecordDao: ExamRecordDao
) {
    
    /**
     * 保存考试记录
     */
    suspend fun saveExamRecord(record: ExamRecord): Long {
        return examRecordDao.insert(record)
    }
    
    /**
     * 获取指定等级的考试记录
     */
    suspend fun getExamRecordsByLevel(level: String): List<ExamRecord> {
        return examRecordDao.getExamRecordsByLevel(level)
    }
    
    /**
     * 获取指定等级的考试记录（Flow）
     */
    fun getExamRecordsByLevelFlow(level: String): Flow<List<ExamRecord>> {
        return examRecordDao.getExamRecordsByLevelFlow(level)
    }
    
    /**
     * 获取最近的考试记录
     */
    suspend fun getRecentExamRecords(limit: Int = 10): List<ExamRecord> {
        return examRecordDao.getRecentExamRecords(limit)
    }
    
    /**
     * 获取考试统计信息
     */
    suspend fun getExamStats(level: String): ExamStats {
        val totalCount = examRecordDao.getExamCountByLevel(level)
        val passedCount = examRecordDao.getPassedCountByLevel(level)
        val highestScore = examRecordDao.getHighestScoreByLevel(level) ?: 0f
        val averageScore = examRecordDao.getAverageScoreByLevel(level) ?: 0f
        val fastestPassTime = examRecordDao.getFastestPassTimeByLevel(level)
        
        return ExamStats(
            level = level,
            totalCount = totalCount,
            passedCount = passedCount,
            highestScore = highestScore,
            averageScore = averageScore,
            fastestPassTime = fastestPassTime
        )
    }
    
    /**
     * 删除考试记录
     */
    suspend fun deleteExamRecord(record: ExamRecord) {
        examRecordDao.delete(record)
    }
    
    /**
     * 清除指定等级的考试记录
     */
    suspend fun clearExamRecords(level: String) {
        examRecordDao.clearRecordsByLevel(level)
    }
}

/**
 * 考试统计信息
 */
data class ExamStats(
    val level: String,
    val totalCount: Int,
    val passedCount: Int,
    val highestScore: Float,
    val averageScore: Float,
    val fastestPassTime: Long?
) {
    /**
     * 通过率
     */
    val passRate: Float
        get() = if (totalCount > 0) passedCount.toFloat() / totalCount else 0f
}
