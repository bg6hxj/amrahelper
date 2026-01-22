package bg6hxj.amatureradiohelper.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * 权限收集清单页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionListScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("权限收集清单") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "应用权限收集清单",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = "本应用为了提供完整的功能服务，可能需要申请以下系统权限。您可以在系统设置中随时管理这些权限。",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Divider()
            
            // 网络权限
            PermissionCard(
                icon = Icons.Default.Share,
                permissionName = "网络权限",
                permissionId = "android.permission.INTERNET",
                isRequired = true,
                purpose = "获取传播预测图片和地图数据",
                scenario = "当您使用传播预测功能查看短波传播预测图时，以及使用通联日志的地图定位功能时，需要访问网络获取相关数据。",
                consequence = "如果拒绝此权限，传播预测功能和地图功能将无法正常使用。"
            )
            
            // 存储权限
            PermissionCard(
                icon = Icons.Default.Create,
                permissionName = "存储权限",
                permissionId = "android.permission.READ_EXTERNAL_STORAGE\nandroid.permission.READ_MEDIA_IMAGES",
                isRequired = false,
                purpose = "读取设备中的图片文件",
                scenario = "当您想要从相册中选择图片作为头像时，需要读取您设备存储中的图片。",
                consequence = "如果拒绝此权限，您将无法从相册选择图片作为头像，但可以使用拍照功能设置头像。"
            )
            
            // 相机权限
            PermissionCard(
                icon = Icons.Default.AccountCircle,
                permissionName = "相机权限",
                permissionId = "android.permission.CAMERA",
                isRequired = false,
                purpose = "拍摄照片",
                scenario = "当您想要拍摄新照片作为头像时，需要使用设备的相机功能。",
                consequence = "如果拒绝此权限，您将无法拍照设置头像，但可以从相册选择已有图片。"
            )
            
            Divider()
            
            // 说明卡片
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "权限管理说明",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    
                    Text(
                        text = """
• 本应用仅在需要使用相关功能时才会申请权限
• 您可以随时在系统设置中开启或关闭权限
• 关闭权限不会影响应用的其他功能
• 本应用不会在后台持续使用这些权限
• 所有通过权限获取的数据仅存储在您的设备本地
                        """.trimIndent(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            
            // 如何管理权限
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "如何管理应用权限",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        text = """
1. 打开手机的"设置"应用
2. 找到"应用"或"应用管理"
3. 找到并点击"业余无线电工具箱"
4. 点击"权限"或"权限管理"
5. 在此处可以开启或关闭各项权限
                        """.trimIndent(),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            
            // 联系方式
            Text(
                text = "如果您对应用权限有任何疑问，请联系我们：\nwulig123@outlook.com",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun PermissionCard(
    icon: ImageVector,
    permissionName: String,
    permissionId: String,
    isRequired: Boolean,
    purpose: String,
    scenario: String,
    consequence: String
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 标题行
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = permissionName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = permissionId,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                AssistChip(
                    onClick = { },
                    label = {
                        Text(if (isRequired) "必需" else "可选")
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = if (isRequired) 
                            MaterialTheme.colorScheme.errorContainer 
                        else 
                            MaterialTheme.colorScheme.secondaryContainer,
                        labelColor = if (isRequired)
                            MaterialTheme.colorScheme.onErrorContainer
                        else
                            MaterialTheme.colorScheme.onSecondaryContainer
                    )
                )
            }
            
            Divider()
            
            // 用途
            PermissionInfoRow(
                label = "用途",
                content = purpose
            )
            
            // 使用场景
            PermissionInfoRow(
                label = "使用场景",
                content = scenario
            )
            
            // 拒绝后果
            PermissionInfoRow(
                label = "拒绝影响",
                content = consequence
            )
        }
    }
}

@Composable
private fun PermissionInfoRow(
    label: String,
    content: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = content,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
