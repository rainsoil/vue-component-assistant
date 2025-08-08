# VueKit

[![Version](https://img.shields.io/badge/version-2.0.0-blue.svg)](https://github.com/rainsoil/vuekit)
[![IntelliJ IDEA](https://img.shields.io/badge/IntelliJ%20IDEA-2023.1+-orange.svg)](https://www.jetbrains.com/idea/)
[![License](https://img.shields.io/badge/license-MIT-green.svg)](LICENSE)

一个专为 Vue.js 开发者设计的 IntelliJ IDEA 插件，提供智能组件补全、文档提示和开发辅助功能。

## 🌟 主要功能

### 1. 智能组件补全
- **多组件库支持**：支持 Element Plus、Element UI、Ant Design Vue 等主流组件库
- **前缀过滤**：根据输入的前缀智能过滤组件
- **自动标签补全**：自动插入完整的组件标签结构
- **自定义组件库**：支持导入和管理自定义组件库

### 2. 属性、事件、插槽补全
- **属性补全**：在组件标签内输入空格时提供属性列表
- **事件补全**：输入 `@` 时提供事件列表
- **插槽补全**：输入 `sl` 或 `slot` 时提供插槽列表
- **智能过滤**：根据输入内容智能过滤相关选项

### 3. 实时文档显示
- **悬停文档**：鼠标悬停时显示详细组件文档
- **右键文档**：右键菜单快速查看组件文档
- **表格展示**：属性、事件、插槽以表格形式展示
- **中文支持**：完整的中文文档和描述

### 4. 多组件库支持
- **自动检测**：根据项目的 `package.json` 自动检测使用的组件库
- **手动配置**：支持手动配置组件库
- **动态切换**：支持在多个组件库间动态切换

### 5. 自定义组件库管理
- **JSON导入**：支持通过JSON文件导入自定义组件库
- **持久化存储**：自定义组件库数据持久化，重启后不丢失
- **模板导出**：提供自定义组件库模板导出功能
- **批量管理**：支持批量导入、导出、删除操作

## 📦 支持的组件库

| 组件库 | 版本 | 状态 | 说明 |
|--------|------|------|------|
| Element Plus | 最新版本 | ✅ 支持 | 现代化的Vue 3组件库 |
| Element UI | 经典版本 | ✅ 支持 | Vue 2经典组件库 |
| Ant Design Vue | 最新版本 | ✅ 支持 | 企业级UI组件库 |
| 自定义组件库 | 任意版本 | ✅ 支持 | 用户自定义组件库 |

## 🚀 快速开始

### 安装插件

1. 在 IntelliJ IDEA 中打开 **Settings/Preferences**
2. 选择 **Plugins**
3. 搜索 "VueKit"
4. 点击 **Install** 安装插件
5. 重启 IntelliJ IDEA

### 基本使用

#### 1. 组件补全
```vue
<template>
  <!-- 输入 < 时显示组件列表 -->
  <el-button type="primary">按钮</el-button>
  
  <!-- 输入 <my- 时显示自定义组件 -->
  <my-button type="primary">自定义按钮</my-button>
</template>
```

#### 2. 属性补全
```vue
<template>
  <!-- 在组件标签内输入空格时显示属性列表 -->
  <el-input 
    v-model="value"
    placeholder="请输入内容"
    clearable
    disabled
  />
</template>
```

#### 3. 事件补全
```vue
<template>
  <!-- 输入 @ 时显示事件列表 -->
  <el-button @click="handleClick">按钮</el-button>
  <el-input @input="handleInput" @change="handleChange" />
</template>
```

#### 4. 插槽补全
```vue
<template>
  <!-- 输入 sl 或 slot 时显示插槽列表 -->
  <el-card>
    <template #header>卡片头部</template>
    <template #default>卡片内容</template>
  </el-card>
</template>
```

## 📚 自定义组件库

### JSON格式说明

#### 单个组件库格式
```json
{
  "name": "my-custom-library",
  "displayName": "我的自定义组件库",
  "version": "1.0.0",
  "description": "这是一个示例自定义组件库",
  "componentPrefix": "my-",
  "documentationUrlTemplate": "https://example.com/docs/%s",
  "components": [
    {
      "name": "my-button",
      "description": "自定义按钮组件",
      "version": "1.0.0",
      "example": "<my-button type=\"primary\">按钮</my-button>",
      "docUrl": "https://example.com/my-button",
      "props": [
        {
          "name": "type",
          "type": "string",
          "description": "按钮类型",
          "defaultValue": "default",
          "required": false,
          "options": ["primary", "success", "warning", "danger", "info", "default"]
        }
      ],
      "events": [
        {
          "name": "click",
          "description": "点击事件",
          "parameters": "event"
        }
      ],
      "slots": [
        {
          "name": "default",
          "description": "按钮内容"
        }
      ]
    }
  ]
}
```

#### 多个组件库格式
```json
[
  {
    "name": "library1",
    "displayName": "组件库1",
    "components": [...]
  },
  {
    "name": "library2", 
    "displayName": "组件库2",
    "components": [...]
  }
]
```

### 导入自定义组件库

1. 打开 **Tools** → **📚 组件库管理**
2. 点击 **上传组件库**
3. 选择JSON文件
4. 点击 **确定** 完成导入

### 管理自定义组件库

- **查看组件库**：在组件库管理界面查看所有已导入的组件库
- **删除组件库**：选择组件库后点击删除按钮
- **导出组件库**：将组件库配置导出为JSON文件
- **导出模板**：获取自定义组件库的模板文件

## ⚙️ 配置说明

### 组件库检测

插件会自动检测项目中使用的组件库：

1. 读取项目根目录的 `package.json` 文件
2. 检查 `dependencies` 和 `devDependencies` 中的组件库
3. 根据检测结果自动加载对应的组件数据

### 缓存机制

- **缓存位置**：`用户主目录/.intellij_idea_system/vuekit/custom_component_libraries.json`
- **自动保存**：数据变更时自动保存到缓存文件
- **自动加载**：启动时自动从缓存文件加载数据

## 🔧 开发指南

### 项目结构

```
vuekit/
├── src/main/java/com/chu7/vuecomponentassistant/
│   ├── action/                    # 动作处理类
│   ├── completion/                # 补全相关类
│   ├── documentation/             # 文档相关类
│   ├── settings/                  # 设置相关类
│   ├── ui/                        # UI组件类
│   └── utils/                     # 工具类
├── src/main/resources/
│   ├── data/                      # 组件数据文件
│   ├── icons/                     # 图标资源
│   └── META-INF/                  # 插件配置
├── test/                          # 测试文件
└── build.gradle                   # 构建配置
```

### 核心类说明

#### CustomComponentLibraryManager
自定义组件库管理器，负责：
- 加载和管理自定义组件库
- 数据持久化
- 组件查询和验证

#### ElementPlusTestCompletionProvider
智能补全提供者，负责：
- 分析用户输入上下文
- 提供组件、属性、事件、插槽补全
- 支持前缀过滤和智能匹配

#### ElementPlusDocumentationProvider
文档提供者，负责：
- 生成组件文档HTML
- 处理悬停和右键文档显示
- 支持表格格式展示

### 扩展开发

#### 添加新的组件库支持

1. 在 `src/main/resources/data/` 目录下添加组件库JSON文件
2. 在 `ComponentProvider` 中添加组件库检测逻辑
3. 更新文档生成逻辑以支持新组件库

#### 自定义补全逻辑

1. 继承 `CompletionProvider` 类
2. 实现 `addCompletions` 方法
3. 在 `plugin.xml` 中注册补全提供者

## 🐛 常见问题

### Q: 为什么输入 `<el-` 时没有显示组件补全？
A: 请检查以下几点：
1. 确保项目中有 `package.json` 文件
2. 确保 `package.json` 中包含了 Element Plus 依赖
3. 确保插件已正确安装并启用

### Q: 自定义组件库导入后重启IDEA就丢失了？
A: 请检查以下几点：
1. 确保缓存目录有写入权限
2. 检查控制台是否有错误日志
3. 尝试重新导入组件库

### Q: 如何查看插件的调试信息？
A: 在 IntelliJ IDEA 中：
1. 打开 **Help** → **Diagnostic Tools** → **Debug Log Settings**
2. 添加日志配置：`com.chu7.vuecomponentassistant`
3. 重启IDEA查看控制台输出

## 📄 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情。

## 🤝 贡献

欢迎提交 Issue 和 Pull Request！

### 贡献指南

1. Fork 本项目
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 打开 Pull Request

## 📞 联系方式

- 邮箱：luyanan0718@163.com
- 项目地址：https://github.com/rainsoil/vuekit
- 问题反馈：https://github.com/rainsoil/vuekit/issues

## 📝 更新日志

### v2.0.0 (2024-01-XX)
- ✨ 新增自定义组件库支持
- ✨ 新增数据持久化功能
- ✨ 新增组件库管理界面
- 🐛 修复组件补全前缀识别问题
- 🐛 修复文档显示编码问题
- 📚 完善文档和注释

### v1.0.2 (2024-01-XX)
- ✨ 新增多组件库支持
- ✨ 新增智能组件库检测
- ✨ 新增表格格式文档显示
- 🐛 修复各种编译错误

### v1.0.0 (2024-01-XX)
- 🎉 首次发布
- ✨ 基础组件补全功能
- ✨ 基础文档显示功能
- ✨ 支持 Element Plus 组件库

---

**VueKit** - 让 Vue.js 开发更高效！ 🚀