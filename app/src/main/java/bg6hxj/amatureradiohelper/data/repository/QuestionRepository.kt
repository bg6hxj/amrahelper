package bg6hxj.amatureradiohelper.data.repository

import bg6hxj.amatureradiohelper.data.dao.QuestionDao
import bg6hxj.amatureradiohelper.data.dao.StudyRecordDao
import bg6hxj.amatureradiohelper.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/**
 * 题库仓库
 * 封装题目和学习记录的数据访问逻辑
 */
class QuestionRepository(
    private val questionDao: QuestionDao,
    private val studyRecordDao: StudyRecordDao
) {
    
    // ==================== 题目查询 ====================
    
    /**
     * 获取指定等级的所有题目
     */
    suspend fun getQuestionsByLevel(level: String): List<Question> {
        return questionDao.getQuestionsByLevel(level)
    }
    
    /**
     * 获取指定等级的题目总数
     */
    suspend fun getQuestionCountByLevel(level: String): Int {
        return questionDao.getQuestionCountByLevel(level)
    }
    
    /**
     * 根据 ID 获取题目
     */
    suspend fun getQuestionById(questionId: Int): Question? {
        return questionDao.getQuestionById(questionId)
    }
    
    /**
     * 随机获取指定等级的题目
     */
    suspend fun getRandomQuestions(level: String, count: Int): List<Question> {
        return questionDao.getRandomQuestions(level, count)
    }
    
    /**
     * 搜索题目
     */
    suspend fun searchQuestions(level: String, keyword: String): List<Question> {
        return questionDao.searchQuestions(level, keyword)
    }
    
    // ==================== 题目与学习记录联合查询 ====================
    
    /**
     * 获取题目及其学习记录
     */
    suspend fun getQuestionWithRecord(questionId: Int): QuestionWithRecord? {
        val question = questionDao.getQuestionById(questionId) ?: return null
        val record = studyRecordDao.getRecordByQuestionId(questionId)
        return QuestionWithRecord(question, record)
    }
    
    /**
     * 获取指定等级的所有题目及学习记录
     */
    suspend fun getQuestionsWithRecords(level: String): List<QuestionWithRecord> {
        val questions = questionDao.getQuestionsByLevel(level)
        val records = studyRecordDao.getRecordsByLevel(level)
        val recordMap = records.associateBy { it.questionId }
        
        return questions.map { question ->
            QuestionWithRecord(question, recordMap[question.id!!])
        }
    }
    
    /**
     * 根据筛选条件获取题目
     */
    suspend fun getQuestionsByFilter(level: String, filter: QuestionFilter): List<QuestionWithRecord> {
        val allQuestions = getQuestionsWithRecords(level)
        
        return when (filter) {
            QuestionFilter.ALL -> allQuestions
            QuestionFilter.LEARNED -> allQuestions.filter { it.isLearned }
            QuestionFilter.UNLEARNED -> allQuestions.filter { !it.isLearned }
            QuestionFilter.WRONG -> allQuestions.filter { it.isWrong }
            QuestionFilter.FAVORITE -> allQuestions.filter { it.isFavorite }
        }
    }
    
    /**
     * 获取未学习的题目（用于顺序学习）
     */
    suspend fun getUnlearnedQuestions(level: String): List<Question> {
        val unlearnedIds = studyRecordDao.getUnlearnedQuestionIds(level)
        return unlearnedIds.mapNotNull { questionDao.getQuestionById(it) }
    }
    
    // ==================== 学习记录操作 ====================
    
    /**
     * 获取或创建学习记录
     */
    suspend fun getOrCreateStudyRecord(questionId: Int, level: String): StudyRecord {
        // 先尝试获取现有记录
        studyRecordDao.getRecordByQuestionId(questionId)?.let { return it }
        
        // 如果不存在，创建新记录并插入
        val newRecord = StudyRecord(
            questionId = questionId,
            level = level
        )
        studyRecordDao.insert(newRecord)
        
        // 重新从数据库读取以获取正确的ID（Room自动生成的ID）
        return studyRecordDao.getRecordByQuestionId(questionId) ?: newRecord
    }
    
    /**
     * 标记题目为已学习
     */
    suspend fun markQuestionAsLearned(questionId: Int, level: String) {
        val record = getOrCreateStudyRecord(questionId, level)
        studyRecordDao.update(
            record.copy(
                isLearned = true,
                lastStudyTime = System.currentTimeMillis(),
                level = level
            )
        )
    }
    
    /**
     * 切换题目关注状态
     */
    suspend fun markQuestionAsMastered(questionId: Int, level: String) {
        val record = getOrCreateStudyRecord(questionId, level)
        studyRecordDao.update(
            record.copy(
                isLearned = true,
                isMastered = !record.isMastered, // 切换关注状态
                lastStudyTime = System.currentTimeMillis(),
                level = level
            )
        )
    }
    
    /**
     * 记录答题结果
     */
    suspend fun recordAnswer(questionId: Int, level: String, isCorrect: Boolean) {
        val record = getOrCreateStudyRecord(questionId, level)
        
        if (isCorrect) {
            studyRecordDao.update(
                record.copy(
                    isLearned = true,
                    lastStudyTime = System.currentTimeMillis(),
                    level = level
                )
            )
        } else {
            studyRecordDao.update(
                record.copy(
                    isLearned = true,
                    isWrong = true,
                    wrongCount = record.wrongCount + 1,
                    lastStudyTime = System.currentTimeMillis(),
                    level = level
                )
            )
        }
    }
    
    /**
     * 切换收藏状态
     */
    suspend fun toggleFavorite(questionId: Int, level: String) {
        val record = getOrCreateStudyRecord(questionId, level)
        studyRecordDao.update(
            record.copy(
                isFavorite = !record.isFavorite,
                lastStudyTime = System.currentTimeMillis(),
                level = level
            )
        )
    }
    
    // ==================== 随机练习相关 ====================
    
    /**
     * 更新随机练习顺序
     */
    suspend fun updateRandomPracticeOrder(questionId: Int, order: Int) {
        studyRecordDao.updateRandomPracticeOrder(questionId, order)
    }
    
    /**
     * 标记随机练习题目已完成
     */
    suspend fun markRandomPracticeDone(questionId: Int, level: String) {
        val record = getOrCreateStudyRecord(questionId, level)
        studyRecordDao.update(
            record.copy(
                randomPracticeDone = true,
                lastStudyTime = System.currentTimeMillis(),
                level = level
            )
        )
    }
    
    /**
     * 重置随机练习状态(保留顺序)
     */
    suspend fun resetRandomPracticeDone(level: String) {
        studyRecordDao.resetRandomPracticeDone(level)
    }
    
    /**
     * 完全重置随机练习(清除顺序和完成标记)
     */
    suspend fun fullResetRandomPractice(level: String) {
        studyRecordDao.fullResetRandomPractice(level)
    }
    
    // ==================== 统计信息 ====================
    
    /**
     * 获取题库统计信息
     */
    suspend fun getQuestionBankStats(level: String): QuestionBankStats {
        val totalCount = questionDao.getQuestionCountByLevel(level)
        val learnedCount = studyRecordDao.getLearnedCount(level)
        val masteredCount = studyRecordDao.getMasteredCount(level)
        val wrongCount = studyRecordDao.getWrongCount(level)
        val favoriteCount = studyRecordDao.getFavoriteCount(level)
        
        return QuestionBankStats(
            level = level,
            totalCount = totalCount,
            learnedCount = learnedCount,
            masteredCount = masteredCount,
            wrongCount = wrongCount,
            favoriteCount = favoriteCount
        )
    }
    
    /**
     * 获取所有题库统计信息（Flow）
     */
    fun getQuestionBankStatsFlow(level: String): Flow<QuestionBankStats> {
        return combine(
            questionDao.getQuestionCountByLevelFlow(level),
            studyRecordDao.getLearnedCountFlow(level),
            studyRecordDao.getMasteredCountFlow(level),
            studyRecordDao.getWrongCountFlow(level),
            studyRecordDao.getFavoriteCountFlow(level)
        ) { totalCount, learnedCount, masteredCount, wrongCount, favoriteCount ->
            QuestionBankStats(
                level = level,
                totalCount = totalCount,
                learnedCount = learnedCount,
                masteredCount = masteredCount,
                wrongCount = wrongCount,
                favoriteCount = favoriteCount
            )
        }
    }
    
    // ==================== 清除数据 ====================
    
    /**
     * 清除指定等级的学习记录
     */
    suspend fun clearStudyRecords(level: String) {
        studyRecordDao.clearRecordsByLevel(level)
    }
    
    /**
     * 清除所有学习记录
     */
    suspend fun clearAllStudyRecords() {
        studyRecordDao.clearAllRecords()
    }
}
