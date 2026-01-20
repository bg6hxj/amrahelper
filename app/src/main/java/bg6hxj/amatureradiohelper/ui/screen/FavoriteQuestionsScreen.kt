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
 * 关注题目列表屏幕
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoriteQuestionsScreen(
    level: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val database = remember { AppDatabase.getDatabase(context) }
    val questionRepository = remember { QuestionRepository(database.questionDao(), database.studyRecordDao()) }
    val examRepository = remember { ExamRepository(database.examRecordDao()) }
    
    val viewModel: ExamViewModel = viewModel(
        factory = ExamViewModelFactory(questionRepository, examRepository)
    )
    
    var showExitDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var sortOrder by remember { mutableStateOf(SortOrder.LATEST) }
    var showSortMenu by remember { mutableStateOf(false) }
    var selectedQuestionIndex by remember { mutableStateOf<Int?>(null) }
    
    val questions by viewModel.questions.collectAsState()
    
    // 加载收藏的题目
    LaunchedEffect(level) {
        viewModel.selectLevel(level)
        viewModel.loadFavoriteQuestions()
    }
    
    // 返回确认对话框
    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text("确认返回") },
            text = { Text("确定要返回吗?") },
            confirmButton = {
                TextButton(onClick = onBack) {
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
    
    // 监听系统返回键
    BackHandler { showExitDialog = true }
    
    // 搜索和排序后的题目列表
    val filteredAndSortedQuestions = remember(questions, searchQuery, sortOrder) {
        var result = questions
        
        // 搜索过滤
        if (searchQuery.isNotBlank()) {
            result = result.filter { qwr ->
                qwr.question.question.contains(searchQuery, ignoreCase = true) ||
                qwr.question.j_code.contains(searchQuery, ignoreCase = true)
            }
        }
        
        // 排序
        result = when (sortOrder) {
            SortOrder.LATEST -> result.sortedByDescending { it.record?.lastStudyTime ?: 0L }
            SortOrder.OLDEST -> result.sortedBy { it.record?.lastStudyTime ?: 0L }
            SortOrder.MOST_WRONG -> result.sortedByDescending { it.record?.wrongCount ?: 0 }
            SortOrder.LEAST_WRONG -> result.sortedBy { it.record?.wrongCount ?: 0 }
        }
        
        result
    }
    
    // 题目详情对话框
    selectedQuestionIndex?.let { index ->
        val qwr = filteredAndSortedQuestions[index]
        FavoriteQuestionDetailDialog(
            questionNumber = index + 1,
            questionWithRecord = qwr,
            onDismiss = { selectedQuestionIndex = null },
            onToggleFavorite = {
                viewModel.toggleFavorite(qwr.question.id!!)
                // 刷新列表
                viewModel.loadFavoriteQuestions()
            },
            viewModel = viewModel
        )
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("我关注的题 (${level}类)") },
                navigationIcon = {
                    IconButton(onClick = { showExitDialog = true }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    // 排序按钮
                    IconButton(onClick = { showSortMenu = true }) {
                        Icon(Icons.Default.ArrowForward, contentDescription = "排序")
                    }
                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("最近关注") },
                            onClick = {
                                sortOrder = SortOrder.LATEST
                                showSortMenu = false
                            },
                            trailingIcon = {
                                if (sortOrder == SortOrder.LATEST) {
                                    Icon(Icons.Default.Check, contentDescription = null)
                                }
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("最早关注") },
                            onClick = {
                                sortOrder = SortOrder.OLDEST
                                showSortMenu = false
                            },
                            trailingIcon = {
                                if (sortOrder == SortOrder.OLDEST) {
                                    Icon(Icons.Default.Check, contentDescription = null)
                                }
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("最多错误") },
                            onClick = {
                                sortOrder = SortOrder.MOST_WRONG
                                showSortMenu = false
                            },
                            trailingIcon = {
                                if (sortOrder == SortOrder.MOST_WRONG) {
                                    Icon(Icons.Default.Check, contentDescription = null)
                                }
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("最少错误") },
                            onClick = {
                                sortOrder = SortOrder.LEAST_WRONG
                                showSortMenu = false
                            },
                            trailingIcon = {
                                if (sortOrder == SortOrder.LEAST_WRONG) {
                                    Icon(Icons.Default.Check, contentDescription = null)
                                }
                            }
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 搜索框
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("搜索题目内容或编号...") },
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
            
            // 统计信息
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "共 ${filteredAndSortedQuestions.size} 道关注",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            // 题目列表
            if (filteredAndSortedQuestions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (searchQuery.isBlank()) "还没有关注的题目" else "没有找到匹配的题目",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredAndSortedQuestions.size) { index ->
                        val qwr = filteredAndSortedQuestions[index]
                        FavoriteQuestionCard(
                            questionNumber = index + 1,
                            questionWithRecord = qwr,
                            onClick = { selectedQuestionIndex = index },
                            onToggleFavorite = {
                                viewModel.toggleFavorite(qwr.question.id!!)
                                // 刷新列表
                                viewModel.loadFavoriteQuestions()
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * 关注题目卡片
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FavoriteQuestionCard(
    questionNumber: Int,
    questionWithRecord: bg6hxj.amatureradiohelper.data.model.QuestionWithRecord,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // 题号、编号和收藏按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "第 $questionNumber 题",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = questionWithRecord.question.j_code,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // 收藏按钮
                IconButton(
                    onClick = { onToggleFavorite() },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "取消关注",
                        tint = MaterialTheme.colorScheme.tertiary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 题目内容
            Text(
                text = questionWithRecord.question.question,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 学习状态
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 掌握状态
                if (questionWithRecord.isMastered) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "已掌握",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                // 答错次数
                if ((questionWithRecord.record?.wrongCount ?: 0) > 0) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "答错 ${questionWithRecord.record?.wrongCount ?: 0} 次",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

/**
 * 关注题目详情对话框
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FavoriteQuestionDetailDialog(
    questionNumber: Int,
    questionWithRecord: bg6hxj.amatureradiohelper.data.model.QuestionWithRecord,
    onDismiss: () -> Unit,
    onToggleFavorite: () -> Unit,
    viewModel: ExamViewModel
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxWidth()
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 600.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                // 标题栏
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "第 $questionNumber 题",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = questionWithRecord.question.j_code,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // 收藏按钮
                        IconButton(onClick = { onToggleFavorite() }) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "取消关注",
                                tint = MaterialTheme.colorScheme.tertiary
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
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 题目内容
                    item {
                        Text(
                            text = questionWithRecord.question.question,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    
                    // 选项
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            val options = questionWithRecord.question.parseOptions()
                            
                            val correctAnswers = questionWithRecord.question.getCorrectAnswers()
                            
                            options.forEach { (label, content) ->
                                val isCorrect = label in correctAnswers
                                
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    color = if (isCorrect) 
                                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                                    else 
                                        MaterialTheme.colorScheme.surface,
                                    shape = MaterialTheme.shapes.small,
                                    border = if (isCorrect) 
                                        androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                                    else 
                                        null
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        if (isCorrect) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                        }
                                        Text(
                                            text = "$label. $content",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = if (isCorrect) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isCorrect) 
                                                MaterialTheme.colorScheme.primary 
                                            else 
                                                MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }
                    
                    // 答案
                    item {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "正确答案: ${questionWithRecord.question.correct_answer}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }
                        }
                    }
                    
                    // 学习状态
                    if ((questionWithRecord.record?.wrongCount ?: 0) > 0) {
                        item {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onErrorContainer,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "已答错 ${questionWithRecord.record?.wrongCount ?: 0} 次",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                            }
                        }
                    }
                    
                    if (questionWithRecord.isMastered) {
                        item {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "已掌握",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
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
