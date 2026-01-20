package bg6hxj.amatureradiohelper.ui.screen

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import bg6hxj.amatureradiohelper.data.database.AppDatabase
import bg6hxj.amatureradiohelper.data.repository.ContactLogRepository
import bg6hxj.amatureradiohelper.data.UserPreferences
import bg6hxj.amatureradiohelper.ui.viewmodel.AddLogEvent
import bg6hxj.amatureradiohelper.ui.viewmodel.ContactLogViewModel
import bg6hxj.amatureradiohelper.ui.viewmodel.ContactLogViewModelFactory
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddContactLogScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val database = remember { AppDatabase.getDatabase(context) }
    val repository = remember { ContactLogRepository(database.contactLogDao()) }
    
    val viewModelKey = remember { "AddContactLog_${System.currentTimeMillis()}" }
    val viewModel: ContactLogViewModel = viewModel(
        key = viewModelKey,
        factory = ContactLogViewModelFactory(repository)
    )

    val uiState by viewModel.addLogUiState.collectAsState()
    
    val currentOnNavigateBack by rememberUpdatedState(onNavigateBack)
    
    // 读取用户设置的呼号，用于自动填充"本台呼号"字段
    val userPreferences = remember { UserPreferences(context) }
    val userCallsign by userPreferences.callsign.collectAsState(initial = null)
    
    // 仅在首次加载时自动填充用户呼号（保留用户修改权限）
    var hasAutoFilledCallsign by remember { mutableStateOf(false) }
    LaunchedEffect(userCallsign) {
        if (!hasAutoFilledCallsign && userCallsign != null && userCallsign != "未设置" && uiState.myCallsign.isEmpty()) {
            viewModel.updateForm(AddLogEvent.MyCallsignChanged(userCallsign!!))
            hasAutoFilledCallsign = true
        }
    }

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            currentOnNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("新建通联记录") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    TextButton(onClick = { viewModel.updateForm(AddLogEvent.Submit) }) {
                        Text("保存")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // 1. 基础信息卡片
            FormSectionCard(title = "基础信息") {
                // Time Selection
                TimeSelector(
                    timestamp = uiState.contactTime,
                    onTimeSelected = { viewModel.updateForm(AddLogEvent.ContactTimeChanged(it)) }
                )

                // Mode Selection
                ModeSelector(
                    selectedMode = uiState.mode,
                    onModeSelected = { viewModel.updateForm(AddLogEvent.ModeChanged(it)) }
                )

                // Frequency
                OutlinedTextField(
                    value = uiState.frequency,
                    onValueChange = { viewModel.updateForm(AddLogEvent.FrequencyChanged(it)) },
                    label = { Text("频率 (Frequency)") },
                    suffix = { Text("MHz") },
                    isError = uiState.errors.containsKey("frequency"),
                    supportingText = { uiState.errors["frequency"]?.let { Text(it) } },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // 2. 呼号与信号卡片
            FormSectionCard(title = "呼号与信号") {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = uiState.myCallsign,
                        onValueChange = { viewModel.updateForm(AddLogEvent.MyCallsignChanged(it)) },
                        label = { Text("本台呼号") },
                        isError = uiState.errors.containsKey("myCallsign"),
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = uiState.theirCallsign,
                        onValueChange = { viewModel.updateForm(AddLogEvent.TheirCallsignChanged(it)) },
                        label = { Text("对方呼号") },
                        isError = uiState.errors.containsKey("theirCallsign"),
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = uiState.rstSent,
                        onValueChange = { viewModel.updateForm(AddLogEvent.RstSentChanged(it)) },
                        label = { Text("发送信号 (Sent)") },
                        placeholder = { Text("59") },
                        isError = uiState.errors.containsKey("rstSent"),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = uiState.rstReceived,
                        onValueChange = { viewModel.updateForm(AddLogEvent.RstReceivedChanged(it)) },
                        label = { Text("接收信号 (Rcvd)") },
                        placeholder = { Text("59") },
                        isError = uiState.errors.containsKey("rstReceived"),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            // 3. 详细信息卡片 (可选)
            FormSectionCard(title = "详细信息 (可选)") {
                OutlinedTextField(
                    value = uiState.cqZone,
                    onValueChange = { viewModel.updateForm(AddLogEvent.CqZoneChanged(it)) },
                    label = { Text("CQ 分区") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = uiState.myPower,
                        onValueChange = { viewModel.updateForm(AddLogEvent.MyPowerChanged(it)) },
                        label = { Text("发射功率 (W)") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = uiState.theirPower,
                        onValueChange = { viewModel.updateForm(AddLogEvent.TheirPowerChanged(it)) },
                        label = { Text("对方功率 (W)") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                OutlinedTextField(
                    value = uiState.theirQth,
                    onValueChange = { viewModel.updateForm(AddLogEvent.TheirQthChanged(it)) },
                    label = { Text("对方位置 (QTH)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = uiState.equipment,
                    onValueChange = { viewModel.updateForm(AddLogEvent.EquipmentChanged(it)) },
                    label = { Text("使用设备 (Rig)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = uiState.antenna,
                    onValueChange = { viewModel.updateForm(AddLogEvent.AntennaChanged(it)) },
                    label = { Text("天线系统") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = uiState.notes,
                    onValueChange = { viewModel.updateForm(AddLogEvent.NotesChanged(it)) },
                    label = { Text("备注信息") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // 底部保存按钮
            Button(
                onClick = { viewModel.updateForm(AddLogEvent.Submit) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Done, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("保存通联记录", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

@Composable
fun FormSectionCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            content()
        }
    }
}

@Composable
fun TimeSelector(
    timestamp: Long,
    onTimeSelected: (Long) -> Unit
) {
    val context = LocalContext.current
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm 'UTC'")
        .withZone(ZoneId.of("UTC"))
    
    val formattedTime = formatter.format(Instant.ofEpochMilli(timestamp))

    OutlinedTextField(
        value = formattedTime,
        onValueChange = {},
        readOnly = true,
        label = { Text("通联时间 (UTC)") },
        shape = RoundedCornerShape(12.dp),
        trailingIcon = {
            IconButton(onClick = {
                val calendar = Calendar.getInstance()
                calendar.timeInMillis = timestamp
                
                DatePickerDialog(
                    context,
                    { _, year, month, dayOfMonth ->
                        TimePickerDialog(
                            context,
                            { _, hourOfDay, minute ->
                                val selectedCal = Calendar.getInstance()
                                selectedCal.set(year, month, dayOfMonth, hourOfDay, minute)
                                onTimeSelected(selectedCal.timeInMillis)
                            },
                            calendar.get(Calendar.HOUR_OF_DAY),
                            calendar.get(Calendar.MINUTE),
                            true
                        ).show()
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                ).show()
            }) {
                Icon(Icons.Default.DateRange, contentDescription = "选择时间")
            }
        },
        modifier = Modifier.fillMaxWidth()
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ModeSelector(
    selectedMode: String,
    onModeSelected: (String) -> Unit
) {
    val modes = listOf("SSB", "CW", "FM", "AM", "RTTY", "PSK31", "FT8", "FT4", "JT65")
    
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("通联模式", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            modes.forEach { mode ->
                FilterChip(
                    selected = selectedMode == mode,
                    onClick = { onModeSelected(mode) },
                    label = { Text(mode) },
                    leadingIcon = if (selectedMode == mode) {
                        { Icon(Icons.Default.Done, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    } else null
                )
            }
        }
    }
}
