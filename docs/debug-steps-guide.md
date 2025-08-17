# 调试步骤指南

## 问题描述

配置文件只有 `element-ui`，但UI中却同时勾选了 `element-plus` 和 `element-ui`。

## 调试步骤

### 1. 启用日志输出

现在已经在关键位置添加了强制输出的日志（使用 `LOG.info` 而不是 `VueKitLogger.debug`），这些日志会直接输出到 IntelliJ IDEA 的日志中。

### 2. 查看日志的方法

#### 方法1：通过 IntelliJ IDEA 界面
1. 打开 `Help -> Show Log in Explorer`
2. 查看最新的日志文件（通常是 `idea.log`）
3. 搜索关键词：`createLibraryCheckBoxes`、`updateCheckBoxes`、`getAllLibraries`

#### 方法2：通过 Event Log
1. 在 IntelliJ IDEA 底部找到 `Event Log` 窗口
2. 查看是否有相关的日志输出

#### 方法3：通过 Debug Console
1. 在代码中设置断点
2. 以调试模式运行插件
3. 查看 Debug Console 输出

### 3. 关键日志位置

#### 3.1 复选框创建日志
```
=== createLibraryCheckBoxes() 开始 ===
创建组件库复选框，找到 X 个组件库类型
- ELEMENT_UI (Element UI)
- ELEMENT_PLUS (Element Plus)
创建复选框: Element UI (ELEMENT_UI) - 初始状态: 未选中
创建复选框: Element Plus (ELEMENT_PLUS) - 初始状态: 未选中
总共创建了 X 个复选框
=== createLibraryCheckBoxes() 结束 ===
```

#### 3.2 配置加载日志
```
=== loadCurrentConfig() 开始 ===
开始获取当前启用的组件库...
获取到的组件库配置:
- currentEnabledLibraries 数量: 1
- currentEnabledLibraries 内容: Element UI
libraryCheckBoxes 已初始化，开始更新复选框状态...
=== loadCurrentConfig() 结束 ===
```

#### 3.3 复选框更新日志
```
=== updateCheckBoxes() 开始 ===
=== 更新复选框状态 ===
当前启用的组件库数量: 1
当前启用的组件库: Element UI
复选框数量: 2
复选框列表:
- ELEMENT_UI (Element UI)
- ELEMENT_PLUS (Element Plus)
复选框 'Element UI' (ELEMENT_UI) -> 选中 (在 currentEnabledLibraries 中: true)
复选框 'Element Plus' (ELEMENT_PLUS) -> 未选中 (在 currentEnabledLibraries 中: false)
=== updateCheckBoxes() 结束 ===
```

#### 3.4 远程组件库获取日志
```
=== getAvailableLibraryTypes() 开始 ===
=== 获取可用组件库类型 ===
开始从远程组件库管理器获取组件库...
远程组件库管理器返回的组件库数量: X
远程组件库列表:
- element-plus (描述: ...)
- element-ui (描述: ...)
开始转换组件库类型...
处理远程组件库: 'element-plus'
组件库 'element-plus' -> ELEMENT_PLUS (Element Plus)
已添加到可用组件库列表: Element Plus
处理远程组件库: 'element-ui'
组件库 'element-ui' -> ELEMENT_UI (Element UI)
已添加到可用组件库列表: Element UI
转换后的组件库类型数量: 2
转换后的组件库列表:
- ELEMENT_PLUS (Element Plus)
- ELEMENT_UI (Element UI)
最终返回的组件库类型: Element Plus, Element UI
=== getAvailableLibraryTypes() 结束 ===
```

#### 3.5 本地缓存日志
```
=== LocalCacheManager.getAllLibraries() 开始 ===
缓存目录: /path/to/cache
找到缓存文件数量: X
处理缓存文件: library_xxx.json
文件内容长度: XXX 字符
文件内容前100字符: {"name":"element-plus",...}
成功加载组件库: element-plus (ID: xxx)
处理缓存文件: library_yyy.json
文件内容长度: XXX 字符
文件内容前100字符: {"name":"element-ui",...}
成功加载组件库: element-ui (ID: yyy)
成功加载 X 个组件库到内存缓存
加载的组件库列表:
- element-plus (ID: xxx, 描述: ...)
- element-ui (ID: yyy, 描述: ...)
=== LocalCacheManager.getAllLibraries() 结束，返回 X 个组件库 ===
```

### 4. 问题分析

根据日志输出，我们可以分析以下问题：

#### 4.1 如果远程组件库管理器返回了多余的组件库
- 检查 `getAvailableLibraryTypes()` 日志
- 查看远程组件库管理器返回的组件库列表
- 确认是否返回了不应该存在的组件库

#### 4.2 如果本地缓存中有多余的组件库
- 检查 `LocalCacheManager.getAllLibraries()` 日志
- 查看缓存目录中的文件
- 确认是否有不应该存在的组件库文件

#### 4.3 如果复选框创建有问题
- 检查 `createLibraryCheckBoxes()` 日志
- 查看创建的复选框列表
- 确认是否创建了多余的复选框

#### 4.4 如果复选框更新有问题
- 检查 `updateCheckBoxes()` 日志
- 查看复选框状态设置过程
- 确认是否正确设置了选中状态

### 5. 预期正确的日志输出

#### 5.1 如果配置只有 element-ui
```
=== getAvailableLibraryTypes() 开始 ===
远程组件库管理器返回的组件库数量: 1
远程组件库列表:
- element-ui (描述: ...)
开始转换组件库类型...
处理远程组件库: 'element-ui'
组件库 'element-ui' -> ELEMENT_UI (Element UI)
已添加到可用组件库列表: Element UI
转换后的组件库类型数量: 1
转换后的组件库列表:
- ELEMENT_UI (Element UI)
最终返回的组件库类型: Element UI
=== getAvailableLibraryTypes() 结束 ===
```

#### 5.2 复选框创建
```
=== createLibraryCheckBoxes() 开始 ===
创建组件库复选框，找到 1 个组件库类型
- ELEMENT_UI (Element UI)
创建复选框: Element UI (ELEMENT_UI) - 初始状态: 未选中
总共创建了 1 个复选框
=== createLibraryCheckBoxes() 结束 ===
```

#### 5.3 复选框更新
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

### 6. 修复建议

根据日志分析结果：

1. **如果远程组件库管理器返回了多余的组件库**：
   - 检查远程组件库管理器的配置
   - 清理不需要的组件库缓存

2. **如果本地缓存中有多余的组件库**：
   - 删除缓存目录中的多余文件
   - 重新启动 IntelliJ IDEA

3. **如果复选框创建有问题**：
   - 检查 `getAvailableLibraryTypes()` 方法的实现
   - 确保只返回需要的组件库类型

4. **如果复选框更新有问题**：
   - 检查 `updateCheckBoxes()` 方法的实现
   - 确保正确设置复选框状态

### 7. 测试步骤

1. 重新启动 IntelliJ IDEA
2. 打开项目组件库配置对话框
3. 查看日志输出
4. 根据日志分析问题原因
5. 实施相应的修复措施

## 结论

通过详细的日志分析，我们可以准确定位问题所在，并采取相应的修复措施。请按照上述步骤查看日志输出，然后根据分析结果进行修复。 