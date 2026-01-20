package bg6hxj.amatureradiohelper.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bg6hxj.amatureradiohelper.data.model.*
import bg6hxj.amatureradiohelper.data.repository.QuestionRepository
import bg6hxj.amatureradiohelper.data.repository.ExamRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * 题目答题状态
 */
data class QuestionAnswerState(
    val selectedAnswers: Set<String> = emptySet(),
    val showAnswer: Boolean = false,
    val isCorrect: Boolean? = null
)

/**
 * 考试模块 ViewModel
 */
class ExamViewModel(
    private val questionRepository: QuestionRepository,
    private val examRepository: ExamRepository
) : ViewModel() {
    
    // ==================== 状态管理 ====================
    
    /** 当前选择的等级 */
    private val _selectedLevel = MutableStateFlow("A")
    val selectedLevel: StateFlow<String> = _selectedLevel.asStateFlow()
    
    /** 当前学习模式 */
    private val _studyMode = MutableStateFlow<StudyMode?>(null)
    val studyMode: StateFlow<StudyMode?> = _studyMode.asStateFlow()
    
    /** 题库统计信息 */
    private val _questionBankStats = MutableStateFlow<QuestionBankStats?>(null)
    val questionBankStats: StateFlow<QuestionBankStats?> = _questionBankStats.asStateFlow()
    
    /** 当前题目列表 */
    private val _questions = MutableStateFlow<List<QuestionWithRecord>>(emptyList())
    val questions: StateFlow<List<QuestionWithRecord>> = _questions.asStateFlow()
    
    /** 当前题目索引 */
    private val _currentQuestionIndex = MutableStateFlow(0)
    val currentQuestionIndex: StateFlow<Int> = _currentQuestionIndex.asStateFlow()
    
    /** 当前题目 */
    val currentQuestion: StateFlow<QuestionWithRecord?> = combine(
        _questions,
        _currentQuestionIndex
    ) { questions, index ->
        questions.getOrNull(index)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    
    /** 用户选择的答案 */
    private val _selectedAnswers = MutableStateFlow<Set<String>>(emptySet())
    val selectedAnswers: StateFlow<Set<String>> = _selectedAnswers.asStateFlow()
    
    /** 是否显示答案 */
    private val _showAnswer = MutableStateFlow(false)
    val showAnswer: StateFlow<Boolean> = _showAnswer.asStateFlow()
    
    /** 每道题的答题状态缓存 (题目ID -> 答题状态) */
    private val _answerStateCache = MutableStateFlow<Map<Int, QuestionAnswerState>>(emptyMap())
    
    /** 加载状态 */
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    // ==================== 模拟考试状态 ====================
    
    /** 模拟考试配置 */
    private val _examConfig = MutableStateFlow<ExamConfig?>(null)
    val examConfig: StateFlow<ExamConfig?> = _examConfig.asStateFlow()
    
    /** 模拟考试答案缓存 (题目ID -> 答案列表) */
    private val _examAnswers = MutableStateFlow<Map<Int, List<String>>>(emptyMap())
    
    /** 模拟考试是否已提交 */
    private val _examSubmitted = MutableStateFlow(false)
    val examSubmitted: StateFlow<Boolean> = _examSubmitted.asStateFlow()
    
    /** 模拟考试得分 */
    private val _examScore = MutableStateFlow<ExamScore?>(null)
    val examScore: StateFlow<ExamScore?> = _examScore.asStateFlow()
    
    /** 剩余时间(秒) */
    private val _remainingTimeSeconds = MutableStateFlow(0)
    val remainingTimeSeconds: StateFlow<Int> = _remainingTimeSeconds.asStateFlow()
    
    /** 考试开始时间 */
    private var examStartTime: Long = 0
    
    // ==================== 初始化 ====================
    
    init {
        loadQuestionBankStats()
    }
    
    // ==================== 题库操作 ====================
    
    /**
     * 切换等级
     */
    fun selectLevel(level: String) {
        _selectedLevel.value = level
        loadQuestionBankStats()
    }
    
    /**
     * 加载题库统计信息
     */
    fun loadQuestionBankStats() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val stats = questionRepository.getQuestionBankStats(_selectedLevel.value)
                _questionBankStats.value = stats
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    // ==================== 学习模式 ====================
    
    /**
     * 开始顺序背题
     */
    fun startSequentialReview() {
        _studyMode.value = StudyMode.SEQUENTIAL_REVIEW
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val questions = questionRepository.getQuestionsWithRecords(_selectedLevel.value)
                _questions.value = questions
                _currentQuestionIndex.value = 0
                // 背题模式恢复状态(会默认显示答案,除非有缓存的其他状态)
                restoreQuestionState()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * 开始顺序练习
     */
    fun startSequentialPractice() {
        _studyMode.value = StudyMode.SEQUENTIAL_PRACTICE
        _answerStateCache.value = emptyMap()  // 清空之前的答题缓存
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val questions = questionRepository.getQuestionsWithRecords(_selectedLevel.value)
                _questions.value = questions
                _currentQuestionIndex.value = 0
                // 恢复第一题的答题状态(由于缓存已清空,会重置为初始状态)
                restoreQuestionState()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * 开始随机练习
     * 智能随机逻辑:
     * 1. 已做过的题目保持原顺序
     * 2. 未做过的题目重新随机排列
     */
    fun startRandomPractice() {
        _studyMode.value = StudyMode.RANDOM_PRACTICE
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val allQuestions = questionRepository.getQuestionsWithRecords(_selectedLevel.value)
                
                // 分离已做和未做的题目
                val doneQuestions = mutableListOf<Pair<QuestionWithRecord, Int>>()
                val undoneQuestions = mutableListOf<QuestionWithRecord>()
                
                allQuestions.forEach { qwr ->
                    val record = qwr.record
                    if (record != null && record.randomPracticeDone && record.randomPracticeOrder >= 0) {
                        // 已做过且有顺序索引,保持原顺序
                        doneQuestions.add(qwr to record.randomPracticeOrder)
                    } else {
                        // 未做过,需要重新随机
                        undoneQuestions.add(qwr)
                    }
                }
                
                // 已做题目按原顺序排序
                doneQuestions.sortBy { it.second }
                val sortedDoneQuestions = doneQuestions.map { it.first }
                
                // 未做题目随机打乱
                val shuffledUndoneQuestions = undoneQuestions.shuffled()
                
                // 为未做题目分配新的顺序索引
                val maxOrder = doneQuestions.maxOfOrNull { it.second } ?: -1
                shuffledUndoneQuestions.forEachIndexed { index, qwr ->
                    val newOrder = maxOrder + 1 + index
                    // 更新数据库中的顺序索引
                    val recordId = qwr.record?.questionId ?: (qwr.question.id ?: 0)
                    questionRepository.updateRandomPracticeOrder(recordId, newOrder)
                }
                
                // 合并: 已做题目在前,未做题目在后
                _questions.value = sortedDoneQuestions + shuffledUndoneQuestions
                _currentQuestionIndex.value = 0
                // 恢复第一题的答题状态
                restoreQuestionState()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * 开始模拟考试
     */
    fun startMockExam() {
        _studyMode.value = StudyMode.MOCK_EXAM
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val config = ExamConfig.getConfig(_selectedLevel.value)
                _examConfig.value = config
                
                val allQuestions = questionRepository.getQuestionsByLevel(_selectedLevel.value)
                val examQuestions = allQuestions.shuffled().take(config.questionCount)
                
                // 转换为 QuestionWithRecord
                _questions.value = examQuestions.map { question ->
                    val record = questionRepository.getOrCreateStudyRecord(question.id!!, _selectedLevel.value)
                    QuestionWithRecord(question, record)
                }
                
                // 初始化考试状态
                _currentQuestionIndex.value = 0
                _examAnswers.value = emptyMap()
                _examSubmitted.value = false
                _examScore.value = null
                _remainingTimeSeconds.value = config.timeLimitMinutes * 60
                examStartTime = System.currentTimeMillis()
                
                // 清空选中答案
                _selectedAnswers.value = emptySet()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * 加载筛选后的题目
     */
    fun loadQuestionsByFilter(filter: QuestionFilter) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val questions = questionRepository.getQuestionsByFilter(_selectedLevel.value, filter)
                _questions.value = questions
                _currentQuestionIndex.value = 0
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * 加载已学题目
     */
    fun loadLearnedQuestions() {
        loadQuestionsByFilter(QuestionFilter.LEARNED)
    }
    
    /**
     * 加载未学题目
     */
    fun loadUnlearnedQuestions() {
        loadQuestionsByFilter(QuestionFilter.UNLEARNED)
    }
    
    /**
     * 加载答错的题目
     */
    fun loadWrongQuestions() {
        loadQuestionsByFilter(QuestionFilter.WRONG)
    }
    
    /**
     * 加载收藏的题目
     */
    fun loadFavoriteQuestions() {
        loadQuestionsByFilter(QuestionFilter.FAVORITE)
    }
    
    // ==================== 答题操作 ====================
    
    /**
     * 选择/取消选择答案
     */
    fun toggleAnswer(answer: String) {
        val current = currentQuestion.value?.question ?: return
        
        if (current.isSingleChoice()) {
            // 单选题：直接替换
            _selectedAnswers.value = setOf(answer)
        } else {
            // 多选题：切换选中状态
            val currentAnswers = _selectedAnswers.value.toMutableSet()
            if (currentAnswers.contains(answer)) {
                currentAnswers.remove(answer)
            } else {
                currentAnswers.add(answer)
            }
            _selectedAnswers.value = currentAnswers
        }
    }
    
    /**
     * 直接设置选中的答案(用于从缓存恢复时的映射转换)
     */
    fun setSelectedAnswers(answers: Set<String>) {
        _selectedAnswers.value = answers
    }
    
    /**
     * 提交答案
     */
    fun submitAnswer() {
        val current = currentQuestion.value?.question ?: return
        val isCorrect = current.checkAnswer(_selectedAnswers.value.toList())
        
        viewModelScope.launch {
            // 记录答题结果
            questionRepository.recordAnswer(
                questionId = current.id!!,
                level = _selectedLevel.value,
                isCorrect = isCorrect
            )
            
            // 如果是随机练习模式,标记题目已完成
            if (_studyMode.value == StudyMode.RANDOM_PRACTICE) {
                questionRepository.markRandomPracticeDone(
                    questionId = current.id!!,
                    level = _selectedLevel.value
                )
            }
            
            // 显示答案
            _showAnswer.value = true
        }
    }
    
    /**
     * 提交答案（带映射）
     * 用于练习模式,传入已映射回原始键的答案
     * @param originalAnswers 原始键的答案列表 (如 ["A", "C"])
     */
    fun submitAnswerWithMapping(originalAnswers: List<String>) {
        val current = currentQuestion.value?.question ?: return
        val questionId = current.id ?: return
        val isCorrect = current.checkAnswer(originalAnswers)
        
        viewModelScope.launch {
            // 记录答题结果到数据库
            questionRepository.recordAnswer(
                questionId = questionId,
                level = _selectedLevel.value,
                isCorrect = isCorrect
            )
            
            // 如果是随机练习模式,标记题目已完成
            if (_studyMode.value == StudyMode.RANDOM_PRACTICE) {
                questionRepository.markRandomPracticeDone(
                    questionId = questionId,
                    level = _selectedLevel.value
                )
            }
            
            // 显示答案
            _showAnswer.value = true
            
            // 保存当前题目的答题状态到内存缓存
            // 注意:这里存储原始答案,而不是映射后的答案
            val currentCache = _answerStateCache.value.toMutableMap()
            currentCache[questionId] = QuestionAnswerState(
                selectedAnswers = originalAnswers.toSet(),  // 存储原始答案
                showAnswer = true,
                isCorrect = isCorrect
            )
            _answerStateCache.value = currentCache
            
            // 刷新当前题目的学习记录（从数据库重新读取）
            val updatedRecord = questionRepository.getOrCreateStudyRecord(questionId, _selectedLevel.value)
            val updatedQuestion = QuestionWithRecord(current, updatedRecord)
            val updatedList = _questions.value.toMutableList()
            updatedList[_currentQuestionIndex.value] = updatedQuestion
            _questions.value = updatedList
            
            // 刷新题库统计信息
            loadQuestionBankStats()
        }
    }
    
    /**
     * 标记为关注/取消关注
     */
    fun markAsMastered() {
        val current = currentQuestion.value?.question ?: return
        
        viewModelScope.launch {
            questionRepository.markQuestionAsMastered(
                questionId = current.id!!,
                level = _selectedLevel.value
            )
            
            // 刷新统计信息
            loadQuestionBankStats()
        }
    }
    
    /**
     * 切换指定题目的掌握状态(用于列表页面)
     */
    fun toggleMastered(questionId: Int) {
        viewModelScope.launch {
            questionRepository.markQuestionAsMastered(
                questionId = questionId,
                level = _selectedLevel.value
            )
        }
    }
    
    /**
     * 切换收藏状态
     */
    fun toggleFavorite() {
        val current = currentQuestion.value?.question ?: return
        
        viewModelScope.launch {
            questionRepository.toggleFavorite(
                questionId = current.id!!,
                level = _selectedLevel.value
            )
            
            // 刷新当前题目
            val updatedRecord = questionRepository.getOrCreateStudyRecord(current.id!!, _selectedLevel.value)
            val updatedQuestion = QuestionWithRecord(current, updatedRecord)
            val updatedList = _questions.value.toMutableList()
            updatedList[_currentQuestionIndex.value] = updatedQuestion
            _questions.value = updatedList
            
            // 刷新题库统计信息，确保"我关注的题"计数正确
            loadQuestionBankStats()
        }
    }
    
    /**
     * 切换指定题目的收藏状态(用于列表页面)
     */
    fun toggleFavorite(questionId: Int) {
        viewModelScope.launch {
            questionRepository.toggleFavorite(
                questionId = questionId,
                level = _selectedLevel.value
            )
        }
    }
    
    // ==================== 导航操作 ====================
    
    /**
     * 恢复题目的答题状态
     */
    private fun restoreQuestionState() {
        val current = currentQuestion.value?.question ?: return
        val questionId = current.id ?: return
        
        val cachedState = _answerStateCache.value[questionId]
        if (cachedState != null) {
            // 只恢复 showAnswer 状态
            // selectedAnswers 由 UI 层的 LaunchedEffect 处理映射转换
            _showAnswer.value = cachedState.showAnswer
            // 暂时清空,等待 UI 层映射
            _selectedAnswers.value = emptySet()
        } else {
            // 没有缓存,重置状态
            _selectedAnswers.value = emptySet()
            _showAnswer.value = _studyMode.value == StudyMode.SEQUENTIAL_REVIEW
        }
    }
    
    /**
     * 获取当前题目的缓存答题状态
     */
    fun getCurrentQuestionAnswerState(): QuestionAnswerState? {
        val questionId = currentQuestion.value?.question?.id ?: return null
        return _answerStateCache.value[questionId]
    }
    
    /**
     * 下一题
     */
    fun nextQuestion() {
        if (_currentQuestionIndex.value < _questions.value.size - 1) {
            _currentQuestionIndex.value++
            restoreQuestionState()
        }
    }
    
    /**
     * 上一题
     */
    fun previousQuestion() {
        if (_currentQuestionIndex.value > 0) {
            _currentQuestionIndex.value--
            restoreQuestionState()
        }
    }
    
    /**
     * 跳转到指定题目
     */
    fun jumpToQuestion(index: Int) {
        if (index in _questions.value.indices) {
            _currentQuestionIndex.value = index
            restoreQuestionState()
        }
    }
    
    /**
     * 显示/隐藏答案
     */
    fun toggleShowAnswer() {
        _showAnswer.value = !_showAnswer.value
    }
    
    // ==================== 模拟考试方法 ====================
    
    /**
     * 保存模拟考试的答案
     */
    fun saveExamAnswer(questionId: Int, answer: List<String>) {
        val currentAnswers = _examAnswers.value.toMutableMap()
        currentAnswers[questionId] = answer
        _examAnswers.value = currentAnswers
    }
    
    /**
     * 获取某道题的答案
     */
    fun getAnswerForQuestion(questionId: Int): List<String> {
        return _examAnswers.value[questionId] ?: emptyList()
    }
    
    /**
     * 倒计时递减
     */
    fun decrementTimer() {
        if (_remainingTimeSeconds.value > 0) {
            _remainingTimeSeconds.value--
        }
    }
    
    /**
     * 提交模拟考试
     */
    fun submitExam() {
        viewModelScope.launch {
            try {
                val config = _examConfig.value ?: return@launch
                var correctCount = 0
                
                // 检查每道题的答案
                _questions.value.forEach { qwr ->
                    val questionId = qwr.question.id ?: 0
                    val userAnswer = _examAnswers.value[questionId] ?: emptyList()
                    val isCorrect = qwr.question.checkAnswer(userAnswer)
                    if (isCorrect) {
                        correctCount++
                    }
                }
                
                val isPassed = correctCount >= config.passCount
                val accuracy = correctCount.toFloat() / config.questionCount
                
                // 设置考试结果
                _examScore.value = ExamScore(
                    correctCount = correctCount,
                    totalCount = config.questionCount,
                    isPassed = isPassed,
                    accuracy = accuracy
                )
                
                // 标记考试已提交
                _examSubmitted.value = true
                
                // 保存考试记录到数据库
                val endTime = System.currentTimeMillis()
                val questionIds = _questions.value.map { it.question.id ?: 0 }
                val userAnswersJson = buildUserAnswersJson()
                
                val examRecord = ExamRecord(
                    level = config.level,
                    startTime = examStartTime,
                    endTime = endTime,
                    totalQuestions = config.questionCount,
                    correctCount = correctCount,
                    wrongCount = config.questionCount - correctCount,
                    score = correctCount.toFloat(),
                    isPassed = isPassed,
                    questionIds = questionIds.joinToString(","),
                    userAnswers = userAnswersJson
                )
                
                examRepository.saveExamRecord(examRecord)
                
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    /**
     * 构建用户答案的JSON字符串
     */
    private fun buildUserAnswersJson(): String {
        val answersMap = _questions.value.map { qwr ->
            val questionId = qwr.question.id ?: 0
            val answer = _examAnswers.value[questionId] ?: emptyList()
            mapOf("questionId" to questionId, "answer" to answer)
        }
        return answersMap.toString()
    }
    
    // ==================== 清除数据 ====================
    
    /**
     * 重置状态
     */
    fun reset() {
        _studyMode.value = null
        _questions.value = emptyList()
        _currentQuestionIndex.value = 0
        _selectedAnswers.value = emptySet()
        _showAnswer.value = false
        _answerStateCache.value = emptyMap()
        _examAnswers.value = emptyMap()
        _examSubmitted.value = false
        _examScore.value = null
        _examConfig.value = null
    }
}

/**
 * 考试得分数据类
 */
data class ExamScore(
    val correctCount: Int,
    val totalCount: Int,
    val isPassed: Boolean,
    val accuracy: Float
)
