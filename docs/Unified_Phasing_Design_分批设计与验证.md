### Unified Vue Component Assistant 分批设计与验证

本设计基于《Unified_Vue_Component_Assistant_设计与可行性研究.md》，按功能优先级与风险分解为六个批次，每批包含：目标与范围、功能设计、数据与接口、扩展点接入、验证与验收、回退策略。批次间具备清晰依赖关系，可阶段性交付与验收。

---

## 批次划分与依赖

- 批次一（B1）：组件标签补全（el-*）
- 批次二（B2）：属性补全与值枚举
- 批次三（B3）：事件补全
- 批次四（B4）：卡槽（先文档展示，后补全为增强）
- 批次五（B5）：悬停文档（组件/属性/事件）
- 批次六（B6）：组件库能力（远程/自定义/缓存/设置/热更新/库切换）

依赖：B1 → B2 → B3 → B4 → B5 → B6；其中 B5 依赖 B1~B4 的数据产物；B6 依赖前述能力统一走 Provider。

---

## B1 组件标签补全

- 目标与范围：在 `<` 输入后，基于当前选中组件库（element-plus/element-ui）提供 `componentPrefix` 下的组件候选，插入时携带基础标签结构。
- 功能设计：
  - Provider：从 JSON 装载 `components[].name`；通过 `componentPrefix` 过滤。
  - 扩展点：`XmlTagNameProvider` 返回候选；`XmlElementDescriptorProvider` 返回 `UnifiedXmlElementDescriptor`（与原生描述符合并）。
- 数据与接口：
  - 输入：`ComponentLibrary`（id, componentPrefix, components[]）。
  - 输出：`List<LookupElement>` 与 `XmlElementDescriptor`。
- 验证与验收：
  - 用例：`<el-` 出现 `el-button`、`el-input` 等；切换库后候选随之变化。
  - 指标：覆盖率≥95%，无误报；插入后标签闭合正确。
- 回退策略：JSON 不可用时使用内置最小组件集（5-10 个）。

---

## B2 属性补全与值枚举

- 目标与范围：在组件标签内提供属性名补全与值枚举，值枚举严格来自 JSON 的 `props[].options`。
- 功能设计：
  - 扩展点：`XmlAttributeDescriptorsProvider` 为当前标签返回属性描述集。
  - 描述：`UnifiedXmlAttributeDescriptor`（PARAM）提供 `isEnumerated()` 与 `getEnumeratedValues()`。
- 数据与接口：
  - 输入：`Component.props[]`（name, type, options, defaultValue, required）。
  - 输出：`XmlAttributeDescriptor[]`；值候选。
- 验证与验收：
  - 用例：`el-button` 的 `type` 在 element-plus 与 element-ui 的枚举不同；`el-input` 的绑定属性（modelValue vs value）差异体现。
  - 指标：属性完整度≥95%；值枚举正确率≥98%。
- 回退策略：缺少属性数据则不提示，不报错。

---

## B3 事件补全

- 目标与范围：以 `@event` 形式补全组件支持的事件；不提供值枚举，展示事件参数信息。
- 功能设计：
  - 扩展点：与 B2 共用 `XmlAttributeDescriptorsProvider`，事件以 `UnifiedXmlAttributeDescriptor`（EVENT）加入。
  - 显示：尾注显示事件简述与参数签名（字符串）。
- 数据与接口：
  - 输入：`Component.events[]`（name, description, parameters）。
  - 输出：`XmlAttributeDescriptor[]`（事件）。
- 验证与验收：
  - 用例：`el-button` 存在 `@click`；`el-input` 在 element-ui 存在 `@input/@change`（按 JSON）。
  - 指标：事件完整度≥95%。
- 回退策略：无事件数据不提示。

---

## B4 卡槽（文档先行，补全增强）

- 目标与范围：为组件提供 `slots[]` 信息展示；二期增强在 SFC `v-slot`/`#` 位置提供 slot 名补全（依赖 Vue PSI）。
- 功能设计：
  - 文档：DocRenderer 组件文档中渲染 Slots 表格（name/description）。
  - 补全（增强）：在 `template v-slot:` 与 `#` 上下文触发 slot 名候选。
- 数据与接口：
  - 输入：`Component.slots[]`（name, description）。
  - 输出：文档 HTML；（增强）slot 补全候选。
- 验证与验收：
  - 用例：element-plus `el-button` 出现 `icon` slot，element-ui 仅 `default`；补全（若实现）在 `#` 位置给出 `icon`。
  - 指标：文档覆盖≥90%；补全正确率≥95%（若启用）。
- 回退策略：无 slot 则不展示，不提示。

---

## B5 悬停文档（组件/属性/事件）

- 目标与范围：对标签、属性、事件分别渲染结构化文档，附统一样式；缺失数据回退通用模板。
- 功能设计：
  - 扩展点：`lang.documentationProvider`（HTML/XML/Vue）。
  - 渲染：DocRenderer 生成 Props/Events/Slots 表格；属性/事件渲染单行明细表格；可选 docUrl 链接。
- 数据与接口：
  - 输入：`Component`、`Prop`、`Event`；
  - 输出：`String`（HTML）。
- 验证与验收：
  - 用例：常见组件悬停展示表格；属性/事件悬停展示细节；无数据回退通用模板。
  - 指标：渲染成功率≥99%；首次渲染≤150ms（不含下载）。
- 回退策略：通用模板；不抛异常。

---

## B6 组件库能力（拆分）

为降低风险、便于阶段交付，将组件库能力拆为 5 个子批次：远程下载→缓存管理→自定义库→设置面板→热更新与切换。

### B6.1 远程官方库下载与校验
- 目标与范围：从官方源/镜像下载指定库与版本 JSON，完成结构校验与基本清洗。
- 功能设计：
  - `RemoteLibraryManager.fetch(id, version)`：HTTP 下载、超时/重试、ETag/If-Modified-Since（可选）。
  - JSON Schema 校验；字段校验（必填/类型/前缀一致性）。
- 数据与接口：输入库 id/version；输出 `ComponentLibrary`。
- 验证与验收：
  - 用例：下载 element-plus@最新；源切换为镜像；错误链接返回失败。
  - 指标：成功率记录；异常不阻断 IDE；日志清晰。
- 回退：下载失败不生效，保持当前库。

### B6.2 本地缓存与版本管理
- 目标与范围：下载后存入本地缓存；支持多版本并存；按 id@version 精确读取。
- 功能设计：
  - `LocalCacheManager.load/save`：持久化（文件/目录布局）；校验 `lastUpdated`。
  - 清理策略：LRU 或手动清理（后续增强）。
- 数据与接口：输入 `ComponentLibrary`；输出持久化结果与读取能力。
- 验证与验收：
  - 用例：首次下载后离线可用；多版本共存读取正确。
  - 指标：读取命中率；离线稳定；无损坏读。
- 回退：损坏文件时忽略该版本，尝试其他可用版本。

### B6.3 自定义库导入与合并
- 目标与范围：支持导入本地 JSON，自定义/扩展/覆盖官方组件定义。
- 功能设计：
  - `CustomLibraryMerger.merge(base, customList)`：基于 name 的覆盖/新增；字段级合并（props/events/slots 合并去重）。
  - 校验自定义库 JSON，隔离坏数据。
- 数据与接口：输入 base（官方+缓存）、custom 列表；输出 merged 库。
- 验证与验收：
  - 用例：为 `el-button` 新增属性/覆盖描述；新增自研组件。
  - 指标：合并后补全/文档与预期一致；坏数据不影响其他组件。
- 回退：合并失败则忽略自定义库，仅使用 base。

### B6.4 设置面板与配置持久化
- 目标与范围：提供 UI 配置：选择库/版本、远程源/镜像、离线模式、热更新开关、自定义库路径。
- 功能设计：
  - `PluginSettings`（应用级）与 `ProjectSettingsManager`（项目级）持久化；
  - 校验输入（有效版本、可达链接、本地路径存在）。
- 数据与接口：输入用户配置；输出保存结果与事件。
- 验证与验收：
  - 用例：切换库/版本并保存；关闭重开保持配置；非法输入报校验错误。
  - 指标：保存和读取稳定；UI 无阻塞。
- 回退：设置读取失败使用默认配置（element-plus@内置版本/离线）。

### B6.5 热更新与库/版本切换
- 目标与范围：在设置变更或远程同步后，原子替换 Provider，并即时影响补全与文档。
- 功能设计：
  - `ProviderManager.notifyReload(project)`：重建 Provider；广播刷新事件。
  - 线程模型：后台重建，完成后原子替换，前台读路径零阻塞。
- 数据与接口：输入配置变更/下载完成事件；输出新版 Provider。
- 验证与验收：
  - 用例：切换 element-plus→element-ui，补全与文档立即变化；远程更新后刷新生效。
  - 指标：生效延迟≤1s；不中断用户操作；失败回退上一版 Provider。
- 回退：替换失败保留旧 Provider 并记录错误。

---

## 关键接口与骨架（跨批次共用）

```java
// Provider 管理
class ProviderManager {
  static UnifiedComponentProvider getProvider(Project p) { /* 构建或返回缓存 */ }
  static void notifyReload(Project p) { /* 原子替换 Provider 并广播 */ }
}

// 装载与合并
class LibraryCoordinator {
  ComponentLibrary loadEffective(String id, String version, List<ComponentLibrary> custom) { /* 缓存→远程→合并 */ }
}

// 描述构建
class DescriptorFactory {
  static List<UnifiedXmlAttributeDescriptor> buildDescriptors(Component c) { /* props+events */ }
}

// 文档渲染
class DocRenderer {
  String renderComponent(Component c) { /* Props/Events/Slots 表格 */ }
  String renderProp(Component c, Prop p) { /* 单行表 */ }
  String renderEvent(Component c, Event e) { /* 单行表 */ }
}
```

---

## 分批交付与里程碑（更新）

- M1：B1 完成（组件）
- M2：B2+B3 完成（属性+事件）
- M3：B4+B5 完成（卡槽展示+悬停文档）
- M4：B6.1（远程） + B6.2（缓存）
- M5：B6.3（自定义库） + B6.4（设置面板）
- M6：B6.5（热更新与库/版本切换）

每个里程碑均需通过本批“验证与验收”条目中定义的用例与指标。

---

## 风险与回退

- JSON 数据质量：Schema 校验、坏数据隔离到组件级；
- 远程稳定性：断网/重试/镜像策略，离线可用；
- 兼容性：合并原生描述，避免破坏现有 HTML/Vue PSI 行为；
- 性能：读路径无 I/O；Provider 构建懒加载与缓存；
- 回退：失败保留上一次可用配置与库。

---

本分批设计文档细化了合并步骤、功能设计与验证方法，可直接用于阶段性实施与验收。