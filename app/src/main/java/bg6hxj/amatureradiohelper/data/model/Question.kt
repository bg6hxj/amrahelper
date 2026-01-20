package bg6hxj.amatureradiohelper.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.jsonPrimitive

/**
 * 题目实体类
 * 对应数据库 questions 表
 */
@Entity(tableName = "questions")
data class Question(
    @PrimaryKey(autoGenerate = true)
    val id: Int? = null,
    
    /** 题目编号（如：LY0001） */
    val j_code: String,
    
    /** 章节编号（如：1.1.1） */
    val p_code: String?,
    
    /** 题库内部编号（如：MC2-0001） */
    val i_code: String,
    
    /** 题目内容 */
    val question: String,
    
    /** 正确答案（如：A、AC、ABC 等） */
    val correct_answer: String,
    
    /** 选项 JSON 字符串（如：[["A", "选项A"], ["B", "选项B"]]） */
    val options: String,
    
    /** 题目类型（single: 单选, multiple: 多选） */
    val question_type: String,
    
    /** 难度等级（A: 初级, B: 中级, C: 高级） */
    val level: String
) {
    /**
     * 解析选项 JSON 字符串为列表
     * @return List<Pair<String, String>> 选项列表，如 [Pair("A", "选项内容"), ...]
     */
    fun parseOptions(): List<Pair<String, String>> {
        return try {
            val json = Json { ignoreUnknownKeys = true }
            val jsonElement = json.parseToJsonElement(options)
            
            if (jsonElement is JsonArray) {
                jsonElement.mapNotNull { element ->
                    if (element is JsonArray && element.size >= 2) {
                        val key = element[0].jsonPrimitive.content
                        val value = element[1].jsonPrimitive.content
                        Pair(key, value)
                    } else {
                        null
                    }
                }
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
    
    /**
     * 判断是否为单选题
     */
    fun isSingleChoice(): Boolean = question_type == "single"
    
    /**
     * 判断是否为多选题
     */
    fun isMultipleChoice(): Boolean = question_type == "multiple"
    
    /**
     * 获取正确答案列表
     * @return List<String> 如 ["A"] 或 ["A", "C"]
     */
    fun getCorrectAnswers(): List<String> {
        return correct_answer.trim()  // 先去除首尾空格
            .toCharArray()
            .map { it.toString() }
            .filter { it.isNotBlank() }  // 过滤掉空白字符
    }
    
    /**
     * 检查用户答案是否正确
     * @param userAnswers 用户选择的答案列表
     * @return Boolean 是否正确
     */
    fun checkAnswer(userAnswers: List<String>): Boolean {
        val correctList = getCorrectAnswers().sorted()
        val userList = userAnswers.sorted()
        return correctList == userList
    }
    
    /**
     * 获取题目等级中文名称
     */
    fun getLevelName(): String {
        return when (level) {
            "A" -> "A类（初级）"
            "B" -> "B类（中级）"
            "C" -> "C类（高级）"
            else -> "未知"
        }
    }
}

/**
 * 题库统计信息
 */
data class QuestionBankStats(
    val level: String,
    val totalCount: Int,
    val learnedCount: Int = 0,
    val masteredCount: Int = 0,
    val wrongCount: Int = 0,
    val favoriteCount: Int = 0
) {
    /**
     * 获取未学习题目数量
     */
    val unlearnedCount: Int
        get() = totalCount - learnedCount
    
    /**
     * 获取学习进度百分比
     */
    val learnProgress: Float
        get() = if (totalCount > 0) learnedCount.toFloat() / totalCount else 0f
    
    /**
     * 获取掌握进度百分比
     */
    val masteryProgress: Float
        get() = if (totalCount > 0) masteredCount.toFloat() / totalCount else 0f
}
