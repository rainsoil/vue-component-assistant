# VueKit API 参考文档

## 概述

本文档详细介绍了 VueKit 插件的所有公共 API，包括类、方法、参数和返回值。开发者可以使用这些 API 来扩展插件功能或集成到其他项目中。

## 核心类

### ComponentProvider

主要的组件数据提供者类，负责管理和提供所有来源的组件数据。

#### 构造函数

```java
public ComponentProvider(Project project)
```

**参数:**
- `project` - IntelliJ IDEA 项目对象，不能为 null

**说明:**
- 自动检测项目使用的组件库类型
- 初始化组件数据加载
- 注册到 ComponentProviderManager

#### 主要方法

##### getAllComponents()

```java
public List<ElementPlusComponent> getAllComponents()
```

**返回值:** 包含所有可用组件的列表

**说明:**
- 合并内置、自定义和官方组件库的组件
- 按优先级排序：内置 > 自定义 > 官方
- 如果没有任何组件则返回空列表

**使用示例:**
```java
ComponentProvider provider = new ComponentProvider(project);
List<ElementPlusComponent> allComponents = provider.getAllComponents();
System.out.println("总组件数: " + allComponents.size());
```

##### getComponent(String)

```java
public ElementPlusComponent getComponent(String componentName)
```

**参数:**
- `componentName` - 组件名称，不能为 null 或空字符串

**返回值:** 找到的组件对象，如果未找到则返回 null

**异常:**
- `IllegalArgumentException` - 如果组件名称为 null 或空字符串

**说明:**
- 按优先级查找：内置 > 自定义 > 官方
- 使用 HashMap 快速查找内置组件
- 支持异常处理和日志记录

**使用示例:**
```java
ElementPlusComponent button = provider.getComponent("el-button");
if (button != null) {
    System.out.println("找到按钮组件: " + button.getDescription());
}
```

##### getComponentsByPrefix(String)

```java
public List<ElementPlusComponent> getComponentsByPrefix(String prefix)
```

**参数:**
- `prefix` - 要搜索的组件前缀，如果为 null 或空字符串则返回所有组件

**返回值:** 匹配前缀的组件列表

**说明:**
- 使用精确前缀匹配（startsWith）
- 不区分大小写
- 按组件库优先级排序结果

**使用示例:**
```java
List<ElementPlusComponent> buttonComponents = provider.getComponentsByPrefix("el-button");
System.out.println("找到 " + buttonComponents.size() + " 个按钮相关组件");
```

##### searchComponents(String)

```java
public List<ElementPlusComponent> searchComponents(String prefix)
```

**参数:**
- `prefix` - 要搜索的前缀，如果为 null 或空字符串则返回所有组件

**返回值:** 匹配的组件列表

**说明:**
- 使用模糊匹配（contains）
- 不区分大小写
- 与 getComponentsByPrefix 的区别：模糊 vs 精确

**使用示例:**
```java
List<ElementPlusComponent> searchResults = provider.searchComponents("button");
System.out.println("模糊搜索 'button' 找到 " + searchResults.size() + " 个组件");
```

##### reloadComponents()

```java
public void reloadComponents()
```

**说明:**
- 清空现有组件数据
- 重新执行组件检测和加载
- 记录重新加载前后的组件数量变化

**使用场景:**
- 组件库发生变化时
- 用户手动刷新组件数据
- 系统检测到配置变更

**使用示例:**
```java
// 添加新的自定义组件库后
provider.reloadComponents();
System.out.println("组件数据已重新加载");
```

##### getComponentProps(String)

```java
public List<ElementPlusProp> getComponentProps(String componentName)
```

**参数:**
- `componentName` - 组件名称，不能为 null 或空字符串

**返回值:** 组件属性列表，如果组件不存在则返回空列表

**异常:**
- `IllegalArgumentException` - 如果组件名称为 null 或空字符串

**使用示例:**
```java
List<ElementPlusProp> props = provider.getComponentProps("el-button");
for (ElementPlusProp prop : props) {
    System.out.println("属性: " + prop.getName() + " - " + prop.getDescription());
}
```

##### getComponentEvents(String)

```java
public List<ElementPlusEvent> getComponentEvents(String componentName)
```

**参数:**
- `componentName` - 组件名称，不能为 null 或空字符串

**返回值:** 组件事件列表，如果组件不存在则返回空列表

**使用示例:**
```java
List<ElementPlusEvent> events = provider.getComponentEvents("el-button");
for (ElementPlusEvent event : events) {
    System.out.println("事件: " + event.getName() + " - " + event.getDescription());
}
```

##### getComponentSlots(String)

```java
public List<ElementPlusSlot> getComponentSlots(String componentName)
```

**参数:**
- `componentName` - 组件名称，不能为 null 或空字符串

**返回值:** 组件插槽列表，如果组件不存在则返回空列表

**使用示例:**
```java
List<ElementPlusSlot> slots = provider.getComponentSlots("el-card");
for (ElementPlusSlot slot : slots) {
    System.out.println("插槽: " + slot.getName() + " - " + slot.getDescription());
}
```

##### hasComponent(String)

```java
public boolean hasComponent(String componentName)
```

**参数:**
- `componentName` - 要检查的组件名称，不能为 null 或空字符串

**返回值:** 如果组件存在于内置组件库中则返回 true，否则返回 false

**注意:** 此方法只检查内置组件库，不检查自定义或官方组件库

**使用示例:**
```java
if (provider.hasComponent("el-button")) {
    System.out.println("el-button 存在于内置组件库中");
}
```

##### getComponentCount()

```java
public int getComponentCount()
```

**返回值:** 内置组件库中的组件数量

**注意:** 此方法只返回内置组件库的组件数量，如需获取所有来源的组件总数，请使用 `getAllComponents().size()`

**使用示例:**
```java
int count = provider.getComponentCount();
System.out.println("内置组件库包含 " + count + " 个组件");
```

##### getLibraryType()

```java
public ComponentLibraryDetector.LibraryType getLibraryType()
```

**返回值:** 当前检测到的组件库类型

**使用示例:**
```java
ComponentLibraryDetector.LibraryType type = provider.getLibraryType();
System.out.println("检测到的组件库: " + type.getDisplayName());
```

##### getLibraryDisplayName()

```java
public String getLibraryDisplayName()
```

**返回值:** 当前组件库的友好显示名称

**使用示例:**
```java
String displayName = provider.getLibraryDisplayName();
System.out.println("组件库显示名称: " + displayName);
```

##### getComponentLibraryDisplayName(String)

```java
public String getComponentLibraryDisplayName(String componentName)
```

**参数:**
- `componentName` - 要查询的组件名称，不能为 null 或空字符串

**返回值:** 组件所属的组件库显示名称

**说明:** 此方法主要用于UI显示，帮助用户了解组件的来源

**使用示例:**
```java
String libraryName = provider.getComponentLibraryDisplayName("custom-button");
System.out.println("组件所属库: " + libraryName);
```

### ComponentLibraryDetector

组件库检测器，负责自动检测项目中使用的 Vue 组件库类型。

#### 静态方法

##### detectComponentLibrary(Project)

```java
public static LibraryType detectComponentLibrary(Project project)
```

**参数:**
- `project` - 当前项目对象，不能为 null

**返回值:** 检测到的组件库类型，如果未检测到则返回 UNKNOWN

**异常:**
- `IllegalArgumentException` - 如果项目对象为 null

**检测逻辑:**
1. 验证项目对象是否有效
2. 获取项目根目录
3. 查找并读取 package.json 文件
4. 解析 JSON 内容，检查 dependencies 和 devDependencies
5. 根据检测到的包名返回对应的组件库类型

**使用示例:**
```java
LibraryType detectedType = ComponentLibraryDetector.detectComponentLibrary(project);
System.out.println("检测到的组件库: " + detectedType.getDisplayName());
```

##### getComponentPrefix(LibraryType)

```java
public static String getComponentPrefix(LibraryType libraryType)
```

**参数:**
- `libraryType` - 组件库类型，不能为 null

**返回值:** 对应的组件前缀字符串

**异常:**
- `IllegalArgumentException` - 如果组件库类型为 null

**组件前缀说明:**
- Element UI/Plus: 使用 "el-" 前缀（如 el-button, el-input）
- Ant Design Vue: 使用 "a-" 前缀（如 a-button, a-input）
- 未知类型: 返回空字符串

**使用示例:**
```java
String prefix = ComponentLibraryDetector.getComponentPrefix(LibraryType.ELEMENT_PLUS);
System.out.println("Element Plus 组件前缀: " + prefix); // 输出: el-
```

##### getDocumentationUrlTemplate(LibraryType)

```java
public static String getDocumentationUrlTemplate(LibraryType libraryType)
```

**参数:**
- `libraryType` - 组件库类型，不能为 null

**返回值:** 对应的文档URL模板字符串

**异常:**
- `IllegalArgumentException` - 如果组件库类型为 null

**文档URL模板说明:**
- Element UI: 官方中文文档，支持组件名占位符 %s
- Element Plus: 官方中文文档，支持组件名占位符 %s
- Ant Design Vue: 官方中文文档，支持组件名占位符 %s
- 未知类型: 返回空字符串

**使用示例:**
```java
String template = ComponentLibraryDetector.getDocumentationUrlTemplate(LibraryType.ELEMENT_PLUS);
String docUrl = String.format(template, "button");
System.out.println("按钮组件文档: " + docUrl);
```

##### isComponentFromLibrary(String, LibraryType)

```java
public static boolean isComponentFromLibrary(String componentName, LibraryType libraryType)
```

**参数:**
- `componentName` - 要检查的组件名称，不能为 null 或空字符串
- `libraryType` - 组件库类型，不能为 null

**返回值:** 如果组件属于该组件库则返回 true，否则返回 false

**异常:**
- `IllegalArgumentException` - 如果组件名称或组件库类型为 null

**检查逻辑:**
1. 验证组件名称不为空
2. 获取组件库的组件前缀
3. 检查组件名称是否以该前缀开头

**使用示例:**
```java
boolean isElementPlus = ComponentLibraryDetector.isComponentFromLibrary("el-button", LibraryType.ELEMENT_PLUS);
System.out.println("el-button 是否属于 Element Plus: " + isElementPlus); // 输出: true
```

##### getComponentDataPath(LibraryType)

```java
public static String getComponentDataPath(LibraryType libraryType)
```

**参数:**
- `libraryType` - 组件库类型，不能为 null

**返回值:** 对应的配置文件路径字符串

**异常:**
- `IllegalArgumentException` - 如果组件库类型为 null

**配置文件路径说明:**
- Element UI: element-ui-components.json
- Element Plus: element-plus-components.json  
- Ant Design Vue: ant-design-vue-components.json
- 未知类型: 默认使用 Element Plus 配置文件

**注意:** 这些文件位于插件的 resources/data/ 目录下

**使用示例:**
```java
String dataPath = ComponentLibraryDetector.getComponentDataPath(LibraryType.ELEMENT_PLUS);
System.out.println("Element Plus 数据文件路径: " + dataPath);
```

##### printDetectionInfo(Project)

```java
public static void printDetectionInfo(Project project)
```

**参数:**
- `project` - 当前项目，可以为 null

**说明:** 此方法主要用于开发和调试阶段，生产环境建议使用日志记录

**调试信息包括:**
- 项目基本信息（路径、名称等）
- 检测到的组件库类型
- 组件前缀
- 文档URL模板
- 数据文件路径

**使用示例:**
```java
ComponentLibraryDetector.printDetectionInfo(project);
```

#### 枚举类型

##### LibraryType

```java
public enum LibraryType {
    ELEMENT_UI("element-ui", "Element UI"),
    ELEMENT_PLUS("element-plus", "Element Plus"),
    ANT_DESIGN_VUE("ant-design-vue", "Ant Design Vue"),
    UNKNOWN("unknown", "未知组件库");
}
```

**字段:**
- `packageName` - npm 包名
- `displayName` - 友好的显示名称

**方法:**
- `getPackageName()` - 获取 npm 包名
- `getDisplayName()` - 获取显示名称

### ComponentLibraryManager

统一组件库管理器，负责管理组件的导入、导出、更新等操作。

#### 构造函数

```java
public ComponentLibraryManager()
```

**说明:**
- 初始化本地缓存管理器
- 初始化远程库管理器
- 初始化官方库管理器
- 加载已存在的组件库

#### 主要方法

##### importLibrary(ComponentLibrary)

```java
public ImportResult importLibrary(ComponentLibrary library)
```

**参数:**
- `library` - 要导入的组件库对象

**返回值:** 导入结果对象，包含成功/失败信息和冲突检测结果

**导入流程:**
1. 检查是否已存在同名组件库
2. 保存组件库到本地缓存
3. 更新内存中的组件库映射
4. 通知所有 ComponentProvider 重新加载数据

**使用示例:**
```java
ComponentLibraryManager manager = new ComponentLibraryManager();
ImportResult result = manager.importLibrary(newLibrary);
if (result.isSuccess()) {
    System.out.println("组件库导入成功: " + result.getMessage());
} else if (result.isConflict()) {
    System.out.println("检测到冲突: " + result.getMessage());
    // 处理冲突
}
```

##### replaceLibrary(ComponentLibrary, ComponentLibrary)

```java
public ImportResult replaceLibrary(ComponentLibrary newLibrary, ComponentLibrary existingLibrary)
```

**参数:**
- `newLibrary` - 新的组件库对象
- `existingLibrary` - 要替换的现有组件库对象

**返回值:** 替换结果对象

**替换流程:**
1. 删除旧的组件库
2. 保存新的组件库
3. 更新内存映射
4. 通知重新加载

**使用示例:**
```java
ImportResult result = manager.replaceLibrary(newVersion, oldVersion);
if (result.isSuccess()) {
    System.out.println("组件库替换成功");
}
```

##### getAllLibraries()

```java
public List<ComponentLibrary> getAllLibraries()
```

**返回值:** 所有已安装的组件库列表

**使用示例:**
```java
List<ComponentLibrary> libraries = manager.getAllLibraries();
for (ComponentLibrary library : libraries) {
    System.out.println("库: " + library.getName() + " (版本: " + library.getVersion() + ")");
}
```

##### removeLibrary(String)

```java
public boolean removeLibrary(String libraryId)
```

**参数:**
- `libraryId` - 要删除的组件库ID

**返回值:** 删除成功返回 true，否则返回 false

**使用示例:**
```java
boolean removed = manager.removeLibrary("custom-library-1");
if (removed) {
    System.out.println("组件库删除成功");
}
```

### CustomComponentLibraryManager

自定义组件库管理器，负责管理用户上传的自定义组件库。

#### 静态方法

##### getAllCustomLibraries()

```java
public static List<CustomLibraryConfig> getAllCustomLibraries()
```

**返回值:** 所有自定义组件库配置列表

**使用示例:**
```java
List<CustomLibraryConfig> customLibraries = CustomComponentLibraryManager.getAllCustomLibraries();
System.out.println("找到 " + customLibraries.size() + " 个自定义组件库");
```

##### getCustomComponent(String)

```java
public static ComponentInfo getCustomComponent(String componentName)
```

**参数:**
- `componentName` - 组件名称

**返回值:** 找到的自定义组件信息，如果未找到则返回 null

**使用示例:**
```java
ComponentInfo component = CustomComponentLibraryManager.getCustomComponent("custom-button");
if (component != null) {
    System.out.println("找到自定义组件: " + component.getDescription());
}
```

##### isCustomComponent(String)

```java
public static boolean isCustomComponent(String componentName)
```

**参数:**
- `componentName` - 组件名称

**返回值:** 如果是自定义组件则返回 true，否则返回 false

**使用示例:**
```java
if (CustomComponentLibraryManager.isCustomComponent("custom-button")) {
    System.out.println("这是一个自定义组件");
}
```

##### getCustomLibraryDisplayName(String)

```java
public static String getCustomLibraryDisplayName(String componentName)
```

**参数:**
- `componentName` - 组件名称

**返回值:** 组件所属的自定义组件库显示名称

**使用示例:**
```java
String libraryName = CustomComponentLibraryManager.getCustomLibraryDisplayName("custom-button");
System.out.println("组件所属库: " + libraryName);
```

## 数据模型

### ElementPlusComponent

表示一个 Vue 组件的完整信息。

#### 属性

```java
public class ElementPlusComponent {
    private String name;                    // 组件名称
    private String description;             // 组件描述
    private List<ElementPlusProp> props;   // 属性列表
    private List<ElementPlusEvent> events; // 事件列表
    private List<ElementPlusSlot> slots;   // 插槽列表
}
```

#### 方法

- `getName()` - 获取组件名称
- `setName(String)` - 设置组件名称
- `getDescription()` - 获取组件描述
- `setDescription(String)` - 设置组件描述
- `getProps()` - 获取属性列表
- `setProps(List<ElementPlusProp>)` - 设置属性列表
- `getEvents()` - 获取事件列表
- `setEvents(List<ElementPlusEvent>)` - 设置事件列表
- `getSlots()` - 获取插槽列表
- `setSlots(List<ElementPlusSlot>)` - 设置插槽列表

### ElementPlusProp

表示组件的属性信息。

#### 属性

```java
public class ElementPlusProp {
    private String name;           // 属性名称
    private String type;           // 属性类型
    private String description;    // 属性描述
    private String defaultValue;   // 默认值
    private boolean required;      // 是否必需
    private List<String> options;  // 可选值列表
}
```

### ElementPlusEvent

表示组件的事件信息。

#### 属性

```java
public class ElementPlusEvent {
    private String name;           // 事件名称
    private String description;    // 事件描述
    private String parameters;     // 事件参数
}
```

### ElementPlusSlot

表示组件的插槽信息。

#### 属性

```java
public class ElementPlusSlot {
    private String name;           // 插槽名称
    private String description;    // 插槽描述
    private String scope;          // 插槽作用域
}
```

## 异常类

### VueKitException

VueKit 插件的根异常类。

```java
public class VueKitException extends Exception {
    public VueKitException(String message);
    public VueKitException(String message, Throwable cause);
}
```

### ComponentLibraryException

组件库相关操作的异常类。

```java
public class ComponentLibraryException extends VueKitException {
    public ComponentLibraryException(String message);
    public ComponentLibraryException(String message, Throwable cause);
}
```

### FileOperationException

文件操作相关的异常类。

```java
public class FileOperationException extends VueKitException {
    public FileOperationException(String message);
    public FileOperationException(String message, Throwable cause);
}
```

### JsonParseException

JSON 解析相关的异常类。

```java
public class JsonParseException extends VueKitException {
    public JsonParseException(String message);
    public JsonParseException(String message, Throwable cause);
}
```

## 工具类

### VueKitLogger

VueKit 专用的日志记录工具类。

#### 静态方法

##### getLogger(Class<?>)

```java
public static Logger getLogger(Class<?> clazz)
```

**参数:**
- `clazz` - 要获取日志记录器的类

**返回值:** IntelliJ IDEA 的 Logger 实例

**使用示例:**
```java
private static final Logger LOG = VueKitLogger.getLogger(MyClass.class);
```

##### debug(Logger, String)

```java
public static void debug(Logger logger, String message)
```

**参数:**
- `logger` - 日志记录器
- `message` - 调试消息

**使用示例:**
```java
VueKitLogger.debug(LOG, "开始处理组件: " + componentName);
```

##### info(Logger, String)

```java
public static void info(Logger logger, String message)
```

**参数:**
- `logger` - 日志记录器
- `message` - 信息消息

**使用示例:**
```java
VueKitLogger.info(LOG, "组件处理完成，共处理 " + count + " 个组件");
```

##### warn(Logger, String)

```java
public static void warn(Logger logger, String message)
```

**参数:**
- `logger` - 日志记录器
- `message` - 警告消息

**使用示例:**
```java
VueKitLogger.warn(LOG, "组件 '" + name + "' 缺少描述信息");
```

##### error(Logger, String, Throwable)

```java
public static void error(Logger logger, String message, Throwable throwable)
```

**参数:**
- `logger` - 日志记录器
- `message` - 错误消息
- `throwable` - 异常对象

**使用示例:**
```java
VueKitLogger.error(LOG, "加载组件库失败", exception);
```

### ErrorHandler

错误处理工具类。

#### 静态方法

##### handleException(Exception, String)

```java
public static void handleException(Exception exception, String operation)
```

**参数:**
- `exception` - 要处理的异常
- `operation` - 操作描述

**使用示例:**
```java
try {
    // 执行操作
} catch (Exception e) {
    ErrorHandler.handleException(e, "加载组件数据");
}
```

## 使用示例

### 基本使用

```java
// 1. 创建组件提供者
Project project = getCurrentProject();
ComponentProvider provider = new ComponentProvider(project);

// 2. 获取所有组件
List<ElementPlusComponent> allComponents = provider.getAllComponents();
System.out.println("总组件数: " + allComponents.size());

// 3. 查找特定组件
ElementPlusComponent button = provider.getComponent("el-button");
if (button != null) {
    System.out.println("按钮组件描述: " + button.getDescription());
    
    // 4. 获取组件属性
    List<ElementPlusProp> props = provider.getComponentProps("el-button");
    for (ElementPlusProp prop : props) {
        System.out.println("属性: " + prop.getName() + " (" + prop.getType() + ")");
    }
    
    // 5. 获取组件事件
    List<ElementPlusEvent> events = provider.getComponentEvents("el-button");
    for (ElementPlusEvent event : events) {
        System.out.println("事件: " + event.getName() + " - " + event.getDescription());
    }
}
```

### 组件库管理

```java
// 1. 创建组件库管理器
ComponentLibraryManager manager = new ComponentLibraryManager();

// 2. 导入自定义组件库
ComponentLibrary customLibrary = createCustomLibrary();
ImportResult result = manager.importLibrary(customLibrary);

if (result.isSuccess()) {
    System.out.println("组件库导入成功");
    
    // 3. 重新加载组件数据
    provider.reloadComponents();
    
    // 4. 验证新组件是否可用
    ElementPlusComponent newComponent = provider.getComponent("custom-component");
    if (newComponent != null) {
        System.out.println("新组件已可用: " + newComponent.getName());
    }
} else if (result.isConflict()) {
    System.out.println("检测到冲突: " + result.getMessage());
    // 处理冲突
}
```

### 组件检测

```java
// 1. 检测项目使用的组件库
LibraryType libraryType = ComponentLibraryDetector.detectComponentLibrary(project);
System.out.println("检测到的组件库: " + libraryType.getDisplayName());

// 2. 获取组件前缀
String prefix = ComponentLibraryDetector.getComponentPrefix(libraryType);
System.out.println("组件前缀: " + prefix);

// 3. 检查组件是否属于特定库
boolean isElementPlus = ComponentLibraryDetector.isComponentFromLibrary("el-button", LibraryType.ELEMENT_PLUS);
System.out.println("el-button 是否属于 Element Plus: " + isElementPlus);

// 4. 获取文档URL模板
String docTemplate = ComponentLibraryDetector.getDocumentationUrlTemplate(libraryType);
String buttonDocUrl = String.format(docTemplate, "button");
System.out.println("按钮组件文档: " + buttonDocUrl);
```

## 注意事项

### 1. 线程安全

- `ComponentProvider` 不是线程安全的，请在单线程环境中使用
- 如果需要多线程访问，请考虑使用同步机制或为每个线程创建独立的实例

### 2. 性能考虑

- 首次创建 `ComponentProvider` 时会加载所有组件数据，可能需要一些时间
- 使用 `getComponent()` 查找特定组件比遍历所有组件更高效
- 考虑使用缓存来避免重复的组件查找操作

### 3. 内存管理

- 组件数据会占用一定的内存空间
- 如果不再需要 `ComponentProvider`，请及时释放引用
- 使用 `reloadComponents()` 会清空现有数据，请谨慎使用

### 4. 异常处理

- 所有公共方法都可能抛出异常，请做好异常处理
- 使用 `VueKitLogger` 记录详细的错误信息
- 对于用户操作，请提供友好的错误提示

### 5. 版本兼容性

- 不同版本的 VueKit 插件可能有 API 变化
- 请查看版本更新日志了解具体的变更
- 建议在升级插件版本后测试现有代码的兼容性

## 更新日志

### v3.0.0
- 新增 `ComponentLibraryManager` 类
- 新增官方组件库市场支持
- 优化组件数据加载性能
- 增强错误处理和日志记录

### v2.0.0
- 重构 `ComponentProvider` 类
- 新增自定义组件库支持
- 改进组件检测逻辑
- 统一日志记录接口

### v1.0.0
- 初始版本发布
- 支持 Element Plus 组件库
- 基本的组件补全功能

---

*本文档基于 VueKit v3.0.0 编写，如有疑问请参考源码或联系开发团队*
