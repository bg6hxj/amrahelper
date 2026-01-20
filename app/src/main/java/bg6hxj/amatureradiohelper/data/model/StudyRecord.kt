package bg6hxj.amatureradiohelper.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 学习记录实体类
 * 记录用户对每道题的学习状态
 */
@Entity(tableName = "study_records")
data class StudyRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    /** 题目 ID（对应 Question.id） */
    val questionId: Int,
    
    /** 是否已学习 */
    val isLearned: Boolean = false,
    
    /** 是否已掌握 */
    val isMastered: Boolean = false,
    
    /** 是否答错过 */
    val isWrong: Boolean = false,
    
    /** 是否已收藏 */
    val isFavorite: Boolean = false,
    
    /** 答错次数 */
    val wrongCount: Int = 0,
    
    /** 最后学习时间（时间戳） */
    val lastStudyTime: Long = System.currentTimeMillis(),
    
    /** 题目等级（A/B/C，用于快速筛选） */
    val level: String = "",
    
    /** 随机练习模式下的顺序索引（-1表示未分配） */
    val randomPracticeOrder: Int = -1,
    
    /** 随机练习模式下是否已做过此题 */
    val randomPracticeDone: Boolean = false
)

/**
 * 题目与学习记录的联合查询结果
 */
data class QuestionWithRecord(
    val question: Question,
    val record: StudyRecord?
) {
    /**
     * 是否已学习
     */
    val isLearned: Boolean
        get() = record?.isLearned == true
    
    /**
     * 是否已掌握
     */
    val isMastered: Boolean
        get() = record?.isMastered == true
    
    /**
     * 是否答错过
     */
    val isWrong: Boolean
        get() = record?.isWrong == true
    
    /**
     * 是否已收藏
     */
    val isFavorite: Boolean
        get() = record?.isFavorite == true
    
    /**
     * 答错次数
     */
    val wrongCount: Int
        get() = record?.wrongCount ?: 0
    
    /**
     * 最后学习时间
     */
    val lastStudyTime: Long?
        get() = record?.lastStudyTime
}

/**
 * 学习模式枚举
 */
enum class StudyMode {
    /** 顺序背题 - 展示题目和答案，不需要作答 */
    SEQUENTIAL_REVIEW,
    
    /** 顺序练习 - 需要作答，记录对错 */
    SEQUENTIAL_PRACTICE,
    
    /** 随机练习 - 打乱顺序练习 */
    RANDOM_PRACTICE,
    
    /** 模拟考试 - 随机抽题，计时考试 */
    MOCK_EXAM
}

/**
 * 题目筛选条件
 */
enum class QuestionFilter {
    /** 全部题目 */
    ALL,
    
    /** 已学习 */
    LEARNED,
    
    /** 未学习 */
    UNLEARNED,
    
    /** 答错过的 */
    WRONG,
    
    /** 已收藏 */
    FAVORITE
}
