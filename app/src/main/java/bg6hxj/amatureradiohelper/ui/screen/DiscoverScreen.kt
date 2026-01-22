package bg6hxj.amatureradiohelper.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import bg6hxj.amatureradiohelper.data.database.AppDatabase
import bg6hxj.amatureradiohelper.data.repository.ContactLogRepository
import bg6hxj.amatureradiohelper.ui.viewmodel.ContactLogViewModel
import bg6hxj.amatureradiohelper.ui.viewmodel.ContactLogViewModelFactory

/**
 * 发现模块主页面
 * 包含无线电传播质量预测、基础知识速查、通联日志记录
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscoverScreen(
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val database = remember { AppDatabase.getDatabase(context) }
    val repository = remember { ContactLogRepository(database.contactLogDao()) }
    val viewModel: ContactLogViewModel = viewModel(
        factory = ContactLogViewModelFactory(repository)
    )

    val logCount by viewModel.logCount.collectAsState()
    val monthLogCount by viewModel.monthLogCount.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        // 标题
        item {
            Text(
                text = "发现",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }

        // 无线电传播质量预测
        item {
            PropagationPredictionCard(
                onClick = { onNavigate("propagation_prediction") }
            )
        }

        // 基础知识速查
        item {
            Text(
                text = "基础知识速查",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        item {
            BasicKnowledgeSection(onNavigate = onNavigate)
        }

        // 通联日志记录
        item {
            Text(
                text = "通联日志",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        item {
            ContactLogCard(
                totalCount = logCount,
                monthCount = monthLogCount,
                onClick = { onNavigate("contact_log_list") }
            )
        }
    }
}

/**
 * 无线电传播质量预测卡片
 */
@Composable
fun PropagationPredictionCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
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
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Column {
                    Text(
                        text = "无线电传播质量预测",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "查看最新的传播条件预测图",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = "查看",
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

/**
 * 基础知识速查区域
 */
@Composable
fun BasicKnowledgeSection(
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            KnowledgeCard(
                title = "字母解释法",
                icon = Icons.Default.Settings,
                onClick = { onNavigate("reference_detail/phonetic_alphabet") },
                modifier = Modifier.weight(1f)
            )
            KnowledgeCard(
                title = "全球呼号前缀",
                icon = Icons.Default.Phone,
                onClick = { onNavigate("reference_detail/callsign_prefix") },
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            KnowledgeCard(
                title = "ITU及CQ分区",
                icon = Icons.Default.LocationOn,
                onClick = { onNavigate("reference_detail/itu_cq_zones") },
                modifier = Modifier.weight(1f)
            )
            KnowledgeCard(
                title = "国内电台分区",
                icon = Icons.Default.Place,
                onClick = { onNavigate("reference_detail/cn_zones") },
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            KnowledgeCard(
                title = "常用Q简语",
                icon = Icons.Default.Email,
                onClick = { onNavigate("reference_detail/q_codes") },
                modifier = Modifier.weight(1f)
            )
            KnowledgeCard(
                title = "常用缩略语",
                icon = Icons.Default.MailOutline,
                onClick = { onNavigate("reference_detail/abbreviations") },
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            KnowledgeCard(
                title = "CW电码表",
                icon = Icons.Default.Build,
                onClick = { onNavigate("reference_detail/cw_codes") },
                modifier = Modifier.weight(1f)
            )
            KnowledgeCard(
                title = "频率对照表",
                icon = Icons.Default.List,
                onClick = { onNavigate("reference_detail/frequency_chart") },
                modifier = Modifier.weight(1f)
            )
        }

        KnowledgeCard(
            title = "波长计算器",
            icon = Icons.Default.Settings,
            onClick = { onNavigate("wavelength_calculator") },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/**
 * 知识卡片（紧凑型）
 */
@Composable
fun KnowledgeCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/**
 * 通联日志卡片
 */
@Composable
fun ContactLogCard(
    totalCount: Int,
    monthCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
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
                    imageVector = Icons.Default.List,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.secondary
                )
                Column {
                    Text(
                        text = "通联日志记录",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "记录和管理您的通联日志",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "总计: $totalCount 条",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            text = "本月: $monthCount 条",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = "查看",
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}
