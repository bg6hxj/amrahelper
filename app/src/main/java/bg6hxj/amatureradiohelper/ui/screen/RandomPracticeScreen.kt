package bg6hxj.amatureradiohelper.ui.screen

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import bg6hxj.amatureradiohelper.data.database.AppDatabase
import bg6hxj.amatureradiohelper.data.repository.ExamRepository
import bg6hxj.amatureradiohelper.data.repository.QuestionRepository
import bg6hxj.amatureradiohelper.ui.viewmodel.ExamViewModel
import bg6hxj.amatureradiohelper.ui.viewmodel.ExamViewModelFactory

/**
 * 随机练习页面
 * 随机打乱题目顺序进行练习,需要用户选择答案并提交
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RandomPracticeScreen(
    level: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tag = "RandomPracticeScreen"
    Log.d(tag, "=== RandomPracticeScreen 启动 ===")
    Log.d(tag, "level: $level")
    
    val context = LocalContext.current
    
    // 使用 remember 创建 Repository
    val questionRepository = remember(context) {
        val database = AppDatabase.getDatabase(context)
        QuestionRepository(
            database.questionDao(),
            database.studyRecordDao()
        )
    }
    
    val examRepository = remember(context) {
        val database = AppDatabase.getDatabase(context)
        ExamRepository(database.examRecordDao())
    }
    
    val viewModel: ExamViewModel = viewModel(
        factory = ExamViewModelFactory(questionRepository, examRepository)
    )
    
    // 初始化时选择等级并开始随机练习
    LaunchedEffect(level) {
        viewModel.selectLevel(level)
        viewModel.startRandomPractice()
    }
    
    val currentQuestion by viewModel.currentQuestion.collectAsState()
    val currentIndex by viewModel.currentQuestionIndex.collectAsState()
    val questions by viewModel.questions.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val stats by viewModel.questionBankStats.collectAsState()
    val selectedAnswers by viewModel.selectedAnswers.collectAsState()
    val showAnswer by viewModel.showAnswer.collectAsState()
    
    // 退出确认对话框状态
    var showExitDialog by remember { mutableStateOf(false) }
    
    // 拦截系统返回事件,显示退出确认对话框
    BackHandler {
        Log.d(tag, ">>> BackHandler: 系统返回手势 -> 显示退出确认对话框")
        showExitDialog = true
    }
    
    // 题目跳转底部表状态
    var showQuestionListSheet by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "随机练习 - ${getLevelDisplayName(level)}",
                            style = MaterialTheme.typography.titleMedium
                        )
                        if (questions.isNotEmpty()) {
                            Text(
                                text = "${currentIndex + 1} / ${questions.size}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { 
                        Log.d(tag, ">>> 点击返回按钮")
                        showExitDialog = true 
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    // 学习进度指示（可点击）
                    stats?.let { s ->
                        TextButton(
                            onClick = { showQuestionListSheet = true }
                        ) {
                            Text(
                                text = "${(s.learnProgress * 100).toInt()}%",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (questions.isEmpty()) {
                Text(
                    text = "暂无题目",
                    modifier = Modifier.align(Alignment.Center),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                currentQuestion?.let { questionWithRecord ->
                    // 使用与 SequentialPracticeScreen 相同的内容组件
                    RandomPracticeContent(
                        questionWithRecord = questionWithRecord,
                        currentIndex = currentIndex,
                        questions = questions,
                        stats = stats,
                        selectedAnswers = selectedAnswers,
                        showAnswer = showAnswer,
                        viewModel = viewModel,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
        
        // 退出确认对话框
        if (showExitDialog) {
            Log.d(tag, "显示退出确认对话框")
            AlertDialog(
                onDismissRequest = { 
                    Log.d(tag, ">>> 对话框被取消")
                    showExitDialog = false 
                },
                icon = { Icon(Icons.Default.Info, contentDescription = null) },
                title = { Text("退出练习") },
                text = { Text("确定要退出随机练习吗?练习进度已自动保存。") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            Log.d(tag, ">>> 确认退出 -> 调用 onNavigateBack()")
                            showExitDialog = false
                            onNavigateBack()
                        }
                    ) {
                        Text("退出")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { 
                        Log.d(tag, ">>> 取消退出,继续练习")
                        showExitDialog = false 
                    }) {
                        Text("继续练习")
                    }
                }
            )
        }
        
        // 题目列表底部表
        if (showQuestionListSheet) {
            ModalBottomSheet(
                onDismissRequest = { showQuestionListSheet = false }
            ) {
                QuestionListSheet(
                    questions = questions,
                    currentIndex = currentIndex,
                    onQuestionSelected = { index ->
                        viewModel.jumpToQuestion(index)
                        showQuestionListSheet = false
                    }
                )
            }
        }
    }
}

/**
 * 随机练习内容区域
 * 复用 SequentialPracticeScreen 的逻辑和组件
 */
@Composable
private fun RandomPracticeContent(
    questionWithRecord: bg6hxj.amatureradiohelper.data.model.QuestionWithRecord,
    currentIndex: Int,
    questions: List<bg6hxj.amatureradiohelper.data.model.QuestionWithRecord>,
    stats: bg6hxj.amatureradiohelper.data.model.QuestionBankStats?,
    selectedAnswers: Set<String>,
    showAnswer: Boolean,
    viewModel: ExamViewModel,
    modifier: Modifier = Modifier
) {
    var dragOffset by remember { mutableStateOf(0f) }
    
    // 打乱选项 (使用题目ID作为随机种子,确保同一题目的打乱结果始终一致)
    val shuffledOptionsData = remember(questionWithRecord.question.id) {
        val originalOptions = questionWithRecord.question.parseOptions()
        
        // 使用题目ID作为随机种子,确保每次打乱结果相同
        val random = kotlin.random.Random(questionWithRecord.question.id?.toLong() ?: 0L)
        val shuffled = originalOptions.shuffled(random)
        val labels = listOf("A", "B", "C", "D", "E", "F")
        
        // 创建原始键到新键的映射
        val answerMapping = mutableMapOf<String, String>()
        shuffled.forEachIndexed { index, (originalKey, _) ->
            answerMapping[originalKey] = labels[index]
        }
        
        // 创建新的选项列表(使用新的标签)
        val newOptions = shuffled.mapIndexed { index, (_, value) ->
            labels[index] to value
        }
        
        ShuffledOptionsData(
            options = newOptions,
            answerMapping = answerMapping
        )
    }
    
    // 当映射关系建立后,将缓存中的原始答案转换为当前映射后的键
    LaunchedEffect(questionWithRecord.question.id, shuffledOptionsData) {
        val cachedState = viewModel.getCurrentQuestionAnswerState()
        if (cachedState != null && cachedState.selectedAnswers.isNotEmpty()) {
            // 将缓存的原始答案(如 ["A", "C"])映射到当前UI的键
            val mappedAnswers = cachedState.selectedAnswers.mapNotNull { originalKey ->
                shuffledOptionsData.answerMapping[originalKey]
            }.toSet()
            
            // 更新 ViewModel 的 selectedAnswers 为映射后的键
            if (mappedAnswers != selectedAnswers) {
                viewModel.setSelectedAnswers(mappedAnswers)
            }
        }
    }
    
    // 映射正确答案到新的键
    val mappedCorrectAnswers = remember(questionWithRecord.question.id) {
        questionWithRecord.question.getCorrectAnswers().map { originalKey ->
            shuffledOptionsData.answerMapping[originalKey] ?: originalKey
        }
    }
    
    // 反向映射（新键 -> 原始键）
    val reverseMapping = remember(shuffledOptionsData) {
        shuffledOptionsData.answerMapping.entries.associate { it.value to it.key }
    }
    
    // 检查答案是否正确
    val isAnswerCorrect = remember(selectedAnswers, showAnswer) {
        if (!showAnswer) null
        else {
            val originalSelectedAnswers = selectedAnswers.map { newKey ->
                reverseMapping[newKey] ?: newKey
            }
            questionWithRecord.question.checkAnswer(originalSelectedAnswers.toList())
        }
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(currentIndex) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        val swipeThreshold = 100f
                        when {
                            dragOffset > swipeThreshold && currentIndex > 0 -> {
                                viewModel.previousQuestion()
                            }
                            dragOffset < -swipeThreshold && currentIndex < questions.size - 1 -> {
                                viewModel.nextQuestion()
                            }
                        }
                        dragOffset = 0f
                    },
                    onDragCancel = {
                        dragOffset = 0f
                    },
                    onHorizontalDrag = { _, dragAmount ->
                        dragOffset += dragAmount
                    }
                )
            }
    ) {
        // 学习进度条
        stats?.let { s ->
            LinearProgressIndicator(
                progress = { s.learnProgress },
                modifier = Modifier.fillMaxWidth(),
            )
        }
        
        // 题目内容区域
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 题目内容
            item {
                PracticeQuestionContentCard(
                    question = questionWithRecord.question.question,
                    currentIndex = currentIndex + 1,
                    totalCount = questions.size,
                    questionType = if (questionWithRecord.question.isSingleChoice()) "单选题" else "多选题",
                    isLearned = questionWithRecord.isLearned,
                    isFavorite = questionWithRecord.isFavorite,
                    onToggleFavorite = { viewModel.toggleFavorite() }
                )
            }
            
            // 选项列表
            item {
                PracticeOptionsCard(
                    options = shuffledOptionsData.options,
                    correctAnswers = mappedCorrectAnswers,
                    selectedAnswers = selectedAnswers.toList(),
                    showAnswer = showAnswer,
                    isSingleChoice = questionWithRecord.question.isSingleChoice(),
                    onOptionSelected = { option ->
                        viewModel.toggleAnswer(option)
                    }
                )
            }
            
            // 答题结果提示
            if (showAnswer && isAnswerCorrect != null) {
                item {
                    AnswerResultCard(
                        isCorrect = isAnswerCorrect,
                        correctAnswers = mappedCorrectAnswers
                    )
                }
            }
        }
        
        // 底部操作栏
        PracticeBottomActionBar(
            currentIndex = currentIndex,
            totalCount = questions.size,
            showAnswer = showAnswer,
            hasSelectedAnswer = selectedAnswers.isNotEmpty(),
            onPrevious = { 
                viewModel.previousQuestion()
            },
            onNext = { 
                viewModel.nextQuestion()
            },
            onSubmit = { 
                // 将用户选择的新键映射回原始键再提交
                val originalSelectedAnswers = selectedAnswers.map { newKey ->
                    reverseMapping[newKey] ?: newKey
                }
                
                viewModel.submitAnswerWithMapping(originalSelectedAnswers.toList())
            }
        )
    }
}

/**
 * 获取等级显示名称
 */
private fun getLevelDisplayName(level: String): String {
    return when (level) {
        "A" -> "A类题库"
        "B" -> "B类题库"
        "C" -> "C类题库"
        else -> level
    }
}
