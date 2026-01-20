package bg6hxj.amatureradiohelper.data.dao

import androidx.room.*
import bg6hxj.amatureradiohelper.data.model.StudyRecord
import bg6hxj.amatureradiohelper.data.model.QuestionWithRecord
import kotlinx.coroutines.flow.Flow

/**
 * 学习记录数据访问对象
 */
@Dao
interface StudyRecordDao {
    
    // ==================== 基础操作 ====================
    
    /**
     * 插入学习记录
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: StudyRecord): Long
    
    /**
     * 更新学习记录
     */
    @Update
    suspend fun update(record: StudyRecord)
    
    /**
     * 删除学习记录
     */
    @Delete
    suspend fun delete(record: StudyRecord)
    
    /**
     * 根据题目 ID 删除记录
     */
    @Query("DELETE FROM study_records WHERE questionId = :questionId")
    suspend fun deleteByQuestionId(questionId: Int)
    
    // ==================== 查询 ====================
    
    /**
     * 获取指定题目的学习记录
     */
    @Query("SELECT * FROM study_records WHERE questionId = :questionId")
    suspend fun getRecordByQuestionId(questionId: Int): StudyRecord?
    
    /**
     * 获取指定题目的学习记录（Flow）
     */
    @Query("SELECT * FROM study_records WHERE questionId = :questionId")
    fun getRecordByQuestionIdFlow(questionId: Int): Flow<StudyRecord?>
    
    /**
     * 获取指定等级的所有学习记录
     */
    @Query("SELECT * FROM study_records WHERE level = :level")
    suspend fun getRecordsByLevel(level: String): List<StudyRecord>
    
    /**
     * 获取指定等级的所有学习记录（Flow）
     */
    @Query("SELECT * FROM study_records WHERE level = :level")
    fun getRecordsByLevelFlow(level: String): Flow<List<StudyRecord>>
    
    // ==================== 统计查询 ====================
    
    /**
     * 获取指定等级已学习的题目数
     */
    @Query("SELECT COUNT(*) FROM study_records WHERE level = :level AND isLearned = 1")
    suspend fun getLearnedCount(level: String): Int
    
    /**
     * 获取指定等级已学习的题目数（Flow）
     */
    @Query("SELECT COUNT(*) FROM study_records WHERE level = :level AND isLearned = 1")
    fun getLearnedCountFlow(level: String): Flow<Int>
    
    /**
     * 获取指定等级已掌握的题目数
     */
    @Query("SELECT COUNT(*) FROM study_records WHERE level = :level AND isMastered = 1")
    suspend fun getMasteredCount(level: String): Int
    
    /**
     * 获取指定等级已掌握的题目数（Flow）
     */
    @Query("SELECT COUNT(*) FROM study_records WHERE level = :level AND isMastered = 1")
    fun getMasteredCountFlow(level: String): Flow<Int>
    
    /**
     * 获取指定等级答错过的题目数
     */
    @Query("SELECT COUNT(*) FROM study_records WHERE level = :level AND isWrong = 1")
    suspend fun getWrongCount(level: String): Int
    
    /**
     * 获取指定等级答错过的题目数（Flow）
     */
    @Query("SELECT COUNT(*) FROM study_records WHERE level = :level AND isWrong = 1")
    fun getWrongCountFlow(level: String): Flow<Int>
    
    /**
     * 获取指定等级收藏的题目数
     */
    @Query("SELECT COUNT(*) FROM study_records WHERE level = :level AND isFavorite = 1")
    suspend fun getFavoriteCount(level: String): Int
    
    /**
     * 获取指定等级收藏的题目数（Flow）
     */
    @Query("SELECT COUNT(*) FROM study_records WHERE level = :level AND isFavorite = 1")
    fun getFavoriteCountFlow(level: String): Flow<Int>
    
    // ==================== 筛选查询 ====================
    
    /**
     * 获取已学习的题目记录
     */
    @Query("SELECT * FROM study_records WHERE level = :level AND isLearned = 1 ORDER BY lastStudyTime DESC")
    suspend fun getLearnedRecords(level: String): List<StudyRecord>
    
    /**
     * 获取未学习的题目 ID（通过反查）
     */
    @Query("SELECT id FROM questions WHERE level = :level AND id NOT IN (SELECT questionId FROM study_records WHERE level = :level AND isLearned = 1)")
    suspend fun getUnlearnedQuestionIds(level: String): List<Int>
    
    /**
     * 获取答错过的题目记录
     */
    @Query("SELECT * FROM study_records WHERE level = :level AND isWrong = 1 ORDER BY wrongCount DESC, lastStudyTime DESC")
    suspend fun getWrongRecords(level: String): List<StudyRecord>
    
    /**
     * 获取收藏的题目记录
     */
    @Query("SELECT * FROM study_records WHERE level = :level AND isFavorite = 1 ORDER BY lastStudyTime DESC")
    suspend fun getFavoriteRecords(level: String): List<StudyRecord>
    
    // ==================== 批量操作 ====================
    
    /**
     * 批量插入学习记录
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(records: List<StudyRecord>)
    
    /**
     * 清空指定等级的学习记录
     */
    @Query("DELETE FROM study_records WHERE level = :level")
    suspend fun clearRecordsByLevel(level: String)
    
    /**
     * 清空所有学习记录
     */
    @Query("DELETE FROM study_records")
    suspend fun clearAllRecords()
    
    // ==================== 更新操作 ====================
    
    /**
     * 标记题目为已学习
     */
    @Query("UPDATE study_records SET isLearned = 1, lastStudyTime = :time WHERE questionId = :questionId")
    suspend fun markAsLearned(questionId: Int, time: Long = System.currentTimeMillis())
    
    /**
     * 标记题目为已掌握
     */
    @Query("UPDATE study_records SET isMastered = 1, lastStudyTime = :time WHERE questionId = :questionId")
    suspend fun markAsMastered(questionId: Int, time: Long = System.currentTimeMillis())
    
    /**
     * 切换收藏状态
     */
    @Query("UPDATE study_records SET isFavorite = NOT isFavorite, lastStudyTime = :time WHERE questionId = :questionId")
    suspend fun toggleFavorite(questionId: Int, time: Long = System.currentTimeMillis())
    
    /**
     * 记录答错
     */
    @Query("UPDATE study_records SET isWrong = 1, wrongCount = wrongCount + 1, lastStudyTime = :time WHERE questionId = :questionId")
    suspend fun recordWrong(questionId: Int, time: Long = System.currentTimeMillis())
    
    // ==================== 随机练习相关 ====================
    
    /**
     * 更新随机练习顺序
     */
    @Query("UPDATE study_records SET randomPracticeOrder = :order WHERE questionId = :questionId")
    suspend fun updateRandomPracticeOrder(questionId: Int, order: Int)
    
    /**
     * 标记随机练习题目已完成
     */
    @Query("UPDATE study_records SET randomPracticeDone = 1, lastStudyTime = :time WHERE questionId = :questionId")
    suspend fun markRandomPracticeDone(questionId: Int, time: Long = System.currentTimeMillis())
    
    /**
     * 重置指定等级的随机练习状态（保留顺序,仅清除完成标记）
     */
    @Query("UPDATE study_records SET randomPracticeDone = 0 WHERE level = :level")
    suspend fun resetRandomPracticeDone(level: String)
    
    /**
     * 完全重置随机练习（清除顺序和完成标记）
     */
    @Query("UPDATE study_records SET randomPracticeOrder = -1, randomPracticeDone = 0 WHERE level = :level")
    suspend fun fullResetRandomPractice(level: String)
    
    /**
     * 获取已分配顺序的最大值
     */
    @Query("SELECT MAX(randomPracticeOrder) FROM study_records WHERE level = :level")
    suspend fun getMaxRandomPracticeOrder(level: String): Int?
}
