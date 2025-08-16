# 组件库过滤功能修复说明

## 问题描述

用户反馈：下载了 element-plus 组件库但没有选中，但在进行组件提示时仍然会提示到该组件库的组件。

## 问题分析

经过代码分析，发现以下问题：

1. **SmartComponentFilter** 中的 `getUserEnabledLibraries` 方法返回空集合，没有正确集成用户配置
2. **SmartComponentFilter** 中的 `isFromEnabledLibrary` 方法总是返回 `true`，没有进行实际的过滤
3. **SmartComponentFilter** 中的 `isFromProjectLibrary` 方法通过描述中的标识判断，但这种方式不准确
4. 缺少用户界面来管理组件库的启用/禁用状态
5. **配置变更后没有重新加载组件数据**：保存组件库配置后，没有通知 ComponentProvider 重新加载组件数据
6. **线程安全问题**：文件写入操作没有在正确的写操作上下文中执行，导致 `Assertion failed: Write access is allowed inside write-action only` 异常

## 修复方案

### 1. 修复 SmartComponentFilter 过滤逻辑

#### 修复 `getUserEnabledLibraries` 方法
```java
private Set<ComponentLibraryDetector.LibraryType> getUserEnabledLibraries(Project project) {
    try {
        // 集成用户配置管理器
        ComponentLibraryConfigManager configManager = ComponentLibraryConfigManager.getInstance(project);
        Set<ComponentLibraryDetector.LibraryType> enabledLibraries = configManager.getEnabledLibraries(project);
        
        VueKitLogger.debug(LOG, "从用户配置获取启用的组件库: " + 
            enabledLibraries.stream()
                .map(ComponentLibraryDetector.LibraryType::getDisplayName)
                .collect(Collectors.joining(", ")));
        
        return enabledLibraries;
        
    } catch (Exception e) {
        VueKitLogger.error(LOG, "获取用户启用的组件库失败", e);
        return new HashSet<>();
    }
}
```

#### 修复 `isFromEnabledLibrary` 方法
```java
private boolean isFromEnabledLibrary(ElementPlusComponent component, 
                                   Set<ComponentLibraryDetector.LibraryType> enabledLibraries) {
    
    // 如果没有启用任何组件库，则显示所有组件（保持向后兼容）
    if (enabledLibraries.isEmpty()) {
        VueKitLogger.debug(LOG, "没有启用任何组件库，显示所有组件");
        return true;
    }
    
    // 根据组件名称前缀判断来源
    String componentName = component.getName();
    if (componentName == null) {
        return false;
    }
    
    // 检查组件是否来自启用的组件库
    for (ComponentLibraryDetector.LibraryType libraryType : enabledLibraries) {
        if (isComponentFromLibrary(componentName, libraryType)) {
            VueKitLogger.debug(LOG, "组件 " + componentName + " 来自启用的组件库: " + libraryType.getDisplayName());
            return true;
        }
    }
    
    VueKitLogger.debug(LOG, "组件 " + componentName + " 不属于任何启用的组件库，将被过滤");
    return false;
}
```

#### 新增 `isComponentFromLibrary` 方法
```java
private boolean isComponentFromLibrary(String componentName, ComponentLibraryDetector.LibraryType libraryType) {
    switch (libraryType) {
        case ELEMENT_UI:
        case ELEMENT_PLUS:
            // Element UI 和 Element Plus 都使用 el- 前缀
            return componentName.startsWith("el-");
        case ANT_DESIGN_VUE:
            // Ant Design Vue 使用 a- 前缀
            return componentName.startsWith("a-");
        case VUETIFY:
            // Vuetify 使用 v- 前缀
            return componentName.startsWith("v-");
        case QUASAR:
            // Quasar 使用 q- 前缀
            return componentName.startsWith("q-");
        default:
            return false;
    }
}
```

### 2. 创建组件库启用管理界面

#### 新增 ComponentLibraryEnablementDialog
- 显示所有可用的组件库
- 允许用户启用/禁用特定的组件库
- 显示组件库的详细信息
- 支持重置为默认配置

#### 功能特性
- 项目级配置：每个项目可以有不同的组件库配置
- 全局默认配置：设置默认启用的组件库
- 智能检测：自动检测项目使用的组件库
- 配置同步：项目配置与全局配置的智能同步

### 3. 集成到设置页面

在设置页面添加"🔧 组件库启用管理"按钮，方便用户访问组件库启用管理功能。

### 4. 修复配置变更后的重新加载问题

#### 修复 ComponentLibraryEnablementDialog
```java
// 在 applyConfiguration 方法中添加重新加载逻辑
try {
    ComponentProviderManager.notifyProviderReload(project);
    VueKitLogger.info(LOG, "已通知 ComponentProvider 重新加载组件数据");
} catch (Exception e) {
    VueKitLogger.error(LOG, "通知 ComponentProvider 重新加载失败", e);
}
```

#### 修复 ComponentLibraryConfigManager
```java
// 在 notifyConfigChanged 方法中添加重新加载逻辑
private void notifyConfigChanged(Project project, Set<ComponentLibraryDetector.LibraryType> enabledLibraries) {
    // 通知配置变更监听器
    for (ConfigChangeListener listener : listeners) {
        try {
            listener.onProjectConfigChanged(project, enabledLibraries);
        } catch (Exception e) {
            VueKitLogger.error(LOG, "通知配置变更监听器失败", e);
        }
    }
    
    // 通知 ComponentProvider 重新加载组件数据
    try {
        ComponentProviderManager.notifyProviderReload(project);
        VueKitLogger.info(LOG, "已通知 ComponentProvider 重新加载组件数据");
    } catch (Exception e) {
        VueKitLogger.error(LOG, "通知 ComponentProvider 重新加载失败", e);
    }
}
```

### 5. 修复关键过滤逻辑问题

#### 修复 SmartComponentFilter 的 isFromEnabledLibrary 方法
```java
private boolean isFromEnabledLibrary(ElementPlusComponent component, 
                                   Set<ComponentLibraryDetector.LibraryType> enabledLibraries) {
    
    // 如果没有启用任何组件库，则不显示任何组件
    if (enabledLibraries.isEmpty()) {
        VueKitLogger.debug(LOG, "没有启用任何组件库，不显示任何组件");
        return false;
    }
    
    // 根据组件名称前缀判断来源
    String componentName = component.getName();
    if (componentName == null) {
        return false;
    }
    
    // 检查组件是否来自启用的组件库
    for (ComponentLibraryDetector.LibraryType libraryType : enabledLibraries) {
        if (isComponentFromLibrary(componentName, libraryType)) {
            VueKitLogger.debug(LOG, "组件 " + componentName + " 来自启用的组件库: " + libraryType.getDisplayName());
            return true;
        }
    }
    
    VueKitLogger.debug(LOG, "组件 " + componentName + " 不属于任何启用的组件库，将被过滤");
    return false;
}
```

**重要修复说明：**
- **修复前**：当没有启用任何组件库时，显示所有组件（保持向后兼容）
- **修复后**：当没有启用任何组件库时，不显示任何组件
- **原因**：用户明确禁用了所有组件库，应该尊重用户的选择，不显示任何组件

### 6. 修复线程安全问题

#### 修复 ProjectSettingsManager
```java
// 在 saveProjectSettingsToFile 方法中添加写操作上下文
private void saveProjectSettingsToFile(Project project, ProjectSettings settings) {
    try {
        String settingsJson = new GsonBuilder().setPrettyPrinting().create().toJson(settings);
        VirtualFile projectDir = project.getBaseDir();
        VirtualFile settingsFile = projectDir.findChild(PROJECT_SETTINGS_FILE);
        
        if (settingsFile == null) {
            settingsFile = projectDir.createChildData(this, PROJECT_SETTINGS_FILE);
        }
        
        // 在写操作上下文中执行文件写入
        ApplicationManager.getApplication().runWriteAction(() -> {
            try {
                settingsFile.setBinaryContent(settingsJson.getBytes(StandardCharsets.UTF_8));
            } catch (Exception e) {
                VueKitLogger.error(LOG, "写入项目设置文件失败", e);
            }
        });
        
    } catch (Exception e) {
        VueKitLogger.error(LOG, "保存项目设置到文件失败", e);
    }
}
```

#### 修复 ComponentLibraryConfigManager
```java
// 在 saveProjectConfig 方法中添加写操作上下文
private void saveProjectConfig(Project project, ProjectConfig config) {
    try {
        String configJson = convertProjectConfigToJson(config);
        VirtualFile projectDir = project.getBaseDir();
        VirtualFile configFile = projectDir.findChild(PROJECT_CONFIG_FILE);
        
        if (configFile == null) {
            configFile = projectDir.createChildData(this, PROJECT_CONFIG_FILE);
        }
        
        // 在写操作上下文中执行文件写入
        ApplicationManager.getApplication().runWriteAction(() -> {
            try {
                configFile.setBinaryContent(configJson.getBytes(StandardCharsets.UTF_8));
            } catch (Exception e) {
                VueKitLogger.error(LOG, "写入项目配置文件失败", e);
            }
        });
        
    } catch (Exception e) {
        VueKitLogger.error(LOG, "保存项目配置失败", e);
    }
}
```

## 使用方法

### 1. 打开组件库启用管理
1. 打开设置页面 (File → Settings → Tools → VueKit)
2. 点击"🔧 组件库启用管理"按钮

### 2. 配置组件库启用状态
1. 在对话框中查看所有可用的组件库
2. 勾选需要启用的组件库
3. 取消勾选不需要的组件库
4. 点击"应用"保存配置

### 3. 验证配置
1. 配置保存后会自动重新加载组件数据
2. 在 Vue 文件中输入组件前缀（如 `<el-`）
3. 验证是否只显示启用的组件库的组件
4. 如果仍有问题，可以使用调试工具检查状态

## 支持的组件库

- **Element UI** - 基于 Vue 2.x 的组件库，组件前缀：`el-`
- **Element Plus** - 基于 Vue 3.x 的组件库，组件前缀：`el-`
- **Ant Design Vue** - 基于 Ant Design 的 Vue 组件库，组件前缀：`a-`
- **Vuetify** - Material Design 组件库，组件前缀：`v-`
- **Quasar** - 高性能 Vue.js 组件库，组件前缀：`q-`

## 配置优先级

1. **项目级配置** - 最高优先级，如果项目有特定配置则使用项目配置
2. **全局默认配置** - 如果项目没有配置则使用全局默认配置
3. **系统默认配置** - 如果都没有配置则使用系统默认配置（启用 Element UI、Element Plus、Ant Design Vue）

## 注意事项

1. **配置生效时间**：更改配置后可能需要重启 IDE 或重新加载项目才能生效
2. **项目检测**：如果项目在 package.json 中配置了组件库，该组件库会自动启用
3. **向后兼容**：如果没有启用任何组件库，会显示所有组件以保持向后兼容
4. **配置持久化**：配置会保存到项目根目录的 `.vuekit-libraries.json` 文件中

## 测试

使用 `test/ComponentLibraryFilteringTest.vue` 文件来测试组件库过滤功能是否正常工作。

## 相关文件

- `src/main/java/com/chu7/vuecomponentassistant/utils/SmartComponentFilter.java` - 智能组件过滤器
- `src/main/java/com/chu7/vuecomponentassistant/settings/ComponentLibraryConfigManager.java` - 组件库配置管理器
- `src/main/java/com/chu7/vuecomponentassistant/settings/ProjectSettingsManager.java` - 项目设置管理器
- `src/main/java/com/chu7/vuecomponentassistant/ui/ComponentLibraryEnablementDialog.java` - 组件库启用管理对话框
- `src/main/java/com/chu7/vuecomponentassistant/action/ComponentLibraryEnablementAction.java` - 组件库启用管理动作
- `src/main/java/com/chu7/vuecomponentassistant/action/ComponentLibraryDebugAction.java` - 组件库调试动作
- `test/ComponentLibraryFilteringTest.vue` - 测试文件
- `test/ComponentLibraryFilteringTest2.vue` - 简单测试文件
- `test/ThreadSafetyTest.java` - 线程安全测试 