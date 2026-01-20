package bg6hxj.amatureradiohelper.ui.component

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.core.graphics.drawable.toBitmap
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Size
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

/**
 * Enhanced Image Viewer with Zoom, Pan, and Toolbar Actions.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ZoomableImageViewer(
    imageUrl: String?,
    title: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var rotation by remember { mutableFloatStateOf(0f) }
    
    // Painter state to get the bitmap for saving/sharing
    val context = LocalContext.current
    val painter = rememberAsyncImagePainter(
        model = ImageRequest.Builder(context)
            .data(imageUrl)
            .size(Size.ORIGINAL) // Load original size for better zoom quality
            .build()
    )

    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Rotate Action
                    IconButton(onClick = { rotation += 90f }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Rotate")
                    }

                    // Save Action
                    IconButton(onClick = {
                        coroutineScope.launch {
                            saveImageToGallery(context, painter)
                        }
                    }) {
                        Icon(Icons.Default.Check, contentDescription = "Save") // Check mark for Save, or Download if available
                    }
                    
                    // Share Action
                    IconButton(onClick = {
                        coroutineScope.launch {
                            shareImage(context, painter)
                        }
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "Share")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                )
            )
        }
    ) { padding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color.Black)
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scale = (scale * zoom).coerceIn(1f, 5f)
                        // Adjust pan based on zoom level to allow moving around
                        val maxOffset = (scale - 1) * 1000f // Rough estimation, improved logic below
                        // Proper pan limit requires knowing image size vs screen size. 
                        // For simplicity, we allow free pan but could clamp it.
                        offset += pan
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            if (painter.state is AsyncImagePainter.State.Loading) {
                CircularProgressIndicator(color = Color.White)
            }
            
            Image(
                painter = painter,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offset.x,
                        translationY = offset.y,
                        rotationZ = rotation
                    ),
                contentScale = ContentScale.Fit
            )
            
            if (painter.state is AsyncImagePainter.State.Error) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = "Error",
                        tint = Color.Red,
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        "无法加载图片",
                        color = Color.White,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
    }
}

private suspend fun saveImageToGallery(context: Context, painter: AsyncImagePainter) {
    val state = painter.state
    if (state is AsyncImagePainter.State.Success) {
        val bitmap = state.result.drawable.toBitmap()
        withContext(Dispatchers.IO) {
            try {
                val filename = "AmraHelper_${System.currentTimeMillis()}.jpg"
                var fos: OutputStream? = null
                var imageUri: Uri? = null

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val resolver = context.contentResolver
                    val contentValues = ContentValues().apply {
                        put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                        put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                        put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                    }
                    imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                    fos = imageUri?.let { resolver.openOutputStream(it) }
                } else {
                    // For legacy versions, simpler to just say not supported or standard file io
                    // Assuming Android 10+ for this modern compose app
                }

                fos?.use {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "图片已保存", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "保存失败: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    } else {
        Toast.makeText(context, "图片未加载完成", Toast.LENGTH_SHORT).show()
    }
}

private suspend fun shareImage(context: Context, painter: AsyncImagePainter) {
    val state = painter.state
    if (state is AsyncImagePainter.State.Success) {
        val bitmap = state.result.drawable.toBitmap()
        withContext(Dispatchers.IO) {
            try {
                // Save to cache directory to share
                val cachePath = File(context.cacheDir, "images")
                cachePath.mkdirs()
                val stream = FileOutputStream("$cachePath/share_image.png")
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                stream.close()

                val newFile = File(cachePath, "share_image.png")
                val contentUri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    newFile
                )

                if (contentUri != null) {
                    val shareIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        setDataAndType(contentUri, context.contentResolver.getType(contentUri))
                        putExtra(Intent.EXTRA_STREAM, contentUri)
                        type = "image/png"
                    }
                    withContext(Dispatchers.Main) {
                        context.startActivity(Intent.createChooser(shareIntent, "分享图片"))
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "分享失败", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
