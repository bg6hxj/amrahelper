package bg6hxj.amatureradiohelper.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

data class ReferenceItem(
    val title: String,
    val icon: ImageVector,
    val route: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReferenceListScreen(
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val references = listOf(
        ReferenceItem("字母解释法", Icons.Default.Settings, "phonetic_alphabet"),
        ReferenceItem("全球呼号前缀", Icons.Default.Phone, "callsign_prefix"),
        ReferenceItem("ITU及CQ分区", Icons.Default.LocationOn, "itu_cq_zones"),
        ReferenceItem("国内电台分区", Icons.Default.Place, "cn_zones"),
        ReferenceItem("常用Q简语", Icons.Default.Email, "q_codes"),
        ReferenceItem("常用缩略语", Icons.Default.MailOutline, "abbreviations"),
        ReferenceItem("CW电码表", Icons.Default.Build, "cw_codes"),
        ReferenceItem("频率对照表", Icons.Default.List, "frequency_chart")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("基础知识速查") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            items(references) { item ->
                ListItem(
                    headlineContent = { Text(item.title) },
                    leadingContent = {
                        Icon(item.icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    },
                    trailingContent = {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
                    },
                    modifier = Modifier.clickable { onNavigateToDetail(item.route) }
                )
                HorizontalDivider()
            }
        }
    }
}
