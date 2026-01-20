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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
 * 顺序背题页面
 * 按顺序展示题目和答案，用户可标记"关注"
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SequentialReviewScreen(
    level: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tag = "SequentialReviewScreen"
    Log.d(tag, "=== SequentialReviewScreen 启动 ===")
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
    
    // 初始化时选择等级并开始顺序背题
    LaunchedEffect(level) {
        viewModel.selectLevel(level)
        viewModel.startSequentialReview()
    }
    
    val currentQuestion by viewModel.currentQuestion.collectAsState()
    val currentIndex by viewModel.currentQuestionIndex.collectAsState()
    val questions by viewModel.questions.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val stats by viewModel.questionBankStats.collectAsState()
    
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
                            text = "顺序背题 - ${getLevelName(level)}",
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
                    EmptyStateView(
                        message = "没有找到题目",
                        onRetry = { viewModel.startSequentialReview() }
                    )
                }
                
                else -> {
                    // 题目内容
                    currentQuestion?.let { questionWithRecord ->
                        // 滑动手势状态
                        var dragOffset by remember { mutableFloatStateOf(0f) }
                        
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
                                    QuestionContentCard(
                                        question = questionWithRecord.question.question,
                                        currentIndex = currentIndex + 1,
                                        totalCount = questions.size,
                                        questionType = if (questionWithRecord.question.isSingleChoice()) "单选题" else "多选题",
                                        isLearned = questionWithRecord.isLearned,
                                        isFavorite = questionWithRecord.isFavorite,
                                        onToggleFavorite = { viewModel.toggleFavorite() }
                                    )
                                }
                                
                                // 选项列表（背题模式直接显示答案）
                                item {
                                    OptionsCard(
                                        options = questionWithRecord.question.parseOptions(),
                                        correctAnswers = questionWithRecord.question.getCorrectAnswers(),
                                        showAnswer = true // 背题模式始终显示答案
                                    )
                                }
                            }
                            
                            // 底部操作栏
                            BottomActionBar(
                                currentIndex = currentIndex,
                                totalCount = questions.size,
                                isMastered = questionWithRecord.isMastered,
                                onPrevious = { viewModel.previousQuestion() },
                                onNext = { viewModel.nextQuestion() },
                                onMarkMastered = { viewModel.markAsMastered() }
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
                title = { Text("退出学习") },
                text = { Text("确定要退出顺序背题吗?学习进度已自动保存。") },
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
                        Log.d(tag, ">>> 取消退出,继续学习")
                        showExitDialog = false 
                    }) {
                        Text("继续学习")
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
 * 题目信息卡片
 */
@Composable
fun QuestionInfoCard(
    questionCode: String,
    chapterCode: String?,
    questionType: String,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 题目编号
                AssistChip(
                    onClick = { },
                    label = { Text(questionCode) },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Phone,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )
                
                // 章节编号
                if (!chapterCode.isNullOrEmpty()) {
                    AssistChip(
                        onClick = { },
                        label = { Text(chapterCode) }
                    )
                }
                
                // 题目类型
                AssistChip(
                    onClick = { },
                    label = { Text(questionType) }
                )
            }
            
            // 收藏按钮
            IconButton(onClick = onToggleFavorite) {
                Icon(
                    imageVector = if (isFavorite) Icons.Default.Star else Icons.Default.Add,
                    contentDescription = if (isFavorite) "取消收藏" else "收藏",
                    tint = if (isFavorite) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * 题目内容卡片
 */
@Composable
fun QuestionContentCard(
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
 * 选项卡片
 */
@Composable
fun OptionsCard(
    options: List<Pair<String, String>>,
    correctAnswers: List<String>,
    showAnswer: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "选项",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            options.forEach { (key, value) ->
                val isCorrect = correctAnswers.contains(key)
                
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = when {
                        showAnswer && isCorrect -> MaterialTheme.colorScheme.primaryContainer
                        else -> MaterialTheme.colorScheme.surface
                    },
                    border = when {
                        showAnswer && isCorrect -> androidx.compose.foundation.BorderStroke(
                            2.dp,
                            MaterialTheme.colorScheme.primary
                        )
                        else -> androidx.compose.foundation.BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.outline
                        )
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
                                    color = if (showAnswer && isCorrect) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.surfaceVariant
                                    },
                                    shape = RoundedCornerShape(16.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = key,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (showAnswer && isCorrect) {
                                    MaterialTheme.colorScheme.onPrimary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }
                        
                        // 选项内容
                        Text(
                            text = value,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                        
                        // 正确答案标识
                        if (showAnswer && isCorrect) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "正确答案",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
            
            // 正确答案提示
//            if (showAnswer) {
//                Spacer(modifier = Modifier.height(4.dp))
//                Text(
//                    text = "正确答案：${correctAnswers.joinToString("、")}",
//                    style = MaterialTheme.typography.bodyMedium,
//                    color = MaterialTheme.colorScheme.primary,
//                    fontWeight = FontWeight.Bold
//                )
//            }
        }
    }
}

/**
 * 学习状态卡片
 */
@Composable
fun StudyStatusCard(
    isLearned: Boolean,
    isMastered: Boolean,
    wrongCount: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 学习状态
            StatusChip(
                icon = Icons.Default.CheckCircle,
                label = if (isLearned) "已学习" else "未学习",
                color = if (isLearned) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.outline
                }
            )
            
            // 关注状态
            if (isMastered) {
                StatusChip(
                    icon = Icons.Default.Star,
                    label = "关注",
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
            
            // 答错次数
            if (wrongCount > 0) {
                StatusChip(
                    icon = Icons.Default.Close,
                    label = "答错 $wrongCount 次",
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

/**
 * 状态标签
 */
@Composable
fun StatusChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    AssistChip(
        onClick = { },
        label = { Text(label) },
        leadingIcon = {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = color
            )
        },
        modifier = modifier
    )
}

/**
 * 底部操作栏
 */
@Composable
fun BottomActionBar(
    currentIndex: Int,
    totalCount: Int,
    isMastered: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onMarkMastered: () -> Unit,
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
            
            // 关注按钮（中间）
            Button(
                onClick = onMarkMastered,
                modifier = Modifier.weight(1.2f),
                colors = if (isMastered) {
                    ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiary
                    )
                } else {
                    ButtonDefaults.buttonColors()
                }
            ) {
                Icon(
                    imageVector = if (isMastered) Icons.Default.Check else Icons.Default.Star,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(if (isMastered) "已关注" else "关注")
            }
            
            // 下一题按钮
            Button(
                onClick = onNext,
                modifier = Modifier.weight(1f),
                enabled = currentIndex < totalCount - 1
            ) {
                Text("下一题")
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

/**
 * 空状态视图
 */
@Composable
fun EmptyStateView(
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
 * 题目列表底部表
 */
@Composable
fun QuestionListSheet(
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
                                
//                                // 题目编号
//                                Text(
//                                    text = questionWithRecord.question.j_code,
//                                    style = MaterialTheme.typography.bodyMedium,
//                                    color = MaterialTheme.colorScheme.onSurfaceVariant
//                                )
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
                            // 已关注标记
                            if (questionWithRecord.isMastered) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "已关注",
                                    tint = MaterialTheme.colorScheme.tertiary,
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
