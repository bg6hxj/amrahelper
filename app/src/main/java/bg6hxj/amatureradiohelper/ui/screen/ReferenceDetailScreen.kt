package bg6hxj.amatureradiohelper.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import bg6hxj.amatureradiohelper.data.ReferenceData
import kotlinx.coroutines.launch

/**
 * 参考资料详情页面
 * 支持表格内容展示和关键词搜索功能
 * UI 优化版：MD3 风格
 */
@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun ReferenceDetailScreen(
    type: String,
    onNavigateBack: () -> Unit,
    onImageClick: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val title = when(type) {
        "phonetic_alphabet" -> "字母解释法"
        "callsign_prefix" -> "全球呼号前缀"
        "itu_cq_zones" -> "ITU及CQ分区"
        "cn_zones" -> "国内电台分区"
        "q_codes" -> "常用Q简语"
        "abbreviations" -> "常用缩略语"
        "cw_codes" -> "CW电码表"
        "frequency_chart" -> "频率对照表"
        else -> "未知"
    }

    // 搜索状态
    var searchQuery by remember { mutableStateOf("") }
    var matchIndices by remember { mutableStateOf<List<Int>>(emptyList()) }
    var currentMatchIndex by remember { mutableStateOf(0) }
    
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    
    // 执行搜索
    fun performSearch(query: String, scrollToFirst: Boolean = true) {
        val trimmedQuery = query.trim()
        if (trimmedQuery.isEmpty()) {
            matchIndices = emptyList()
            currentMatchIndex = 0
            return
        }
        
        val indices = mutableListOf<Int>()
        when (type) {
            "q_codes" -> {
                ReferenceData.qCodes.forEachIndexed { index, qCode ->
                    if (qCode.code.contains(trimmedQuery, ignoreCase = true) ||
                        qCode.question.contains(trimmedQuery, ignoreCase = true) ||
                        qCode.answer.contains(trimmedQuery, ignoreCase = true)) {
                        indices.add(index + 1) // +1 for header row
                    }
                }
            }
            "frequency_chart" -> {
                ReferenceData.frequencyBands.forEachIndexed { index, band ->
                    if (band.frequency.contains(trimmedQuery, ignoreCase = true) ||
                        band.category.contains(trimmedQuery, ignoreCase = true)) {
                        indices.add(index + 1)
                    }
                }
            }
            "abbreviations" -> {
                ReferenceData.abbreviations.forEachIndexed { index, abbr ->
                    if (abbr.code.contains(trimmedQuery, ignoreCase = true) ||
                        abbr.english.contains(trimmedQuery, ignoreCase = true) ||
                        abbr.chinese.contains(trimmedQuery, ignoreCase = true)) {
                        indices.add(index + 1)
                    }
                }
            }
            "callsign_prefix" -> {
                ReferenceData.callsignPrefixes.forEachIndexed { index, item ->
                    if (item.prefix.contains(trimmedQuery, ignoreCase = true) ||
                        item.allocatedTo.contains(trimmedQuery, ignoreCase = true) ||
                        item.allocatedToEn.contains(trimmedQuery, ignoreCase = true)) {
                        indices.add(index + 1)
                    }
                }
            }
            "phonetic_alphabet" -> {
                ReferenceData.phoneticAlphabets.forEachIndexed { index, item ->
                    if (item.letter.contains(trimmedQuery, ignoreCase = true) ||
                        item.standard.contains(trimmedQuery, ignoreCase = true) ||
                        item.others.contains(trimmedQuery, ignoreCase = true)) {
                        indices.add(index + 1)
                    }
                }
            }

            "cn_zones" -> {
                ReferenceData.cnZones.forEachIndexed { index, zone ->
                    if (zone.suffix.contains(trimmedQuery, ignoreCase = true) ||
                        zone.province.contains(trimmedQuery, ignoreCase = true)) {
                        indices.add(index + 1)
                    }
                }
            }
        }
        
        matchIndices = indices
        currentMatchIndex = if (indices.isNotEmpty()) 0 else 0
        
        if (scrollToFirst && indices.isNotEmpty()) {
            coroutineScope.launch {
                listState.animateScrollToItem(indices[0])
            }
        }
    }
    
    LaunchedEffect(searchQuery) {
        performSearch(searchQuery, scrollToFirst = true)
    }

    // 获取图片资源
    val images = ReferenceData.getReferenceImages(type)
    // 根据类型决定是否显示搜索栏（仅图片类型不显示）
    val showSearchBar = type != "itu_cq_zones" && type != "cw_codes"
    
    fun navigateToNext() {
        if (matchIndices.isEmpty()) return
        currentMatchIndex = (currentMatchIndex + 1) % matchIndices.size
        coroutineScope.launch {
            listState.animateScrollToItem(matchIndices[currentMatchIndex])
        }
    }
    
    fun navigateToPrevious() {
        if (matchIndices.isEmpty()) return
        currentMatchIndex = if (currentMatchIndex > 0) currentMatchIndex - 1 else matchIndices.size - 1
        coroutineScope.launch {
            listState.animateScrollToItem(matchIndices[currentMatchIndex])
        }
    }
    
    fun clearSearch() {
        searchQuery = ""
        matchIndices = emptyList()
        currentMatchIndex = 0
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
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
        ) {
            // 1. 搜索区域 (Increased padding for spacing)
            if (showSearchBar) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, bottom = 16.dp, top = 8.dp)
                ) {
                    SearchBar(
                        query = searchQuery,
                        onQueryChange = { searchQuery = it },
                        onSearch = { 
                            if (matchIndices.isNotEmpty()) {
                                currentMatchIndex = 0
                                coroutineScope.launch {
                                    listState.animateScrollToItem(matchIndices[0])
                                }
                            }
                            focusManager.clearFocus()
                        },
                        onClear = { clearSearch() },
                        matchCount = matchIndices.size,
                        currentMatch = if (matchIndices.isNotEmpty()) currentMatchIndex + 1 else 0,
                        onNavigatePrevious = { navigateToPrevious() },
                        onNavigateNext = { navigateToNext() }
                    )
                }
            }
            
            // 2. 图片展示区域 (如果有图片)
            if (images.isNotEmpty()) {
                LazyColumn(
                   modifier = Modifier
                       .fillMaxWidth()
                       .padding(horizontal = 16.dp)
                       .padding(bottom = if (showSearchBar) 0.dp else 16.dp)
                       .then(if (!showSearchBar) Modifier.weight(1f) else Modifier),
                   verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    itemsIndexed(images) { _, image ->
                         if (type == "cw_codes") {
                             // CW Code Chart Special Handling: Fill parent, transparent background
                             ImageCard(
                                 title = image.title,
                                 imageUrl = image.url,
                                 onClick = { onImageClick(image.url, image.title) },
                                 backgroundColor = Color.Transparent,
                                 imageHeight = 500.dp, // Or use a larger fixed height to simulate "filling" usually available space
                                 modifier = Modifier.fillParentMaxHeight()
                             )
                         } else {
                             ImageCard(
                                 title = image.title,
                                 imageUrl = image.url,
                                 onClick = { onImageClick(image.url, image.title) }
                             )
                         }
                    }
                }
                if (showSearchBar) {
                   Spacer(modifier = Modifier.height(16.dp))
                }
            } else {
                 // 占位，避免布局错乱
            }
            
            // 3. 内容表格区域 (仅当显示搜索栏时才显示表格，或者混合模式)
            // 如果只有图片（如地图），则不需要表格容器
            if (showSearchBar) {
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 16.dp)
                    .padding(bottom = 16.dp), // Bottom padding
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                when (type) {
                    "q_codes" -> QCodeTable(
                        listState = listState,
                        highlightQuery = searchQuery.trim(),
                        matchIndices = matchIndices,
                        currentMatchIndex = currentMatchIndex
                    )
                    "frequency_chart" -> FrequencyTable(
                        listState = listState,
                        highlightQuery = searchQuery.trim(),
                        matchIndices = matchIndices,
                        currentMatchIndex = currentMatchIndex
                    )
                    "abbreviations" -> AbbreviationTable(
                        listState = listState,
                        highlightQuery = searchQuery.trim(),
                        matchIndices = matchIndices,
                        currentMatchIndex = currentMatchIndex
                    )
                    "callsign_prefix" -> CallsignPrefixTable(
                        listState = listState,
                        highlightQuery = searchQuery.trim(),
                        matchIndices = matchIndices,
                        currentMatchIndex = currentMatchIndex
                    )
                    "phonetic_alphabet" -> PhoneticAlphabetTable(
                        listState = listState,
                        highlightQuery = searchQuery.trim(),
                        matchIndices = matchIndices,
                        currentMatchIndex = currentMatchIndex
                    )
                    "cn_zones" -> CnZoneTable(
                        listState = listState,
                        highlightQuery = searchQuery.trim(),
                        matchIndices = matchIndices,
                        currentMatchIndex = currentMatchIndex
                    )
                    else -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "内容建设中: $title",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.secondary
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
 * MD3 风格搜索栏 (Floating Style)
 */
@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onClear: () -> Unit,
    matchCount: Int,
    currentMatch: Int,
    onNavigatePrevious: () -> Unit,
    onNavigateNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isNoResult = query.isNotEmpty() && matchCount == 0
    val containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
    
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = CircleShape,
        color = containerColor,
        tonalElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp), // Slightly compact
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            IconButton(onClick = onSearch) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "搜索",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Input
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.CenterStart
            ) {
                if (query.isEmpty()) {
                    Text(
                        text = "输入关键词查找...",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
                androidx.compose.foundation.text.BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { onSearch() }),
                    cursorBrush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.primary),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            // Clear Button
            if (query.isNotEmpty()) {
                IconButton(onClick = onClear) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "清除",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            // Divider
            if (query.isNotEmpty() || matchCount > 0) {
                 VerticalDivider(
                     modifier = Modifier.height(24.dp).padding(horizontal = 8.dp),
                     color = MaterialTheme.colorScheme.outlineVariant
                 )
            }

            // Result Counter & Navigation
            if (query.isNotEmpty() || matchCount > 0) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = "$currentMatch/$matchCount",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isNoResult) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.widthIn(min = 36.dp),
                        textAlign = TextAlign.Center
                    )
                    
                    IconButton(
                        onClick = onNavigatePrevious,
                        enabled = matchCount > 0,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            Icons.Default.KeyboardArrowUp, 
                            contentDescription = "上一个",
                            tint = if (matchCount > 0) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(
                        onClick = onNavigateNext,
                        enabled = matchCount > 0,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            Icons.Default.KeyboardArrowDown, 
                            contentDescription = "下一个",
                            tint = if (matchCount > 0) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * 现代风格表格头
 */
@Composable
fun TableHeader(
    content: @Composable RowScope.() -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 8.dp) // Proper padding
                .height(IntrinsicSize.Min), // Ensure header height consistency
            verticalAlignment = Alignment.CenterVertically,
            content = content
        )
    }
}

@Composable
fun TableRow(
    index: Int,
    isMatch: Boolean,
    isCurrentMatch: Boolean,
    content: @Composable RowScope.() -> Unit
) {
    // Zebra striping with better colors
    val backgroundColor = when {
        isCurrentMatch -> MaterialTheme.colorScheme.tertiaryContainer
        isMatch -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
        index % 2 == 1 -> MaterialTheme.colorScheme.surfaceContainerLow
        else -> MaterialTheme.colorScheme.surface
    }

    Surface(
        color = backgroundColor,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min) // Important for alignment
                    .padding(vertical = 12.dp, horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                content = content
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        }
    }
}

/**
 * 现代风格单元格 (No Borders)
 */
@Composable
fun RowScope.TableCell(
    text: String,
    weight: Float,
    isHeader: Boolean = false,
    highlightQuery: String = "",
    align: TextAlign = TextAlign.Start
) {
    val highlightColor = MaterialTheme.colorScheme.tertiary
    
    Box(
        modifier = Modifier
            .weight(weight)
            .fillMaxHeight()
            .padding(horizontal = 4.dp), // Spacing between columns
        contentAlignment = if (isHeader) Alignment.Center else Alignment.CenterStart
    ) {
        if (highlightQuery.isNotEmpty() && text.contains(highlightQuery, ignoreCase = true)) {
            HighlightedText(
                text = text,
                query = highlightQuery,
                highlightColor = highlightColor,
                isHeader = isHeader,
                textAlign = align
            )
        } else {
            Text(
                text = text,
                style = if (isHeader) MaterialTheme.typography.titleSmall else MaterialTheme.typography.bodyMedium,
                color = if (isHeader) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                fontWeight = if (isHeader) FontWeight.SemiBold else FontWeight.Normal,
                textAlign = if (isHeader) TextAlign.Center else align,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun QCodeTable(
    listState: LazyListState,
    highlightQuery: String,
    matchIndices: List<Int>,
    currentMatchIndex: Int,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize()
    ) {
        // Sticky Header in future if needed, but simple item for now inside Card
        stickyHeader {
            TableHeader {
                TableCell(text = "Q简语", weight = 0.25f, isHeader = true)
                TableCell(text = "问句意思", weight = 0.375f, isHeader = true)
                TableCell(text = "答句意思", weight = 0.375f, isHeader = true)
            }
        }
        
        itemsIndexed(ReferenceData.qCodes) { index, qCode ->
            val itemIndex = index + 1
            val isCurrentMatch = matchIndices.isNotEmpty() && matchIndices.getOrNull(currentMatchIndex) == itemIndex
            val isMatch = itemIndex in matchIndices
            
            TableRow(index, isMatch, isCurrentMatch) {
                TableCell(text = qCode.code, weight = 0.25f, highlightQuery = highlightQuery, align = TextAlign.Center)
                TableCell(text = qCode.question, weight = 0.375f, highlightQuery = highlightQuery)
                TableCell(text = qCode.answer, weight = 0.375f, highlightQuery = highlightQuery)
            }
        }
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun FrequencyTable(
    listState: LazyListState,
    highlightQuery: String,
    matchIndices: List<Int>,
    currentMatchIndex: Int,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize()
    ) {
        stickyHeader {
            TableHeader {
                TableCell(text = "频率范围", weight = 0.6f, isHeader = true)
                TableCell(text = "操作类别", weight = 0.4f, isHeader = true)
            }
        }
        
        itemsIndexed(ReferenceData.frequencyBands) { index, band ->
            val itemIndex = index + 1
            val isCurrentMatch = matchIndices.isNotEmpty() && matchIndices.getOrNull(currentMatchIndex) == itemIndex
            val isMatch = itemIndex in matchIndices
            
            TableRow(index, isMatch, isCurrentMatch) {
                TableCell(text = band.frequency, weight = 0.6f, highlightQuery = highlightQuery, align = TextAlign.Center)
                TableCell(text = band.category, weight = 0.4f, highlightQuery = highlightQuery, align = TextAlign.Center)
            }
        }
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun AbbreviationTable(
    listState: LazyListState,
    highlightQuery: String,
    matchIndices: List<Int>,
    currentMatchIndex: Int,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize()
    ) {
        stickyHeader {
            TableHeader {
                 TableCell(text = "#", weight = 0.1f, isHeader = true)
                 TableCell(text = "缩略语", weight = 0.2f, isHeader = true)
                 TableCell(text = "英文原意", weight = 0.4f, isHeader = true)
                 TableCell(text = "中文含义", weight = 0.3f, isHeader = true)
            }
        }
        
        itemsIndexed(ReferenceData.abbreviations) { index, abbr ->
            val itemIndex = index + 1
            val isCurrentMatch = matchIndices.isNotEmpty() && matchIndices.getOrNull(currentMatchIndex) == itemIndex
            val isMatch = itemIndex in matchIndices
            
            TableRow(index, isMatch, isCurrentMatch) {
                TableCell(text = abbr.index.toString(), weight = 0.1f, highlightQuery = highlightQuery, align = TextAlign.Center)
                TableCell(text = abbr.code, weight = 0.2f, highlightQuery = highlightQuery, align = TextAlign.Center)
                TableCell(text = abbr.english, weight = 0.4f, highlightQuery = highlightQuery)
                TableCell(text = abbr.chinese, weight = 0.3f, highlightQuery = highlightQuery)
            }
        }
    }
}

@Composable
fun HighlightedText(
    text: String,
    query: String,
    highlightColor: Color,
    isHeader: Boolean = false,
    textAlign: TextAlign = TextAlign.Start
) {
    val annotatedString = buildAnnotatedString {
        var currentIndex = 0
        val lowerText = text.lowercase()
        val lowerQuery = query.lowercase()
        
        while (currentIndex < text.length) {
            val matchIndex = lowerText.indexOf(lowerQuery, currentIndex)
            if (matchIndex == -1) {
                append(text.substring(currentIndex))
                break
            } else {
                if (matchIndex > currentIndex) {
                    append(text.substring(currentIndex, matchIndex))
                }
                withStyle(SpanStyle(background = highlightColor, fontWeight = FontWeight.Bold)) {
                    append(text.substring(matchIndex, matchIndex + query.length))
                }
                currentIndex = matchIndex + query.length
            }
        }
    }
    
    Text(
        text = annotatedString,
        style = if (isHeader) MaterialTheme.typography.titleSmall else MaterialTheme.typography.bodyMedium,
        color = if (isHeader) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
        fontWeight = if (isHeader) FontWeight.SemiBold else FontWeight.Normal,
        textAlign = textAlign,
        modifier = Modifier.fillMaxWidth()
    )
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun CallsignPrefixTable(
    listState: LazyListState,
    highlightQuery: String,
    matchIndices: List<Int>,
    currentMatchIndex: Int,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize()
    ) {
        stickyHeader {
            TableHeader {
                TableCell(text = "呼号系列", weight = 0.25f, isHeader = true)
                TableCell(text = "划分给", weight = 0.35f, isHeader = true)
                TableCell(text = "Allocated to", weight = 0.4f, isHeader = true)
            }
        }
        
        itemsIndexed(ReferenceData.callsignPrefixes) { index, item ->
            val itemIndex = index + 1
            val isCurrentMatch = matchIndices.isNotEmpty() && matchIndices.getOrNull(currentMatchIndex) == itemIndex
            val isMatch = itemIndex in matchIndices
            
            TableRow(index, isMatch, isCurrentMatch) {
                TableCell(text = item.prefix, weight = 0.25f, highlightQuery = highlightQuery, align = TextAlign.Center)
                TableCell(text = item.allocatedTo, weight = 0.35f, highlightQuery = highlightQuery)
                TableCell(text = item.allocatedToEn, weight = 0.4f, highlightQuery = highlightQuery)
            }
        }
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun PhoneticAlphabetTable(
    listState: LazyListState,
    highlightQuery: String,
    matchIndices: List<Int>,
    currentMatchIndex: Int,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize()
    ) {
        stickyHeader {
            TableHeader {
                TableCell(text = "字母", weight = 0.2f, isHeader = true)
                TableCell(text = "标准读法", weight = 0.4f, isHeader = true)
                TableCell(text = "其他读法/备注", weight = 0.4f, isHeader = true)
            }
        }
        
        itemsIndexed(ReferenceData.phoneticAlphabets) { index, item ->
            val itemIndex = index + 1
            val isCurrentMatch = matchIndices.isNotEmpty() && matchIndices.getOrNull(currentMatchIndex) == itemIndex
            val isMatch = itemIndex in matchIndices
            
            TableRow(index, isMatch, isCurrentMatch) {
                TableCell(text = item.letter, weight = 0.2f, highlightQuery = highlightQuery, align = TextAlign.Center)
                TableCell(text = item.standard, weight = 0.4f, highlightQuery = highlightQuery)
                TableCell(text = item.others, weight = 0.4f, highlightQuery = highlightQuery)
            }
        }
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun CnZoneTable(
    listState: LazyListState,
    highlightQuery: String,
    matchIndices: List<Int>,
    currentMatchIndex: Int,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize()
    ) {
        stickyHeader {
            TableHeader {
                TableCell(text = "呼号后缀", weight = 0.5f, isHeader = true)
                TableCell(text = "省市", weight = 0.5f, isHeader = true)
            }
        }
        
        itemsIndexed(ReferenceData.cnZones) { index, item ->
            val itemIndex = index + 1
            val isCurrentMatch = matchIndices.isNotEmpty() && matchIndices.getOrNull(currentMatchIndex) == itemIndex
            val isMatch = itemIndex in matchIndices
            
            TableRow(index, isMatch, isCurrentMatch) {
                TableCell(text = item.suffix, weight = 0.5f, highlightQuery = highlightQuery, align = TextAlign.Center)
                TableCell(text = item.province, weight = 0.5f, highlightQuery = highlightQuery, align = TextAlign.Center)
            }
        }
    }
}
