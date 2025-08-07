# Element Plus Assistant 插件测试指南

## 构建插件

1. 确保已安装 Gradle
2. 在项目根目录运行：
   ```bash
   gradle buildPlugin
   ```
3. 插件文件将生成在 `build/distributions/` 目录中

## 安装插件

1. 打开 IntelliJ IDEA
2. 进入 `File` -> `Settings` -> `Plugins`
3. 点击齿轮图标 -> `Install Plugin from Disk`
4. 选择生成的 `.jar` 文件
5. 重启 IntelliJ IDEA

## 测试补全功能

### 组件补全测试
1. 创建一个新的 Vue 文件（`.vue` 扩展名）
2. 在 `<template>` 标签内输入 `<`
3. 应该能看到 Element Plus 组件的补全提示，包含组件描述
4. 尝试输入 `el-bu`，应该能看到 `el-button` 的提示
5. 选择组件后会自动插入完整的标签结构

### 属性补全测试
1. 在 Element Plus 组件标签内输入空格
2. 应该能看到该组件特有的属性列表
3. 每个属性都会显示描述信息
4. 选择属性后会自动插入默认值

### 事件补全测试
1. 在 Element Plus 组件标签内输入 `@`
2. 应该能看到该组件支持的事件列表
3. 每个事件都会显示描述信息和参数
4. 选择事件后会自动生成事件处理函数名
5. 不同组件的事件不会混淆（如 el-button 只显示按钮事件）
6. **重要**: 即使组件还没有任何事件，输入 `@` 也应该能触发事件补全

### 事件补全测试场景
- **空组件**: `<el-button @` 应该显示所有按钮事件
- **已有属性**: `<el-input placeholder="test" @` 应该显示所有输入框事件
- **已有事件**: `<el-select @change="handleChange" @` 应该显示其他选择器事件
- **多个事件**: `<el-table @selection-change="handleSelection" @sort-change="handleSort" @` 应该显示其他表格事件

### 事件补全示例
- `<el-button @` 应该显示：click, dblclick 等按钮事件
- `<el-input @` 应该显示：input, change, focus, blur 等输入框事件
- `<el-select @` 应该显示：change, visible-change, remove-tag 等选择器事件
- `<el-table @` 应该显示：select, selection-change, sort-change 等表格事件
- `<el-form @` 应该显示：submit, validate 等表单事件
- `<el-dialog @` 应该显示：close, open 等对话框事件

### 上下文感知测试
1. 在 `<el-button>` 标签内只能看到按钮相关的属性
2. 在 `<el-input>` 标签内只能看到输入框相关的属性
3. 不同组件的属性不会混淆

## 测试文档功能

1. 在 Vue 文件中输入一个 Element Plus 组件（如 `<el-button`）
2. 将鼠标悬停在组件名称上
3. 应该能看到组件的详细文档

## 测试右键菜单

1. 在 Vue 文件中右键点击 Element Plus 组件
2. 应该能看到"查看 Element Plus 文档"选项
3. 点击后会在浏览器中打开官方文档

## 故障排除

### 补全不工作
- 确保文件扩展名是 `.vue` 或 `.html`
- 检查插件是否正确安装和启用
- 查看 IntelliJ IDEA 的事件日志是否有错误

### JSON 解析错误
- 检查 `element-plus-components.json` 文件格式是否正确
- 确保所有 `defaultValue` 字段都是有效的 JSON 值

### 图标不显示
- 确保 `src/main/resources/icons/` 目录中的 SVG 文件存在
- 检查图标文件的格式是否正确

## 开发调试

1. 在 IntelliJ IDEA 中打开项目
2. 运行 `gradle runIde` 启动测试实例
3. 在测试实例中测试插件功能
4. 查看控制台输出和日志

## 已知问题

1. 某些复杂的 Vue 模板结构可能无法正确识别
2. 动态组件名称可能无法提供准确的补全
3. 在大型项目中，补全可能较慢

## 下一步改进

1. 改进上下文分析，提供更准确的补全
2. 添加更多组件库支持
3. 优化性能和内存使用
4. 添加更多自定义选项
