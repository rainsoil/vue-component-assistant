### Unified Vue Component Assistant 每批次详细设计

本文件对应《Unified_Phasing_Design_分批设计与验证.md》，对每个批次提供：数据结构要点、关键步骤代码骨架（示例）与设计说明，确保实现可落地、可测试、可演进。

---

## B1 组件标签补全（el-*）

### 数据结构要点
- 输入：`ComponentLibrary`（id, componentPrefix, components[]）
- 组件条目：`Component.name`（如 `el-button`）、`description`、`docUrl`（可选）

```json
{
  "id": "element-plus",
  "componentPrefix": "el-",
  "components": [{"name":"el-button"}, {"name":"el-input"}]
}
```

### 关键步骤代码（示例）
```java
public class UnifiedTagNameProvider implements XmlElementDescriptorProvider {
  @Override public XmlElementDescriptor getDescriptor(XmlTag tag) {
    String name = tag.getName();
    UnifiedComponentProvider provider = ProviderManager.getProvider(tag.getProject());
    if (provider == null || !provider.supportsTag(name)) return null;
    Component c = provider.getComponent(name);
    XmlNSDescriptor ns = null; XmlElementDescriptor origin = null;
    try { ns = tag.getNSDescriptor(tag.getNamespace(), false); if (ns != null) origin = ns.getElementDescriptor(tag); } catch (ProcessCanceledException ignored) {}
    return new UnifiedXmlElementDescriptor(c, origin, ns, name);
  }
}
```

### 设计说明
- 由 Provider 基于 `componentPrefix` 与 `components[].name` 判定是否支持标签。
- 元素描述与原生描述符合并，避免破坏内置 HTML/Vue 行为。

---

## B2 属性补全与值枚举

### 数据结构要点
- 入口：`Component.props[]`
- 字段：`name`、`type`（string|string[]）、`options`（枚举，可空）、`defaultValue`、`required`

```json
{"props":[{"name":"type","type":"string","options":["primary","success"],"defaultValue":"primary"}]}
```

### 关键步骤代码（示例）
```java
public class UnifiedXmlAttributeDescriptorsProvider implements XmlAttributeDescriptorsProvider {
  @Override public XmlAttributeDescriptor[] getAttributeDescriptors(XmlTag tag) {
    UnifiedComponentProvider provider = ProviderManager.getProvider(tag.getProject());
    if (provider == null || !provider.supportsTag(tag.getName())) return XmlAttributeDescriptor.EMPTY;
    Component c = provider.getComponent(tag.getName());
    List<UnifiedXmlAttributeDescriptor> list = new ArrayList<>();
    if (c.props != null) for (Prop p : c.props) list.add(toDescriptor(p));
    return list.toArray(new UnifiedXmlAttributeDescriptor[0]);
  }
  private UnifiedXmlAttributeDescriptor toDescriptor(Prop p){
    return new UnifiedXmlAttributeDescriptor(p.name, p.description, toArray(p.options), safe(p.defaultValue), AttributeType.PARAM);
  }
}
```

### 设计说明
- 值枚举严格来自 `options`；`isEnumerated()` 返回 true/false 以配合 IDE 显示策略。
- 对布尔型可提供示例值；复杂类型延后到类型化增强。

---

## B3 事件补全

### 数据结构要点
- 入口：`Component.events[]`
- 字段：`name`、`description`、`parameters`（string 或参数数组）

```json
{"events":[{"name":"click","description":"点击","parameters":"event"}]}
```

### 关键步骤代码（示例）
```java
class DescriptorFactory {
  static List<UnifiedXmlAttributeDescriptor> buildDescriptors(Component c){
    List<UnifiedXmlAttributeDescriptor> out = new ArrayList<>();
    if (c.props != null) for (Prop p : c.props) out.add(new UnifiedXmlAttributeDescriptor(p.name, p.description, toArray(p.options), safe(p.defaultValue), AttributeType.PARAM));
    if (c.events != null) for (Event e : c.events) out.add(new UnifiedXmlAttributeDescriptor(e.name, e.description, new String[0], stringifyParams(e.parameters), AttributeType.EVENT));
    return out;
  }
}
```

### 设计说明
- 事件以 `@event` 呈现（IDE 层表现）；值不枚举；尾注显示参数签名。

---

## B4 卡槽（文档先行，补全增强）

### 数据结构要点
- 入口：`Component.slots[]`
- 字段：`name`、`description`

```json
{"slots":[{"name":"default","description":"内容"},{"name":"icon","description":"图标"}]}
```

### 关键步骤代码（示例）
```java
class DocRenderer {
  String renderSlots(List<Slot> slots){
    if (slots == null || slots.isEmpty()) return "";
    StringBuilder sb = new StringBuilder("<h3>Slots</h3><table><thead><tr><th>name</th><th>desc</th></tr></thead><tbody>");
    for (Slot s : slots) sb.append("<tr><td>").append(escape(s.name)).append("</td><td>").append(escape(s.description)).append("</td></tr>");
    return sb.append("</tbody></table>").toString();
  }
}
```

### 设计说明
- 先在文档展示；补全为增强项（SFC `v-slot`/`#` 上下文），需利用 Vue PSI 精确定位。

---

## B5 悬停文档（组件/属性/事件）

### 数据结构要点
- 组件维度：`Component` + `props/events/slots`
- 属性维度：匹配 `Prop`
- 事件维度：匹配 `Event`

### 关键步骤代码（示例）
```java
public class UnifiedDocumentationProvider extends AbstractDocumentationProvider {
  private final DocRenderer renderer = new DocRenderer();
  @Override public @Nullable String generateDoc(PsiElement el, @Nullable PsiElement origin){
    if (el instanceof HtmlTag) return docForTag((HtmlTag) el);
    if (el instanceof XmlAttribute && el.getParent() instanceof HtmlTag) return docForAttr((XmlAttribute) el, (HtmlTag) el.getParent());
    return null;
  }
  private String docForTag(HtmlTag tag){
    UnifiedComponentProvider p = ProviderManager.getProvider(tag.getProject());
    if (p == null || !p.supportsTag(tag.getName())) return null;
    return renderer.renderComponent(p.getComponent(tag.getName()));
  }
  private String docForAttr(XmlAttribute attr, HtmlTag parent){
    UnifiedComponentProvider p = ProviderManager.getProvider(parent.getProject());
    if (p == null || !p.supportsTag(parent.getName())) return null;
    Component c = p.getComponent(parent.getName());
    Prop prop = findProp(c, attr.getName()); if (prop != null) return renderer.renderProp(c, prop);
    Event evt = findEvent(c, attr.getName()); if (evt != null) return renderer.renderEvent(c, evt);
    return null;
  }
}
```

### 设计说明
- 统一样式（暗色紧凑表格），`docUrl` 存在则提供跳转链接。
- 无数据回退通用模板，不抛异常。

---

## B6 组件库能力（拆分）

### B6.1 远程官方库下载与校验

#### 数据结构要点
- 输入：库 id、版本；
- 输出：`ComponentLibrary`（完整对象）

#### 关键步骤代码（示例）
```java
interface RemoteLibraryManager { Optional<ComponentLibrary> fetch(String id, String version); }
class HttpRemoteLibraryManager implements RemoteLibraryManager {
  public Optional<ComponentLibrary> fetch(String id, String version){
    // GET {base}/{id}/{version}.json with timeout, retry
    // validate JSON with schema
    // return Optional.of(parsedLibrary) or Optional.empty()
  }
}
```

#### 设计说明
- 支持超时、重试、镜像；下载失败不阻断使用。

---

### B6.2 本地缓存与版本管理

#### 数据结构要点
- 路径：`${cacheRoot}/${id}/${version}/library.json`；
- 元信息：`lastUpdated`。

#### 关键步骤代码（示例）
```java
interface LocalCacheManager { Optional<ComponentLibrary> load(String id, String version); void save(ComponentLibrary lib); }
class FileLocalCacheManager implements LocalCacheManager {
  public Optional<ComponentLibrary> load(String id, String version){ /* read file & parse */ }
  public void save(ComponentLibrary lib){ /* mkdirs & write */ }
}
```

#### 设计说明
- 支持多版本并存；损坏文件隔离，不影响其他版本。

---

### B6.3 自定义库导入与合并

#### 数据结构要点
- 输入：自定义库列表（JSON 路径或对象）；
- 合并规则：按 `Component.name` 覆盖/新增；`props/events/slots` 去重合并。

#### 关键步骤代码（示例）
```java
class CustomLibraryMerger {
  static ComponentLibrary merge(ComponentLibrary base, List<ComponentLibrary> customs){
    Map<String, Component> map = toMap(base.components);
    for (ComponentLibrary cl : customs) for (Component c : cl.components) map.merge(c.name, c, CustomLibraryMerger::mergeComponent);
    base.components = new ArrayList<>(map.values());
    return base;
  }
  private static Component mergeComponent(Component a, Component b){
    // description/docUrl 以 b 优先；props/events/slots 合并去重（按 name）
    a.description = prefer(b.description, a.description);
    a.docUrl = prefer(b.docUrl, a.docUrl);
    a.props = mergeByName(a.props, b.props);
    a.events = mergeByName(a.events, b.events);
    a.slots = mergeByName(a.slots, b.slots);
    return a;
  }
}
```

#### 设计说明
- 自定义库错误不传播，隔离到组件级；合并日志可审计。

---

### B6.4 设置面板与配置持久化

#### 数据结构要点
- 配置：`libraryId`、`version`、`remoteBase`、`offline`、`enableHotReload`、`customLibPaths[]`。

#### 关键步骤代码（示例）
```java
class PluginSettings { String libraryId; String version; String remoteBase; boolean offline; boolean enableHotReload; List<String> customPaths; }
class ProjectSettingsManager { void save(PluginSettings s); PluginSettings load(); }
```

#### 设计说明
- UI 校验：版本有效性、远程地址可达性、本地路径存在性；失败不给保存。

---

### B6.5 热更新与库/版本切换

#### 数据结构要点
- 事件：`SettingsChanged`、`RemoteSynced`；
- 行为：原子替换 Provider。

#### 关键步骤代码（示例）
```java
class ProviderManager {
  private static final Map<Project, UnifiedComponentProvider> providers = new ConcurrentHashMap<>();
  static UnifiedComponentProvider getProvider(Project p){ return providers.computeIfAbsent(p, ProviderManager::build); }
  static void notifyReload(Project p){ providers.computeIfPresent(p, (proj, old)->build(proj)); }
  private static UnifiedComponentProvider build(Project p){ PluginSettings s = ProjectSettingsManager.of(p).load(); ComponentLibrary lib = new LibraryCoordinator().loadEffective(s.libraryId, s.version, CustomLoader.loadAll(s.customPaths)); return new UnifiedComponentProvider(lib); }
}
```

#### 设计说明
- 后台重建，新旧 Provider 原子切换；读路径零阻塞；失败回退旧 Provider。

---

## 共性非功能设计

- 性能：读路径无 I/O；Provider 缓存；远程拉取异步；
- 可靠性：JSON Schema 校验；坏数据隔离；
- 可观测性：构建耗时、补全命中、文档渲染、远程成功率打点；
- 安全：远程请求超时/重试/退避；缓存校验签名（可选）。

---

以上为每批次的详细数据结构、关键实现骨架与设计要点，可与分批设计文档联动用于阶段实施与验收。

---

## 深化补充：每批次序列流程、边界情形、性能预算与测试矩阵

以下内容在原有每批设计基础上进一步细化，便于直接实现与评审。

### B1 组件标签补全（深化）

- 前置条件
  - Provider 已构建且载入当前库（element-plus 或 element-ui）。
  - `componentPrefix` 与 `components[].name` 一致。
- 序列流程（文本序列图）
  1) 编辑器输入 `<el-` → IDE 调用 `XmlTagNameProvider.addTagNameVariants`
  2) ProviderManager.getProvider(project) 返回 Provider
  3) Provider.filterByPrefix("el-") → 返回候选组件名
  4) 构造 `LookupElement` 列表（带 `XmlTagInsertHandler`）
- 关键插入处理（示例）
```java
LookupElementBuilder.create(name).withInsertHandler((ctx, item) -> {
  // 自动闭合：<el-button>| → <el-button></el-button>
  XmlTagInsertHandler.INSTANCE.handleInsert(ctx, item);
});
```
- 边界情形
  - 非 `componentPrefix`：不返回候选。
  - 自定义库新增组件：候选应即时包含（依赖 B6.3/6.5）。
- 性能预算
  - addTagNameVariants ≤ 5ms（缓存命中场景）。
- 测试矩阵
  - 前缀匹配/不匹配；库切换前后候选差异；自定义库新增组件。

### B2 属性补全与值枚举（深化）

- 前置条件
  - B1 可用；组件命中。
- 序列流程
  1) 光标处于 `<el-button |>` 属性区域
  2) IDE 调用 `XmlAttributeDescriptorsProvider.getAttributeDescriptors`
  3) Provider 返回 `Component` → `DescriptorFactory` 生成描述集合
  4) IDE 展示属性名候选；若 `isEnumerated()` 为真，则在 `=` 后展示值枚举
- 关键代码：值插入处理（示例）
```java
public class UnifiedXmlAttributeValueProvider extends XmlAttributeValueProvider {
  @Override public void addAttributeValueCompletions(XmlAttribute attribute, CompletionResultSet result){
    UnifiedComponentProvider p = ProviderManager.getProvider(attribute.getProject());
    if (p == null) return; Component c = p.getComponent(attribute.getParent().getName());
    Prop prop = findProp(c, attribute.getName());
    if (prop != null && prop.options != null) prop.options.forEach(opt -> result.addElement(LookupElementBuilder.create(opt)));
  }
}
```
- 边界情形
  - Boolean/Number 类型无 `options`：不枚举，允许自由输入。
  - 默认值展示：在补全的 `tailText` 中显示 `defaultValue`。
- 性能预算
  - 描述构建 ≤ 10ms（单组件）。
- 测试矩阵
  - `type`/`size` 值差异（Plus vs UI）；缺少 `options` 时不枚举；默认值显示。

### B3 事件补全（深化）

- 前置条件
  - B2 可用。
- 序列流程
  1) 输入 `@` → IDE 调用同一 Provider 获取事件描述符
  2) 以 `UnifiedXmlAttributeDescriptor(type=EVENT)` 形式加入候选
- 关键代码：事件参数尾注
```java
LookupElementBuilder.create("@" + e.name)
  .withTypeText("event", true)
  .withTailText(" (" + stringifyParams(e.parameters) + ")", true);
```
- 边界情形
  - 与原生 DOM 事件重名：按库优先，必要时在 `typeText` 中区分（Element Vs DOM）。
- 性能预算
  - 同 B2。
- 测试矩阵
  - `el-input` 在 UI 库存在 `@input/@change`；Plus 的差异；与 DOM 事件并存。

### B4 卡槽（深化）

- 文档展示流程
  1) 文档渲染 `renderSlots(slots)` → 表格展示 slot 名/描述
- 补全增强流程（可选）
  1) Vue PSI 识别 `template` 标签或 `#` 语法上下文
  2) 调用 Provider 获取 `slots[]`，作为候选
- 关键代码：上下文识别思路（伪代码）
```java
boolean inSlotPosition(PsiElement el){
  // 检测 Token 在 template 标签属性名处或 shorthand #xxx 处
  return VuePsiUtil.isSlotContext(el);
}
```
- 边界情形
  - 无 slots：不展示、不补全。
- 性能预算
  - 文档渲染 ≤ 20ms。
- 测试矩阵
  - Plus 的 `el-button` 存在 `icon`；UI 仅 `default`；`#` 位置的候选正确。

### B5 悬停文档（深化）

- 序列流程
  1) 鼠标悬停 → `DocumentationProvider.generateDoc`
  2) 标签：`renderComponent`；属性：`renderProp`；事件：`renderEvent`
  3) 若 `docUrl` 存在，附超链接
- 关键代码：通用回退模板
```java
private String fallbackDoc(PsiElement el){
  return "<h1>元素信息</h1><p>类型: " + el.getClass().getSimpleName() + "</p><p>文本: " + escape(el.getText()) + "</p>";
}
```
- 边界情形
  - 数据缺失：使用回退模板；不抛异常。
- 性能预算
  - 首次渲染 ≤ 150ms；缓存样式字符串。
- 测试矩阵
  - 三表（Props/Events/Slots）均可渲染；缺数据回退；链接存在时点击打开。

### B6.1 远程官方库下载与校验（深化）

- HTTP 约定
  - GET `${remoteBase}/${id}/${version}.json`
  - Headers：`User-Agent`, `If-None-Match`, `If-Modified-Since`（可选）
  - 超时：连接 2s / 读取 3s；重试 2 次指数退避
- 关键代码：下载与校验（示例）
```java
ComponentLibrary fetchOrEmpty(String id, String version){
  HttpResponse r = http.get(remoteBase + "/" + id + "/" + version + ".json");
  if (!r.is2xx()) throw new IOException("bad status: " + r.code());
  ComponentLibrary lib = json.parse(r.body(), ComponentLibrary.class);
  SchemaValidator.validate(lib); // 必填/类型/前缀一致性
  return lib;
}
```
- 错误处理
  - 网络失败：记录 warn；返回 empty，不替换现有库。
- 测试矩阵
  - 成功、404、超时、镜像切换、Schema 失败。

### B6.2 本地缓存与版本管理（深化）

- 路径布局
  - `${cacheRoot}/${id}/${version}/library.json`
  - `${cacheRoot}/${id}/${version}/meta.json`（可选，记录 `lastUpdated`、`etag`）
- 关键代码：读写
```java
Path path = cacheRoot.resolve(id).resolve(version).resolve("library.json");
Files.createDirectories(path.getParent());
Files.writeString(path, json.toString(lib));
```
- 测试矩阵
  - 首次下载后断网读取成功；损坏文件跳过当前版本。

### B6.3 自定义库导入与合并（深化）

- 合并规则矩阵
  - Component.description/docUrl：自定义优先
  - props/events/slots：按 name 去重合并，自定义覆盖同名项
- 关键代码：数组合并（通用）
```java
static <T extends Named> List<T> mergeByName(List<T> a, List<T> b){
  Map<String,T> m = new LinkedHashMap<>();
  if (a != null) a.forEach(x -> m.put(x.getName(), x));
  if (b != null) b.forEach(x -> m.put(x.getName(), x));
  return new ArrayList<>(m.values());
}
```
- 测试矩阵
  - 覆盖描述；新增属性；坏数据（缺 name）被隔离。

### B6.4 设置面板与配置持久化（深化）

- 配置键
  - `libraryId`（默认 `element-plus`）
  - `version`（默认内置版本）
  - `remoteBase`（默认官方）
  - `offline`（默认 false）
  - `enableHotReload`（默认 true）
  - `customLibPaths[]`（默认空）
- 关键代码：校验
```java
void validate(PluginSettings s){
  requireNonEmpty(s.libraryId, "libraryId");
  requireMatches(s.version, ".+", "version");
  if (!s.offline) requireUrl(s.remoteBase);
  for (String p : s.customPaths) requireFileExists(p);
}
```
- 测试矩阵
  - 非法 URL；不存在的自定义路径；关闭重开配置保留。

### B6.5 热更新与库/版本切换（深化）

- 并发模型
  - 后台线程构建新 Provider；CAS 替换；读路径仅访问 volatile 引用。
- 关键代码：原子替换（示例）
```java
class AtomicProviderHolder {
  private static final AtomicReference<UnifiedComponentProvider> REF = new AtomicReference<>();
  static UnifiedComponentProvider get(){ return REF.get(); }
  static void set(UnifiedComponentProvider p){ REF.set(p); }
}
```
- 测试矩阵
  - 切换库/版本延迟；切换期间补全/文档不报错；失败回退旧 Provider。

---

## 日志字段与可观测性（统一）

- Provider 构建：`libraryId`、`version`、`componentsCount`、`buildMs`
- 下载：`url`、`status`、`retry`、`bytes`、`elapsedMs`
- 合并：`baseCount`、`customCount`、`mergedCount`
- 补全：`tagHits`、`attrHits`、`eventHits`、`slotHits`、`latencyMs(p50/p95)`
- 文档：`renderMs(p50/p95)`、`fallbackCount`

---

## 回滚与恢复

- 远程/合并失败：保留旧库与旧 Provider；
- 配置损坏：重置为默认配置并提示；
- 缓存损坏：跳过当前版本，尝试其他版本或内置库。

---

以上细化为每批次提供了更明确的序列流程、关键实现点、边界场景、性能预算与测试矩阵，可直接用于实现计划与质量保障。