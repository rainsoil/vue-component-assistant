# 包名不匹配问题修复总结

## 问题描述
当 `package.json` 中是 `element-plus` 时，`vuekit-project-config.json` 中却显示 `"enabledLibraryNames": ["element-ui"]`，这明显是错误的。应该是 `"element-plus"`。

## 问题分析

### 根本原因
问题在于 `PackageJsonAutoDetector.enableDetectedLibraries()` 方法中的逻辑：

1. **检测阶段**：正确检测到 `package.json` 中的 `element-plus`
2. **转换阶段**：将 `element-plus` 转换为 `LibraryType.ELEMENT_PLUS`
3. **保存阶段**：调用 `configManager.setProjectEnabledLibraries(project, enabledLibraries)`
4. **覆盖问题**：`setProjectEnabledLibraries` 会调用 `ProjectConfig.setEnabledLibraries()`，该方法会使用 `type.getPackageName()` 来设置 `enabledLibraryNames`
5. **硬编码问题**：`LibraryType.ELEMENT_PLUS.getPackageName()` 返回的是硬编码的 `"element-plus"`，但如果有其他问题导致转换错误，可能会返回错误的包名

### 具体问题
1. **双重设置冲突**：
   - `enabledLibraryNames.add(libraryName)` 设置正确的原始包名（`element-plus`）
   - `setProjectEnabledLibraries()` 调用 `setEnabledLibraries()` 覆盖为硬编码包名

2. **转换错误**：
   - 如果 `LibraryType.fromLibraryName("element-plus")` 错误地返回了 `ELEMENT_UI`
   - 那么 `ELEMENT_UI.getPackageName()` 会返回 `"element-ui"`

## 修复方案

### 修复逻辑
在 `enableDetectedLibraries` 方法中，在调用 `setProjectEnabledLibraries` 之后，手动重新设置正确的 `enabledLibraryNames`：

```java
// 直接设置项目启用的组件库，而不是通过 enableLibrary 方法
configManager.setProjectEnabledLibraries(project, enabledLibraries);

// 重要：手动设置正确的 enabledLibraryNames，覆盖 setProjectEnabledLibraries 中的硬编码包名
ProjectConfig projectConfig = configManager.getProjectConfigForFeatures(project);
if (projectConfig != null) {
    projectConfig.setEnabledLibraryNames(enabledLibraryNames);
    configManager.setProjectConfig(project, projectConfig);
    VueKitLogger.info(LOG, "✅ 已手动设置正确的 enabledLibraryNames: " + String.join(", ", enabledLibraryNames));
}
```

### 修复原理
1. **保持原始包名**：确保 `enabledLibraryNames` 保存的是从 `package.json` 中检测到的原始包名
2. **覆盖硬编码**：在 `setProjectEnabledLibraries` 之后，手动重新设置正确的包名
3. **双重保障**：同时更新 `ProjectSettingsManager` 中的配置

## 修改的文件

### PackageJsonAutoDetector.java
- **enableDetectedLibraries()**：添加手动设置 `enabledLibraryNames` 的逻辑
- **导入修复**：添加 `ProjectConfig` 的导入

## 验证步骤

### 1. 测试 element-plus
1. 确保 `package.json` 中包含 `"element-plus"`
2. 删除 `.idea/vuekit-project-config.json` 文件
3. 重启 IDEA 或重新加载项目
4. 检查生成的配置文件

### 2. 检查配置文件
`.idea/vuekit-project-config.json` 应该包含：
```json
{
  "projectId": "xxx",
  "projectName": "test",
  "enableComponentCompletion": true,
  "enableAttributeCompletion": true,
  "enableEventCompletion": true,
  "enableSlotCompletion": true,
  "enableHoverDocumentation": true,
  "enableRightClickDocumentation": true,
  "enableCaching": true,
  "enableDebugMode": false,
  "enabledLibraryNames": ["element-plus"]
}
```

### 3. 查看日志输出
应该能看到以下日志：
```
=== 开始自动检测项目组件库 ===
项目名称: test
检测到的组件库: element-plus
=== LibraryType.fromLibraryName() 开始 ===
输入参数 libraryName: 'element-plus'
尝试从远程组件库管理器获取信息...
找到匹配的远程组件库: 'element-plus'
推断结果: ELEMENT_PLUS (Element Plus)
=== LibraryType.fromLibraryName() 结束 ===
检测到组件库: Element Plus (包名: element-plus)
✅ 已手动设置正确的 enabledLibraryNames: element-plus
✅ 已启用组件库: element-plus
✅ 组件库类型: Element Plus
✅ 自动检测并启用组件库成功
```

### 4. 检查UI显示
打开 `Settings/Tools/Vue Kit` 项目组件库配置，应该看到：
- Element Plus 复选框被正确选中
- 其他组件库复选框未被选中

## 预期结果

修复后，当 `package.json` 中包含 `element-plus` 时：

1. **正确检测**：系统能够正确检测到 `element-plus`
2. **正确保存**：`enabledLibraryNames` 字段保存为 `["element-plus"]`
3. **正确转换**：`LibraryType` 正确转换为 `ELEMENT_PLUS`
4. **正确显示**：UI 中正确显示 Element Plus 被选中

## 技术细节

### 修复流程
1. **检测阶段**：从 `package.json` 检测到 `element-plus`
2. **转换阶段**：将 `element-plus` 转换为 `LibraryType.ELEMENT_PLUS`
3. **设置阶段**：调用 `setProjectEnabledLibraries()` 设置组件库类型
4. **覆盖阶段**：手动重新设置 `enabledLibraryNames` 为原始包名
5. **保存阶段**：保存配置到文件

### 关键改进
- **保持原始包名**：确保配置文件中的包名与 `package.json` 中的一致
- **避免硬编码覆盖**：防止 `setEnabledLibraries()` 中的硬编码包名覆盖原始检测结果
- **双重更新**：同时更新 `ComponentLibraryConfigManager` 和 `ProjectSettingsManager`

这个修复确保了自动检测的包名能够正确保存到配置文件中，解决了包名不匹配的问题。 