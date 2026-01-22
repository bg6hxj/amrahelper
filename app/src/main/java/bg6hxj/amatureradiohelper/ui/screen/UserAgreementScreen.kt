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
 * 用户协议页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserAgreementScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("用户协议") },
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
                text = "业余无线电工具箱用户协议",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = "更新日期：2026年1月22日\n生效日期：2026年1月22日",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Divider()
            
            SectionTitle("一、协议的接受与修改")
            SectionContent("""
欢迎使用"业余无线电工具箱"（以下简称"本应用"）。在使用本应用前，请您仔细阅读本用户协议（以下简称"本协议"）。

当您下载、安装或使用本应用时，即表示您已阅读、理解并同意接受本协议的全部条款。如果您不同意本协议的任何条款，请勿使用本应用。

我们保留随时修改本协议的权利。修改后的协议将在应用内公布，您继续使用本应用即视为接受修改后的协议。
            """.trimIndent())
            
            SectionTitle("二、服务内容")
            SectionContent("""
本应用为业余无线电爱好者提供以下服务：

1. 考试题库练习：包括顺序练习、随机练习、模拟考试等功能
2. 基础知识速查：提供Q简语、莫尔斯电码、CQWW分区等参考资料
3. 通联日志管理：记录和管理您的业余无线电通联记录
4. 传播预测查看：查看短波传播预测图
5. 工具计算器：提供波长计算等实用工具

本应用提供的题库内容来源于中国无线电协会公开的官方题库，仅供学习参考使用。
            """.trimIndent())
            
            SectionTitle("三、用户行为规范")
            SectionContent("""
您在使用本应用时应遵守以下规范：

1. 遵守中华人民共和国相关法律法规
2. 遵守《中华人民共和国无线电管理条例》
3. 不得利用本应用从事任何违法违规活动
4. 不得对本应用进行反向工程、反编译或反汇编
5. 不得利用本应用传播任何有害信息

您理解并同意，如因您违反上述规范而产生的任何法律责任由您自行承担。
            """.trimIndent())
            
            SectionTitle("四、知识产权")
            SectionContent("""
1. 本应用的名称、图标、界面设计、源代码等均受知识产权法律保护

2. 本应用采用 CC BY-SA 4.0 开源协议，您可以在遵守该协议的前提下使用、修改和分发本应用的源代码

3. 题库内容的版权归中国无线电协会所有，本应用仅提供学习和练习功能

4. 用户在使用本应用过程中产生的通联日志等数据，其知识产权归用户所有
            """.trimIndent())
            
            SectionTitle("五、免责声明")
            SectionContent("""
1. 本应用按"现状"提供，不对应用的适用性、可靠性、准确性作任何明示或暗示的保证

2. 本应用提供的题库内容仅供学习参考，不保证与实际考试内容完全一致

3. 因网络状况、设备兼容性等原因导致的服务中断或数据丢失，本应用不承担责任

4. 对于因使用本应用而产生的任何直接或间接损失，本应用不承担责任

5. 本应用可能包含指向第三方网站的链接，对于第三方网站的内容和隐私政策，本应用不承担责任
            """.trimIndent())
            
            SectionTitle("六、数据安全")
            SectionContent("""
1. 您的学习进度、通联日志等数据存储在您的设备本地

2. 本应用不会将您的个人数据上传至服务器

3. 建议您定期备份重要数据，以防数据丢失

4. 卸载应用将会清除所有本地数据，请在卸载前做好备份
            """.trimIndent())
            
            SectionTitle("七、协议终止")
            SectionContent("""
1. 您可以随时停止使用本应用并卸载

2. 如您违反本协议的任何条款，我们有权终止向您提供服务

3. 协议终止后，您应停止使用本应用，相关条款中依据其性质应继续有效的条款将继续有效
            """.trimIndent())
            
            SectionTitle("八、适用法律与争议解决")
            SectionContent("""
1. 本协议的订立、执行、解释及争议解决均适用中华人民共和国法律

2. 因本协议引起的或与本协议有关的任何争议，双方应友好协商解决

3. 协商不成的，任何一方均可向有管辖权的人民法院提起诉讼
            """.trimIndent())
            
            SectionTitle("九、联系方式")
            SectionContent("""
如果您对本协议有任何疑问或建议，请通过以下方式联系我们：

开发者：BG6HXJ
邮箱：wulig123@outlook.com
GitHub：github.com/bg6hxj/amrahelper
            """.trimIndent())
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 8.dp)
    )
}

@Composable
private fun SectionContent(content: String) {
    Text(
        text = content,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurface
    )
}
