# VueKit API 文档

## 概述

本文档详细介绍了 VueKit 插件的 API 接口，包括核心类、方法、配置选项等。

## 核心类

### CustomComponentLibraryManager

自定义组件库管理器，负责加载、管理和持久化自定义组件库数据。

#### 类定义
```java
public class CustomComponentLibraryManager
```

#### 主要方法

##### 静态方法

###### `getAllCustomLibraries()`
获取所有已加载的自定义组件库。

**返回值：** `List<CustomLibraryConfig>` - 自定义组件库配置列表

**示例：**
```java
List<CustomLibraryConfig> libraries = CustomComponentLibraryManager.getAllCustomLibraries();
for (CustomLibraryConfig config : libraries) {
    System.out.println("组件库: " + config.getDisplayName());
}
```

###### `loadCustomLibrary(String jsonContent)`
从JSON字符串加载自定义组件库。

**参数：**
- `jsonContent` (String) - JSON格式的组件库配置

**返回值：** `boolean` - 是否加载成功

**示例：**
```java
String json = "{\"name\":\"my-lib\",\"components\":[...]}";
boolean success = CustomComponentLibraryManager.loadCustomLibrary(json);
```

###### `loadCustomLibraryFromFile(VirtualFile file)`
从文件加载自定义组件库。

**参数：**
- `file` (VirtualFile) - 组件库配置文件

**返回值：** `boolean` - 是否加载成功

**示例：**
```java
VirtualFile file = // 获取文件对象
boolean success = CustomComponentLibraryManager.loadCustomLibraryFromFile(file);
```

###### `isCustomComponent(String componentName)`
检查指定组件是否为自定义组件。

**参数：**
- `componentName` (String) - 组件名称

**返回值：** `boolean` - 是否为自定义组件

**示例：**
```java
boolean isCustom = CustomComponentLibraryManager.isCustomComponent("my-button");
```

###### `getCustomComponent(String componentName)`
获取自定义组件对象。

**参数：**
- `componentName` (String) - 组件名称

**返回值：** `ElementPlusComponent` - 组件对象，不存在时返回null

**示例：**
```java
ElementPlusComponent component = CustomComponentLibraryManager.getCustomComponent("my-button");
if (component != null) {
    System.out.println("组件描述: " + component.getDescription());
}
```

###### `getCustomLibraryForComponent(String componentName)`
获取组件所属的自定义组件库。

**参数：**
- `componentName` (String) - 组件名称

**返回值：** `CustomLibraryConfig` - 组件库配置，不属于任何自定义库时返回null

**示例：**
```java
CustomLibraryConfig config = CustomComponentLibraryManager.getCustomLibraryForComponent("my-button");
if (config != null) {
    System.out.println("组件库: " + config.getDisplayName());
}
```

###### `removeCustomLibrary(String libraryName)`
删除指定的自定义组件库。

**参数：**
- `libraryName` (String) - 组件库名称

**返回值：** `boolean` - 是否删除成功

**示例：**
```java
boolean removed = CustomComponentLibraryManager.removeCustomLibrary("my-lib");
```

###### `clearAllCustomLibraries()`
清空所有自定义组件库。

**示例：**
```java
CustomComponentLibraryManager.clearAllCustomLibraries();
```

###### `validateCustomLibraryConfig(String jsonContent)`
验证自定义组件库配置的JSON格式。

**参数：**
- `jsonContent` (String) - JSON配置内容

**返回值：** `ValidationResult` - 验证结果

**示例：**
```java
ValidationResult result = CustomComponentLibraryManager.validateCustomLibraryConfig(json);
if (result.isValid()) {
    System.out.println("配置验证通过");
} else {
    System.out.println("验证失败: " + result.getErrorMessage());
}
```

#### 内部类

##### CustomLibraryConfig

自定义组件库配置类。

**字段：**
- `name` (String) - 组件库唯一标识名称
- `displayName` (String) - 组件库显示名称
- `version` (String) - 组件库版本号
- `description` (String) - 组件库描述信息
- `componentPrefix` (String) - 组件前缀
- `documentationUrlTemplate` (String) - 文档URL模板
- `components` (List<ElementPlusComponent>) - 组件列表

**方法：**
- `getName()` / `setName(String)` - 获取/设置组件库名称
- `getDisplayName()` / `setDisplayName(String)` - 获取/设置显示名称
- `getVersion()` / `setVersion(String)` - 获取/设置版本号
- `getDescription()` / `setDescription(String)` - 获取/设置描述
- `getComponentPrefix()` / `setComponentPrefix(String)` - 获取/设置组件前缀
- `getDocumentationUrlTemplate()` / `setDocumentationUrlTemplate(String)` - 获取/设置文档URL模板
- `getComponents()` / `setComponents(List<ElementPlusComponent>)` - 获取/设置组件列表

##### ValidationResult

验证结果类。

**方法：**
- `addError(String)` - 添加错误信息
- `addWarning(String)` - 添加警告信息
- `isValid()` - 检查是否验证通过
- `getErrors()` - 获取错误信息列表
- `getWarnings()` - 获取警告信息列表
- `getErrorMessage()` - 获取错误信息字符串

### ElementPlusTestCompletionProvider

智能补全提供者，负责分析用户输入上下文并提供相应的补全建议。

#### 类定义
```java
public class ElementPlusTestCompletionProvider extends CompletionProvider<CompletionParameters>
```

#### 主要方法

##### `addCompletions(CompletionParameters parameters, ProcessingContext context, CompletionResultSet result)`
添加补全建议。

**参数：**
- `parameters` (CompletionParameters) - 补全参数
- `context` (ProcessingContext) - 处理上下文
- `result` (CompletionResultSet) - 补全结果集

**示例：**
```java
@Override
protected void addCompletions(@NotNull CompletionParameters parameters,
                             @NotNull ProcessingContext context,
                             @NotNull CompletionResultSet result) {
    // 实现补全逻辑
}
```

##### `analyzeContext(CompletionParameters parameters)`
分析用户输入上下文。

**参数：**
- `parameters` (CompletionParameters) - 补全参数

**返回值：** `ContextInfo` - 上下文信息

**示例：**
```java
ContextInfo contextInfo = analyzeContext(parameters);
String prefix = contextInfo.getPrefix();
String currentComponent = contextInfo.getCurrentComponent();
```

### ElementPlusDocumentationProvider

文档提供者，负责生成和显示组件文档。

#### 类定义
```java
public class ElementPlusDocumentationProvider implements DocumentationProvider
```

#### 主要方法

##### `generateDoc(PsiElement element, PsiElement originalElement)`
生成文档内容。

**参数：**
- `element` (PsiElement) - 目标元素
- `originalElement` (PsiElement) - 原始元素

**返回值：** `String` - 文档内容

**示例：**
```java
@Override
public String generateDoc(PsiElement element, PsiElement originalElement) {
    // 生成文档HTML
    return DocumentationStyleGenerator.generateHtmlDocumentation(component);
}
```

### DocumentationStyleGenerator

文档样式生成器，负责生成格式化的HTML文档。

#### 类定义
```java
public class DocumentationStyleGenerator
```

#### 主要方法

##### `generateHtmlDocumentation(ElementPlusComponent component)`
生成HTML格式的组件文档。

**参数：**
- `component` (ElementPlusComponent) - 组件对象

**返回值：** `String` - HTML文档内容

**示例：**
```java
String html = DocumentationStyleGenerator.generateHtmlDocumentation(component);
```

##### `generateTextDocumentation(ElementPlusComponent component)`
生成纯文本格式的组件文档。

**参数：**
- `component` (ElementPlusComponent) - 组件对象

**返回值：** `String` - 文本文档内容

**示例：**
```java
String text = DocumentationStyleGenerator.generateTextDocumentation(component);
```

### ComponentProvider

组件数据提供者，负责加载和管理组件数据。

#### 类定义
```java
public class ComponentProvider
```

#### 主要方法

##### `getAllComponents()`
获取所有组件。

**返回值：** `List<ElementPlusComponent>` - 组件列表

**示例：**
```java
List<ElementPlusComponent> components = ComponentProvider.getAllComponents();
```

##### `getComponentByName(String name)`
根据名称获取组件。

**参数：**
- `name` (String) - 组件名称

**返回值：** `ElementPlusComponent` - 组件对象

**示例：**
```java
ElementPlusComponent component = ComponentProvider.getComponentByName("el-button");
```

### ComponentLibraryDetector

组件库检测器，负责检测项目中使用的组件库。

#### 类定义
```java
public class ComponentLibraryDetector
```

#### 主要方法

##### `detectComponentLibrary(Project project)`
检测项目使用的组件库。

**参数：**
- `project` (Project) - 项目对象

**返回值：** `String` - 组件库类型

**示例：**
```java
String libraryType = ComponentLibraryDetector.detectComponentLibrary(project);
```

## 数据模型

### ElementPlusComponent

组件数据模型。

**字段：**
- `name` (String) - 组件名称
- `description` (String) - 组件描述
- `version` (String) - 组件版本
- `example` (String) - 使用示例
- `docUrl` (String) - 文档URL
- `props` (List<ElementPlusProp>) - 属性列表
- `events` (List<ElementPlusEvent>) - 事件列表
- `slots` (List<ElementPlusSlot>) - 插槽列表

### ElementPlusProp

组件属性数据模型。

**字段：**
- `name` (String) - 属性名称
- `type` (String) - 属性类型
- `description` (String) - 属性描述
- `defaultValue` (String) - 默认值
- `required` (boolean) - 是否必填
- `options` (List<String>) - 可选值列表

### ElementPlusEvent

组件事件数据模型。

**字段：**
- `name` (String) - 事件名称
- `description` (String) - 事件描述
- `parameters` (String) - 事件参数

### ElementPlusSlot

组件插槽数据模型。

**字段：**
- `name` (String) - 插槽名称
- `description` (String) - 插槽描述

## 配置选项

### 插件设置

插件支持以下配置选项：

#### 组件库设置
- **自动检测**：是否自动检测项目中的组件库
- **默认组件库**：设置默认使用的组件库
- **自定义路径**：自定义组件库文件路径

#### 补全设置
- **启用组件补全**：是否启用组件补全功能
- **启用属性补全**：是否启用属性补全功能
- **启用事件补全**：是否启用事件补全功能
- **启用插槽补全**：是否启用插槽补全功能

#### 文档设置
- **启用悬停文档**：是否启用悬停文档显示
- **启用右键文档**：是否启用右键文档菜单
- **文档样式**：文档显示样式（表格/列表）

## 扩展开发

### 添加新的组件库支持

1. 创建组件库JSON数据文件
2. 在 `ComponentProvider` 中添加加载逻辑
3. 更新 `ComponentLibraryDetector` 添加检测逻辑
4. 在 `plugin.xml` 中注册相关扩展点

### 自定义补全逻辑

1. 继承 `CompletionProvider` 类
2. 实现 `addCompletions` 方法
3. 在 `plugin.xml` 中注册补全提供者

### 自定义文档生成

1. 继承 `DocumentationProvider` 接口
2. 实现 `generateDoc` 方法
3. 在 `plugin.xml` 中注册文档提供者

## 错误处理

### 常见异常

#### CustomLibraryLoadException
自定义组件库加载异常。

**原因：**
- JSON格式错误
- 文件读取失败
- 组件数据验证失败

**处理方式：**
```java
try {
    CustomComponentLibraryManager.loadCustomLibrary(jsonContent);
} catch (Exception e) {
    LOG.error("加载自定义组件库失败", e);
    // 显示错误信息给用户
}
```

#### ComponentNotFoundException
组件未找到异常。

**原因：**
- 组件名称不存在
- 组件库未加载
- 组件数据损坏

**处理方式：**
```java
ElementPlusComponent component = ComponentProvider.getComponentByName(name);
if (component == null) {
    LOG.warn("组件未找到: " + name);
    // 返回默认组件或显示错误信息
}
```

## 性能优化

### 缓存机制

插件使用多层缓存机制提高性能：

1. **内存缓存**：组件数据加载到内存中
2. **文件缓存**：自定义组件库数据持久化到文件
3. **智能加载**：按需加载组件数据

### 延迟加载

- 组件数据在首次使用时才加载
- 文档内容在需要时才生成
- 补全建议在用户输入时才计算

## 调试和日志

### 日志配置

插件使用 IntelliJ IDEA 的日志系统：

```java
private static final Logger LOG = Logger.getInstance(YourClass.class);
```

### 调试模式

启用调试模式查看详细日志：

1. 打开 **Help** → **Diagnostic Tools** → **Debug Log Settings**
2. 添加日志配置：`com.chu7.vuecomponentassistant`
3. 重启 IntelliJ IDEA

### 常见调试信息

- 组件库检测结果
- 补全上下文分析
- 文档生成过程
- 缓存操作状态

## 版本兼容性

### IntelliJ IDEA 版本

- **最低版本**：2023.1
- **推荐版本**：2023.2+
- **最新版本**：2024.1+

### Java 版本

- **最低版本**：Java 11
- **推荐版本**：Java 17
- **最新版本**：Java 21

### 插件版本

- **v1.0.0**：基础功能
- **v1.0.2**：多组件库支持
- **v2.0.0**：自定义组件库支持

## 许可证

本API文档遵循与主项目相同的 MIT 许可证。
