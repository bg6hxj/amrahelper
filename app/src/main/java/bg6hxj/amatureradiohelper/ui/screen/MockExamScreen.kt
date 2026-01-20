package bg6hxj.amatureradiohelper.ui.screen

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import bg6hxj.amatureradiohelper.data.database.AppDatabase
import bg6hxj.amatureradiohelper.data.repository.ExamRepository
import bg6hxj.amatureradiohelper.data.repository.QuestionRepository
import bg6hxj.amatureradiohelper.ui.viewmodel.ExamViewModel
import bg6hxj.amatureradiohelper.ui.viewmodel.ExamViewModelFactory
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.minutes

/**
 * 模拟考试页面
 * 特点:
 * 1. 限时考试,倒计时显示
 * 2. 所有题目答完后点击提交才显示答案和得分
 * 3. 最后一题"下一题"按钮变为"提交"
 * 4. 题目列表可查看已答题目并提交(需二次确认)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MockExamScreen(
    level: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tag = "MockExamScreen"
    Log.d(tag, "=== MockExamScreen 启动 ===")
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
    
    // 初始化时选择等级并开始模拟考试
    LaunchedEffect(level) {
        viewModel.selectLevel(level)
        viewModel.startMockExam()
    }
    
    val currentQuestion by viewModel.currentQuestion.collectAsState()
    val currentIndex by viewModel.currentQuestionIndex.collectAsState()
    val questions by viewModel.questions.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val selectedAnswers by viewModel.selectedAnswers.collectAsState()
    val examConfig by viewModel.examConfig.collectAsState()
    val examSubmitted by viewModel.examSubmitted.collectAsState()
    val examScore by viewModel.examScore.collectAsState()
    val remainingTimeSeconds by viewModel.remainingTimeSeconds.collectAsState()
    
    // 倒计时
    LaunchedEffect(examSubmitted, remainingTimeSeconds) {
        if (!examSubmitted && remainingTimeSeconds > 0) {
            delay(1000)
            viewModel.decrementTimer()
        } else if (!examSubmitted && remainingTimeSeconds <= 0) {
            // 时间到,自动提交
            viewModel.submitExam()
        }
    }
    
    // 退出确认对话框状态
    var showExitDialog by remember { mutableStateOf(false) }
    
    // 提交确认对话框状态
    var showSubmitDialog by remember { mutableStateOf(false) }
    
    // 题目列表底部表单状态
    var showQuestionListSheet by remember { mutableStateOf(false) }
    
    // 拦截系统返回事件
    BackHandler {
        Log.d(tag, ">>> BackHandler: 系统返回手势 -> 显示退出确认对话框")
        showExitDialog = true
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("模拟考试 - ${level}类")
                        if (examConfig != null && !examSubmitted) {
                            // 倒计时显示
                            val minutes = remainingTimeSeconds / 60
                            val seconds = remainingTimeSeconds % 60
                            Text(
                                text = String.format("剩余时间: %02d:%02d", minutes, seconds),
                                style = MaterialTheme.typography.bodySmall,
                                color = if (remainingTimeSeconds < 300) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { 
                        Log.d(tag, ">>> TopBar返回按钮: onClick -> 显示退出确认对话框")
                        showExitDialog = true
                    }) {
                        Icon(Icons.Default.ArrowBack, "返回")
                    }
                },
                actions = {
                    if (!examSubmitted) {
                        // 显示答题进度
                        val answeredCount = questions.count { qwr ->
                            viewModel.getAnswerForQuestion(qwr.question.id ?: 0).isNotEmpty()
                        }
                        TextButton(
                            onClick = { showQuestionListSheet = true }
                        ) {
                            Text("${answeredCount}/${questions.size}")
                        }
                    }
                }
            )
        },
        modifier = modifier
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                examSubmitted -> {
                    // 显示考试结果
                    ExamResultScreen(
                        examConfig = examConfig!!,
                        examScore = examScore,
                        questions = questions,
                        viewModel = viewModel,
                        onExit = onNavigateBack
                    )
                }
                currentQuestion != null -> {
                    MockExamContent(
                        questionWithRecord = currentQuestion!!,
                        currentIndex = currentIndex,
                        questions = questions,
                        selectedAnswers = selectedAnswers,
                        viewModel = viewModel,
                        onSubmitExam = { showSubmitDialog = true }
                    )
                }
            }
        }
        
        // 退出确认对话框
        if (showExitDialog) {
            AlertDialog(
                onDismissRequest = { showExitDialog = false },
                icon = { Icon(Icons.Default.Warning, contentDescription = null) },
                title = { Text("确认退出") },
                text = { Text("考试尚未完成,确定要退出吗?退出后本次考试将不会保存。") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            Log.d(tag, ">>> 退出对话框: 确认退出")
                            showExitDialog = false
                            onNavigateBack()
                        }
                    ) {
                        Text("退出")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showExitDialog = false }) {
                        Text("继续考试")
                    }
                }
            )
        }
        
        // 提交确认对话框
        if (showSubmitDialog) {
            val answeredCount = questions.count { qwr ->
                viewModel.getAnswerForQuestion(qwr.question.id ?: 0).isNotEmpty()
            }
            AlertDialog(
                onDismissRequest = { showSubmitDialog = false },
                icon = { Icon(Icons.Default.Send, contentDescription = null) },
                title = { Text("确认提交") },
                text = { 
                    Text("已作答 $answeredCount/${questions.size} 题,确定要提交考试吗?提交后将无法修改答案。") 
                },
                confirmButton = {
                    Button(
                        onClick = {
                            Log.d(tag, ">>> 提交对话框: 确认提交")
                            showSubmitDialog = false
                            viewModel.submitExam()
                        }
                    ) {
                        Text("确认提交")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showSubmitDialog = false }) {
                        Text("继续答题")
                    }
                }
            )
        }
        
        // 题目列表底部表单
        if (showQuestionListSheet) {
            ModalBottomSheet(
                onDismissRequest = { showQuestionListSheet = false }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "选择题目",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Button(
                            onClick = {
                                showQuestionListSheet = false
                                showSubmitDialog = true
                            }
                        ) {
                            Text("提交")
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    QuestionListGrid(
                        questions = questions,
                        currentIndex = currentIndex,
                        viewModel = viewModel,
                        onQuestionSelected = { index ->
                            viewModel.jumpToQuestion(index)
                            showQuestionListSheet = false
                        }
                    )
                }
            }
        }
    }
}

/**
 * 模拟考试内容区域
 */
@Composable
private fun MockExamContent(
    questionWithRecord: bg6hxj.amatureradiohelper.data.model.QuestionWithRecord,
    currentIndex: Int,
    questions: List<bg6hxj.amatureradiohelper.data.model.QuestionWithRecord>,
    selectedAnswers: Set<String>,
    viewModel: ExamViewModel,
    onSubmitExam: () -> Unit,
    modifier: Modifier = Modifier
) {
    // 打乱选项 (使用题目ID作为随机种子)
    val shuffledOptionsData = remember(questionWithRecord.question.id) {
        val originalOptions = questionWithRecord.question.parseOptions()
        val random = kotlin.random.Random(questionWithRecord.question.id?.toLong() ?: 0L)
        val shuffled = originalOptions.shuffled(random)
        val labels = listOf("A", "B", "C", "D", "E", "F")
        
        val answerMapping = mutableMapOf<String, String>()
        shuffled.forEachIndexed { index, (originalKey, _) ->
            answerMapping[originalKey] = labels[index]
        }
        
        val newOptions = shuffled.mapIndexed { index, (_, value) ->
            labels[index] to value
        }
        
        ShuffledOptionsData(
            options = newOptions,
            answerMapping = answerMapping
        )
    }
    
    // 恢复已选答案
    LaunchedEffect(questionWithRecord.question.id, shuffledOptionsData) {
        val questionId = questionWithRecord.question.id ?: 0
        val savedAnswers = viewModel.getAnswerForQuestion(questionId)
        if (savedAnswers.isNotEmpty()) {
            val mappedAnswers = savedAnswers.mapNotNull { originalKey ->
                shuffledOptionsData.answerMapping[originalKey]
            }.toSet()
            if (mappedAnswers != selectedAnswers) {
                viewModel.setSelectedAnswers(mappedAnswers)
            }
        } else {
            viewModel.setSelectedAnswers(emptySet())
        }
    }
    
    // 反向映射
    val reverseMapping = remember(shuffledOptionsData) {
        shuffledOptionsData.answerMapping.entries.associate { it.value to it.key }
    }
    
    val isLastQuestion = currentIndex == questions.size - 1
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 题目内容 (包含题号、题型、收藏等)
            item {
                bg6hxj.amatureradiohelper.ui.component.QuestionContentCard(
                    question = questionWithRecord.question.question,
                    currentIndex = currentIndex + 1,
                    totalCount = questions.size,
                    questionType = if (questionWithRecord.question.getCorrectAnswers().size > 1) "多选" else "单选",
                    isLearned = questionWithRecord.isLearned,
                    isFavorite = questionWithRecord.isFavorite,
                    isWrong = questionWithRecord.isWrong,
                    onToggleFavorite = { viewModel.toggleFavorite() }
                )
            }
            
            // 选项列表
            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "选项",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (questionWithRecord.question.getCorrectAnswers().size > 1) "多选" else "单选",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    shuffledOptionsData.options.forEach { (key, value) ->
                        val isSelected = selectedAnswers.contains(key)
                        
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surface,
                            border = androidx.compose.foundation.BorderStroke(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline
                            ),
                            onClick = {
                                viewModel.toggleAnswer(key)
                            }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(
                                            color = if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surfaceVariant,
                                            shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = key,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                
                                Text(
                                    text = value,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 底部导航按钮
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // 上一题按钮
            OutlinedButton(
                onClick = {
                    // 保存当前答案
                    val originalAnswers = selectedAnswers.map { newKey ->
                        reverseMapping[newKey] ?: newKey
                    }
                    viewModel.saveExamAnswer(questionWithRecord.question.id ?: 0, originalAnswers)
                    viewModel.previousQuestion()
                },
                enabled = currentIndex > 0,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("上一题")
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // 下一题/提交按钮
            Button(
                onClick = {
                    // 保存当前答案
                    val originalAnswers = selectedAnswers.map { newKey ->
                        reverseMapping[newKey] ?: newKey
                    }
                    viewModel.saveExamAnswer(questionWithRecord.question.id ?: 0, originalAnswers)
                    
                    if (isLastQuestion) {
                        // 最后一题,显示提交确认
                        onSubmitExam()
                    } else {
                        // 下一题
                        viewModel.nextQuestion()
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Text(if (isLastQuestion) "提交" else "下一题")
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    if (isLastQuestion) Icons.Default.Send else Icons.Default.ArrowForward,
                    contentDescription = null
                )
            }
        }
    }
}

/**
 * 考试结果页面
 */
@Composable
private fun ExamResultScreen(
    examConfig: bg6hxj.amatureradiohelper.data.model.ExamConfig,
    examScore: bg6hxj.amatureradiohelper.ui.viewmodel.ExamScore?,
    questions: List<bg6hxj.amatureradiohelper.data.model.QuestionWithRecord>,
    viewModel: ExamViewModel,
    onExit: () -> Unit
) {
    // 题目详情对话框状态
    var selectedQuestionIndex by remember { mutableStateOf<Int?>(null) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // 考试结果卡片
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (examScore?.isPassed == true) 
                    MaterialTheme.colorScheme.primaryContainer 
                else 
                    MaterialTheme.colorScheme.errorContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = if (examScore?.isPassed == true) Icons.Default.CheckCircle else Icons.Default.Close,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = if (examScore?.isPassed == true) 
                        MaterialTheme.colorScheme.primary 
                    else 
                        MaterialTheme.colorScheme.error
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = if (examScore?.isPassed == true) "恭喜通过!" else "未通过",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "得分: ${examScore?.correctCount ?: 0}/${examConfig.questionCount}",
                    style = MaterialTheme.typography.titleLarge
                )
                
                Text(
                    text = "合格线: ${examConfig.passCount}分",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "正确率: ${String.format("%.1f", (examScore?.accuracy ?: 0f) * 100)}%",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 题目详情列表
        Text(
            text = "题目详情",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(questions.size) { index ->
                val qwr = questions[index]
                val questionId = qwr.question.id ?: 0
                val userAnswer = viewModel.getAnswerForQuestion(questionId)
                val correctAnswer = qwr.question.getCorrectAnswers()
                val isCorrect = qwr.question.checkAnswer(userAnswer)
                
                // 为卡片创建打乱后的选项数据(使用相同的seed)
                val originalOptions = qwr.question.parseOptions()
                
                val random = kotlin.random.Random(questionId.toLong())
                val shuffled = originalOptions.shuffled(random)
                val labels = listOf("A", "B", "C", "D", "E", "F")
                
                val answerMapping = mutableMapOf<String, String>()
                shuffled.forEachIndexed { idx, (originalKey, _) ->
                    answerMapping[originalKey] = labels[idx]
                }
                
                val newOptions = shuffled.mapIndexed { idx, (_, value) ->
                    labels[idx] to value
                }
                
                val shuffledOptionsData = ShuffledOptionsData(
                    options = newOptions,
                    answerMapping = answerMapping
                )
                
                ExamQuestionResultCard(
                    questionNumber = index + 1,
                    question = qwr.question,
                    userAnswer = userAnswer,
                    correctAnswer = correctAnswer,
                    isCorrect = isCorrect,
                    shuffledOptionsData = shuffledOptionsData,
                    onClick = { selectedQuestionIndex = index }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 退出按钮
        Button(
            onClick = onExit,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("返回")
        }
    }
    
    // 题目详情对话框
    selectedQuestionIndex?.let { index ->
        val qwr = questions[index]
        val questionId = qwr.question.id ?: 0
        val userAnswer = viewModel.getAnswerForQuestion(questionId)
        val correctAnswer = qwr.question.getCorrectAnswers()
        val isCorrect = qwr.question.checkAnswer(userAnswer)
        
        QuestionDetailDialog(
            questionNumber = index + 1,
            question = qwr.question,
            userAnswer = userAnswer,
            correctAnswer = correctAnswer,
            isCorrect = isCorrect,
            onDismiss = { selectedQuestionIndex = null }
        )
    }
}

/**
 * 考试题目结果卡片
 */
@Composable
private fun ExamQuestionResultCard(
    questionNumber: Int,
    question: bg6hxj.amatureradiohelper.data.model.Question,
    userAnswer: List<String>,
    correctAnswer: List<String>,
    isCorrect: Boolean,
    shuffledOptionsData: ShuffledOptionsData,
    onClick: () -> Unit
) {
    // 将用户的原始答案映射到打乱后的标签
    val mappedUserAnswer = remember(userAnswer, shuffledOptionsData) {
        userAnswer.mapNotNull { originalKey ->
            shuffledOptionsData.answerMapping[originalKey]
        }
    }
    
    // 将正确答案映射到打乱后的标签
    val mappedCorrectAnswer = remember(correctAnswer, shuffledOptionsData) {
        correctAnswer.mapNotNull { originalKey ->
            shuffledOptionsData.answerMapping[originalKey]
        }
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isCorrect) 
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            else 
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
        ),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "第 $questionNumber 题",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Icon(
                    imageVector = if (isCorrect) Icons.Default.Check else Icons.Default.Close,
                    contentDescription = null,
                    tint = if (isCorrect) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = question.question,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row {
                Text(
                    text = "你的答案: ",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = if (mappedUserAnswer.isEmpty()) "未作答" else mappedUserAnswer.sorted().joinToString(", "),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isCorrect) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
            }
            
            Row {
                Text(
                    text = "正确答案: ",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = mappedCorrectAnswer.sorted().joinToString(", "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * 题目列表网格(显示已答/未答状态)
 */
@Composable
private fun QuestionListGrid(
    questions: List<bg6hxj.amatureradiohelper.data.model.QuestionWithRecord>,
    currentIndex: Int,
    viewModel: ExamViewModel,
    onQuestionSelected: (Int) -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 每5个题目一行
        items(questions.chunked(5).size) { rowIndex ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                questions.chunked(5)[rowIndex].forEachIndexed { colIndex, qwr ->
                    val index = rowIndex * 5 + colIndex
                    val questionId = qwr.question.id ?: 0
                    val isAnswered = viewModel.getAnswerForQuestion(questionId).isNotEmpty()
                    
                    OutlinedButton(
                        onClick = { onQuestionSelected(index) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = when {
                                index == currentIndex -> MaterialTheme.colorScheme.primary
                                isAnswered -> MaterialTheme.colorScheme.primaryContainer
                                else -> MaterialTheme.colorScheme.surface
                            }
                        )
                    ) {
                        Text(
                            text = "${index + 1}",
                            color = when {
                                index == currentIndex -> MaterialTheme.colorScheme.onPrimary
                                isAnswered -> MaterialTheme.colorScheme.onPrimaryContainer
                                else -> MaterialTheme.colorScheme.onSurface
                            }
                        )
                    }
                }
                // 填充空白
                repeat(5 - questions.chunked(5)[rowIndex].size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

/**
 * 题目详情对话框
 * 显示题目、打乱的选项(保持用户作答时的顺序)、用户答案和正确答案
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QuestionDetailDialog(
    questionNumber: Int,
    question: bg6hxj.amatureradiohelper.data.model.Question,
    userAnswer: List<String>,
    correctAnswer: List<String>,
    isCorrect: Boolean,
    onDismiss: () -> Unit
) {
    // 使用题目ID作为随机种子重现当时的选项顺序
    val shuffledOptionsData = remember(question.id) {
        val originalOptions = question.parseOptions()
        val random = kotlin.random.Random(question.id?.toLong() ?: 0L)
        val shuffled = originalOptions.shuffled(random)
        val labels = listOf("A", "B", "C", "D", "E", "F")
        
        val answerMapping = mutableMapOf<String, String>()
        shuffled.forEachIndexed { index, (originalKey, _) ->
            answerMapping[originalKey] = labels[index]
        }
        
        val newOptions = shuffled.mapIndexed { index, (_, value) ->
            labels[index] to value
        }
        
        ShuffledOptionsData(
            options = newOptions,
            answerMapping = answerMapping
        )
    }
    
    // 将用户的原始答案映射到打乱后的标签
    val mappedUserAnswer = remember(userAnswer, shuffledOptionsData) {
        userAnswer.mapNotNull { originalKey ->
            shuffledOptionsData.answerMapping[originalKey]
        }
    }
    
    // 将正确答案映射到打乱后的标签
    val mappedCorrectAnswer = remember(correctAnswer, shuffledOptionsData) {
        correctAnswer.mapNotNull { originalKey ->
            shuffledOptionsData.answerMapping[originalKey]
        }
    }
    
    AlertDialog(
        onDismissRequest = onDismiss
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                // 标题栏
                Surface(
                    color = if (isCorrect) 
                        MaterialTheme.colorScheme.primaryContainer 
                    else 
                        MaterialTheme.colorScheme.errorContainer
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "第 $questionNumber 题",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "关闭")
                        }
                    }
                }
                
                // 内容区域
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 题目内容
                    item {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = "题目",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = question.question,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                    }
                    
                    // 选项列表(保持用户作答时的顺序)
                    item {
                        Text(
                            text = "选项",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    items(shuffledOptionsData.options.size) { index ->
                        val (key, value) = shuffledOptionsData.options[index]
                        val isUserSelected = mappedUserAnswer.contains(key)
                        val isCorrectOption = mappedCorrectAnswer.contains(key)
                        val isWrong = isUserSelected && !isCorrectOption
                        
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                            color = when {
                                isCorrectOption -> MaterialTheme.colorScheme.primaryContainer
                                isWrong -> MaterialTheme.colorScheme.errorContainer
                                isUserSelected -> MaterialTheme.colorScheme.secondaryContainer
                                else -> MaterialTheme.colorScheme.surface
                            },
                            border = androidx.compose.foundation.BorderStroke(
                                width = if (isUserSelected || isCorrectOption) 2.dp else 1.dp,
                                color = when {
                                    isCorrectOption -> MaterialTheme.colorScheme.primary
                                    isWrong -> MaterialTheme.colorScheme.error
                                    isUserSelected -> MaterialTheme.colorScheme.secondary
                                    else -> MaterialTheme.colorScheme.outline
                                }
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(
                                            color = when {
                                                isCorrectOption -> MaterialTheme.colorScheme.primary
                                                isWrong -> MaterialTheme.colorScheme.error
                                                isUserSelected -> MaterialTheme.colorScheme.secondary
                                                else -> MaterialTheme.colorScheme.surfaceVariant
                                            },
                                            shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = key,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = when {
                                            isCorrectOption || isWrong -> MaterialTheme.colorScheme.onPrimary
                                            isUserSelected -> MaterialTheme.colorScheme.onSecondary
                                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                                        }
                                    )
                                }
                                
                                Text(
                                    text = value,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.weight(1f)
                                )
                                
                                // 状态图标
                                when {
                                    isCorrectOption -> {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "正确",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    isWrong -> {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "错误",
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }
                        }
                    }
                    
                    // 答案说明
                    item {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "你的答案:",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = if (mappedUserAnswer.isEmpty()) "未作答" else mappedUserAnswer.sorted().joinToString(", "),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (isCorrect) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "正确答案:",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = mappedCorrectAnswer.sorted().joinToString(", "),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
