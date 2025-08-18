# VueKit 组件库导入模板指南

## 概述

本文档说明了 VueKit 插件中组件库导入模板的最新配置规则和结构。模板遵循最新的数据模型，支持动态配置和类型安全的默认值。

## 模板结构

### 组件库基本信息

```json
{
  "id": "my-component-library",
  "name": "my-component-library",
  "displayName": "我的组件库",
  "description": "这是一个自定义组件库的示例",
  "version": "1.0.0",
  "source": "CUSTOM_LOCAL",
  "sourceUrl": "",
  "componentPrefix": "my-",
  "lastUpdated": "2024-12-01T10:00:00",
  "components": [...]
}
```

### 关键字段说明

- **`id`**: 组件库唯一标识符
- **`name`**: 组件库包名（通常与 npm 包名一致）
- **`displayName`**: 显示名称（用于 UI 展示）
- **`componentPrefix`**: 组件前缀（如 "el-", "a-", "my-"）
- **`source`**: 来源类型（"OFFICIAL", "CUSTOM_LOCAL", "CUSTOM_REMOTE"）

## 组件定义

### 组件基本信息

```json
{
  "name": "my-button",
  "displayName": "自定义按钮",
  "description": "一个功能丰富的自定义按钮组件",
  "tag": "my-button",
  "documentation": "详细的使用说明和文档",
  "props": [...],
  "events": [...],
  "slots": [...]
}
```

### 属性定义

```json
{
  "name": "disabled",
  "type": "boolean",
  "description": "是否禁用按钮",
  "defaultValue": false,
  "required": false,
  "options": null
}
```

#### 支持的属性类型

- **`string`**: 字符串类型，默认值用双引号包围
- **`boolean`**: 布尔类型，默认值直接使用 `true` 或 `false`
- **`number`**: 数字类型，默认值直接使用数字
- **`object`**: 对象类型，默认值用双引号包围
- **`array`**: 数组类型，默认值用双引号包围

#### 默认值规则

- **字符串类型**: `"defaultValue": "文本内容"`
- **布尔类型**: `"defaultValue": false`
- **数字类型**: `"defaultValue": 100`
- **空值**: `"defaultValue": ""` 或 `"defaultValue": null`

### 事件定义

```json
{
  "name": "click",
  "description": "点击按钮时触发",
  "parameters": "event"
}
```

### 插槽定义

```json
{
  "name": "default",
  "description": "按钮内容",
  "scope": ""
}
```

## 完整示例

### 按钮组件

```json
{
  "name": "my-button",
  "displayName": "自定义按钮",
  "description": "一个功能丰富的自定义按钮组件，支持多种类型和状态",
  "tag": "my-button",
  "documentation": "这是一个自定义按钮组件，支持多种类型和状态。包括主要按钮、成功按钮、警告按钮、危险按钮等。",
  "props": [
    {
      "name": "type",
      "type": "string",
      "description": "按钮类型，支持多种预设样式",
      "defaultValue": "default",
      "required": false,
      "options": ["primary", "success", "warning", "danger", "info", "default"]
    },
    {
      "name": "disabled",
      "type": "boolean",
      "description": "是否禁用按钮",
      "defaultValue": false,
      "required": false
    },
    {
      "name": "size",
      "type": "string",
      "description": "按钮尺寸",
      "defaultValue": "medium",
      "required": false,
      "options": ["large", "medium", "small", "mini"]
    }
  ],
  "events": [
    {
      "name": "click",
      "description": "点击按钮时触发",
      "parameters": "event"
    }
  ],
  "slots": [
    {
      "name": "default",
      "description": "按钮内容",
      "scope": ""
    }
  ]
}
```

## 配置规则更新

### 1. 移除硬编码枚举

- 不再使用 `LibraryType` 枚举
- 直接使用字符串包名标识组件库
- 支持动态发现和配置

### 2. 类型安全的默认值

- `defaultValue` 字段支持多种类型
- 根据类型自动处理引号
- 代码补全时正确插入值

### 3. 组件前缀支持

- 新增 `componentPrefix` 字段
- 用于自动补全和组件识别
- 支持自定义前缀

### 4. 动态配置

- 支持运行时注册新组件库
- 自动发现已安装的组件库
- 智能推断配置信息

## 使用建议

### 1. 命名规范

- 组件库 ID 使用小写字母和连字符
- 组件名称使用小写字母和连字符
- 属性名称使用 camelCase

### 2. 类型定义

- 明确指定属性类型
- 使用正确的默认值类型
- 提供完整的描述信息

### 3. 文档完整性

- 为每个组件提供详细描述
- 说明属性的用途和限制
- 提供使用示例

## 模板文件位置

- **Java 生成器**: `src/main/java/com/chu7/vuecomponentassistant/utils/ComponentLibraryTemplateGenerator.java`
- **JSON 模板**: `examples/component-library-template.json`
- **配置文件模板**: `src/main/resources/templates/vuekit-libraries-config.json`

## 版本兼容性

- **v2.0.0+**: 支持新的模板结构
- **v1.x**: 向后兼容旧格式
- **迁移**: 自动升级旧配置到新格式

## 常见问题

### Q: 如何添加新的组件库类型？
A: 直接在 `enabledLibraries` 数组中添加包名即可，系统会自动发现和配置。

### Q: 布尔类型的默认值如何处理？
A: 直接使用 `true` 或 `false`，不需要引号。

### Q: 组件前缀有什么作用？
A: 用于自动补全时识别组件来源，以及生成正确的组件标签。

### Q: 如何自定义组件库？
A: 参考模板创建 JSON 文件，包含完整的组件定义，然后通过导入功能添加到系统中。
