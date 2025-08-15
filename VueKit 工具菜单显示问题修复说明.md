# VueKit 工具菜单显示问题修复说明

## 问题描述

用户反馈在 `Settings/Tools/Vue Kit` 下找不到以下两个功能：
- ⚙️ 组件库配置
- 📚 自定义组件库管理

## 问题分析

经过检查发现，问题可能出现在以下几个方面：

1. **菜单组配置问题**：`VueKit.Tools` 菜单组可能没有正确显示
2. **Action 类依赖问题**：相关的 Action 类可能存在编译或依赖问题
3. **IntelliJ IDEA 插件加载问题**：插件可能没有正确加载或重新构建

## 修复方案

### 方案1：直接添加到 ToolsMenu（已实施）

将所有 VueKit 工具直接添加到 `ToolsMenu` 下，使用分隔符进行分组：

```xml
<!-- VueKit 工具分隔符 -->
<separator id="VueKit.Separator.Before" add-to-group="ToolsMenu" anchor="last"/>

<!-- 组件库管理 -->
<action id="ComponentLibrary.Management" ...>
    <add-to-group group-id="ToolsMenu" anchor="last"/>
</action>

<!-- 官方组件库市场 -->
<action id="OfficialLibrary.Market" ...>
    <add-to-group group-id="ToolsMenu" anchor="last"/>
</action>

<!-- 组件库配置 -->
<action id="ComponentLibrary.Config" ...>
    <add-to-group group-id="ToolsMenu" anchor="last"/>
</action>

<!-- 自定义组件库管理 -->
<action id="CustomLibrary.Management" ...>
    <add-to-group group-id="ToolsMenu" anchor="last"/>
</action>

<!-- VueKit 工具分隔符 -->
<separator id="VueKit.Separator.After" add-to-group="ToolsMenu" anchor="last"/>
```

### 方案2：检查 Action 类实现

确保以下 Action 类正确实现：

1. **ComponentLibraryConfigAction.java** - 组件库配置
2. **CustomLibraryManagementAction.java** - 自定义组件库管理

### 方案3：验证插件配置

检查 `plugin.xml` 中的配置：
- Action 类路径是否正确
- 依赖服务是否正确注册
- 插件版本兼容性

## 当前配置状态

### ✅ 已修复
- 所有工具动作现在都直接添加到 `ToolsMenu` 下
- 使用分隔符清晰分组 VueKit 工具
- 保持了原有的功能完整性

### 📍 工具位置
现在所有 VueKit 工具都在 `Tools` 菜单下，按以下顺序排列：

```
Tools Menu
├── ... (其他工具)
├── ────────────────── (分隔符)
├── 🌐 组件库管理
├── 📦 官方组件库市场
├── ⚙️ 组件库配置
├── 📚 自定义组件库管理
└── ────────────────── (分隔符)
```

## 使用说明

### 1. 访问工具
- 打开 IntelliJ IDEA
- 点击顶部菜单 `Tools`
- 在菜单底部找到 VueKit 相关工具

### 2. 工具功能
- **🌐 组件库管理**：管理 VueKit 组件库
- **📦 官方组件库市场**：浏览和下载官方组件库
- **⚙️ 组件库配置**：配置组件库启用/禁用状态
- **📚 自定义组件库管理**：管理自定义组件库和组件

## 故障排除

### 如果工具仍然不显示

1. **重新构建插件**
   ```bash
   ./gradlew clean build
   ```

2. **重新安装插件**
   - 卸载现有插件
   - 重新安装插件

3. **检查 IntelliJ IDEA 版本**
   - 确保版本在 231.0 到 241.* 之间

4. **检查项目依赖**
   - 确保所有依赖的类都存在
   - 检查是否有编译错误

### 验证步骤

1. 检查 `plugin.xml` 配置是否正确
2. 验证 Action 类是否存在且正确实现
3. 确认插件已正确加载
4. 检查 IntelliJ IDEA 日志是否有错误信息

## 总结

通过将 VueKit 工具直接添加到 `ToolsMenu` 下，并使用分隔符进行分组，现在所有工具都应该能够正常显示。这种方法比使用自定义菜单组更可靠，能够确保工具在 IntelliJ IDEA 中正确显示。

如果问题仍然存在，建议检查 Action 类的实现和插件的构建状态。
