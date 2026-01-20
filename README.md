# 业余无线电工具箱 (Amateur Radio Helper)

<p align="center">
  <img src="app/src/main/res/mipmap-xxxhdpi/ic_launcher.webp" width="120" alt="App Icon"/>
</p>

<p align="center">
  一款专为业余无线电爱好者设计的综合性工具应用
</p>

<p align="center">
  <a href="LICENSE"><img src="https://img.shields.io/badge/License-CC%20BY--SA%204.0-lightgrey.svg" alt="License: CC BY-SA 4.0"/></a>
  <img src="https://img.shields.io/badge/Android-12%2B-green.svg" alt="Android 12+"/>
  <img src="https://img.shields.io/badge/Kotlin-2.0-blue.svg" alt="Kotlin 2.0"/>
</p>

---

## ✨ 功能特性

### 📚 考试模块
- **题库练习**：支持 A/B/C 三类业余无线电操作证考试题库
- **顺序背题**：按顺序浏览题目，快速记忆
- **顺序练习**：逐题作答，即时反馈
- **随机练习**：打乱顺序，强化记忆
- **模拟考试**：真实模拟考试环境，计时作答
- **错题本**：自动记录错题，专项训练
- **收藏夹**：收藏重点题目，反复练习

### 🔍 发现模块
- **无线电传播预测**：实时查看 HF 传播条件
- **ITU 及 CQ 分区图**：世界无线电分区速查
- **国内电台分区**：中国业余电台分区信息
- **呼号前缀查询**：快速查询各国呼号前缀
- **字母解释法**：国际无线电通话字母表
- **CW 电码表**：摩尔斯电码速查
- **波长计算器**：频率与波长快速换算
- **通联日志**：记录和管理 QSO 日志，支持导入/导出

### 👤 我的模块
- **个人信息**：设置呼号、昵称等信息
- **学习统计**：查看学习进度和成绩
- **数据管理**：清除缓存等功能

## 📱 截图

*即将添加*

## 🛠️ 技术栈

| 技术 | 说明 |
|------|------|
| **Kotlin 2.0** | 主开发语言 |
| **Jetpack Compose** | 现代声明式 UI 框架 |
| **Material Design 3** | Google 最新设计规范 |
| **Room** | 本地数据库 |
| **DataStore** | 偏好设置存储 |
| **Coil** | 图片加载 |
| **MVVM** | 架构模式 |

## 📦 题库信息

| 类别 | 题目数量 | 考试题数 | 考试时间 | 及格线 |
|------|----------|----------|----------|--------|
| A 类 | 683 题 | 40 题 | 30 分钟 | 80% |
| B 类 | 1143 题 | 60 题 | 50 分钟 | 80% |
| C 类 | 1282 题 | 90 题 | 80 分钟 | 80% |

## 🚀 快速开始

### 环境要求
- Android Studio Ladybug (2024.2.1) 或更高版本
- JDK 17
- Android SDK 36

### 构建步骤

1. 克隆仓库
```bash
git clone https://github.com/bg6hxj/amrahelper.git
cd amrahelper
```

2. 配置本地属性（可选，用于签名发布版本）

在 `local.properties` 中添加：
```properties
KEYSTORE_FILE=/path/to/your/keystore.jks
KEY_ALIAS=your_key_alias
KEYSTORE_PASSWORD=your_keystore_password
KEY_PASSWORD=your_key_password
```

3. 构建并运行
```bash
./gradlew assembleDebug
```

或在 Android Studio 中直接点击运行按钮。

## 📄 开源协议

本项目采用 [CC BY-SA 4.0](LICENSE) 协议开源。

这意味着您可以：
- ✅ 自由分享和修改本项目
- ✅ 用于商业目的
- ⚠️ 必须注明原作者
- ⚠️ 修改后需使用相同协议发布

## 🤝 贡献

欢迎提交 Issue 和 Pull Request！

## 📞 联系方式

- **呼号**: BG6HXJ
- **GitHub**: [BG6HXJ](https://github.com/BG6HXJ)

---

**73 de BG6HXJ** 🎙️
