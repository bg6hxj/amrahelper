package bg6hxj.amatureradiohelper.ui.screen

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import bg6hxj.amatureradiohelper.data.database.AppDatabase
import bg6hxj.amatureradiohelper.data.entity.ContactLog
import bg6hxj.amatureradiohelper.data.repository.ContactLogRepository
import bg6hxj.amatureradiohelper.ui.viewmodel.ContactLogViewModel
import bg6hxj.amatureradiohelper.ui.viewmodel.ContactLogViewModelFactory
import bg6hxj.amatureradiohelper.util.ContactLogExporter
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactLogListScreen(
    onNavigateBack: () -> Unit,
    onAddLogClick: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    
    val database = remember { AppDatabase.getDatabase(context) }
    val repository = remember { ContactLogRepository(database.contactLogDao()) }
    val viewModel: ContactLogViewModel = viewModel(
        factory = ContactLogViewModelFactory(repository)
    )

    val logs by viewModel.contactLogs.collectAsState()
    val logCount by viewModel.logCount.collectAsState()
    
    // 对话框状态
    var selectedLog by remember { mutableStateOf<ContactLog?>(null) }
    var logToDelete by remember { mutableStateOf<ContactLog?>(null) }
    
    // 菜单状态
    var showMenu by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    
    // 文件选择器 - 导出 JSON
    val exportJsonLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        uri?.let {
            val result = ContactLogExporter.exportToJson(context, it, logs)
            scope.launch {
                result.fold(
                    onSuccess = { count -> snackbarHostState.showSnackbar("成功导出 $count 条日志 (JSON)") },
                    onFailure = { e -> snackbarHostState.showSnackbar("导出失败: ${e.message}") }
                )
            }
        }
    }
    
    // 文件选择器 - 导出 CSV
    val exportCsvLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri: Uri? ->
        uri?.let {
            val result = ContactLogExporter.exportToCsv(context, it, logs)
            scope.launch {
                result.fold(
                    onSuccess = { count -> snackbarHostState.showSnackbar("成功导出 $count 条日志 (CSV)") },
                    onFailure = { e -> snackbarHostState.showSnackbar("导出失败: ${e.message}") }
                )
            }
        }
    }
    
    // 文件选择器 - 导入 JSON
    val importJsonLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            val result = ContactLogExporter.importFromJson(context, it)
            result.fold(
                onSuccess = { importedLogs ->
                    viewModel.importLogs(importedLogs) { count ->
                        scope.launch {
                            snackbarHostState.showSnackbar("成功导入 $count 条日志 (JSON)")
                        }
                    }
                },
                onFailure = { e ->
                    scope.launch {
                        snackbarHostState.showSnackbar("导入失败: ${e.message}")
                    }
                }
            )
        }
    }
    
    // 文件选择器 - 导入 CSV
    val importCsvLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            val result = ContactLogExporter.importFromCsv(context, it)
            result.fold(
                onSuccess = { importedLogs ->
                    viewModel.importLogs(importedLogs) { count ->
                        scope.launch {
                            snackbarHostState.showSnackbar("成功导入 $count 条日志 (CSV)")
                        }
                    }
                },
                onFailure = { e ->
                    scope.launch {
                        snackbarHostState.showSnackbar("导入失败: ${e.message}")
                    }
                }
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("通联日志") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "更多选项")
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("导入日志") },
                                onClick = {
                                    showMenu = false
                                    showImportDialog = true
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("导出日志") },
                                onClick = {
                                    showMenu = false
                                    showExportDialog = true
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddLogClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "新建日志")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            // Real-time Clock Banner
            CurrentTimeBanner()

            // Summary
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Info, 
                    contentDescription = null, 
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "总通联记录: $logCount",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Log List
            if (logs.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "暂无通联记录，点击 + 新建",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(logs) { log ->
                        ContactLogItem(
                            log = log,
                            onClick = { selectedLog = log },
                            onDelete = { logToDelete = log }
                        )
                    }
                }
            }
        }
    }

    // 详情对话框
    selectedLog?.let { log ->
        ContactLogDetailDialog(
            log = log,
            onDismiss = { selectedLog = null }
        )
    }

    // 删除确认对话框
    logToDelete?.let { log ->
        AlertDialog(
            onDismissRequest = { logToDelete = null },
            title = { Text("确认删除") },
            text = { Text("确定要删除与 ${log.theirCallsign} 的这条通联记录吗？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteLog(log)
                        logToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("删除")
                }
            },
            dismissButton = {
                TextButton(onClick = { logToDelete = null }) {
                    Text("取消")
                }
            }
        )
    }

    // 导入格式选择对话框
    if (showImportDialog) {
        FormatSelectionDialog(
            title = "选择导入格式",
            onDismiss = { showImportDialog = false },
            onJsonSelected = {
                showImportDialog = false
                importJsonLauncher.launch(arrayOf("application/json"))
            },
            onCsvSelected = {
                showImportDialog = false
                importCsvLauncher.launch(arrayOf("text/csv", "text/comma-separated-values", "*/*"))
            }
        )
    }

    // 导出格式选择对话框
    if (showExportDialog) {
        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
        FormatSelectionDialog(
            title = "选择导出格式",
            onDismiss = { showExportDialog = false },
            onJsonSelected = {
                showExportDialog = false
                exportJsonLauncher.launch("contact_logs_$timestamp.json")
            },
            onCsvSelected = {
                showExportDialog = false
                exportCsvLauncher.launch("contact_logs_$timestamp.csv")
            }
        )
    }
}

/**
 * 格式选择对话框
 */
@Composable
fun FormatSelectionDialog(
    title: String,
    onDismiss: () -> Unit,
    onJsonSelected: () -> Unit,
    onCsvSelected: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "请选择文件格式:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(onClick = onCsvSelected) {
                    Text("CSV 格式")
                }
                Button(onClick = onJsonSelected) {
                    Text("JSON 格式")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@Composable
fun CurrentTimeBanner() {
    var currentTime by remember { mutableStateOf(System.currentTimeMillis()) }
    
    LaunchedEffect(Unit) {
        while (true) {
            currentTime = System.currentTimeMillis()
            delay(1000)
        }
    }

    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        .withZone(ZoneId.of("UTC"))
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "UTC 时间",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
            )
            Text(
                text = formatter.format(Instant.ofEpochMilli(currentTime)),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
fun ContactLogItem(log: ContactLog, onClick: () -> Unit, onDelete: () -> Unit) {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        .withZone(ZoneId.of("UTC"))

    ElevatedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = log.mode.take(2).uppercase(),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = log.theirCallsign,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${log.frequency} MHz",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete, 
                        contentDescription = "删除",
                        tint = MaterialTheme.colorScheme.outline
                    )
                }
            }
            
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "时间 (UTC)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        text = formatter.format(Instant.ofEpochMilli(log.contactTime)),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "信号报告 (S/R)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        text = "${log.rstSent} / ${log.rstReceived}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            
            if (!log.notes.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "备注: ${log.notes}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }
    }
}

/**
 * 通联日志详情对话框
 */
@Composable
fun ContactLogDetailDialog(
    log: ContactLog,
    onDismiss: () -> Unit
) {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss 'UTC'")
        .withZone(ZoneId.of("UTC"))

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "通联详情",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = log.theirCallsign,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                DialogDetailItem("通联时间", formatter.format(Instant.ofEpochMilli(log.contactTime)))
                DialogDetailItem("通联模式", log.mode)
                DialogDetailItem("频率", "${log.frequency} MHz")
                DialogDetailItem("我的呼号", log.myCallsign)
                DialogDetailItem("对方呼号", log.theirCallsign)
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                
                DialogDetailItem("发送信号 (RST)", log.rstSent)
                DialogDetailItem("接收信号 (RST)", log.rstReceived)
                
                log.cqZone?.let { if(it.isNotEmpty()) DialogDetailItem("CQ 分区", it) }
                log.myPower?.let { if(it.isNotEmpty()) DialogDetailItem("发射功率", it) }
                log.theirPower?.let { if(it.isNotEmpty()) DialogDetailItem("对方功率", it) }
                log.theirQth?.let { if(it.isNotEmpty()) DialogDetailItem("对方位置 (QTH)", it) }
                log.equipment?.let { if(it.isNotEmpty()) DialogDetailItem("设备", it) }
                log.antenna?.let { if(it.isNotEmpty()) DialogDetailItem("天线", it) }
                
                if (!log.notes.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "备注:",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = log.notes,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("关闭")
            }
        }
    )
}

@Composable
private fun DialogDetailItem(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}
