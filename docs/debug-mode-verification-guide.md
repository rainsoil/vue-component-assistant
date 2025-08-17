# 调试模式功能验证指南

## 问题描述
用户报告在项目功能设置中没有找到"启用调试模式"的功能。

## 验证步骤

### 1. 检查UI创建日志
在 `ComponentLibraryConfigDialog.java` 中已经添加了详细的调试日志：

- `createMainInterface()` - 确认组件库配置面板被添加到主界面
- `createLibraryConfigPanel()` - 确认调试模式面板被添加到组件库配置面板
- `createDebugModePanel()` - 确认调试模式复选框被正确创建

### 2. 查看日志的方法

#### 方法1：通过IntelliJ IDEA的日志窗口
1. 打开 IntelliJ IDEA
2. 打开项目
3. 打开 `Settings/Tools/Vue Kit` 项目组件库配置
4. 在 IDEA 的底部找到 "Event Log" 或 "Log" 窗口
5. 查看是否有以下日志输出：
   ```
   === createMainInterface() 开始 ===
   开始创建组件库配置面板...
   开始创建调试模式面板...
   === createDebugModePanel() 开始 ===
   调试模式复选框初始状态: false
   调试模式面板创建完成，复选框可见: true
   === createDebugModePanel() 结束 ===
   调试模式面板已添加到组件库配置面板
   组件库配置面板已添加到主界面
   ```

#### 方法2：通过IDEA的Help菜单
1. 在 IDEA 中打开 `Help` -> `Show Log in Explorer` 或 `Help` -> `Show Log`
2. 查看最新的日志文件
3. 搜索 "VueKit" 或 "调试模式" 相关的日志

#### 方法3：通过IDEA的Debug模式
1. 在 IDEA 中设置断点
2. 打开项目组件库配置对话框
3. 在断点处检查 `debugModeCheckBox` 对象的状态

### 3. 可能的问题和解决方案

#### 问题1：调试模式面板不可见
**原因**：可能是布局问题或面板被其他组件遮挡

**解决方案**：
1. 检查 `createDebugModePanel()` 方法中的布局设置
2. 确认面板的 `setVisible(true)` 和 `setEnabled(true)`
3. 检查面板的边框和背景色设置

#### 问题2：调试模式复选框不可见
**原因**：复选框可能被面板布局影响

**解决方案**：
1. 检查 `BorderLayout.CENTER` 是否正确
2. 确认复选框的文本和工具提示是否正确设置
3. 检查复选框的初始状态设置

#### 问题3：日志没有输出
**原因**：可能是日志级别设置问题

**解决方案**：
1. 检查 IDEA 的日志级别设置
2. 确认 `VueKitLogger.debug()` 方法是否被正确调用
3. 检查项目的调试模式是否已启用

### 4. 手动验证步骤

1. **打开项目组件库配置**：
   - 在 IDEA 中打开 `Settings/Tools/Vue Kit`
   - 或者通过 `File` -> `Settings` -> `Tools` -> `Vue Kit`

2. **查找调试模式选项**：
   - 在组件库配置对话框中查找"启用调试模式"复选框
   - 该复选框应该位于组件库列表下方

3. **测试调试模式功能**：
   - 勾选"启用调试模式"复选框
   - 查看是否有日志输出确认状态改变
   - 检查 `vuekit-project-config.json` 文件中的 `enableDebugMode` 字段

### 5. 配置文件验证

检查项目根目录下的 `.idea/vuekit-project-config.json` 文件：

```json
{
  "projectId": "xxx",
  "projectName": "xxx",
  "enableComponentCompletion": true,
  "enableAttributeCompletion": true,
  "enableEventCompletion": true,
  "enableSlotCompletion": true,
  "enableHoverDocumentation": true,
  "enableRightClickDocumentation": true,
  "enableCaching": true,
  "enableDebugMode": true,  // 这个字段应该存在
  "enabledLibraryNames": ["element-ui"]
}
```

### 6. 如果仍然看不到调试模式选项

1. **重新构建项目**：
   ```bash
   ./gradlew clean build
   ```

2. **重启 IDEA**：
   - 完全关闭 IDEA
   - 重新打开项目

3. **检查插件版本**：
   - 确认使用的是最新版本的 VueKit 插件

4. **清除缓存**：
   - 删除 `.idea` 目录下的缓存文件
   - 重新导入项目

## 联系支持

如果按照以上步骤仍然无法看到调试模式选项，请：

1. 提供 IDEA 的版本信息
2. 提供 VueKit 插件的版本信息
3. 提供项目的 `package.json` 文件内容
4. 提供 `.idea/vuekit-project-config.json` 文件内容
5. 提供 IDEA 的日志文件内容

这样可以帮助进一步诊断问题。 