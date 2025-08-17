# 配置文件调试指南

## 问题描述

用户反馈：配置文件 `vuekit-project-config.json` 中只有 `element-ui`，但打开项目组件库配置对话框时，却同时勾选了 `element-plus` 和 `element-ui`。

## 调试步骤

### 1. 启用调试日志

确保在 IntelliJ IDEA 中启用了 VueKit 的调试日志：

1. 打开 `Help -> Edit Custom VM Options...`
2. 添加以下行：
   ```
   -Didea.log.debug.categories=com.chu7.vuecomponentassistant
   ```
3. 重启 IntelliJ IDEA

### 2. 查看调试日志

打开项目组件库配置对话框后，查看 IntelliJ IDEA 的日志输出：

1. 打开 `Help -> Show Log in Explorer`
2. 查看最新的日志文件
3. 搜索以下关键词：
   - `ProjectConfig.getEnabledLibraries()`
   - `LibraryType.fromLibraryName()`
   - `LibraryType.inferLibraryType()`
   - `updateCheckBoxes()`

### 3. 分析日志输出

#### 3.1 配置文件加载日志

查找类似以下的日志：
```
=== ProjectConfig.getEnabledLibraries() 开始 ===
enabledLibraries 当前状态: 0 个
enabledLibraryNames 当前状态: 1 个
enabledLibraryNames 内容: [element-ui]
需要从 enabledLibraryNames 转换为 enabledLibraries
处理组件库名称: 'element-ui'
```

#### 3.2 组件库名称转换日志

查找类似以下的日志：
```
=== LibraryType.fromLibraryName() 开始 ===
输入参数 libraryName: 'element-ui'
尝试从远程组件库管理器获取信息...
远程组件库管理器返回 X 个组件库
检查远程组件库: 'element-plus' 是否匹配 'element-ui'
检查远程组件库: 'element-ui' 是否匹配 'element-ui'
找到匹配的远程组件库: 'element-ui'
=== LibraryType.inferLibraryType() 开始 ===
输入参数 libraryName: 'element-ui'
标准化后的名称: 'elementui'
比较: 'elementui' vs 'elementui' (来自 ELEMENT_UI)
找到匹配: ELEMENT_UI (Element UI)
```

#### 3.3 复选框更新日志

查找类似以下的日志：
```
=== updateCheckBoxes() 开始 ===
当前启用的组件库数量: 1
当前启用的组件库: Element UI
复选框数量: 2
复选框列表:
- ELEMENT_UI (Element UI)
- ELEMENT_PLUS (Element Plus)
复选框 'Element UI' (ELEMENT_UI) -> 选中 (在 currentEnabledLibraries 中: true)
复选框 'Element Plus' (ELEMENT_PLUS) -> 未选中 (在 currentEnabledLibraries 中: false)
```

### 4. 可能的问题原因

#### 4.1 远程组件库管理器问题

如果远程组件库管理器返回了错误的组件库列表，可能导致：
- 返回了 `element-plus` 和 `element-ui` 两个组件库
- 在 `getAvailableLibraryTypes()` 中创建了多余的复选框

#### 4.2 字符串匹配问题

如果 `StringNormalizer.matches()` 方法存在问题，可能导致：
- `element-ui` 错误地匹配了 `element-plus`
- 或者反之

#### 4.3 复选框创建问题

如果 `createLibraryCheckBoxes()` 方法存在问题，可能导致：
- 创建了不应该存在的复选框
- 复选框状态设置错误

### 5. 验证步骤

#### 5.1 检查配置文件

确认 `vuekit-project-config.json` 文件内容：
```json
{
  "projectId": "98f51b13",
  "projectName": "test",
  "enabledLibraryNames": ["element-ui"],
  "enableComponentCompletion": true,
  "enableAttributeCompletion": true,
  "enableEventCompletion": true,
  "enableSlotCompletion": true,
  "enableHoverDocumentation": true,
  "enableRightClickDocumentation": true,
  "enableCaching": true,
  "enableDebugMode": false
}
```

#### 5.2 检查远程组件库管理器

查看远程组件库管理器返回的组件库列表：
```
远程组件库管理器返回 X 个组件库
- element-plus
- element-ui
```

#### 5.3 检查复选框创建

查看创建的复选框列表：
```
复选框数量: 2
复选框列表:
- ELEMENT_UI (Element UI)
- ELEMENT_PLUS (Element Plus)
```

### 6. 预期结果

正确的日志输出应该是：

#### 6.1 配置文件加载
```
=== ProjectConfig.getEnabledLibraries() 开始 ===
enabledLibraries 当前状态: 0 个
enabledLibraryNames 当前状态: 1 个
enabledLibraryNames 内容: [element-ui]
需要从 enabledLibraryNames 转换为 enabledLibraries
处理组件库名称: 'element-ui'
转换结果: 'element-ui' -> ELEMENT_UI (Element UI)
已添加到 enabledLibraries: Element UI
最终返回的 enabledLibraries: Element UI
=== ProjectConfig.getEnabledLibraries() 结束 ===
```

#### 6.2 复选框更新
```
=== updateCheckBoxes() 开始 ===
当前启用的组件库数量: 1
当前启用的组件库: Element UI
复选框数量: 1
复选框列表:
- ELEMENT_UI (Element UI)
复选框 'Element UI' (ELEMENT_UI) -> 选中 (在 currentEnabledLibraries 中: true)
=== updateCheckBoxes() 结束 ===
```

### 7. 修复建议

根据日志分析结果，可能的修复方案：

1. **如果远程组件库管理器返回了多余的组件库**：
   - 检查远程组件库管理器的配置
   - 过滤掉不需要的组件库

2. **如果字符串匹配有问题**：
   - 检查 `StringNormalizer.matches()` 方法的实现
   - 确保匹配逻辑正确

3. **如果复选框创建有问题**：
   - 检查 `getAvailableLibraryTypes()` 方法
   - 确保只返回需要的组件库类型

### 8. 测试文件

使用 `test/ConfigDebugTest.vue` 文件来测试修复效果。

## 结论

通过详细的调试日志，我们可以准确定位问题所在，并采取相应的修复措施。请按照上述步骤查看日志输出，然后根据分析结果进行修复。 