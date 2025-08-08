# 🔧 自定义组件库管理指南

## 📋 功能概述

Vue Component Assistant 插件现在支持完整的自定义组件库管理功能，包括：

### 🎯 主要功能
1. **📚 组件展示** - 首先展示所有可用组件（内置 + 自定义）
2. **📦 组件库上传** - 上传自定义组件库 JSON 文件
3. **📤 组件库导出** - 导出任何组件库为 JSON 文件
4. **🗑️ 组件库删除** - 删除自定义组件库（内置组件库不可删除）
5. **📋 模板导出** - 导出组件库模板，方便创建新的组件库

### 🛡️ 安全特性
- **内置组件库保护**：Element Plus、Element UI、Ant Design Vue 等内置组件库不可删除
- **数据验证**：上传的 JSON 文件会进行格式验证
- **错误处理**：完善的错误提示和异常处理

## 🚀 使用方法

### 1. 打开管理界面
1. 在 IntelliJ IDEA 中打开 "工具" 菜单
2. 选择 "🔧 自定义组件库管理"
3. 首先会显示所有组件的概览
4. 然后可以选择执行管理操作

### 2. 组件展示
管理界面首先会展示所有可用组件，按组件库分组：
- **Element Plus** - Element Plus 组件库
- **Element UI** - Element UI 组件库  
- **Ant Design Vue** - Ant Design Vue 组件库
- **自定义组件库** - 用户上传的自定义组件库

每个组件库显示：
- 组件库名称和组件数量
- 组件列表（名称和描述）
- 格式化显示，便于查看

### 3. 管理操作

#### 📦 上传新组件库
1. 选择 "📦 上传新组件库"
2. 在弹出的对话框中选择 JSON 文件或直接输入 JSON 内容
3. 点击 "验证配置" 检查格式是否正确
4. 点击 "加载组件库" 完成上传

#### 📤 导出组件库
1. 选择 "📤 导出组件库"
2. 选择要导出的组件库（内置或自定义）
3. 选择保存位置
4. 组件库将以 JSON 格式导出

#### 🗑️ 删除自定义组件库
1. 选择 "🗑️ 删除自定义组件库"
2. 选择要删除的自定义组件库
3. 确认删除操作
4. **注意**：内置组件库不可删除

#### 📋 导出组件库模板
1. 选择 "📋 导出组件库模板"
2. 选择保存位置
3. 获得标准的组件库 JSON 模板

## 📦 JSON 格式规范

### 基本结构
```json
{
  "name": "my-component-library",
  "displayName": "我的组件库",
  "version": "1.0.0",
  "description": "自定义组件库描述",
  "componentPrefix": "my-",
  "components": [
    // 组件列表
  ]
}
```

### 字段说明
- **name**: 组件库唯一标识符（小写，用连字符分隔）
- **displayName**: 组件库显示名称
- **version**: 组件库版本
- **description**: 组件库描述
- **componentPrefix**: 组件前缀（如 "my-"）
- **components**: 组件数组

### 组件结构
```json
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
      "options": ["primary", "success", "warning", "danger"]
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
```

### 属性字段说明
- **name**: 属性名称
- **type**: 属性类型
- **description**: 属性描述
- **defaultValue**: 默认值
- **required**: 是否必需
- **options**: 可选值列表（可选）

### 事件字段说明
- **name**: 事件名称
- **description**: 事件描述
- **parameters**: 事件参数

### 插槽字段说明
- **name**: 插槽名称
- **description**: 插槽描述

## 📝 完整示例

### 示例组件库
```json
{
  "name": "my-ui-library",
  "displayName": "我的 UI 组件库",
  "version": "1.0.0",
  "description": "一个包含常用 UI 组件的自定义组件库",
  "componentPrefix": "my-",
  "components": [
    {
      "name": "my-button",
      "description": "自定义按钮组件，支持多种样式和状态",
      "version": "1.0.0",
      "example": "<my-button type=\"primary\" size=\"large\">主要按钮</my-button>",
      "docUrl": "https://example.com/components/button",
      "props": [
        {
          "name": "type",
          "type": "string",
          "description": "按钮类型",
          "defaultValue": "default",
          "required": false,
          "options": ["primary", "success", "warning", "danger", "info", "default"]
        },
        {
          "name": "size",
          "type": "string",
          "description": "按钮尺寸",
          "defaultValue": "medium",
          "required": false,
          "options": ["large", "medium", "small", "mini"]
        },
        {
          "name": "disabled",
          "type": "boolean",
          "description": "是否禁用",
          "defaultValue": false,
          "required": false
        }
      ],
      "events": [
        {
          "name": "click",
          "description": "点击事件",
          "parameters": "event"
        },
        {
          "name": "dblclick",
          "description": "双击事件",
          "parameters": "event"
        }
      ],
      "slots": [
        {
          "name": "default",
          "description": "按钮内容"
        },
        {
          "name": "icon",
          "description": "按钮图标"
        }
      ]
    },
    {
      "name": "my-input",
      "description": "自定义输入框组件，支持多种输入类型",
      "version": "1.0.0",
      "example": "<my-input v-model=\"value\" placeholder=\"请输入内容\" clearable />",
      "docUrl": "https://example.com/components/input",
      "props": [
        {
          "name": "type",
          "type": "string",
          "description": "输入框类型",
          "defaultValue": "text",
          "required": false,
          "options": ["text", "password", "email", "number", "tel", "url"]
        },
        {
          "name": "placeholder",
          "type": "string",
          "description": "占位符文本",
          "defaultValue": "",
          "required": false
        },
        {
          "name": "clearable",
          "type": "boolean",
          "description": "是否可清空",
          "defaultValue": false,
          "required": false
        }
      ],
      "events": [
        {
          "name": "input",
          "description": "输入事件",
          "parameters": "value"
        },
        {
          "name": "change",
          "description": "值改变事件",
          "parameters": "value"
        },
        {
          "name": "clear",
          "description": "清空事件",
          "parameters": ""
        }
      ],
      "slots": [
        {
          "name": "prefix",
          "description": "输入框前缀"
        },
        {
          "name": "suffix",
          "description": "输入框后缀"
        }
      ]
    }
  ]
}
```

## 🔧 高级功能

### 组件库检测
插件会自动检测项目中使用的组件库：
1. 读取 `package.json` 文件
2. 分析 `dependencies` 和 `devDependencies`
3. 根据依赖包名判断使用的组件库
4. 加载对应的组件数据

### 智能补全
- 根据检测到的组件库提供相应的组件补全
- 支持属性、事件、插槽的智能提示
- 显示组件所属的组件库名称

### 文档生成
- 自动生成组件的 HTML 文档
- 支持表格格式的属性、事件、插槽展示
- 提供官方文档链接

## 🐛 故障排除

### 常见问题

#### 1. 组件库检测失败
**问题**：插件无法正确检测项目使用的组件库
**解决**：
- 确保 `package.json` 文件存在且格式正确
- 检查依赖包名是否正确
- 重启 IntelliJ IDEA

#### 2. 自定义组件库上传失败
**问题**：上传 JSON 文件时出现错误
**解决**：
- 检查 JSON 格式是否正确
- 确保所有必需字段都存在
- 使用 "验证配置" 功能检查格式

#### 3. 组件补全不工作
**问题**：自定义组件没有出现在补全列表中
**解决**：
- 确保组件库已成功上传
- 检查组件前缀是否正确
- 重启 IntelliJ IDEA

#### 4. 内置组件库被误删
**问题**：内置组件库被意外删除
**解决**：
- 内置组件库不可删除，此问题不会发生
- 如果出现问题，重新安装插件

### 调试信息
插件会输出详细的调试信息，包括：
- 组件库检测过程
- 数据加载状态
- 错误详情

查看调试信息：
1. 打开 IntelliJ IDEA 的 "事件日志"
2. 查看插件相关的日志信息
3. 根据日志信息进行故障排除

## 📞 技术支持

如果遇到问题，请：
1. 查看本文档的故障排除部分
2. 检查 IntelliJ IDEA 的事件日志
3. 提交 Issue 到项目仓库

## 🔄 版本更新

### v2.0.0
- ✅ 新增完整的自定义组件库管理功能
- ✅ 支持组件库展示、上传、导出、删除
- ✅ 内置组件库保护机制
- ✅ 模板导出功能
- ✅ 改进的用户界面

### v1.0.0
- ✅ 基础的自定义组件库支持
- ✅ JSON 格式的组件数据
- ✅ 基本的组件补全功能

---

**注意**：本功能需要 IntelliJ IDEA 2023.1 或更高版本。
