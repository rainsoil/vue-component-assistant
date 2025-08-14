# 导出JSON模板功能使用指南

## 功能概述

VueKit组件助手提供了"导出JSON模板"功能，允许用户生成标准的组件库JSON模板文件。这个功能可以帮助开发者快速创建符合VueKit远程组件库规范的自定义组件库。

## 功能特点

- ✅ **标准化模板**：生成的模板完全符合VueKit远程组件库规范
- ✅ **丰富示例**：包含4个完整的组件示例（按钮、输入框、卡片、模态框）
- ✅ **完整定义**：每个组件都包含属性、事件、插槽的完整定义
- ✅ **UTF-8编码**：确保生成的JSON文件使用正确的UTF-8编码
- ✅ **详细说明**：提供详细的使用说明和指导

## 使用方法

### 1. 访问功能

1. 在IntelliJ IDEA中打开Vue项目
2. 打开 **Tools** → **📚 组件库管理**
3. 在组件库管理对话框中，点击工具栏中的 **📋 导出模板** 按钮

### 2. 选择保存位置

1. 系统会弹出文件选择对话框
2. 选择保存位置和文件名（默认：`component-library-template.json`）
3. 点击"保存"按钮

### 3. 查看生成结果

导出成功后，系统会显示详细的成功信息，包括：
- 保存位置
- 模板包含的内容
- 使用说明
- 导入方法

## 模板结构说明

生成的JSON模板包含以下结构：

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
  "lastUpdated": "2024-12-01T10:00:00"
}
```

### 组件定义
每个组件包含以下字段：
- `name`: 组件名称
- `displayName`: 显示名称
- `description`: 组件描述
- `tag`: 组件标签
- `documentation`: 详细文档
- `props`: 属性列表
- `events`: 事件列表
- `slots`: 插槽列表

### 属性定义
```json
{
  "name": "type",
  "type": "string",
  "description": "属性描述",
  "defaultValue": "default",
  "required": false,
  "options": ["option1", "option2"]
}
```

### 事件定义
```json
{
  "name": "click",
  "description": "事件描述",
  "parameters": "event"
}
```

### 插槽定义
```json
{
  "name": "default",
  "description": "插槽描述",
  "scope": ""
}
```

## 包含的示例组件

### 1. 按钮组件 (my-button)
- **功能**：支持多种类型和状态的按钮
- **属性**：type, size, disabled, loading, round, plain
- **事件**：click, focus, blur
- **插槽**：default, icon

### 2. 输入框组件 (my-input)
- **功能**：支持多种类型和验证的输入框
- **属性**：value, placeholder, type, disabled, readonly, clearable, maxlength, minlength
- **事件**：input, change, focus, blur, clear
- **插槽**：prefix, suffix

### 3. 卡片组件 (my-card)
- **功能**：灵活的卡片组件，用于展示内容块
- **属性**：header, shadow, bodyStyle
- **事件**：header-click
- **插槽**：header, default

### 4. 模态框组件 (my-modal)
- **功能**：功能完整的模态框组件
- **属性**：visible, title, width, closeOnClickMask, showClose
- **事件**：open, close, confirm, cancel
- **插槽**：header, default, footer

## 自定义模板

### 修改组件库信息
1. 打开生成的JSON文件
2. 修改基本信息字段：
   - `id`: 组件库唯一标识
   - `name`: 组件库名称
   - `displayName`: 显示名称
   - `description`: 描述信息
   - `version`: 版本号

### 添加新组件
1. 在`components`数组中添加新的组件对象
2. 按照示例格式定义组件的属性、事件、插槽
3. 确保组件名称唯一

### 修改现有组件
1. 找到要修改的组件
2. 根据需要修改属性、事件、插槽定义
3. 保持JSON格式的正确性

## 导入自定义组件库

### 方法1：本地文件导入
1. 修改模板文件并保存
2. 在VueKit中选择"上传自定义组件库"
3. 选择"本地文件"选项
4. 选择修改后的JSON文件
5. 点击"导入"按钮

### 方法2：远程URL导入
1. 将JSON文件上传到可访问的URL
2. 在VueKit中选择"上传自定义组件库"
3. 选择"远程URL"选项
4. 输入JSON文件的URL
5. 点击"导入"按钮

## 最佳实践

### 1. 命名规范
- 组件库ID使用小写字母和连字符
- 组件名称使用小写字母和连字符
- 属性名称使用驼峰命名法

### 2. 文档完整性
- 为每个组件提供清晰的描述
- 详细说明每个属性的用途和可选值
- 说明事件触发的条件和参数

### 3. 类型定义
- 使用标准的JavaScript类型：string, number, boolean, object, array
- 为枚举类型提供options数组
- 明确标注required属性

### 4. 版本管理
- 使用语义化版本号
- 在修改组件库时更新版本号
- 记录版本变更历史

## 故障排除

### 常见问题

**Q: 生成的JSON文件无法导入？**
A: 检查JSON格式是否正确，确保没有语法错误。

**Q: 组件显示不正确？**
A: 检查组件名称是否唯一，属性定义是否完整。

**Q: 编码问题？**
A: 确保文件使用UTF-8编码保存。

### 验证JSON格式
可以使用在线JSON验证工具验证生成的模板文件格式是否正确。

## 技术支持

如果在使用过程中遇到问题，请：
1. 检查JSON格式是否正确
2. 确认所有必需字段都已填写
3. 查看错误日志获取详细信息
4. 参考示例模板进行对比

---

*本文档适用于VueKit组件助手 v3.0.0及以上版本* 