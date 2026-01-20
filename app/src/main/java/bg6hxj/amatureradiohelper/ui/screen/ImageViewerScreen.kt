package bg6hxj.amatureradiohelper.ui.screen

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import bg6hxj.amatureradiohelper.ui.component.ZoomableImageViewer

/**
 * 通用图片查看页面
 * 封装了 ZoomableImageViewer 组件，用于全屏查看图片
 */
@Composable
fun ImageViewerScreen(
    url: String,
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    ZoomableImageViewer(
        imageUrl = url,
        title = title,
        onBackClick = onBack,
        modifier = modifier
    )
}
