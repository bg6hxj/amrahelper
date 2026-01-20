package bg6hxj.amatureradiohelper.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import java.text.DecimalFormat

/**
 * 波长计算器页面
 * 提供频率到波长的实时转换，并显示所属波段信息
 * UI 优化版：MD3 风格
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WavelengthCalculatorScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var frequencyInput by remember { mutableStateOf("") }
    var wavelengthM by remember { mutableStateOf("") }
    var wavelengthCm by remember { mutableStateOf("") }
    var bandName by remember { mutableStateOf("") }
    var bandDesc by remember { mutableStateOf("") }

    // Constants for calculation
    val c = 300.0 // speed of light approximation in Mm/s

    fun calculateWavelength(freqUi: String) {
        val freq = freqUi.toDoubleOrNull()
        if (freq != null && freq > 0) {
            val waveM = c / freq
            val df = DecimalFormat("#.####") // More precision
            wavelengthM = df.format(waveM)
            wavelengthCm = df.format(waveM * 100)

            // Determine band and description
            when {
                freq in 0.003..0.03 -> {
                    bandName = "VLF (Very Low Frequency)"
                    bandDesc = "甚低频。主要用于潜艇通信、无线导航等。"
                }
                freq in 0.03..0.3 -> {
                    bandName = "LF (Low Frequency)"
                    bandDesc = "低频。用于导航、授时广播、业余无线电(136kHz)。"
                }
                freq in 0.3..3.0 -> {
                    bandName = "MF (Medium Frequency)"
                    bandDesc = "中频。包含标准AM广播、业余无线电(160m)。地面波传播为主。"
                }
                freq in 3.0..30.0 -> {
                    bandName = "HF (High Frequency)"
                    bandDesc = "高频。短波广播、主要业余无线电波段。依靠电离层反射进行远距离通信。"
                }
                freq in 30.0..300.0 -> {
                    bandName = "VHF (Very High Frequency)"
                    bandDesc = "甚高频。FM广播、对讲机、业余无线电(6m/2m)。视距传播为主。"
                }
                freq in 300.0..3000.0 -> {
                    bandName = "UHF (Ultra High Frequency)"
                    bandDesc = "特高频。电视广播、手机通信、WiFi、业余无线电(70cm/23cm)。穿透力强。"
                }
                freq in 3000.0..30000.0 -> {
                    bandName = "SHF (Super High Frequency)"
                    bandDesc = "超高频。微波通信、雷达、卫星通信。定向性极强。"
                }
                else -> {
                    bandName = "其他频段"
                    bandDesc = "超出常用业余无线电定义范围或极其特殊的频段。"
                }
            }
        } else {
            wavelengthM = ""
            wavelengthCm = ""
            bandName = ""
            bandDesc = ""
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("波长计算器") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
                )
            )
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // 1. 输入区域
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "输入频率",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = frequencyInput,
                        onValueChange = {
                            frequencyInput = it
                            calculateWavelength(it)
                        },
                        label = { Text("频率 (Frequency)") },
                        suffix = { Text("MHz") },
                        placeholder = { Text("例如: 14.270") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        trailingIcon = {
                            if (frequencyInput.isNotEmpty()) {
                                IconButton(onClick = { 
                                    frequencyInput = ""
                                    calculateWavelength("")
                                }) {
                                    Icon(Icons.Default.Clear, contentDescription = "清除")
                                }
                            }
                        },
                        shape = MaterialTheme.shapes.medium
                    )
                }
            }

            // 2. 结果区域
            AnimatedVisibility(
                visible = wavelengthM.isNotEmpty(),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 主结果卡片 (计算结果)
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(24.dp)
                                .fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "计算结果 (波长)",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                            
                            // 米
                            Text(
                                text = buildAnnotatedString {
                                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                                        append(wavelengthM)
                                    }
                                    withStyle(SpanStyle(fontSize = MaterialTheme.typography.titleMedium.fontSize)) {
                                        append(" 米")
                                    }
                                },
                                style = MaterialTheme.typography.displayMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            
                            HorizontalDivider(
                                modifier = Modifier
                                    .padding(vertical = 8.dp)
                                    .width(64.dp),
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f)
                            )
                            
                            // 厘米
                            Text(
                                text = "$wavelengthCm 厘米",
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }

                    // 详细信息卡片 (波段)
                    OutlinedCard(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.outlinedCardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Info, 
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "所属波段",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            
                            Text(
                                text = bandName,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            
                            if (bandDesc.isNotEmpty()) {
                                Text(
                                    text = bandDesc,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // 3. 公式说明
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "计算公式: λ (m) = 300 / f (MHz)",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}
