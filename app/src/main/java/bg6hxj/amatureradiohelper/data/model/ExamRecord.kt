package bg6hxj.amatureradiohelper.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 考试记录实体类
 */
@Entity(tableName = "exam_records")
data class ExamRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    /** 考试等级（A/B/C） */
    val level: String,
    
    /** 考试开始时间 */
    val startTime: Long,
    
    /** 考试结束时间 */
    val endTime: Long,
    
    /** 总题目数 */
    val totalQuestions: Int,
    
    /** 正确题目数 */
    val correctCount: Int,
    
    /** 错误题目数 */
    val wrongCount: Int,
    
    /** 得分 */
    val score: Float,
    
    /** 是否及格 */
    val isPassed: Boolean,
    
    /** 暂停次数 */
    val pauseCount: Int = 0,
    
    /** 题目 ID 列表（JSON 字符串） */
    val questionIds: String,
    
    /** 用户答案列表（JSON 字符串，格式：[{"questionId": 1, "answer": ["A"]}, ...]） */
    val userAnswers: String
) {
    /**
     * 获取考试用时（秒）
     */
    val duration: Long
        get() = (endTime - startTime) / 1000
    
    /**
     * 获取考试用时（分钟）
     */
    val durationMinutes: Int
        get() = (duration / 60).toInt()
    
    /**
     * 获取正确率
     */
    val accuracy: Float
        get() = if (totalQuestions > 0) correctCount.toFloat() / totalQuestions else 0f
}

/**
 * 模拟考试配置
 */
data class ExamConfig(
    val level: String,
    val questionCount: Int,
    val passCount: Int,
    val timeLimitMinutes: Int
) {
    companion object {
        /**
         * 获取指定等级的考试配置
         */
        fun getConfig(level: String): ExamConfig {
            return when (level) {
                "A" -> ExamConfig(
                    level = "A",
                    questionCount = 40,      // 32单选 + 8多选
                    passCount = 30,          // 30分合格
                    timeLimitMinutes = 40    // 40分钟
                )
                "B" -> ExamConfig(
                    level = "B",
                    questionCount = 60,      // 45单选 + 15多选
                    passCount = 45,          // 45分合格
                    timeLimitMinutes = 60    // 60分钟
                )
                "C" -> ExamConfig(
                    level = "C",
                    questionCount = 90,      // 70单选 + 20多选
                    passCount = 70,          // 70分合格
                    timeLimitMinutes = 90    // 90分钟
                )
                else -> ExamConfig(
                    level = "A",
                    questionCount = 40,
                    passCount = 30,
                    timeLimitMinutes = 40
                )
            }
        }
    }
    
    /**
     * 及格分数
     */
    val passScore: Float
        get() = (passCount.toFloat() / questionCount) * 100
}
