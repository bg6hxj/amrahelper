package bg6hxj.amatureradiohelper.ui.screen

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

/**
 * 考试模块主页面
 * 包含题库选择、学习模式、学习记录
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamScreen(
    onStartSequentialReview: (String) -> Unit = {},
    onStartSequentialPractice: (String) -> Unit = {},
    onStartRandomPractice: (String) -> Unit = {},
    onStartMockExam: (String) -> Unit = {},
    onShowLearnedQuestions: (String) -> Unit = {},
    onShowUnlearnedQuestions: (String) -> Unit = {},
    onShowWrongQuestions: (String) -> Unit = {},
    onShowFavoriteQuestions: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    // 使用 remember 创建 Repository 和 ViewModel
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
    
    val selectedLevel by viewModel.selectedLevel.collectAsState()
    val selectedBank = remember(selectedLevel) {
        QuestionBank.entries.find { it.label == selectedLevel } ?: QuestionBank.A
    }

    // 监听生命周期，当页面可见时刷新数据(如从练习页返回时)
    val lifecycleOwner = androidx.compose.ui.platform.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                viewModel.loadQuestionBankStats()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    
    val questionBankStats by viewModel.questionBankStats.collectAsState()

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // 顶部题库选择 Banner
        QuestionBankSelector(
            selectedBank = selectedBank,
            onBankSelected = { viewModel.selectLevel(it.label) }
        )

        // 主要内容区域
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // 学习模式卡片
            item {
                Text(
                    text = "学习模式",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            
            item {
                StudyModeSection(
                    selectedBank = selectedBank,
                    onStartSequentialReview = onStartSequentialReview,
                    onStartSequentialPractice = onStartSequentialPractice,
                    onStartRandomPractice = onStartRandomPractice,
                    onStartMockExam = onStartMockExam
                )
            }

            // 学习记录卡片
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "学习记录",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            
            item {
                StudyRecordSection(
                    selectedBank = selectedBank,
                    stats = questionBankStats,
                    onShowLearnedQuestions = onShowLearnedQuestions,
                    onShowUnlearnedQuestions = onShowUnlearnedQuestions,
                    onShowWrongQuestions = onShowWrongQuestions,
                    onShowFavoriteQuestions = onShowFavoriteQuestions
                )
            }
        }
    }
}

/**
 * 题库选择器 - 顶部 Banner
 */
@Composable
fun QuestionBankSelector(
    selectedBank: QuestionBank,
    onBankSelected: (QuestionBank) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "选择题库",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                QuestionBank.entries.forEach { bank ->
                    FilterChip(
                        selected = selectedBank == bank,
                        onClick = { onBankSelected(bank) },
                        label = { 
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "${bank.label}类",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = "${bank.totalQuestions}题",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        },
                        leadingIcon = if (selectedBank == bank) {
                            { Icon(Icons.Default.Check, contentDescription = null) }
                        } else null
                    )
                }
            }
//
            Spacer(modifier = Modifier.height(8.dp))
//
//            Text(
//                text = selectedBank.description,
//                style = MaterialTheme.typography.bodyMedium,
//                color = MaterialTheme.colorScheme.onPrimaryContainer
//            )
        }
    }
}

/**
 * 学习模式区域
 */
@Composable
fun StudyModeSection(
    selectedBank: QuestionBank,
    onStartSequentialReview: (String) -> Unit,
    onStartSequentialPractice: (String) -> Unit,
    onStartRandomPractice: (String) -> Unit,
    onStartMockExam: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StudyModeCard(
            title = "顺序背题",
            description = "按顺序浏览题目和答案，标记已掌握",
            icon = Icons.Default.List,
            onClick = { onStartSequentialReview(selectedBank.label) }
        )
        
        StudyModeCard(
            title = "顺序练习",
            description = "按顺序作答题目，系统判断对错",
            icon = Icons.Default.Edit,
            onClick = { onStartSequentialPractice(selectedBank.label) }
        )
        
        StudyModeCard(
            title = "随机练习",
            description = "随机打乱题目顺序进行练习",
            icon = Icons.Default.Refresh,
            onClick = { onStartRandomPractice(selectedBank.label) }
        )
        
        StudyModeCard(
            title = "模拟考试",
            description = "抽取${selectedBank.examQuestionCount}题，限时${selectedBank.examTimeLimit}分钟",
            icon = Icons.Default.Create,
            onClick = { onStartMockExam(selectedBank.label) },
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer
            )
        )
    }
}

/**
 * 学习记录区域
 */
@Composable
fun StudyRecordSection(
    selectedBank: QuestionBank,
    stats: bg6hxj.amatureradiohelper.data.model.QuestionBankStats?,
    onShowLearnedQuestions: (String) -> Unit = {},
    onShowUnlearnedQuestions: (String) -> Unit = {},
    onShowWrongQuestions: (String) -> Unit = {},
    onShowFavoriteQuestions: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StudyRecordCard(
            title = "已学题目",
            count = stats?.learnedCount ?: 0,
            total = selectedBank.totalQuestions,
            icon = Icons.Default.CheckCircle,
            iconTint = MaterialTheme.colorScheme.primary,
            onClick = { onShowLearnedQuestions(selectedBank.label) }
        )
        
        StudyRecordCard(
            title = "未学题目",
            count = stats?.unlearnedCount ?: selectedBank.totalQuestions,
            total = selectedBank.totalQuestions,
            icon = Icons.Default.Add,
            iconTint = MaterialTheme.colorScheme.outline,
            onClick = { onShowUnlearnedQuestions(selectedBank.label) }
        )
        
        StudyRecordCard(
            title = "我答错的题",
            count = stats?.wrongCount ?: 0,
            total = selectedBank.totalQuestions,
            icon = Icons.Default.Close,
            iconTint = MaterialTheme.colorScheme.error,
            onClick = { onShowWrongQuestions(selectedBank.label) }
        )
        
        StudyRecordCard(
            title = "我关注的题",
            count = stats?.favoriteCount ?: 0,
            total = selectedBank.totalQuestions,
            icon = Icons.Default.Star,
            iconTint = MaterialTheme.colorScheme.tertiary,
            onClick = { onShowFavoriteQuestions(selectedBank.label) }
        )
    }
}

/**
 * 学习模式卡片
 */
@Composable
fun StudyModeCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    colors: CardColors = CardDefaults.cardColors()
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = colors
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = "进入",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * 学习记录卡片
 */
@Composable
fun StudyRecordCard(
    title: String,
    count: Int,
    total: Int,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = iconTint
                )
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "$count / $total 题",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = "查看",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * 题库枚举类
 */
enum class QuestionBank(
    val label: String,
    val totalQuestions: Int,
    val examQuestionCount: Int,
    val examTimeLimit: Int,
    val description: String
) {
    A(
        label = "A",
        totalQuestions = 683,
        examQuestionCount = 40,
        examTimeLimit = 30,
        description = "操作证书（初级） - 683题"
    ),
    B(
        label = "B",
        totalQuestions = 1143,
        examQuestionCount = 60,
        examTimeLimit = 50,
        description = "操作证书（中级） - 1143题"
    ),
    C(
        label = "C",
        totalQuestions = 1282,
        examQuestionCount = 90,
        examTimeLimit = 80,
        description = "操作证书（高级） - 1282题"
    )
}
