# Vue Component Assistant (Vue组件助手)

[![Version](https://img.shields.io/badge/version-1.0.0-blue.svg)](https://plugins.jetbrains.com/plugin/vue-component-assistant)
[![IntelliJ IDEA](https://img.shields.io/badge/IntelliJ%20IDEA-2022.1+-orange.svg)](https://www.jetbrains.com/idea/)
[![License](https://img.shields.io/badge/license-MIT-green.svg)](LICENSE)

一个专为Vue.js开发者设计的IntelliJ IDEA插件，提供智能的组件补全、文档提示和开发辅助功能。

## 🚀 主要功能

### ✨ 智能组件补全
- 支持Element Plus、Element UI、Ant Design Vue等主流UI组件库
- 根据当前组件类型提供相关属性、事件、卡槽的智能提示
- 自动检测项目使用的组件库，无需手动配置

### 📚 实时文档显示
- 悬停或选中时显示详细的组件文档和使用示例
- 右键菜单快速查看组件、属性、事件的详细文档
- 包含组件描述、版本信息、使用示例、官方文档链接

### 🎯 智能提示功能
- **属性提示**: 输入 `:` 触发属性补全，显示类型、默认值、可选值
- **事件提示**: 输入 `@` 触发事件补全，显示参数信息和使用示例
- **卡槽提示**: 输入 `slot:` 触发卡槽补全，显示作用域和使用模板

### 🔧 自定义组件库
- 支持导入和管理自定义组件库
- 支持JSON格式的组件库配置文件
- 组件库模板生成和导出功能

## 📦 支持的组件库

| 组件库 | 版本 | 状态 |
|--------|------|------|
| Element Plus | 最新版本 | ✅ 支持 |
| Element UI | 经典版本 | ✅ 支持 |
| Ant Design Vue | 最新版本 | ✅ 支持 |
| 自定义组件库 | 任意 | ✅ 支持 |

## 🛠️ 安装使用

### 方式一：从JetBrains插件市场安装
1. 打开IntelliJ IDEA
2. 进入 `File` → `Settings` → `Plugins`
3. 搜索 "Vue Component Assistant"
4. 点击安装并重启IDE

### 方式二：手动安装
1. 下载插件文件 `vue-component-assistant-1.0.0.jar`
2. 进入 `File` → `Settings` → `Plugins`
3. 点击齿轮图标 → `Install Plugin from Disk`
4. 选择下载的jar文件并重启IDE

## 📖 使用指南

### 基本使用
1. **自动检测**: 插件会自动检测项目中的组件库依赖
2. **智能补全**: 在Vue模板中输入组件名称即可获得补全提示
3. **文档查看**: 悬停在组件或属性上查看详细文档

### 高级配置
1. **组件库设置**: 
   - 进入 `File` → `Settings` → `Tools` → `Vue Component Assistant`
   - 选择要使用的组件库
   - 启用/禁用自动检测功能

2. **自定义组件库**:
   - 在设置页面点击"管理自定义组件库"
   - 上传JSON格式的组件库配置文件
   - 查看和管理已导入的组件库

### 快捷键和触发方式
- **组件补全**: 直接输入组件名称
- **属性补全**: 输入 `:` 或 `:属性名`
- **事件补全**: 输入 `@` 或 `@事件名`
- **卡槽补全**: 输入 `slot:` 或 `slot:卡槽名`
- **文档查看**: 右键选择"显示文档"

## 📁 项目结构

```
vue-component-assistant/
├── src/main/java/com/chu7/
│   ├── ElementPlusCompletionContributor.java    # 补全贡献者
│   ├── ElementPlusDocumentationProvider.java    # 文档提供者
│   ├── ElementLibraryConfigurable.java          # 设置界面
│   ├── ElementLibrarySettings.java              # 设置管理
│   ├── ComponentLibraryManager.java             # 组件库管理器
│   ├── DocumentationDialog.java                 # 文档对话框
│   ├── ShowDocumentationAction.java             # 右键菜单动作
│   ├── ComponentMeta.java                      # 组件元数据
│   └── ComponentLibrary.java                   # 组件库数据
├── src/main/resources/
│   ├── META-INF/plugin.xml                     # 插件配置
│   └── data/                                   # 组件库数据
│       ├── element-plus-components.json         # Element Plus组件
│       ├── element-ui-components.json          # Element UI组件
│       ├── ant-design-vue-components.json      # Ant Design Vue组件
│       └── custom-component-library-example.json # 自定义组件库示例
├── build.gradle                                # Gradle构建配置
├── settings.gradle                             # 项目设置
└── README.md                                   # 项目说明
```

## 🔧 开发环境

### 系统要求
- IntelliJ IDEA 2022.1+
- Java 11+
- Gradle 7.0+

### 构建步骤
```bash
# 克隆项目
git clone https://github.com/chu7/vue-component-assistant.git

# 进入项目目录
cd vue-component-assistant

# 构建项目
./gradlew build

# 运行测试
./gradlew test
```

### 开发调试
1. 在IntelliJ IDEA中打开项目
2. 运行 `build.gradle` 中的 `runIde` 任务
3. 在沙盒环境中测试插件功能

## 📝 组件库配置文件格式

### 基本结构
```json
[
  {
    "name": "组件名称",
    "description": "组件描述",
    "version": "版本号",
    "example": "使用示例",
    "docUrl": "官方文档链接",
    "props": [
      {
        "name": "属性名",
        "type": "属性类型",
        "description": "属性描述",
        "defaultValue": "默认值",
        "required": false,
        "options": ["可选值1", "可选值2"]
      }
    ],
    "events": [
      {
        "name": "事件名",
        "description": "事件描述",
        "parameters": "参数信息"
      }
    ],
    "slots": [
      {
        "name": "卡槽名",
        "description": "卡槽描述",
        "scope": "作用域变量"
      }
    ]
  }
]
```

## 🤝 贡献指南

欢迎提交Issue和Pull Request！

### 开发流程
1. Fork本项目
2. 创建功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 创建Pull Request

### 代码规范
- 遵循Java编码规范
- 添加适当的注释和文档
- 确保代码通过所有测试

## 📄 许可证

本项目采用MIT许可证 - 查看 [LICENSE](LICENSE) 文件了解详情。

## 🙏 致谢

- [JetBrains](https://www.jetbrains.com/) - 提供优秀的IDE平台
- [Element Plus](https://element-plus.org/) - 优秀的Vue组件库
- [Element UI](https://element.eleme.io/) - 经典的Vue组件库
- [Ant Design Vue](https://antdv.com/) - 企业级Vue组件库

## 📞 联系方式

- 项目主页: [https://github.com/rainsoil/vue-component-assistant](https://github.com/chu7/vue-component-assistant)
- 问题反馈: [Issues](https://github.com/rainsoil/vue-component-assistant/issues)


---

⭐ 如果这个项目对您有帮助，请给我们一个星标！