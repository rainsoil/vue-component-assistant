# 调试日志可见性修复说明

## 问题描述
用户报告控制台只打印 info 日志，不打印 debug 日志。

## 问题原因
IDEA 的日志级别设置默认只显示 INFO 及以上级别的日志，而 `VueKitLogger.debug()` 方法输出的 DEBUG 级别日志被过滤掉了。

## 解决方案
将所有关键的调试日志从 `VueKitLogger.debug()` 改为 `VueKitLogger.info()`，确保这些重要的调试信息能够在控制台中显示。

## 修改内容

### 1. UI创建过程的日志
- `createCenterPanel()` - 中心面板创建
- `createMainInterface()` - 主界面创建
- `createLibraryConfigPanel()` - 组件库配置面板创建
- `createDebugModePanel()` - 调试模式面板创建
- `createLibraryCheckBoxes()` - 组件库复选框创建

### 2. 配置加载过程的日志
- `loadCurrentConfig()` - 配置加载
- `updateCheckBoxes()` - 复选框状态更新
- `getAvailableLibraryTypes()` - 可用组件库类型获取

### 3. 关键状态检查日志
- 调试模式面板可见性检查
- 调试模式复选框状态更新
- 组件库复选框创建和状态设置

## 修改后的日志输出

现在在 IDEA 的 Event Log 窗口中应该能看到以下日志：

```
开始创建中心面板
开始创建组件库配置面板...
开始创建调试模式面板...
=== createDebugModePanel() 开始 ===
调试模式复选框初始状态设置为: false（将在配置加载后更新）
调试模式面板创建完成，复选框可见: true
调试模式面板可见: true
调试模式面板启用: true
调试模式复选框启用: true
=== createDebugModePanel() 结束 ===
调试模式面板已添加到组件库配置面板
调试模式面板可见: true
调试模式面板启用: true
组件库配置面板子组件数量: X
组件库配置面板已添加到主界面
=== createLibraryCheckBoxes() 开始 ===
创建组件库复选框，找到 X 个组件库类型
- ELEMENT_UI (Element UI)
- ELEMENT_PLUS (Element Plus)
...
总共创建了 X 个复选框
=== createLibraryCheckBoxes() 结束 ===
=== loadCurrentConfig() 开始 ===
开始获取当前启用的组件库...
获取到的组件库配置:
- currentEnabledLibraries 数量: 1
- currentEnabledLibraries 内容: Element UI
libraryCheckBoxes 已初始化，开始更新复选框状态...
debugModeCheckBox 已初始化，开始更新调试模式状态...
调试模式复选框状态已更新: true/false
当前配置已加载，启用的组件库: Element UI
=== loadCurrentConfig() 结束 ===
中心面板创建完成，主面板大小: java.awt.Dimension[width=500,height=400]
```

## 验证步骤

### 1. 打开项目组件库配置
1. 在 IDEA 中打开 `Settings/Tools/Vue Kit` 项目组件库配置
2. 观察 IDEA 底部的 Event Log 窗口

### 2. 查看日志输出
应该能看到详细的 UI 创建和配置加载过程日志，包括：
- 调试模式面板的创建和状态
- 组件库复选框的创建和状态
- 配置文件的加载和解析
- 复选框状态的更新

### 3. 检查调试模式选项
在组件库配置对话框中查找"启用调试模式"复选框，该复选框应该：
- 位于组件库列表下方
- 有灰色边框包围
- 正确显示配置文件中的状态

## 如果仍然看不到调试模式选项

如果日志显示调试模式面板已创建但UI中看不到，请检查：

1. **面板尺寸**：调试模式面板设置了最小尺寸 200x30，首选尺寸 300x40
2. **可见性状态**：日志中会显示面板和复选框的可见性状态
3. **布局问题**：检查是否有其他组件遮挡了调试模式面板

## 技术细节

### 日志级别说明
- **INFO 级别**：现在使用 `VueKitLogger.info()` 输出关键调试信息
- **DEBUG 级别**：`VueKitLogger.debug()` 仍然存在，但只在调试模式启用时输出
- **WARN/ERROR 级别**：用于警告和错误信息

### 关键日志检查点
1. **调试模式面板创建**：确认面板是否正确创建
2. **面板可见性**：确认面板和复选框是否可见
3. **配置加载**：确认配置文件是否正确加载
4. **状态更新**：确认复选框状态是否正确更新

这个修复确保了所有重要的调试信息都能在控制台中显示，帮助诊断调试模式UI显示问题。 