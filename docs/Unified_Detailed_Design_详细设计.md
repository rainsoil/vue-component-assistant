### Unified Vue Component Assistant 详细设计

本详细设计对应《Unified_Migration_Plan_详细方案.md》，在保持阶段性目标不变的前提下，给出面向实现的模块划分、关键数据结构、核心流程与关键代码片段（骨架），确保实现具备可操作性与可测试性。

---

## 1. 架构与模块

- 扩展层（IDE 接入）
  - XmlTagNameProvider（标签名候选）
  - XmlElementDescriptorProvider（元素描述）
  - XmlAttributeDescriptorsProvider（属性/事件描述）
  - DocumentationProvider（悬停文档）
- 数据层（Provider）
  - ComponentProvider（按库提供组件/属性/事件/插槽模型）
  - ComponentProviderManager（Project→Provider 映射、reload 通知）
- 存储层（库装载）
  - JsonLibraryLoader（本地 JSON 解析）
  - RemoteLibraryManager（远程官方库下载/校验/落盘）
  - LocalCacheManager（缓存读写）
- 配置层
  - PluginSettings（应用级）
  - ProjectSettingsManager、ComponentLibraryConfigManager（项目级库/版本/源/优先级）
- 文档层
  - DocRenderer（组件/属性/事件/插槽 HTML 渲染、样式）

---

## 2. 统一数据结构（与 data/ JSON 对齐）

```java
// 简化的统一模型（与 data/element-*.json 对齐）
class ComponentLibrary {
  String id;              // e.g. "element-plus"
  String name;            // display name
  String componentPrefix; // e.g. "el-"
  String version;
  String sourceUrl;       // 可选
  String lastUpdated;
  List<Component> components;
}

class Component {
  String name;            // e.g. "el-input"
  String description;     // 可选
  String version;         // 可选
  String example;         // 可选
  String docUrl;          // 可选
  List<Prop> props;       // 可为空
  List<Event> events;     // 可为空
  List<Slot> slots;       // 可为空
}

class Prop {
  String name;
  Object type;            // string or string[] (保持 JSON 兼容)
  String description;     // 可选
  String defaultValue;    // 可选
  boolean required;       // 可选
  List<String> options;   // 可选（枚举值）
}

class Event {
  String name;
  String description;     // 可选
  Object parameters;      // string or array<param>
}

class Slot {
  String name;
  String description;     // 可选
}
```

---

## 3. 核心流程

### 3.1 Provider 装载流程

```java
class UnifiedComponentProvider {
  private final String libraryId;              // element-plus / element-ui
  private final String componentPrefix;        // el-
  private final Map<String, Component> nameToComponent;

  UnifiedComponentProvider(ComponentLibrary lib) {
    this.libraryId = lib.id;
    this.componentPrefix = lib.componentPrefix;
    this.nameToComponent = lib.components.stream()
      .collect(Collectors.toMap(c -> c.name, c -> c, (a,b)->a, LinkedHashMap::new));
  }

  boolean supportsTag(String tagName) {
    return tagName != null && tagName.startsWith(componentPrefix) && nameToComponent.containsKey(tagName);
  }

  @Nullable Component getComponent(String tagName) { return nameToComponent.get(tagName); }
}
```

数据优先级：LocalCacheManager → RemoteLibraryManager → CustomLibrary 合并（策略见迁移方案）。

### 3.2 标签/元素描述接入

```java
// XmlElementDescriptorProvider（核心要点）
public class UnifiedTagNameProvider implements XmlElementDescriptorProvider {
  @Override
  public XmlElementDescriptor getDescriptor(XmlTag xmlTag) {
    String tagName = xmlTag.getName();
    UnifiedComponentProvider provider = ProviderManager.getProvider(xmlTag.getProject());
    if (provider == null || !provider.supportsTag(tagName)) return null;
    Component c = provider.getComponent(tagName);

    XmlNSDescriptor ns = null; XmlElementDescriptor origin = null;
    try {
      ns = xmlTag.getNSDescriptor(xmlTag.getNamespace(), false);
      if (ns != null) origin = ns.getElementDescriptor(xmlTag);
    } catch (ProcessCanceledException ignored) { }

    return new UnifiedXmlElementDescriptor(c, origin, ns, tagName);
  }
}
```

### 3.3 属性/事件描述生成

```java
public class UnifiedXmlAttributeDescriptorsProvider implements XmlAttributeDescriptorsProvider {
  @Override
  public XmlAttributeDescriptor[] getAttributeDescriptors(XmlTag xmlTag) {
    UnifiedComponentProvider provider = ProviderManager.getProvider(xmlTag.getProject());
    if (provider == null || !provider.supportsTag(xmlTag.getName())) return XmlAttributeDescriptor.EMPTY;
    Component c = provider.getComponent(xmlTag.getName());
    return DescriptorFactory.buildDescriptors(c).toArray(new XmlAttributeDescriptor[0]);
  }
}

class DescriptorFactory {
  static List<UnifiedXmlAttributeDescriptor> buildDescriptors(Component c) {
    List<UnifiedXmlAttributeDescriptor> out = new ArrayList<>();
    if (c.props != null) for (Prop p : c.props) {
      out.add(new UnifiedXmlAttributeDescriptor(
        p.name,
        p.description,
        (p.options != null ? p.options.toArray(new String[0]) : new String[0]),
        safeDefault(p.defaultValue),
        AttributeType.PARAM
      ));
    }
    if (c.events != null) for (Event e : c.events) {
      out.add(new UnifiedXmlAttributeDescriptor(
        e.name,
        e.description,
        new String[0],
        stringifyParams(e.parameters),
        AttributeType.EVENT
      ));
    }
    return out;
  }
}
```

### 3.4 悬停文档渲染

```java
public class UnifiedDocumentationProvider extends AbstractDocumentationProvider {
  private final DocRenderer renderer = new DocRenderer();

  @Override public @Nullable String generateDoc(PsiElement element, @Nullable PsiElement original) {
    if (element instanceof HtmlTag) {
      HtmlTag tag = (HtmlTag) element;
      UnifiedComponentProvider provider = ProviderManager.getProvider(tag.getProject());
      if (provider == null || !provider.supportsTag(tag.getName())) return null;
      Component c = provider.getComponent(tag.getName());
      return renderer.renderComponent(c);
    }
    if (element instanceof XmlAttribute && element.getParent() instanceof HtmlTag) {
      HtmlTag parent = (HtmlTag) element.getParent();
      UnifiedComponentProvider provider = ProviderManager.getProvider(parent.getProject());
      if (provider == null || !provider.supportsTag(parent.getName())) return null;
      Component c = provider.getComponent(parent.getName());
      String attrName = ((XmlAttribute) element).getName();
      // 优先属性，再事件
      Prop p = findProp(c, attrName); if (p != null) return renderer.renderProp(c, p);
      Event e = findEvent(c, attrName); if (e != null) return renderer.renderEvent(c, e);
    }
    return null; // 交由其它 Provider
  }
}
```

---

## 4. 远程/自定义/缓存与热更新

### 4.1 装载与合并

```java
class LibraryCoordinator {
  ComponentLibrary loadEffectiveLibrary(String libraryId, String version) {
    // 1) LocalCacheManager.tryLoad(libraryId, version)
    // 2) if miss: RemoteLibraryManager.fetch(libraryId, version) + cache.save()
    // 3) CustomLibraryMerger.merge(base, custom)
    // 4) return merged
  }
}
```

### 4.2 Reload 通知

```java
class ProviderManager {
  private static final Map<Project, UnifiedComponentProvider> providers = new ConcurrentHashMap<>();

  static UnifiedComponentProvider getProvider(Project p) {
    return providers.computeIfAbsent(p, proj -> buildProvider(proj));
  }

  static void notifyReload(Project p) {
    providers.computeIfPresent(p, (proj, old) -> buildProvider(proj));
    // 可进一步发事件让 UI/Doc 刷新
  }

  private static UnifiedComponentProvider buildProvider(Project p) {
    PluginSettings s = PluginSettings.getInstance();
    ComponentLibrary lib = new LibraryCoordinator().loadEffectiveLibrary(s.getLibraryId(), s.getVersion());
    return new UnifiedComponentProvider(lib);
  }
}
```

---

## 5. 差异适配与规则

- 绑定属性归一：`modelValue`（ElPlus）优先，否则 `value`（ElUI）。
- 值枚举按库生效：如 `el-button.size` 枚举随库不同而不同。
- 插槽：先文档展示，补全为增强项（结合 Vue PSI）。
- 文档链接：优先使用 `component.docUrl`，否则从库级 `sourceUrl/docsBase` 推导。

---

## 6. 测试设计（摘要）

- 单元测试：
  - JsonLibraryLoader 解析（字段完整、类型兼容、必填校验）。
  - DescriptorFactory 生成（属性/事件数量、枚举值一致）。
  - DocRenderer 渲染（含 Props/Events/Slots 表格）。
- 集成测试：
  - P1-P5 用例集（参考《Unified_Migration_Plan_详细方案.md》）在两库间切换回归。
  - P6：远程下载失败→缓存回退、切换库/版本的即时生效。

---

## 7. 性能与稳定性

- Provider 懒加载与缓存；
- 补全/文档路径避免 I/O；
- 远程拉取异步进行，失败不阻断；
- 记录统计指标（补全命中、渲染时延、下载失败率）。

---

## 8. 扩展与演进

- 增加更多库（Ant Design Vue）：仅需提供符合 Schema 的 JSON。
- 类型化增强：从 `Prop.type` 推导更智能的值提示与校验。
- 插槽补全：结合 Vue PSI，在 `v-slot`/`#` 上下文提供候选。

---

## 9. 安全与回退

- JSON 校验与签名（可选）；
- 错误隔离（坏数据跳过组件级）；
- 永不在补全/文档路径做网络请求。

---

本详细设计提供了实现层必须遵循的模块职责划分、数据模型、关键流程与代码骨架，确保与 `data/element-plus-libraries.json`、`data/element-ui-libraries.json` 一致，且与迁移计划的阶段化测试用例相互对照，可作为开发与评审的实现依据。

---

## 10. 架构设计（深化）

### 10.1 逻辑分层
- IDE 扩展层：统一对 IntelliJ 的扩展点接入，尽量“合并原生描述，不做破坏性覆盖”。
- 服务编排层：ProviderManager、LibraryCoordinator 负责 Provider 的构建、切换与热更新广播。
- 数据接入层：JsonLibraryLoader、RemoteLibraryManager、LocalCacheManager、自定义库合并器。
- 领域模型层：ComponentLibrary / Component / Prop / Event / Slot 统一模型与校验。
- 文档渲染层：DocRenderer 生成统一的 HTML 风格文档，支持主题/语言可配置。

### 10.2 关键交互
- 启动：StartupActivity → ProviderManager.buildProvider() → LibraryCoordinator.loadEffectiveLibrary()
- 用户切换库/版本：Settings 保存 → ProviderManager.notifyReload() → 新 Provider 生效 → 下次补全/文档即时读取新模型。
- 远程同步：RemoteLibraryManager.fetch() 成功后 → 缓存落盘 → ProviderManager.notifyReload()。

---

## 11. 目录架构设计（建议）

```text
src/main/java/com/xxx/unified/
  startup/
    UnifiedPluginStartupActivity.java
  extension/
    UnifiedTagNameProvider.java
    UnifiedXmlElementDescriptor.java
    UnifiedXmlAttributeDescriptorsProvider.java
    UnifiedXmlAttributeDescriptor.java
    UnifiedDocumentationProvider.java
  provider/
    ProviderManager.java
    UnifiedComponentProvider.java
    DescriptorFactory.java
    BindingPropHelper.java
  library/
    LibraryCoordinator.java
    JsonLibraryLoader.java
    RemoteLibraryManager.java
    LocalCacheManager.java
    CustomLibraryMerger.java
    model/
      ComponentLibrary.java
      Component.java
      Prop.java
      Event.java
      Slot.java
  docs/
    DocRenderer.java
    DocStyle.css (或内联样式常量)
  settings/
    PluginSettings.java
    ProjectSettingsManager.java
    ComponentLibraryConfigManager.java
resources/META-INF/
  plugin.xml
resources/data/
  element-plus-libraries.json
  element-ui-libraries.json
```

---

## 12. plugin.xml 设计（示例）

```xml
<idea-plugin>
  <id>com.xxx.unified.vuekit</id>
  <name>Unified Vue Component Assistant</name>
  <depends>com.intellij.modules.platform</depends>
  <depends>com.intellij.modules.xml</depends>
  <depends>JavaScript</depends>
  <depends>org.jetbrains.plugins.vue</depends>

  <extensions defaultExtensionNs="com.intellij">
    <postStartupActivity implementation="com.xxx.unified.startup.UnifiedPluginStartupActivity"/>

    <xml.tagNameProvider implementation="com.xxx.unified.extension.UnifiedTagNameProvider" order="first"/>
    <xml.elementDescriptorProvider implementation="com.xxx.unified.extension.UnifiedTagNameProvider" order="first"/>
    <xml.attributeDescriptorsProvider implementation="com.xxx.unified.extension.UnifiedXmlAttributeDescriptorsProvider" order="first"/>

    <lang.documentationProvider language="HTML" implementationClass="com.xxx.unified.extension.UnifiedDocumentationProvider" order="first"/>
    <lang.documentationProvider language="XML" implementationClass="com.xxx.unified.extension.UnifiedDocumentationProvider" order="first"/>
    <lang.documentationProvider language="Vue" implementationClass="com.xxx.unified.extension.UnifiedDocumentationProvider" order="first"/>

    <applicationService serviceImplementation="com.xxx.unified.settings.PluginSettings"/>
    <projectService serviceImplementation="com.xxx.unified.settings.ProjectSettingsManager"/>
    <projectService serviceImplementation="com.xxx.unified.settings.ComponentLibraryConfigManager"/>
  </extensions>
</idea-plugin>
```

---

## 13. 数据结构设计（扩展 Schema）

### 13.1 JSON Schema 约束（片段）

```json
{
  "$schema": "http://json-schema.org/draft-07/schema#",
  "title": "ComponentLibrary",
  "type": "object",
  "required": ["id", "name", "componentPrefix", "version", "components"],
  "properties": {
    "id": {"type": "string"},
    "name": {"type": "string"},
    "componentPrefix": {"type": "string"},
    "version": {"type": "string"},
    "sourceUrl": {"type": "string"},
    "lastUpdated": {"type": "string"},
    "components": {
      "type": "array",
      "items": {"$ref": "#/definitions/Component"}
    }
  },
  "definitions": {
    "Component": {
      "type": "object",
      "required": ["name"],
      "properties": {
        "name": {"type": "string"},
        "description": {"type": "string"},
        "version": {"type": "string"},
        "example": {"type": "string"},
        "docUrl": {"type": "string"},
        "props": {"type": "array", "items": {"$ref": "#/definitions/Prop"}},
        "events": {"type": "array", "items": {"$ref": "#/definitions/Event"}},
        "slots": {"type": "array", "items": {"$ref": "#/definitions/Slot"}}
      }
    },
    "Prop": {
      "type": "object",
      "required": ["name"],
      "properties": {
        "name": {"type": "string"},
        "type": {"oneOf": [{"type": "string"}, {"type": "array", "items": {"type": "string"}}]},
        "description": {"type": "string"},
        "defaultValue": {},
        "required": {"type": "boolean"},
        "options": {"type": "array", "items": {"type": ["string", "number"]}}
      }
    },
    "Event": {
      "type": "object",
      "required": ["name"],
      "properties": {
        "name": {"type": "string"},
        "description": {"type": "string"},
        "parameters": {}
      }
    },
    "Slot": {
      "type": "object",
      "required": ["name"],
      "properties": {
        "name": {"type": "string"},
        "description": {"type": "string"}
      }
    }
  }
}
```

### 13.2 典型组件 JSON（对齐 data/）

```json
{
  "name": "el-button",
  "docUrl": "https://element-plus.org/zh-CN/component/button.html",
  "props": [
    {"name": "type", "type": "string", "options": ["primary","success","warning","danger","info","default"], "defaultValue": "default"},
    {"name": "size", "type": "string", "options": ["large","default","small"], "defaultValue": "default"},
    {"name": "disabled", "type": "boolean", "defaultValue": "false"}
  ],
  "events": [
    {"name": "click", "description": "点击按钮时触发", "parameters": "event"}
  ],
  "slots": [
    {"name": "default", "description": "按钮内容"},
    {"name": "icon", "description": "按钮图标"}
  ]
}
```

---

## 14. 关键性代码（深化）

### 14.1 统一 AttributeDescriptor（含图标、类型、默认值）

```java
public class UnifiedXmlAttributeDescriptor extends BasicXmlAttributeDescriptor {
  private final String name;
  private final String description;
  private final String[] options;
  private final String defaultValue;
  private final AttributeType type; // PARAM or EVENT

  public UnifiedXmlAttributeDescriptor(String name, String description, String[] options, String defaultValue, AttributeType type) {
    this.name = name;
    this.description = description == null ? "" : description;
    this.options = options == null ? new String[0] : options;
    this.defaultValue = defaultValue == null ? "" : defaultValue;
    this.type = type;
  }

  @Override public String getName() { return name; }
  @Override public boolean isEnumerated() { return options.length > 0; }
  @Override public String[] getEnumeratedValues() { return options; }
  @Override public String getDefaultValue() { return defaultValue; }
  @Nullable @Override public Icon getIcon() {
    return type == AttributeType.EVENT ? AllIcons.Nodes.Event : AllIcons.Nodes.Property;
  }
}
```

### 14.2 ElementDescriptor 合并原生描述

```java
public class UnifiedXmlElementDescriptor implements XmlElementDescriptor {
  private final Component component;
  private final XmlElementDescriptor origin; // may be null
  private final XmlNSDescriptor ns;
  private final String tagName;

  public UnifiedXmlElementDescriptor(Component c, XmlElementDescriptor origin, XmlNSDescriptor ns, String tagName) {
    this.component = c; this.origin = origin; this.ns = ns; this.tagName = tagName;
  }

  @Override public XmlAttributeDescriptor[] getAttributesDescriptors(XmlTag context) {
    List<XmlAttributeDescriptor> merged = new ArrayList<>();
    Collections.addAll(merged, DescriptorFactory.buildDescriptors(component).toArray(new XmlAttributeDescriptor[0]));
    if (origin != null) Collections.addAll(merged, origin.getAttributesDescriptors(context));
    return merged.toArray(new XmlAttributeDescriptor[0]);
  }

  // 其余方法：优先返回 component 信息，fallback 到 origin
}
```

### 14.3 文档渲染器（可配置样式）

```java
public class DocRenderer {
  private static final String STYLE = "<style>body{background:#2b2b2b;color:#a9b7c6;font-size:11px}table{width:640px;border-collapse:collapse;margin:8px 0}th{background:#3c3f41;color:#fff}td{background:#2b2b2b}tr:nth-child(even) td{background:#323232}</style>";

  public String renderComponent(Component c) {
    StringBuilder sb = new StringBuilder(STYLE);
    sb.append("<h1>").append(c.name).append("</h1>");
    if (c.description != null) sb.append("<p>").append(escape(c.description)).append("</p>");
    if (c.docUrl != null) sb.append("<p><a href=\"").append(c.docUrl).append("\">官方文档</a></p>");
    renderPropsTable(sb, c.props); renderEventsTable(sb, c.events); renderSlotsTable(sb, c.slots);
    return sb.toString();
  }

  public String renderProp(Component c, Prop p) { /* 生成单行属性表格 */ }
  public String renderEvent(Component c, Event e) { /* 生成单行事件表格 */ }
}
```

### 14.4 远程与缓存（接口）

```java
interface RemoteLibraryManager {
  Optional<ComponentLibrary> fetch(String id, String version);
}

interface LocalCacheManager {
  Optional<ComponentLibrary> load(String id, String version);
  void save(ComponentLibrary lib);
}

class LibraryCoordinator {
  ComponentLibrary loadEffective(String id, String version, List<ComponentLibrary> customLibs) {
    return LocalCacheManagerFactory.get().load(id, version)
      .or(() -> RemoteLibraryManagerFactory.get().fetch(id, version).map(lib -> { LocalCacheManagerFactory.get().save(lib); return lib; }))
      .map(base -> CustomLibraryMerger.merge(base, customLibs))
      .orElseThrow(() -> new IllegalStateException("library not available: " + id + "@" + version));
  }
}
```

---

## 15. 错误处理与数据校验

- JSON 解析失败：抛出结构化异常并记录；Provider 构建回退到上一次可用库；通知用户。
- 必填字段缺失：跳过该组件，继续其余组件；统计缺陷计数。
- 远程失败：离线模式启用缓存；重试/超时/镜像。

---

## 16. 性能与并发

- Provider 构建在后台线程，使用惰性初始化；
- 读路径（补全/文档）只读内存结构，不做 I/O；
- Reload 使用原子替换 Provider，避免锁竞争；
- 大库分段解析与对象池复用（可选）。

---

## 17. 测试用例（补充）

- JSON 校验：基于 `data/element-plus-libraries.json` 与 `element-ui-libraries.json` 做 Schema 校验与必填检查。
- 跨库差异：`el-button.size` 值在两库切换后不同；`el-input` 绑定属性 `modelValue` vs `value`。
- 文档链接：存在 `docUrl` 时渲染链接；不存在时不渲染或回退库级链接。
- 断网场景：清空缓存后模拟远程失败，期望报错提示且不影响 IDE 稳定性。

---

## 18. 日志与可观测性

- 关键点打点：Provider 构建耗时、补全调用次数、文档渲染耗时、远程下载成功/失败统计。
- 级别：信息（info）记录构建与切换，警告（warn）记录回退，错误（error）记录不可恢复异常。

---

以上补充从架构/目录/数据/代码/运行/质量多个维度深化了详细设计，可直接指导实现与评审，并与既有 `data/` JSON 精确对齐。