package bg6hxj.amatureradiohelper.ui.screen

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.clickable
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import bg6hxj.amatureradiohelper.data.UserPreferences
import coil.compose.AsyncImage
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.canhub.cropper.CropImageView
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 将外部 URI 图片复制到应用私有目录
 * @param context Context
 * @param sourceUri 源图片 URI
 * @return 成功返回本地文件路径,失败返回 null
 */
private fun copyImageToPrivateStorage(context: Context, sourceUri: Uri): String? {
    return try {
        // 创建私有目录文件
        val avatarDir = File(context.filesDir, "avatar")
        if (!avatarDir.exists()) {
            avatarDir.mkdirs()
        }
        
        // 删除旧头像文件
        avatarDir.listFiles()?.forEach { it.delete() }
        
        // 生成新文件名
        val extension = context.contentResolver.getType(sourceUri)?.let { mimeType ->
            when {
                mimeType.contains("png") -> "png"
                mimeType.contains("webp") -> "webp"
                else -> "jpg"
            }
        } ?: "jpg"
        
        val avatarFile = File(avatarDir, "user_avatar.$extension")
        
        // 复制图片数据
        context.contentResolver.openInputStream(sourceUri)?.use { input ->
            avatarFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        
        avatarFile.absolutePath
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

/**
 * 我的模块主页面
 * 包含个人信息展示、清除缓存、关于
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onAboutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val userPreferences = remember { UserPreferences(context) }
    val scope = rememberCoroutineScope()
    
    // 收集用户信息
    val nickname by userPreferences.nickname.collectAsState(initial = "未设置")
    val callsign by userPreferences.callsign.collectAsState(initial = "未设置")
    val avatarUri by userPreferences.avatarUri.collectAsState(initial = null)
    
    // 监控头像 URI 变化
    var showClearCacheDialog by remember { mutableStateOf(false) }
    var showNicknameDialog by remember { mutableStateOf(false) }
    var showCallsignDialog by remember { mutableStateOf(false) }
    var showImageSourceDialog by remember { mutableStateOf(false) }
    var showCameraPermissionDialog by remember { mutableStateOf(false) }
    
    // 拍照临时文件 URI
    var cameraPhotoUri by remember { mutableStateOf<Uri?>(null) }
    
    // 是否等待裁切（用于拍照流程）
    var pendingCameraCrop by remember { mutableStateOf(false) }
    
    // 图片裁切器
    val cropImageLauncher = rememberLauncherForActivityResult(
        contract = CropImageContract()
    ) { result ->
        if (result.isSuccessful) {
            result.uriContent?.let { croppedUri ->
                scope.launch {
                    try {
                        val savedPath = copyImageToPrivateStorage(context, croppedUri)
                        if (savedPath != null) {
                            userPreferences.saveAvatarUri(savedPath)
                            Toast.makeText(context, "头像已更新", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "头像更新失败", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(context, "头像更新失败: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } else {
            val exception = result.error
            if (exception != null) {
                Toast.makeText(context, "裁切失败: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
        }
        pendingCameraCrop = false
    }
    
    // 启动裁切的辅助函数
    fun launchCrop(uri: Uri) {
        val cropOptions = CropImageContractOptions(
            uri = uri,
            cropImageOptions = CropImageOptions(
                guidelines = CropImageView.Guidelines.ON,
                aspectRatioX = 1,
                aspectRatioY = 1,
                fixAspectRatio = true,
                cropShape = CropImageView.CropShape.OVAL,
                outputCompressQuality = 90
            )
        )
        cropImageLauncher.launch(cropOptions)
    }
    
    // 图片选择器
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { sourceUri ->
            // 启动裁切
            launchCrop(sourceUri)
        }
    }
    
    // 相机拍照
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && cameraPhotoUri != null) {
            // 拍照成功，启动裁切
            pendingCameraCrop = true
            launchCrop(cameraPhotoUri!!)
        }
    }
    
    // 相机权限请求
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // 权限已授予，启动相机
            try {
                val avatarDir = File(context.filesDir, "avatar")
                if (!avatarDir.exists()) {
                    avatarDir.mkdirs()
                }
                val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                val photoFile = File(avatarDir, "camera_$timeStamp.jpg")
                val photoUri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    photoFile
                )
                cameraPhotoUri = photoUri
                cameraLauncher.launch(photoUri)
            } catch (e: Exception) {
                Toast.makeText(context, "无法打开相机: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "需要相机权限才能拍照", Toast.LENGTH_SHORT).show()
        }
    }
    
    // 检查并请求相机权限
    fun checkAndRequestCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                // 已有权限，直接启动相机
                try {
                    val avatarDir = File(context.filesDir, "avatar")
                    if (!avatarDir.exists()) {
                        avatarDir.mkdirs()
                    }
                    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                    val photoFile = File(avatarDir, "camera_$timeStamp.jpg")
                    val photoUri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        photoFile
                    )
                    cameraPhotoUri = photoUri
                    cameraLauncher.launch(photoUri)
                } catch (e: Exception) {
                    Toast.makeText(context, "无法打开相机: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
            else -> {
                // 请求权限
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        // 个人信息卡片
        item {
            ProfileInfoCard(
                nickname = nickname,
                callsign = callsign,
                avatarUri = avatarUri,
                onAvatarClick = { showImageSourceDialog = true },
                onNicknameClick = { showNicknameDialog = true },
                onCallsignClick = { showCallsignDialog = true }
            )
        }

        // 功能区域
        item {
            Text(
                text = "设置",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        item {
            SettingsSection(
                onClearCacheClick = { showClearCacheDialog = true },
                onAboutClick = onAboutClick
            )
        }

        // 版本信息
        item {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "版本 1.0.0",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    // 选择图片来源对话框
    if (showImageSourceDialog) {
        AlertDialog(
            onDismissRequest = { showImageSourceDialog = false },
            icon = { Icon(Icons.Default.Person, contentDescription = null) },
            title = { Text("选择头像") },
            text = {
                Column {
                    TextButton(
                        onClick = {
                            showImageSourceDialog = false
                            imagePickerLauncher.launch("image/*")
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.AccountCircle, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("从相册选择")
                    }
                    TextButton(
                        onClick = {
                            showImageSourceDialog = false
                            // 检查并请求相机权限
                            checkAndRequestCameraPermission()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("拍照")
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showImageSourceDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
    
    // 昵称编辑对话框
    if (showNicknameDialog) {
        NicknameEditDialog(
            currentNickname = if (nickname == "未设置") "" else nickname,
            onDismiss = { showNicknameDialog = false },
            onConfirm = { newNickname ->
                scope.launch {
                    userPreferences.saveNickname(newNickname)
                    Toast.makeText(context, "昵称已更新", Toast.LENGTH_SHORT).show()
                }
                showNicknameDialog = false
            }
        )
    }
    
    // 呼号编辑对话框
    if (showCallsignDialog) {
        CallsignEditDialog(
            currentCallsign = if (callsign == "未设置") "" else callsign,
            onDismiss = { showCallsignDialog = false },
            onConfirm = { newCallsign ->
                scope.launch {
                    userPreferences.saveCallsign(newCallsign)
                    Toast.makeText(context, "呼号已更新", Toast.LENGTH_SHORT).show()
                }
                showCallsignDialog = false
            }
        )
    }

    // 清除缓存确认对话框
    if (showClearCacheDialog) {
        AlertDialog(
            onDismissRequest = { showClearCacheDialog = false },
            icon = { Icon(Icons.Default.Delete, contentDescription = null) },
            title = { Text("清除缓存") },
            text = { 
                Text("确定要清除所有缓存吗？\n\n这将删除图片缓存和网络请求缓存，但不会影响题库数据、通联日志和学习记录。") 
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        // 清除缓存逻辑
                        clearCache(context)
                        Toast.makeText(context, "缓存已清除", Toast.LENGTH_SHORT).show()
                        showClearCacheDialog = false
                    }
                ) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearCacheDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
}

/**
 * 个人信息卡片
 */
@Composable
fun ProfileInfoCard(
    nickname: String,
    callsign: String,
    avatarUri: String?,
    onAvatarClick: () -> Unit,
    onNicknameClick: () -> Unit,
    onCallsignClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 头像
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .clickable { onAvatarClick() },
                contentAlignment = Alignment.Center
            ) {
                if (avatarUri != null) {
                    // 支持本地文件路径和 content URI
                    val imageModel = if (avatarUri.startsWith("/")) {
                        File(avatarUri)  // 本地文件路径
                    } else {
                        avatarUri  // content:// URI (兼容旧数据)
                    }
                    
                    AsyncImage(
                        model = imageModel,
                        contentDescription = "头像",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "修改头像",
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }

            // 昵称
            ProfileInfoItem(
                label = "昵称",
                value = nickname,
                icon = Icons.Default.Edit,
                onClick = onNicknameClick
            )

            // 呼号
            ProfileInfoItem(
                label = "呼号",
                value = callsign,
                icon = Icons.Default.Notifications,
                onClick = onCallsignClick
            )
        }
    }
}

/**
 * 个人信息项
 */
@Composable
fun ProfileInfoItem(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Column {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = value,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = "编辑",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * 设置区域
 */
@Composable
fun SettingsSection(
    onClearCacheClick: () -> Unit,
    onAboutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SettingItem(
            title = "清除缓存",
            description = "清除图片和网络请求缓存",
            icon = Icons.Default.Delete,
            onClick = onClearCacheClick
        )

        SettingItem(
            title = "关于",
            description = "查看应用信息和开发者信息",
            icon = Icons.Default.Info,
            onClick = onAboutClick
        )
    }
}

/**
 * 设置项卡片
 */
@Composable
fun SettingItem(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth()
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
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = "进入",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * 昵称编辑对话框
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NicknameEditDialog(
    currentNickname: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var nickname by remember { mutableStateOf(currentNickname) }
    var error by remember { mutableStateOf<String?>(null) }
    val keyboardController = LocalSoftwareKeyboardController.current

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Edit, contentDescription = null) },
        title = { Text("编辑昵称") },
        text = {
            Column {
                OutlinedTextField(
                    value = nickname,
                    onValueChange = {
                        nickname = it
                        error = when {
                            it.isEmpty() -> "昵称不能为空"
                            it.length < 2 -> "昵称至少2个字符"
                            it.length > 20 -> "昵称最多20个字符"
                            else -> null
                        }
                    },
                    label = { Text("昵称") },
                    supportingText = {
                        Text("2-20个字符，支持中英文")
                    },
                    isError = error != null,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            keyboardController?.hide()
                            if (error == null && nickname.isNotEmpty()) {
                                onConfirm(nickname)
                            }
                        }
                    ),
                    singleLine = true
                )
                if (error != null) {
                    Text(
                        text = error!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(nickname) },
                enabled = error == null && nickname.isNotEmpty()
            ) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

/**
 * 呼号编辑对话框
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CallsignEditDialog(
    currentCallsign: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var callsign by remember { mutableStateOf(currentCallsign) }
    var error by remember { mutableStateOf<String?>(null) }
    val keyboardController = LocalSoftwareKeyboardController.current

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Notifications, contentDescription = null) },
        title = { Text("编辑呼号") },
        text = {
            Column {
                OutlinedTextField(
                    value = callsign,
                    onValueChange = {
                        callsign = it.uppercase()
                        error = if (it.isNotEmpty() && !isValidCallsign(it)) {
                            "呼号格式不正确"
                        } else {
                            null
                        }
                    },
                    label = { Text("呼号") },
                    supportingText = {
                        Text("例如：BG1ABC (可选填)")
                    },
                    isError = error != null,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            keyboardController?.hide()
                            if (error == null) {
                                onConfirm(callsign)
                            }
                        }
                    ),
                    singleLine = true
                )
                if (error != null) {
                    Text(
                        text = error!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(callsign) },
                enabled = error == null
            ) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

/**
 * 验证呼号格式
 * 简单的正则验证：字母+数字+字母（如BG1ABC）
 */
fun isValidCallsign(callsign: String): Boolean {
    val pattern = Regex("^[A-Z]{1,2}\\d{1,2}[A-Z]{1,4}$")
    return callsign.matches(pattern)
}

/**
 * 清除缓存
 */
fun clearCache(context: Context) {
    try {
        // 清除应用缓存目录
        context.cacheDir.deleteRecursively()
        
        // 清除外部缓存目录
        context.externalCacheDir?.deleteRecursively()
        
        // 清除 Coil 图片缓存
        // ImageLoader 会在需要时自动重新创建
        
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
