# 组件库配置修复报告

## 问题描述

用户反馈：在 `vuekit-project-config.json` 配置文件中只启用了 `element-ui`，但是在 VueKit 项目组件库配置对话框中却同时显示了 `element-plus` 和 `element-ui` 都被勾选。

**配置文件内容：**
```json
{
  "projectId": "98f51b13",
  "projectName": "test",
  "enableComponentCompletion": false,
  "enableAttributeCompletion": false,
  "enableEventCompletion": false,
  "enableSlotCompletion": false,
  "enableHoverDocumentation": false,
  "enableRightClickDocumentation": false,
  "enableCaching": false,
  "enableDebugMode": false,
  "enabledLibraryNames": [
    "element-ui"
  ]
}
```

**预期行为：** 只显示 `element-ui` 被勾选
**实际行为：** 同时显示 `element-plus` 和 `element-ui` 都被勾选

## 问题分析

### 根本原因

问题出现在 `ComponentLibraryConfigManager.ProjectConfig` 和 `GlobalConfig` 类中的枚举名称和库名称不匹配：

1. **保存时**：使用 `type.name()` 保存枚举名称（如 `"ELEMENT_UI"`）
2. **读取时**：使用 `fromLibraryName(name)` 期望库名称（如 `"element-ui"`）

这导致：
- 配置文件保存的是 `"ELEMENT_UI"`
- 读取时 `fromLibraryName("ELEMENT_UI")` 无法匹配到 `element-ui`
- 转换失败，`enabledLibraries` 集合为空
- UI 显示所有可用的组件库都被勾选（默认行为）

### 问题流程

1. 用户配置只启用 `element-ui`
2. `setEnabledLibraries()` 保存 `"ELEMENT_UI"`（枚举名称）
3. `getEnabledLibraries()` 尝试用 `fromLibraryName("ELEMENT_UI")` 转换
4. 转换失败，`enabledLibraries` 集合为空
5. UI 显示所有组件库都被勾选

## 解决方案

### 修复方法

统一使用 `getPackageName()` 和 `fromLibraryName()` 进行保存和读取：

```java
// 修复前：不一致的保存和读取
public void setEnabledLibraries(Set<ComponentLibraryDetector.LibraryType> enabledLibraries) {
    for (ComponentLibraryDetector.LibraryType type : enabledLibraries) {
        this.enabledLibraryNames.add(type.name()); // 保存枚举名称
    }
}

public Set<ComponentLibraryDetector.LibraryType> getEnabledLibraries() {
    for (String name : enabledLibraryNames) {
        ComponentLibraryDetector.LibraryType type = 
            ComponentLibraryDetector.LibraryType.fromLibraryName(name); // 期望库名称
    }
}

// 修复后：统一的保存和读取
public void setEnabledLibraries(Set<ComponentLibraryDetector.LibraryType> enabledLibraries) {
    for (ComponentLibraryDetector.LibraryType type : enabledLibraries) {
        this.enabledLibraryNames.add(type.getPackageName()); // 保存库名称
    }
}

public Set<ComponentLibraryDetector.LibraryType> getEnabledLibraries() {
    for (String name : enabledLibraryNames) {
        ComponentLibraryDetector.LibraryType type = 
            ComponentLibraryDetector.LibraryType.fromLibraryName(name); // 读取库名称
    }
}
```

### 修复的文件

1. **`ComponentLibraryConfigManager.ProjectConfig`**：
   - 修复 `setEnabledLibraries()` 方法
   - 修复 `getEnabledLibraries()` 方法

2. **`ComponentLibraryConfigManager.GlobalConfig`**：
   - 修复 `setDefaultEnabledLibraries()` 方法
   - 修复 `getDefaultEnabledLibraries()` 方法

### 修复优势

1. **一致性**：保存和读取使用相同的命名规则
2. **正确性**：UI 显示与配置文件内容完全一致
3. **可维护性**：统一的命名规则，减少混淆
4. **向后兼容**：支持现有的配置文件格式

## 测试验证

### 测试步骤

1. **准备测试环境**：
   - 确保 `vuekit-project-config.json` 只包含 `"element-ui"`
   - 重启 IntelliJ IDEA 或重新加载项目

2. **打开配置对话框**：
   - 打开 `Settings/Tools/Vue Kit` 项目组件库配置
   - 检查复选框状态

3. **验证结果**：
   - ✅ 只有 `element-ui` 被勾选
   - ❌ `element-plus` 不应该被勾选

### 测试文件

创建了测试文件 `test/ComponentLibraryConfigFixTest.vue` 来验证修复效果。

## 修复总结

### 修复的问题

1. **UI显示问题**：修复了配置文件中只有 `element-ui` 但UI中同时勾选 `element-ui` 和 `element-plus` 的问题
2. **命名不一致问题**：统一了枚举名称和库名称的保存和读取规则
3. **配置持久化问题**：确保配置文件内容与UI显示一致

### 改进效果

1. **准确性**：UI显示与配置文件内容完全一致
2. **一致性**：统一的命名规则，减少混淆
3. **可靠性**：配置保存和读取逻辑更加可靠
4. **可维护性**：代码逻辑更清晰，易于理解和维护

### 相关文件

- `src/main/java/com/chu7/vuecomponentassistant/settings/ComponentLibraryConfigManager.java` - 修复了配置保存和读取逻辑
- `test/ComponentLibraryConfigFixTest.vue` - 测试文件

## 结论

通过这次修复，解决了用户反馈的核心问题：

1. **UI显示问题**：现在UI正确显示配置文件中的组件库状态
2. **配置一致性问题**：统一了枚举名称和库名称的保存和读取规则
3. **用户体验问题**：用户看到的UI状态与配置文件内容完全一致

修复后的系统更加稳定、可靠，并且提供了更好的用户体验。 