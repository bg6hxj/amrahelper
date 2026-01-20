package bg6hxj.amatureradiohelper.ui.screen

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import bg6hxj.amatureradiohelper.data.repository.QuestionRepository
import bg6hxj.amatureradiohelper.data.repository.ExamRepository
import bg6hxj.amatureradiohelper.ui.viewmodel.ExamViewModel
import bg6hxj.amatureradiohelper.ui.viewmodel.ExamViewModelFactory

/**
 * 未学题目列表页面
 * 显示用户还未学习的所有题目,可以点击进入学习
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnlearnedQuestionsScreen(
    level: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tag = "UnlearnedQuestionsScreen"
    Log.d(tag, "=== UnlearnedQuestionsScreen 启动 ===")
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
    
    // 初始化时选择等级并加载未学题目
    LaunchedEffect(level) {
        viewModel.selectLevel(level)
        viewModel.loadUnlearnedQuestions()
    }
    
    val questions by viewModel.questions.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    
    // UI 状态
    var showExitDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedQuestionIndex by remember { mutableStateOf<Int?>(null) }
    
    // 搜索筛选后的题目列表
    val filteredQuestions = remember(questions, searchQuery) {
        if (searchQuery.isEmpty()) {
            questions
        } else {
            questions.filter { qwr ->
                qwr.question.question.contains(searchQuery, ignoreCase = true) ||
                qwr.question.j_code.contains(searchQuery, ignoreCase = true)
            }
        }
    }
    
    // 拦截系统返回事件
    BackHandler {
        showExitDialog = true
    }
    
    // 退出确认对话框
    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text("退出确认") },
            text = { Text("确定要退出未学题目吗?") },
            confirmButton = {
                TextButton(onClick = onNavigateBack) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
    
    // 题目详情对话框
    selectedQuestionIndex?.let { index ->
        val qwr = filteredQuestions[index]
        UnlearnedQuestionDetailDialog(
            questionNumber = index + 1,
            questionWithRecord = qwr,
            onDismiss = { selectedQuestionIndex = null },
            viewModel = viewModel
        )
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("未学题目 (${level}类)") },
                navigationIcon = {
                    IconButton(onClick = { showExitDialog = true }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 搜索框
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("搜索题目或题号...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "搜索") },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "清除")
                        }
                    }
                },
                singleLine = true
            )
            
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (filteredQuestions.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = if (questions.isEmpty()) "恭喜!已完成所有题目学习" else "未找到匹配的题目",
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (questions.isEmpty()) 
                            MaterialTheme.colorScheme.primary 
                        else 
                            MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = if (questions.isEmpty()) FontWeight.Bold else FontWeight.Normal
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (questions.isEmpty()) "继续复习巩固吧" else "试试调整搜索条件",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(
                            text = "还有 ${filteredQuestions.size} 题未学",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    items(filteredQuestions.size) { index ->
                        val qwr = filteredQuestions[index]
                        UnlearnedQuestionCard(
                            questionNumber = index + 1,
                            questionWithRecord = qwr,
                            onClick = {
                                viewModel.jumpToQuestion(
                                    questions.indexOf(qwr)
                                )
                                selectedQuestionIndex = index
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * 未学题目卡片
 */
@Composable
private fun UnlearnedQuestionCard(
    questionNumber: Int,
    questionWithRecord: bg6hxj.amatureradiohelper.data.model.QuestionWithRecord,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "第 $questionNumber 题",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = questionWithRecord.question.question,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "题型: ${if (questionWithRecord.question.isSingleChoice()) "单选题" else "多选题"}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * 未学题目详情对话框
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UnlearnedQuestionDetailDialog(
    questionNumber: Int,
    questionWithRecord: bg6hxj.amatureradiohelper.data.model.QuestionWithRecord,
    onDismiss: () -> Unit,
    viewModel: ExamViewModel
) {
    val question = questionWithRecord.question
    val options = question.parseOptions()
    val correctAnswers = question.getCorrectAnswers()
    
    AlertDialog(
        onDismissRequest = onDismiss
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // 标题栏
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "第 $questionNumber 题",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "未学习",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                        
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
                    
                    // 选项列表
                    item {
                        Text(
                            text = "选项",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    items(options.size) { index ->
                        val (key, value) = options[index]
                        val isCorrect = correctAnswers.contains(key)
                        
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                            color = if (isCorrect) 
                                MaterialTheme.colorScheme.primaryContainer 
                            else 
                                MaterialTheme.colorScheme.surface,
                            border = androidx.compose.foundation.BorderStroke(
                                width = if (isCorrect) 2.dp else 1.dp,
                                color = if (isCorrect) 
                                    MaterialTheme.colorScheme.primary 
                                else 
                                    MaterialTheme.colorScheme.outline
                            )
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
                                            color = if (isCorrect) 
                                                MaterialTheme.colorScheme.primary 
                                            else 
                                                MaterialTheme.colorScheme.surfaceVariant,
                                            shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = key,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isCorrect) 
                                            MaterialTheme.colorScheme.onPrimary 
                                        else 
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                
                                // 选项内容
                                Text(
                                    text = value,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.weight(1f)
                                )
                                
                                // 正确答案标记
                                if (isCorrect) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "正确答案",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                    
                    // 答案信息
                    item {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "正确答案: ${correctAnswers.sorted().joinToString(", ")}",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                Text(
                                    text = "题型: ${if (question.isSingleChoice()) "单选题" else "多选题"}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
