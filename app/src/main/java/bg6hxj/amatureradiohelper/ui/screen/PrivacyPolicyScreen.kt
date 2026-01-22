package bg6hxj.amatureradiohelper.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * 隐私协议页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("隐私政策") },
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
                text = "业余无线电工具箱隐私政策",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = "更新日期：2026年1月22日\n生效日期：2026年1月22日",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Divider()
            
            PrivacySectionTitle("一、引言")
            PrivacySectionContent("""
"业余无线电工具箱"（以下简称"本应用"）非常重视用户的隐私保护。本隐私政策旨在向您说明我们如何收集、使用、存储和保护您的个人信息。

请您在使用本应用前仔细阅读本隐私政策。当您使用本应用时，即表示您已阅读、理解并同意本隐私政策的全部内容。
            """.trimIndent())
            
            PrivacySectionTitle("二、信息收集")
            PrivacySectionContent("""
本应用可能收集以下类型的信息：

【本地存储的信息】
1. 学习进度数据：包括已学习题目、错题记录、收藏题目等
2. 通联日志数据：您手动记录的业余无线电通联信息
3. 应用设置：您的偏好设置，如等级选择等
4. 头像图片：您选择设置的个人头像（如有）

【网络请求信息】
1. 传播预测图：从网络获取短波传播预测图片
2. 地图数据：用于显示QTH定位的地图瓦片

以上信息均存储在您的设备本地，不会上传至任何服务器。
            """.trimIndent())
            
            PrivacySectionTitle("三、信息使用")
            PrivacySectionContent("""
我们收集的信息仅用于以下目的：

1. 提供题库练习功能：记录和展示您的学习进度
2. 提供通联日志功能：存储和管理您的通联记录
3. 提供传播预测功能：显示短波传播预测信息
4. 改善用户体验：根据您的使用习惯优化应用功能

我们承诺：
- 不会将您的个人信息出售给第三方
- 不会将您的个人信息用于广告投放
- 不会在未经您同意的情况下分享您的个人信息
            """.trimIndent())
            
            PrivacySectionTitle("四、信息存储")
            PrivacySectionContent("""
【存储位置】
您的所有数据均存储在您的设备本地，包括：
- SQLite 数据库：存储学习进度、通联日志等结构化数据
- SharedPreferences：存储应用设置和偏好
- 本地文件：存储头像图片等文件

【存储期限】
- 数据将一直保存在您的设备上，直到您主动清除或卸载应用
- 卸载应用将清除所有本地数据

【数据安全】
- 所有数据存储在应用的私有目录中，其他应用无法访问
- 建议您定期备份重要数据
            """.trimIndent())
            
            PrivacySectionTitle("五、信息共享")
            PrivacySectionContent("""
本应用不会主动将您的个人信息共享给第三方，以下情况除外：

1. 获得您的明确同意后
2. 根据法律法规的规定或司法机关的要求
3. 为保护本应用、用户或公众的权益、财产或安全

本应用使用的第三方服务：
- 地图服务：用于显示QTH定位，可能会向地图服务提供商发送位置请求
- 网络图片加载：用于加载传播预测图，会向图片服务器发送请求
            """.trimIndent())
            
            PrivacySectionTitle("六、用户权利")
            PrivacySectionContent("""
您对您的个人信息享有以下权利：

1. 【查阅权】您可以在应用内查看您的学习进度、通联日志等信息

2. 【更正权】您可以修改您的通联日志、个人设置等信息

3. 【删除权】您可以：
   - 清除学习进度和缓存数据
   - 删除通联日志记录
   - 卸载应用以删除所有数据

4. 【导出权】您可以导出您的通联日志数据

5. 【撤回同意权】您可以在系统设置中随时撤回授予本应用的权限
            """.trimIndent())
            
            PrivacySectionTitle("七、权限说明")
            PrivacySectionContent("""
本应用可能申请以下系统权限：

1. 【网络权限】(INTERNET)
   - 用途：获取传播预测图、加载地图瓦片
   - 必要性：传播预测和地图功能必需

2. 【存储权限】(READ_EXTERNAL_STORAGE / READ_MEDIA_IMAGES)
   - 用途：选择图片作为头像
   - 必要性：更换头像功能需要

3. 【相机权限】(CAMERA)
   - 用途：拍照设置头像
   - 必要性：拍照功能需要

您可以在系统设置中随时开启或关闭这些权限。关闭权限可能导致相关功能无法正常使用。
            """.trimIndent())
            
            PrivacySectionTitle("八、未成年人保护")
            PrivacySectionContent("""
本应用适合所有年龄段用户使用。我们不会主动收集未成年人的个人信息。

如果您是未成年人的监护人，发现您监护的未成年人向我们提供了个人信息，请及时与我们联系，我们将及时删除相关信息。
            """.trimIndent())
            
            PrivacySectionTitle("九、隐私政策更新")
            PrivacySectionContent("""
我们可能会不时更新本隐私政策。更新后的政策将在应用内公布。

如果更新涉及重大变更，我们会在应用内通知您。您继续使用本应用即视为接受更新后的隐私政策。

建议您定期查阅本隐私政策，了解最新的隐私保护措施。
            """.trimIndent())
            
            PrivacySectionTitle("十、联系我们")
            PrivacySectionContent("""
如果您对本隐私政策有任何疑问、意见或建议，请通过以下方式联系我们：

开发者：BG6HXJ
邮箱：wulig123@outlook.com
GitHub：github.com/bg6hxj/amrahelper

我们将在收到您的反馈后尽快回复。
            """.trimIndent())
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun PrivacySectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 8.dp)
    )
}

@Composable
private fun PrivacySectionContent(content: String) {
    Text(
        text = content,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurface
    )
}
