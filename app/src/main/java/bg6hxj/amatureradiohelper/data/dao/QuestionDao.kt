package bg6hxj.amatureradiohelper.data.dao

import androidx.room.*
import bg6hxj.amatureradiohelper.data.model.Question
import bg6hxj.amatureradiohelper.data.model.QuestionBankStats
import kotlinx.coroutines.flow.Flow

/**
 * 题目数据访问对象
 */
@Dao
interface QuestionDao {
    
    // ==================== 基础查询 ====================
    
    /**
     * 获取所有题目
     */
    @Query("SELECT * FROM questions")
    suspend fun getAllQuestions(): List<Question>
    
    /**
     * 根据 ID 获取题目
     */
    @Query("SELECT * FROM questions WHERE id = :questionId")
    suspend fun getQuestionById(questionId: Int): Question?
    
    /**
     * 根据题目编号获取题目
     */
    @Query("SELECT * FROM questions WHERE j_code = :jCode")
    suspend fun getQuestionByJCode(jCode: String): Question?
    
    // ==================== 按等级查询 ====================
    
    /**
     * 获取指定等级的所有题目
     * @param level A/B/C
     */
    @Query("SELECT * FROM questions WHERE level = :level ORDER BY id ASC")
    suspend fun getQuestionsByLevel(level: String): List<Question>
    
    /**
     * 获取指定等级的所有题目（Flow）
     */
    @Query("SELECT * FROM questions WHERE level = :level ORDER BY id ASC")
    fun getQuestionsByLevelFlow(level: String): Flow<List<Question>>
    
    /**
     * 获取指定等级的题目总数
     */
    @Query("SELECT COUNT(*) FROM questions WHERE level = :level")
    suspend fun getQuestionCountByLevel(level: String): Int
    
    /**
     * 获取指定等级的题目总数（Flow）
     */
    @Query("SELECT COUNT(*) FROM questions WHERE level = :level")
    fun getQuestionCountByLevelFlow(level: String): Flow<Int>
    
    // ==================== 按题目类型查询 ====================
    
    /**
     * 获取指定等级的单选题
     */
    @Query("SELECT * FROM questions WHERE level = :level AND question_type = 'single' ORDER BY id ASC")
    suspend fun getSingleChoiceQuestions(level: String): List<Question>
    
    /**
     * 获取指定等级的多选题
     */
    @Query("SELECT * FROM questions WHERE level = :level AND question_type = 'multiple' ORDER BY id ASC")
    suspend fun getMultipleChoiceQuestions(level: String): List<Question>
    
    // ==================== 随机查询 ====================
    
    /**
     * 随机获取指定等级的题目
     * @param level 等级
     * @param count 数量
     */
    @Query("SELECT * FROM questions WHERE level = :level ORDER BY RANDOM() LIMIT :count")
    suspend fun getRandomQuestions(level: String, count: Int): List<Question>
    
    // ==================== 按章节查询 ====================
    
    /**
     * 获取指定等级的所有章节
     */
    @Query("SELECT DISTINCT p_code FROM questions WHERE level = :level AND p_code IS NOT NULL ORDER BY p_code ASC")
    suspend fun getChaptersByLevel(level: String): List<String>
    
    /**
     * 获取指定章节的题目
     */
    @Query("SELECT * FROM questions WHERE level = :level AND p_code = :chapter ORDER BY id ASC")
    suspend fun getQuestionsByChapter(level: String, chapter: String): List<Question>
    
    // ==================== 搜索 ====================
    
    /**
     * 搜索题目（按内容）
     */
    @Query("SELECT * FROM questions WHERE level = :level AND question LIKE '%' || :keyword || '%' ORDER BY id ASC")
    suspend fun searchQuestions(level: String, keyword: String): List<Question>
    
    /**
     * 搜索题目（全库搜索）
     */
    @Query("SELECT * FROM questions WHERE question LIKE '%' || :keyword || '%' ORDER BY level ASC, id ASC")
    suspend fun searchAllQuestions(keyword: String): List<Question>
    
    // ==================== 统计信息 ====================
    
    /**
     * 获取各等级题目统计
     */
    @Query("""
        SELECT level, COUNT(*) as totalCount, 0 as learnedCount, 0 as masteredCount, 0 as wrongCount, 0 as favoriteCount
        FROM questions 
        GROUP BY level 
        ORDER BY level ASC
    """)
    suspend fun getQuestionBankStats(): List<QuestionBankStats>
    
    /**
     * 获取题目总数
     */
    @Query("SELECT COUNT(*) FROM questions")
    suspend fun getTotalQuestionCount(): Int
}
