package bg6hxj.amatureradiohelper.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier // Correctly imported
import bg6hxj.amatureradiohelper.ui.component.ZoomableImageViewer
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun PropagationScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentDate = remember {
        val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        sdf.format(Date())
    }
    // URL format: https://www.dxinfocentre.com/tr_map/fcst/wnp006.png?v{YYYYMMDD}
    val imageUrl = "https://www.dxinfocentre.com/tr_map/fcst/wnp006.png?v$currentDate"

    ZoomableImageViewer(
        imageUrl = imageUrl,
        title = "无线电传播预测",
        onBackClick = onNavigateBack,
        modifier = modifier
    )
}
