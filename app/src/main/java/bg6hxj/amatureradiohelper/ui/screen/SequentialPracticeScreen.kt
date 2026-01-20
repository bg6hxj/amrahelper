package bg6hxj.amatureradiohelper.ui.screen

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import bg6hxj.amatureradiohelper.data.database.AppDatabase
import bg6hxj.amatureradiohelper.data.model.QuestionWithRecord
import bg6hxj.amatureradiohelper.data.repository.ExamRepository
import bg6hxj.amatureradiohelper.data.repository.QuestionRepository
import bg6hxj.amatureradiohelper.ui.viewmodel.ExamViewModel
import bg6hxj.amatureradiohelper.ui.viewmodel.ExamViewModelFactory

/**
 * 打乱后的选项数据
 */
internal data class ShuffledOptionsData(
    val options: List<Pair<String, String>>, // 新标签和选项内容
    val answerMapping: Map<String, String>    // 原始键 -> 新键的映射
)

/**
 * 顺序练习页面
 * 按顺序练习题目，需要用户选择答案并提交
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SequentialPracticeScreen(
    level: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tag = "SequentialPracticeScreen"
    Log.d(tag, "=== SequentialPracticeScreen 启动 ===")
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
    
    // 初始化时选择等级并开始顺序练习
    LaunchedEffect(level) {
        viewModel.selectLevel(level)
        viewModel.startSequentialPractice()
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
                            text = "顺序练习 - ${getLevelName(level)}",
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
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                isLoading -> {
                    // 加载状态
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                
                currentQuestion == null || questions.isEmpty() -> {
                    // 空状态
                    PracticeEmptyStateView(
                        message = "没有找到题目",
                        onRetry = { viewModel.startSequentialPractice() }
                    )
                }
                
                else -> {
                    // 题目内容
                    currentQuestion?.let { questionWithRecord ->
                        // 调试:监控题目变化
                        LaunchedEffect(currentIndex, questionWithRecord.question.id) {
                            // 题目切换时的简化日志
                        }
                        
                        // 滑动手势状态
                        var dragOffset by remember { mutableFloatStateOf(0f) }
                        
                        // 随机排序的选项(使用题目ID作为随机种子,确保同一题目的打乱结果始终一致)
                        // 返回 ShuffledOptions 包含选项列表和答案映射
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
                        
                        // 将原始正确答案映射到新的位置
                        val mappedCorrectAnswers = remember(questionWithRecord.question.id) {
                            questionWithRecord.question.getCorrectAnswers().map { originalKey ->
                                shuffledOptionsData.answerMapping[originalKey] ?: originalKey
                            }
                        }
                        
                        // 创建反向映射（新键 -> 原始键）用于答案检查
                        val reverseMapping = remember(questionWithRecord.question.id) {
                            shuffledOptionsData.answerMapping.entries.associate { (k, v) -> v to k }
                        }
                        
                        // 检查用户答案是否正确(需要映射回原始键)
                        val isAnswerCorrect = remember(selectedAnswers.toList(), showAnswer, questionWithRecord.question.id) {
                            if (!showAnswer) {
                                false
                            } else {
                                // 优先从缓存获取答题结果
                                val cachedState = viewModel.getCurrentQuestionAnswerState()
                                if (cachedState?.isCorrect != null) {
                                    cachedState.isCorrect
                                } else {
                                    // 如果没有缓存，实时计算
                                    val originalSelectedAnswers = selectedAnswers.map { newKey ->
                                        reverseMapping[newKey] ?: newKey
                                    }
                                    questionWithRecord.question.checkAnswer(originalSelectedAnswers.toList())
                                }
                            }
                        }
                        
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .pointerInput(currentIndex) {
                                    detectHorizontalDragGestures(
                                        onDragEnd = {
                                            // 根据滑动距离判断是否切换题目
                                            val swipeThreshold = 100f // 滑动阈值（像素）
                                            when {
                                                dragOffset > swipeThreshold && currentIndex > 0 -> {
                                                    // 向右滑动，显示上一题
                                                    viewModel.previousQuestion()
                                                }
                                                dragOffset < -swipeThreshold && currentIndex < questions.size - 1 -> {
                                                    // 向左滑动，显示下一题
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
                // 题目内容（包含题型标签）
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
                }                                // 选项列表（练习模式需要用户选择）
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
                                if (showAnswer) {
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
                text = { Text("确定要退出顺序练习吗?学习进度已自动保存。") },
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
                onDismissRequest = { showQuestionListSheet = false },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ) {
                PracticeQuestionListSheet(
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
 * 题目内容卡片（练习模式）
 */
@Composable
fun PracticeQuestionContentCard(
    question: String,
    currentIndex: Int,
    totalCount: Int,
    questionType: String,
    isLearned: Boolean,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // 题号和题型标签行
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 题号（根据学习状态显示不同颜色）
                    Text(
                        text = "第 $currentIndex 题",
                        style = MaterialTheme.typography.labelLarge,
                        color = if (isLearned) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.outline
                        },
                        fontWeight = FontWeight.Bold
                    )
                    
                    // 题型标签
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text(
                            text = questionType,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
                
                // 收藏按钮
                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Star else Icons.Outlined.Star,
                        contentDescription = if (isFavorite) "取消收藏" else "收藏",
                        tint = if (isFavorite) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 题目内容
            Text(
                text = question,
                style = MaterialTheme.typography.bodyLarge,
                lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.5f
            )
        }
    }
}

/**
 * 练习模式选项卡片（可选择）
 */
@Composable
fun PracticeOptionsCard(
    options: List<Pair<String, String>>,
    correctAnswers: List<String>,
    selectedAnswers: List<String>,
    showAnswer: Boolean,
    isSingleChoice: Boolean,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
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
                
                // 题型提示
                Text(
                    text = if (isSingleChoice) "单选" else "多选",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            options.forEach { (key, value) ->
                val isSelected = selectedAnswers.contains(key)
                val isCorrect = correctAnswers.contains(key)
                val isWrong = showAnswer && isSelected && !isCorrect
                
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = when {
                        showAnswer && isCorrect -> MaterialTheme.colorScheme.primaryContainer
                        showAnswer && isWrong -> MaterialTheme.colorScheme.errorContainer
                        isSelected -> MaterialTheme.colorScheme.secondaryContainer
                        else -> MaterialTheme.colorScheme.surface
                    },
                    border = androidx.compose.foundation.BorderStroke(
                        width = if (isSelected || (showAnswer && isCorrect)) 2.dp else 1.dp,
                        color = when {
                            showAnswer && isCorrect -> MaterialTheme.colorScheme.primary
                            showAnswer && isWrong -> MaterialTheme.colorScheme.error
                            isSelected -> MaterialTheme.colorScheme.secondary
                            else -> MaterialTheme.colorScheme.outline
                        }
                    ),
                    onClick = {
                        if (!showAnswer) {
                            onOptionSelected(key)
                        }
                    }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // 选项标签
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(
                                    color = when {
                                        showAnswer && isCorrect -> MaterialTheme.colorScheme.primary
                                        showAnswer && isWrong -> MaterialTheme.colorScheme.error
                                        isSelected -> MaterialTheme.colorScheme.secondary
                                        else -> MaterialTheme.colorScheme.surfaceVariant
                                    },
                                    shape = RoundedCornerShape(16.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = key,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = when {
                                    showAnswer && (isCorrect || isWrong) -> MaterialTheme.colorScheme.onPrimary
                                    isSelected -> MaterialTheme.colorScheme.onSecondary
                                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }
                        
                        // 选项内容
                        Text(
                            text = value,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                        
                        // 状态图标
                        when {
                            showAnswer && isCorrect -> {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "正确",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            showAnswer && isWrong -> {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "错误",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                            isSelected && !showAnswer -> {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "已选择",
                                    tint = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * 答题结果卡片
 */
@Composable
fun AnswerResultCard(
    isCorrect: Boolean,
    correctAnswers: List<String>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isCorrect) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.errorContainer
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = if (isCorrect) Icons.Default.CheckCircle else Icons.Default.Close,
                contentDescription = null,
                tint = if (isCorrect) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.error
                },
                modifier = Modifier.size(32.dp)
            )
            
            Column {
                Text(
                    text = if (isCorrect) "回答正确！" else "回答错误",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isCorrect) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onErrorContainer
                    }
                )
                
                Text(
                    text = "正确答案：${correctAnswers.joinToString("、")}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isCorrect) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onErrorContainer
                    }
                )
            }
        }
    }
}

/**
 * 练习模式底部操作栏
 */
@Composable
fun PracticeBottomActionBar(
    currentIndex: Int,
    totalCount: Int,
    showAnswer: Boolean,
    hasSelectedAnswer: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        tonalElevation = 3.dp,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 上一题按钮
            OutlinedButton(
                onClick = onPrevious,
                modifier = Modifier.weight(1f),
                enabled = currentIndex > 0
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("上一题")
            }
            
            // 提交/下一题按钮
            Button(
                onClick = {
                    if (showAnswer) {
                        onNext()
                    } else {
                        onSubmit()
                    }
                },
                modifier = Modifier.weight(1.2f),
                enabled = if (showAnswer) {
                    currentIndex < totalCount - 1
                } else {
                    hasSelectedAnswer
                }
            ) {
                if (showAnswer) {
                    Text("下一题")
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("提交答案")
                }
            }
        }
    }
}

/**
 * 空状态视图（练习模式）
 */
@Composable
fun PracticeEmptyStateView(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Info,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.outline
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(onClick = onRetry) {
            Text("重试")
        }
    }
}

/**
 * 题目列表底部表（练习模式）
 */
@Composable
fun PracticeQuestionListSheet(
    questions: List<QuestionWithRecord>,
    currentIndex: Int,
    onQuestionSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
    ) {
        // 标题
        Text(
            text = "选择题目",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
        )
        
        HorizontalDivider()
        
        // 题目列表
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(questions.size) { index ->
                val questionWithRecord = questions[index]
                val isCurrentQuestion = index == currentIndex
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isCurrentQuestion) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surface
                        }
                    ),
                    onClick = { onQuestionSelected(index) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // 题号
                                Text(
                                    text = "${index + 1}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isCurrentQuestion) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    }
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            // 题目内容预览（截取前30个字符）
                            Text(
                                text = questionWithRecord.question.question.take(30) + 
                                    (if (questionWithRecord.question.question.length > 30) "..." else ""),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                        }
                        
                        // 状态指示器
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 已学习标记
                            if (questionWithRecord.isLearned) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "已学习",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            
                            // 收藏标记
                            if (questionWithRecord.isFavorite) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "已收藏",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            
                            // 当前题目指示
                            if (isCurrentQuestion) {
                                Icon(
                                    imageVector = Icons.Default.ArrowForward,
                                    contentDescription = "当前题目",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * 获取等级名称
 */
private fun getLevelName(level: String): String {
    return when (level) {
        "A" -> "A类（初级）"
        "B" -> "B类（中级）"
        "C" -> "C类（高级）"
        else -> level
    }
}
