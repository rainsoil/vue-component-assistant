### Unified Vue Component Assistant 样例项目结构与类清单

本文件给出一个可直接落地的样例项目目录结构、类清单与关键类的代码骨架（可复制为起始实现）。包名用 `com.xxx.unified` 占位，落地时按实际组织更改。

---

## 1. 目录结构（建议）

```text
src/main/java/com/xxx/unified/
  startup/
    UnifiedPluginStartupActivity.java
  extension/
    UnifiedTagNameProvider.java
    UnifiedXmlElementDescriptor.java
    UnifiedXmlAttributeDescriptorsProvider.java
    UnifiedXmlAttributeDescriptor.java
    UnifiedXmlAttributeValueProvider.java
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
    HttpRemoteLibraryManager.java
    LocalCacheManager.java
    FileLocalCacheManager.java
    CustomLibraryMerger.java
    model/
      ComponentLibrary.java
      Component.java
      Prop.java
      Event.java
      Slot.java
  docs/
    DocRenderer.java
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

## 2. plugin.xml（示例）

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

## 3. 关键类骨架

### 3.1 启动
```java
package com.xxx.unified.startup;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import org.jetbrains.annotations.NotNull;

public class UnifiedPluginStartupActivity implements StartupActivity.DumbAware {
  @Override public void runActivity(@NotNull Project project) {
    // 预热 Provider 或读取设置做延迟构建
  }
}
```

### 3.2 Tag/Descriptor/Attribute Providers
```java
package com.xxx.unified.extension;

import com.intellij.psi.impl.source.xml.XmlElementDescriptorProvider;
import com.intellij.psi.xml.XmlTag;
import com.intellij.xml.XmlElementDescriptor;
import com.xxx.unified.provider.ProviderManager;
import com.xxx.unified.library.model.Component;

public class UnifiedTagNameProvider implements XmlElementDescriptorProvider {
  @Override public XmlElementDescriptor getDescriptor(XmlTag tag) {
    var provider = ProviderManager.getProvider(tag.getProject());
    if (provider == null || !provider.supportsTag(tag.getName())) return null;
    Component c = provider.getComponent(tag.getName());
    var ns = tag.getNSDescriptor(tag.getNamespace(), false);
    var origin = ns != null ? ns.getElementDescriptor(tag) : null;
    return new UnifiedXmlElementDescriptor(c, origin, ns, tag.getName());
  }
}
```

```java
package com.xxx.unified.extension;

import com.intellij.xml.impl.BasicXmlAttributeDescriptor;
import javax.swing.*;

public class UnifiedXmlAttributeDescriptor extends BasicXmlAttributeDescriptor {
  public enum AttributeType { PARAM, EVENT }
  private final String name, description, defaultValue;
  private final String[] options; private final AttributeType type;
  public UnifiedXmlAttributeDescriptor(String name, String desc, String[] options, String def, AttributeType t){
    this.name = name; this.description = desc == null ? "" : desc; this.options = options == null ? new String[0] : options; this.defaultValue = def == null ? "" : def; this.type = t;
  }
  @Override public String getName(){ return name; }
  @Override public boolean isEnumerated(){ return options.length > 0; }
  @Override public String[] getEnumeratedValues(){ return options; }
  @Override public String getDefaultValue(){ return defaultValue; }
  @Override public Icon getIcon(){ return null; }
}
```

```java
package com.xxx.unified.extension;

import com.intellij.xml.XmlAttributeDescriptor;
import com.intellij.xml.XmlAttributeDescriptorsProvider;
import com.intellij.psi.xml.XmlTag;
import com.xxx.unified.provider.ProviderManager;
import com.xxx.unified.provider.DescriptorFactory;

public class UnifiedXmlAttributeDescriptorsProvider implements XmlAttributeDescriptorsProvider {
  @Override public XmlAttributeDescriptor[] getAttributeDescriptors(XmlTag tag) {
    var p = ProviderManager.getProvider(tag.getProject());
    if (p == null || !p.supportsTag(tag.getName())) return XmlAttributeDescriptor.EMPTY;
    var c = p.getComponent(tag.getName());
    return DescriptorFactory.buildDescriptors(c).toArray(new XmlAttributeDescriptor[0]);
  }
}
```

### 3.3 Provider & Factory
```java
package com.xxx.unified.provider;

import com.intellij.openapi.project.Project;
import com.xxx.unified.library.LibraryCoordinator;
import com.xxx.unified.library.model.ComponentLibrary;
import java.util.concurrent.ConcurrentHashMap;

public class ProviderManager {
  private static final ConcurrentHashMap<Project, UnifiedComponentProvider> MAP = new ConcurrentHashMap<>();
  public static UnifiedComponentProvider getProvider(Project p){ return MAP.computeIfAbsent(p, ProviderManager::build); }
  public static void notifyReload(Project p){ MAP.computeIfPresent(p, (proj, old) -> build(proj)); }
  private static UnifiedComponentProvider build(Project p){ ComponentLibrary lib = new LibraryCoordinator().loadEffective(/* from settings */); return new UnifiedComponentProvider(lib); }
}
```

```java
package com.xxx.unified.provider;

import com.xxx.unified.library.model.*;
import java.util.*;
import java.util.stream.Collectors;

public class UnifiedComponentProvider {
  private final String componentPrefix; private final Map<String, Component> byName;
  public UnifiedComponentProvider(ComponentLibrary lib){
    this.componentPrefix = lib.componentPrefix;
    this.byName = lib.components.stream().collect(Collectors.toMap(c -> c.name, c -> c, (a,b)->a, LinkedHashMap::new));
  }
  public boolean supportsTag(String tag){ return tag != null && tag.startsWith(componentPrefix) && byName.containsKey(tag); }
  public Component getComponent(String tag){ return byName.get(tag); }
}
```

```java
package com.xxx.unified.provider;

import com.xxx.unified.extension.UnifiedXmlAttributeDescriptor;
import com.xxx.unified.library.model.*;
import java.util.*;

public class DescriptorFactory {
  public static List<UnifiedXmlAttributeDescriptor> buildDescriptors(Component c){
    List<UnifiedXmlAttributeDescriptor> out = new ArrayList<>();
    if (c.props != null) for (Prop p : c.props)
      out.add(new UnifiedXmlAttributeDescriptor(p.name, p.description, toArray(p.options), safe(p.defaultValue), UnifiedXmlAttributeDescriptor.AttributeType.PARAM));
    if (c.events != null) for (Event e : c.events)
      out.add(new UnifiedXmlAttributeDescriptor(e.name, e.description, new String[0], stringify(e.parameters), UnifiedXmlAttributeDescriptor.AttributeType.EVENT));
    return out;
  }
  private static String[] toArray(List<String> list){ return list == null ? new String[0] : list.toArray(new String[0]); }
  private static String safe(Object v){ return v == null ? "" : String.valueOf(v); }
  private static String stringify(Object p){ return p == null ? "" : String.valueOf(p); }
}
```

### 3.4 Library & Models
```java
package com.xxx.unified.library;

import com.xxx.unified.library.model.ComponentLibrary;
import java.util.List;

public class LibraryCoordinator {
  public ComponentLibrary loadEffective(){
    // 读取设置：libraryId/version/remoteBase/offline/customPaths
    // 顺序：LocalCache → RemoteDownload(+cache.save) → CustomMerge
    return null;
  }
}
```

```java
package com.xxx.unified.library.model;
import java.util.List;

public class ComponentLibrary { public String id, name, componentPrefix, version, sourceUrl, lastUpdated; public List<Component> components; }
public class Component { public String name, description, version, example, docUrl; public List<Prop> props; public List<Event> events; public List<Slot> slots; }
public class Prop { public String name; public Object type; public String description; public Object defaultValue; public boolean required; public List<String> options; }
public class Event { public String name; public String description; public Object parameters; }
public class Slot { public String name; public String description; }
```

### 3.5 文档渲染
```java
package com.xxx.unified.docs;

import com.xxx.unified.library.model.*;

public class DocRenderer {
  private static final String STYLE = "<style>body{background:#2b2b2b;color:#a9b7c6;font-size:11px}table{width:640px;border-collapse:collapse;margin:8px 0}th{background:#3c3f41;color:#fff}td{background:#2b2b2b}tr:nth-child(even) td{background:#323232}</style>";
  public String renderComponent(Component c){ StringBuilder sb = new StringBuilder(STYLE).append("<h1>").append(c.name).append("</h1>"); /* props/events/slots 表格拼装 */ return sb.toString(); }
  public String renderProp(Component c, Prop p){ return STYLE + "<h1>"+p.name+"</h1>"; }
  public String renderEvent(Component c, Event e){ return STYLE + "<h1>@"+e.name+"</h1>"; }
}
```

### 3.6 设置
```java
package com.xxx.unified.settings;

import java.util.*;

public class PluginSettings {
  public String libraryId = "element-plus";
  public String version = "latest";
  public String remoteBase = "https://example.com/libs";
  public boolean offline = false;
  public boolean enableHotReload = true;
  public List<String> customPaths = new ArrayList<>();
}
```

---

## 4. 说明
- 以上骨架与现有详细设计一一对应，可直接作为起步代码结构。
- 可先以 element-plus JSON 落地 B1~B5，再逐步接入 B6 系列能力（远程/缓存/自定义/设置/热更新）。
