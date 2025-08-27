### ElementPlugin 架构与源码详解

本文对 `bak/ElementPlugin` 模块进行深入分析，聚焦其如何实现：组件提示（标签补全）、属性提示、事件提示、卡槽信息以及悬停文档（Hover Docs）。内容包括架构设计、关键扩展点、源码走查、数据来源与局限性，并给出二次扩展建议。

---

## 一、总体架构

ElementPlugin 是一个基于 IntelliJ 平台的插件，主要通过注册若干 IntelliJ 扩展点（extension points）来为 HTML/Vue 模板内的 Element UI 组件提供提示与文档。

- 核心思路：
  - 使用数据驱动的常量表描述组件→属性/事件/默认值等（`ElementTagConstant`）。
  - 在 XML/HTML 环境中接入：
    - 标签名补全与元素描述（XmlTagNameProvider / XmlElementDescriptorProvider）
    - 属性描述与属性值提示（XmlAttributeDescriptorsProvider + 自定义 XmlAttributeDescriptor）
    - 悬停文档（DocumentationProvider）
  - 辅助：代码模板（Live Templates）。

- 关键注册（`bak/ElementPlugin/src/main/resources/META-INF/plugin.xml`）：
```1:52:bak/ElementPlugin/src/main/resources/META-INF/plugin.xml
<idea-plugin>
    ...
    <extensions defaultExtensionNs="com.intellij">
        <fileTypeFactory implementation="com.element.ElementFileTypeFactory" />
        <!-- 代码块提示 -->
        <defaultLiveTemplatesProvider implementation="com.element.ElementTemplatesProvider"/>
        <!-- 标签文档提示（Hover Docs） -->
        <lang.documentationProvider language="HTML" implementationClass="com.element.document.DocumentProvider" order="first"/>
        <!-- 标签自动完成、属性提示 -->
        <xml.tagNameProvider implementation="com.element.xml.ElementTagNameProvider"/>
        <xml.elementDescriptorProvider implementation="com.element.xml.ElementTagNameProvider" order="first"/>
        <xml.attributeDescriptorsProvider implementation="com.element.xml.ElementAttributesProvider" />
    </extensions>
</idea-plugin>
```

---

## 二、数据模型与来源

- 组件/属性/事件枚举：
  - `ElementTagConstant.TAG_CONSTANT` 将 “标签名 → 属性名 → 备选值数组” 建模，驱动标签与属性相关的补全。
```1:40:bak/ElementPlugin/src/main/java/com/element/xml/ElementTagConstant.java
public class ElementTagConstant {
    public static HashMap<String, HashMap<String, String[]>> TAG_CONSTANT = new HashMap<>();
    static {
        HashMap<String, String[]> elButtonMap = new HashMap<>();
        elButtonMap.put("size", new String[]{"medium", "small", "mini"});
        elButtonMap.put("type", new String[]{"primary", "success", "warning", "danger", "info", "text"});
        elButtonMap.put("@change", new String[]{}); // 事件作为“属性名”出现
        TAG_CONSTANT.put("el-button", elButtonMap);
        ...
    }
}
```
  - 特别说明：事件通过以 `@event` 形式加入到“属性名”集合中，从而以“属性提示”的方式出现（事件名补全）。

- 悬停文档 HTML 模板：
  - `DocumentConstant` 中以字段名的方式保存每个组件的富文本 HTML 说明；字段名等于标签名去掉横线后的形式，例如 `el-input` → `elinput`。
```1:12:bak/ElementPlugin/src/main/java/com/element/document/DocumentConstant.java
public class DocumentConstant {
    public static String elinput = "<a href=\"http://element-cn.eleme.io/#/zh-CN/component/input\" ...>...";
    public static String elradio = "<a href=\"http://element-cn.eleme.io/#/zh-CN/component/radio\" ...>...";
    ...
}
```

---

## 三、标签补全与元素描述

- 入口类：`ElementTagNameProvider` 同时实现 `XmlTagNameProvider` 与 `XmlElementDescriptorProvider`。
  - 标签名补全：在输入 `<` 时，将 `TAG_CONSTANT` 的 key（全部支持的 Element 标签）作为候选项加入，并使用 `XmlTagInsertHandler` 处理插入。
```22:36:bak/ElementPlugin/src/main/java/com/element/xml/ElementTagNameProvider.java
public void addTagNameVariants(List<LookupElement> list, @NotNull XmlTag xmlTag, String s) {
    for (Map.Entry<String, HashMap<String, String[]>> next : ElementTagConstant.TAG_CONSTANT.entrySet()) {
        list.add(LookupElementBuilder.create(next.getKey()).withInsertHandler(XmlTagInsertHandler.INSTANCE));
    }
}
```
  - 元素描述挂载：当匹配到“受支持的标签名”时，返回自定义的 `XmlElementDescriptor`（`ElementAnyXmlElementDescriptor`）。
```45:63:bak/ElementPlugin/src/main/java/com/element/xml/ElementTagNameProvider.java
public XmlElementDescriptor getDescriptor(XmlTag xmlTag) {
    final XmlNSDescriptor nsDescriptor = xmlTag.getNSDescriptor(xmlTag.getNamespace(), false);
    final XmlElementDescriptor descriptor = nsDescriptor != null ? nsDescriptor.getElementDescriptor(xmlTag) : null;
    boolean special = ElementTagConstant.TAG_CONSTANT.containsKey(xmlTag.getName());
    if (!special) { return null; }
    return new ElementAnyXmlElementDescriptor(descriptor, nsDescriptor, xmlTag.getName());
}
```
- 元素描述类：`ElementAnyXmlElementDescriptor`
  - 保持 XML 结构行为尽量通用（内容类型为 ANY、公共属性集来自 HTML NS）。
```60:69:bak/ElementPlugin/src/main/java/com/element/xml/ElementAnyXmlElementDescriptor.java
public XmlAttributeDescriptor[] getAttributesDescriptors(@Nullable XmlTag context) {
    return HtmlNSDescriptorImpl.getCommonAttributeDescriptors(context);
}
```
  - 注意：真正的 Element 组件“专有属性”由下面的属性提供器注入。

---

## 四、属性/事件提示与属性值提示

- 入口类：`ElementAttributesProvider` 实现 `XmlAttributeDescriptorsProvider`
  - 当标签名在 `TAG_CONSTANT` 中命中时，构造一组 `ElementAttributeDescriptor`：每个“属性名”（包含事件名、如 `@change`）对应一个描述实例；若常量表内该属性有备选值数组，则用于值补全。
```21:37:bak/ElementPlugin/src/main/java/com/element/xml/ElementAttributesProvider.java
public XmlAttributeDescriptor[] getAttributeDescriptors(XmlTag xmlTag) {
    for (Map.Entry<String, HashMap<String, String[]>> next : ElementTagConstant.TAG_CONSTANT.entrySet()) {
        if (next.getKey().equals(xmlTag.getName())) {
            HashMap<String, String[]> attrMap = next.getValue();
            XmlAttributeDescriptor[] attributeDescriptors = new ElementAttributeDescriptor[attrMap.size()];
            int i = 0;
            for(Map.Entry<String, String[]> attr : attrMap.entrySet()){
                attributeDescriptors[i] = new ElementAttributeDescriptor(project, attr.getKey(), attr.getValue());
                i++;
            }
            return attributeDescriptors;
        }
    }
    return XmlAttributeDescriptor.EMPTY;
}
```
- 属性描述类：`ElementAttributeDescriptor`
  - 继承 `BasicXmlAttributeDescriptor` 并实现 `XmlAttributeDescriptorEx`，核心是 `getEnumeratedValues()` 返回候选值数组，从而让 IDE 提供属性值补全。
```84:90:bak/ElementPlugin/src/main/java/com/element/xml/ElementAttributeDescriptor.java
@Override
public String[] getEnumeratedValues() {
    return attributeValues;
}
```
  - 图标：返回 `ElementIcons.FILE`，用于 UI 呈现。
```105:110:bak/ElementPlugin/src/main/java/com/element/xml/ElementAttributeDescriptor.java
@Nullable
@Override
public Icon getIcon() {
    return ElementIcons.FILE;
}
```
- 事件提示：
  - 事件以 `@event` 作为“属性名”写入 `TAG_CONSTANT`，因此在属性名补全时一并出现。
  - 事件的“值”并没有提供枚举（常量表中多为空数组），但不会影响事件名本身的补全。

- 卡槽提示：
  - 代码层面没有单独的“卡槽名补全”提供器；但卡槽信息以文档说明的方式（见下节）体现在悬停文档中（如 `el-input` 文档包含 `slots: prefix、suffix、prepend、append`）。

---

## 五、悬停文档（Hover Documentation）

- 入口类：`DocumentProvider`（注册为 HTML 的 `DocumentationProvider`）。
  - 处理逻辑：
    1. 取 `originalElement.getText()`（例如 `el-input`）。
    2. 去掉连字符 `-` 得到 `elinput`，再用反射在 `DocumentConstant` 的字段中查找同名字符串常量。
    3. 命中则返回该 HTML 片段作为悬停文档；否则返回 `null`（交由其它 Provider）。
```39:67:bak/ElementPlugin/src/main/java/com/element/document/DocumentProvider.java
public String generateDoc(final PsiElement element, @Nullable final PsiElement originalElement) {
    String text = originalElement.getText();
    if (null != text) {
        String doc = "doc: " + text;
        String textHandle = text.replaceAll("-", "").replaceAll("\n|\r\n", "");
        Class clazz = DocumentConstant.class;
        Field[] fields = clazz.getFields();
        for (Field field : fields) {
            if (textHandle.equals(field.getName()) && field.getType().toString().endsWith("java.lang.String")) {
                try { doc = (String) field.get(DocumentConstant.class); } catch (IllegalAccessException e) { }
                break;
            }
        }
        if ("doc: ".equals(doc)) { return null; } else { return doc; }
    }
    return null;
}
```
- 文档内容：
  - `DocumentConstant` 提供了大量 HTML 表格片段，包含 API 参数、事件、方法、slot 名称等，基本覆盖常见 Element 组件。
  - 文档来源属于“静态快照”，与 Element 文档版本绑定，后续需手动维护。

---

## 六、代码模板（Live Templates）

- `ElementTemplatesProvider` 将 `element.xml`（资源文件）作为 Live Templates 源，从而提供常用片段的快速插入。
```9:21:bak/ElementPlugin/src/main/java/com/element/ElementTemplatesProvider.java
public class ElementTemplatesProvider implements DefaultLiveTemplatesProvider {
    @Override
    public String[] getDefaultLiveTemplateFiles() {
        return new String[]{"element"};
    }
    @Nullable
    @Override
    public String[] getHiddenLiveTemplateFiles() { return null; }
}
```

---

## 七、行为小结（对应需求点）

- 组件提示（标签补全）：
  - 来自 `ElementTagNameProvider.addTagNameVariants`，数据源为 `ElementTagConstant.TAG_CONSTANT` 的 key。
- 属性提示：
  - 来自 `ElementAttributesProvider.getAttributeDescriptors`，按标签提供属性名列表。
- 事件提示：
  - 事件以 `@xxx` 形式出现在 `TAG_CONSTANT` 的属性名集合中，因此会一并补全（视为属性名）。
- 卡槽提示：
  - 无专门“卡槽名补全”；但部分组件的 slot 信息体现在 `DocumentConstant` 的 HTML 文档中（通过 Hover 展示）。
- 悬停文档：
  - `DocumentProvider.generateDoc` 通过“去横线 + 反射”定位 `DocumentConstant` 的对应字段返回 HTML 内容。

---

## 八、局限性与改进建议

- 局限性：
  - 数据静态硬编码：`ElementTagConstant` 与 `DocumentConstant` 都是手写常量，版本升级与生态差异（Element UI vs Element Plus）需要手动同步。
  - 属性值枚举的 `isEnumerated()` 返回 `false`，虽然 `getEnumeratedValues()` 已返回了备选值，但部分 IDE 可能依赖 `isEnumerated()` 标记来优化提示策略，可考虑返回 `true`。
  - 卡槽名未提供自动补全，仅在文档中说明。
  - 缺少上下文感知：未根据 Vue 版本、按需引入、组件注册范围、命名空间等做动态过滤。

- 改进建议：
  1. 数据外部化：将组件元数据迁移到 JSON（与 `vuekit` 模块靠拢），支持按版本/库切换；并在运行时加载与缓存。
  2. 丰富属性值提示：
     - 根据类型与约束（例如 Boolean/Number/Union）生成更符合语义的提示与校验。
     - 事件参数提示与签名展示（通过 `type info`/`tailText` 等方式）。
  3. 卡槽名补全：
     - 为常见组件提供 slot 名集合，注册 `XmlTagNameProvider` 或自定义 Vue 上下文的插槽补全（需结合 Vue 插件 PSI）。
  4. 悬停文档增强：
     - 除标签名外，也支持对属性名（含事件名）提供定向的“子章节”跳转和说明。
     - 允许在线/本地文档链接切换，并支持版本化文档。
  5. 与 JetBrains Vue 插件更深集成：
     - 利用 Vue 插件的 PSI/Scope/Components 识别，避免在非 Element 组件上下文中误报。

---

## 九、源码导览清单

- 扩展点与入口：
  - `plugin.xml`
  - `com.element.ElementFileTypeFactory`、`ElementFileType`（文件类型注册）
  - `com.element.ElementTemplatesProvider`（Live Templates）

- 标签/属性：
  - `com.element.xml.ElementTagNameProvider`
  - `com.element.xml.ElementAnyXmlElementDescriptor`
  - `com.element.xml.ElementAttributesProvider`
  - `com.element.xml.ElementAttributeDescriptor`
  - `com.element.xml.ElementTagConstant`（数据）

- 文档：
  - `com.element.document.DocumentProvider`
  - `com.element.document.DocumentConstant`（数据）

---

## 十、运行与调试要点

- 此插件基于 IntelliJ 平台与 JavaScript/Vue 生态：在 IDEA 2023+ 上运行；`plugin.xml` 已声明依赖：`JavaScript` 与 `com.intellij.modules.platform`。
- 常量数据较大，建议对 `DocumentConstant`/`ElementTagConstant` 的维护使用脚本从官方文档生成，以降低人为失误。

---

以上即为 ElementPlugin 在组件/属性/事件/卡槽信息（文档层面）与悬停文档方面的实现原理与源码细节解析。若需要将其能力迁移/统一到主工程 `vuekit` 的 JSON 数据管线，建议先抽象统一的数据 Schema，再将本插件的常量同步至 JSON 并接入加载层。