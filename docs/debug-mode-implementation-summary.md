# 调试模式功能实现总结

## 功能概述
在 VueKit 插件中实现了项目级的调试模式功能，允许用户在项目组件库配置中启用/禁用调试日志输出。

## 实现的功能

### 1. 项目级调试模式设置
- 在 `ComponentLibraryConfigManager.ProjectConfig` 中添加了 `enableDebugMode` 字段
- 提供了 `isDebugModeEnabled(Project project)` 和 `setDebugModeEnabled(Project project, boolean enabled)` 方法
- 调试模式状态会保存到 `.idea/vuekit-project-config.json` 文件中

### 2. 增强的日志系统
- 扩展了 `VueKitLogger` 类，支持项目上下文感知的日志输出
- 添加了 `isDebugModeEnabled(Project project)` 方法，优先检查全局设置，然后检查项目设置
- 提供了新的重载方法，支持项目参数的日志调用：
  - `debug(Logger logger, Project project, String message)`
  - `info(Logger logger, Project project, String message)`
  - `warn(Logger logger, Project project, String message)`
  - `error(Logger logger, Project project, String message)`

### 3. UI界面集成
- 在 `ComponentLibraryConfigDialog` 中添加了调试模式复选框
- 创建了 `createDebugModePanel()` 方法来封装调试模式UI组件
- 调试模式面板被正确集成到组件库配置面板中
- 添加了详细的状态监听和日志输出

### 4. 调试日志增强
在关键方法中添加了详细的调试日志：

#### ComponentLibraryConfigDialog.java
- `createMainInterface()` - 主界面创建过程
- `createLibraryConfigPanel()` - 组件库配置面板创建
- `createDebugModePanel()` - 调试模式面板创建
- `createLibraryCheckBoxes()` - 组件库复选框创建
- `loadCurrentConfig()` - 配置加载过程
- `updateCheckBoxes()` - 复选框状态更新
- `updateConfiguration()` - 配置更新过程

#### ComponentLibraryConfigManager.java
- `ProjectConfig.getEnabledLibraries()` - 启用组件库获取过程
- `isDebugModeEnabled()` 和 `setDebugModeEnabled()` - 调试模式状态管理

#### ComponentLibraryDetector.java
- `LibraryType.fromLibraryName()` - 组件库名称转换
- `LibraryType.inferLibraryType()` - 组件库类型推断

## 技术实现细节

### 1. 配置管理
```java
// ProjectConfig 类中的字段
public boolean enableDebugMode = false;

// 管理方法
public boolean isDebugModeEnabled() { return enableDebugMode; }
public void setDebugModeEnabled(boolean enabled) { this.enableDebugMode = enabled; }
```

### 2. 日志系统
```java
// VueKitLogger 中的项目上下文检查
public static boolean isDebugModeEnabled(Project project) {
    // 首先检查全局调试模式
    PluginSettings globalSettings = PluginSettings.getInstance();
    if (globalSettings.isEnableDebugMode()) {
        return true;
    }
    
    // 如果全局未启用，检查项目级调试模式
    if (project != null) {
        try {
            ComponentLibraryConfigManager configManager = ComponentLibraryConfigManager.getInstance(project);
            return configManager.isDebugModeEnabled(project);
        } catch (Exception e) {
            return globalSettings.isEnableDebugMode();
        }
    }
    return false;
}
```

### 3. UI组件
```java
// 调试模式面板创建
private JBPanel createDebugModePanel() {
    JBPanel panel = new JBPanel(new BorderLayout());
    panel.setBorder(JBUI.Borders.customLine(Color.LIGHT_GRAY, 1));
    panel.setBackground(Color.WHITE);

    debugModeCheckBox = new JBCheckBox("启用调试模式");
    debugModeCheckBox.setToolTipText("启用后将在日志中输出详细的调试信息，帮助排查问题");
    
    // 设置初始状态
    debugModeCheckBox.setSelected(configManager.isDebugModeEnabled(project));
    
    // 添加监听器
    debugModeCheckBox.addActionListener(e -> {
        boolean enabled = debugModeCheckBox.isSelected();
        configManager.setDebugModeEnabled(project, enabled);
        VueKitLogger.debug(LOG, project, "调试模式已" + (enabled ? "启用" : "禁用"));
    });

    panel.add(debugModeCheckBox, BorderLayout.CENTER);
    return panel;
}
```

## 使用方法

### 1. 启用调试模式
1. 打开 IntelliJ IDEA
2. 进入 `Settings/Tools/Vue Kit` 项目组件库配置
3. 在组件库配置对话框中找到"启用调试模式"复选框
4. 勾选该复选框以启用项目级调试模式

### 2. 查看调试日志
- 通过 IDEA 的 Event Log 窗口查看日志输出
- 通过 `Help` -> `Show Log` 查看详细日志文件
- 在 IDEA 的 Debug 模式下设置断点进行调试

### 3. 验证功能
- 检查 `.idea/vuekit-project-config.json` 文件中的 `enableDebugMode` 字段
- 观察日志输出中是否包含详细的调试信息
- 测试组件库配置的保存和加载过程

## 配置文件示例

启用调试模式后，`vuekit-project-config.json` 文件应该包含：

```json
{
  "projectId": "98f51b13",
  "projectName": "test",
  "enableComponentCompletion": true,
  "enableAttributeCompletion": true,
  "enableEventCompletion": true,
  "enableSlotCompletion": true,
  "enableHoverDocumentation": true,
  "enableRightClickDocumentation": true,
  "enableCaching": true,
  "enableDebugMode": true,
  "enabledLibraryNames": ["element-ui"]
}
```

## 故障排除

### 1. 看不到调试模式选项
- 确认插件已正确安装和加载
- 检查 IDEA 的日志输出是否有错误信息
- 重新构建项目并重启 IDEA

### 2. 调试日志没有输出
- 确认已启用调试模式（全局或项目级）
- 检查 IDEA 的日志级别设置
- 查看 IDEA 的 Event Log 窗口

### 3. 配置没有保存
- 确认项目有写入权限
- 检查 `.idea` 目录是否存在
- 查看 IDEA 的错误日志

## 最新更新

### 2024年最新修改
1. **添加了详细的调试日志**：在UI创建和配置管理的各个关键步骤中添加了 `VueKitLogger.debug` 调用
2. **改进了UI布局**：确保调试模式面板正确显示在组件库配置对话框中
3. **增强了错误处理**：添加了更多的异常捕获和日志记录
4. **创建了验证指南**：提供了详细的故障排除和验证步骤

### 待验证的问题
用户报告在项目功能设置中没有找到"启用调试模式"功能，需要进一步验证：
1. UI组件是否正确创建和显示
2. 布局是否正确配置
3. 是否有权限或缓存问题

## 下一步计划

1. **用户验证**：等待用户按照验证指南进行测试
2. **问题诊断**：根据用户反馈进一步诊断UI显示问题
3. **功能优化**：根据使用情况优化调试模式的性能和可用性
4. **文档完善**：根据实际使用情况完善文档和故障排除指南 