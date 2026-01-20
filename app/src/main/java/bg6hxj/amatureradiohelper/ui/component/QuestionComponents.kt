package bg6hxj.amatureradiohelper.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * 题目内容卡片
 * 显示题目文本、题号、题型、收藏状态等
 */
@Composable
fun QuestionContentCard(
    question: String,
    currentIndex: Int,
    totalCount: Int,
    questionType: String,
    isLearned: Boolean,
    isFavorite: Boolean,
    isWrong: Boolean,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // 顶部信息行：题号、标签、收藏
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 题号
                    Text(
                        text = "第 $currentIndex/$totalCount 题",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )

                    // 题型标签
                    AssistChip(
                        onClick = {},
                        label = { Text(questionType) },
                        colors = AssistChipDefaults.assistChipColors(
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        border = null,
                        modifier = Modifier.height(24.dp)
                    )

                    // 易错标签
                    if (isWrong) {
                        SuggestionChip(
                            onClick = {},
                            label = { Text("易错") },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                labelColor = MaterialTheme.colorScheme.onErrorContainer
                            ),
                            border = null,
                            modifier = Modifier.height(24.dp)
                        )
                    }
                }

                // 收藏按钮
                IconButton(
                    onClick = onToggleFavorite
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Star else Icons.Outlined.Star,
                        contentDescription = if (isFavorite) "取消收藏" else "收藏",
                        tint = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 题目正文
            Text(
                text = question,
                style = MaterialTheme.typography.titleMedium,
                lineHeight = MaterialTheme.typography.titleMedium.lineHeight * 1.4f,
                fontWeight = FontWeight.Normal
            )
        }
    }
}

/**
 * 选项列表
 */
@Composable
fun QuestionOptionList(
    options: List<Pair<String, String>>,
    selectedAnswers: List<String>,
    correctAnswers: List<String>,
    showAnswer: Boolean, // 如果为true，则显示对错状态（练习模式）；如果为false，只显示选中状态（考试模式/练习未提交）
    isSingleChoice: Boolean,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach { (key, value) ->
            val isSelected = selectedAnswers.contains(key)
            val isCorrect = correctAnswers.contains(key)
            
            // 判断显示状态
            // 练习模式(showAnswer=true):
            // - 正确选项: 无论选没选，都高亮为绿色(Primary)
            // - 错误选项: 如果用户选了但不是正确答案，高亮为红色(Error)
            // - 其他: 默认
            // 
            // 考试模式/未提交(showAnswer=false):
            // - 选中: 高亮(Secondary/Primary)
            // - 未选中: 默认
            
            val stateColor: Color
            val contentColor: Color
            val containerColor: Color

            if (showAnswer) {
                if (isCorrect) {
                    // 正确答案 -> 绿色
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    stateColor = MaterialTheme.colorScheme.primary
                } else if (isSelected) {
                    // 选了但不对 -> 红色
                    containerColor = MaterialTheme.colorScheme.errorContainer
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                    stateColor = MaterialTheme.colorScheme.error
                } else {
                    // 没选也不是答案 -> 默认
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    contentColor = MaterialTheme.colorScheme.onSurface
                    stateColor = MaterialTheme.colorScheme.onSurfaceVariant
                }
            } else {
                if (isSelected) {
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    stateColor = MaterialTheme.colorScheme.secondary
                } else {
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    contentColor = MaterialTheme.colorScheme.onSurface
                    stateColor = MaterialTheme.colorScheme.onSurfaceVariant
                }
            }

            QuestionOptionItem(
                label = key,
                text = value,
                isSelected = isSelected,
                stateColor = stateColor,
                contentColor = contentColor,
                containerColor = containerColor,
                isSingleChoice = isSingleChoice,
                onClick = { if (!showAnswer) onOptionSelected(key) }, // 提交后不可点击
                showIcon = showAnswer && (isCorrect || (isSelected && !isCorrect)) // 只有在显示结果时，才显示对号/错号图标
            )
        }
    }
}

@Composable
private fun QuestionOptionItem(
    label: String,
    text: String,
    isSelected: Boolean,
    stateColor: Color,
    contentColor: Color,
    containerColor: Color,
    isSingleChoice: Boolean,
    onClick: () -> Unit,
    showIcon: Boolean
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = containerColor,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 选项标号 (A, B, C...) 或 选择框
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(stateColor.copy(alpha = if (isSelected || showIcon) 1f else 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected || showIcon) MaterialTheme.colorScheme.surface else stateColor
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // 选项文本
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                color = contentColor,
                modifier = Modifier.weight(1f)
            )

            // 尾部图标 (对号/错号 或 Checkbox/Radio状态)
            if (showIcon) {
                Spacer(modifier = Modifier.width(8.dp))
                if (contentColor == MaterialTheme.colorScheme.onPrimaryContainer) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = stateColor)
                } else {
                    Icon(Icons.Default.Close, contentDescription = null, tint = stateColor)
                }
            } else if (isSelected) {
                Spacer(modifier = Modifier.width(8.dp))
                // 如果是简单的选中状态，显示一个小勾选或者RadioDot
                 Icon(Icons.Default.CheckCircle, contentDescription = null, tint = stateColor)
            }
        }
    }
}

/**
 * 答案结果卡片 (练习模式反馈)
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
            containerColor = if (isCorrect) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isCorrect) Icons.Default.CheckCircle else Icons.Default.Close,
                contentDescription = null,
                tint = if (isCorrect) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.size(32.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column {
                Text(
                    text = if (isCorrect) "回答正确" else "回答错误",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isCorrect) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer
                )
                if (!isCorrect) {
                    Text(
                        text = "正确答案: ${correctAnswers.joinToString(", ")}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
    }
}

/**
 * 底部操作栏
 */
@Composable
fun QuestionBottomBar(
    currentIndex: Int,
    totalCount: Int,
    showAnswer: Boolean,
    hasSelectedAnswer: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onSubmit: () -> Unit,
    isExampleMode: Boolean = false, // 是否是考试模式(最后一题显示提交而不是下一题)
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        tonalElevation = 3.dp,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .navigationBarsPadding() // 适配底部导航栏高度
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            FilledTonalButton(
                onClick = onPrevious,
                enabled = currentIndex > 0,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("上一题")
            }

            Button(
                onClick = {
                    if (isExampleMode) {
                        if (currentIndex == totalCount - 1) onSubmit() else onNext()
                    } else {
                         if (showAnswer) onNext() else onSubmit()
                    }
                },
                enabled = if (isExampleMode) true else (if (showAnswer) currentIndex < totalCount - 1 else hasSelectedAnswer),
                modifier = Modifier.weight(1f)
            ) {
                if (isExampleMode) {
                    if (currentIndex == totalCount - 1) {
                        Text("交卷")
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                    } else {
                        Text("下一题")
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
                    }
                } else {
                    if (showAnswer) {
                        Text("下一题")
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
                    } else {
                        Text("提交")
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}
