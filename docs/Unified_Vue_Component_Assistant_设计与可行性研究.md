### Unified Vue Component Assistant 设计与可行性研究

本文提出将 `ElementPlugin` 的“稳定直连的补全/悬停实现”与 `vuekit` 的“多组件库、远程/本地/自定义 JSON 加载、热更新、按库切换”等能力融合为一个统一项目的方案。文档覆盖架构设计、统一数据规范、运行流转、可行性评估、风险与缓解、迁移与里程碑，供实现前评审使用（不包含代码）。

---

## 1. 目标与范围

- 目标：
  - 统一提供“组件标签、属性、事件、插槽”的智能补全与悬停文档。
  - 支持 Element Plus、Element UI、Ant Design Vue 及自定义库；支持远程官方库下载、本地缓存、按库/版本切换与热更新。
  - 在 IntelliJ 平台（含 Vue 插件）下平滑运行，兼容 HTML/Vue/XML 上下文。
- 范围：
  - 设计层面融合，复用现有两项目的成熟能力与经验；不产出实现代码。

---

## 2. 顶层架构

- 层次划分：
  - 扩展层（IDE 接入）：注册 XML/Vue 扩展点与悬停文档提供者。
  - 文档层（渲染/样式）：基于模型动态构造组件/属性/事件/插槽文档，保留回退策略。
  - 数据层（Provider）：统一组件模型与 Provider 管理器；提供多源装载、热更新通知、上下文过滤。
  - 存储层（远程/本地/自定义）：官方仓库下载、缓存与版本管理；自定义库导入与合并。
  - 配置层（应用/项目）：启用库与版本、默认库优先级、在线/离线模式、调试日志开关。

- 关键组件与职责：
  - Tag/Attribute Providers：按标签名获取 `XmlElementDescriptor`；按标签+属性返回 `XmlAttributeDescriptor`；与原生描述符合并。
  - Documentation Provider：在标签或属性/事件位置，按统一模型渲染文档（HTML 结构与样式可配置）。
  - ComponentProviderManager：维护 Project→Provider 映射；统一触发 reload；统计监控。
  - ComponentProvider：统一装载顺序（缓存→远程→自定义），产出标准模型；支持过滤（库/版本/范围）。
  - RemoteLibrary/Cache：官方库拉取、校验、落盘缓存；版本策略；断网回退。
  - Settings/Config：应用/项目级开关、库优先级、默认库、远程源地址、缓存策略、语言与主题。

---

## 3. 统一数据规范（JSON Schema 草案）

- 顶层 `ComponentLibrary`：
  - `name`: string（库标识，如 element-plus）
  - `version`: string（语义化版本）
  - `components`: Component[]
  - `meta`: { homepage?: string, docsBase?: string, updatedAt: string }
- `Component`：
  - `name`: string（标签名，如 el-input）
  - `displayName`?: string（UI 展示名）
  - `description`?: string
  - `props`?: Prop[]
  - `events`?: Event[]
  - `slots`?: Slot[]
  - `deprecated`?: boolean | string
  - `since`?: string（版本）
- `Prop`：
  - `name`: string
  - `type`: string | string[]（基础或联合类型）
  - `required`: boolean
  - `default`: string | number | boolean | object | null
  - `options`?: (string | number)[]（枚举值）
  - `description`?: string
- `Event`：
  - `name`: string（无需 @ 前缀）
  - `description`?: string
  - `parameters`?: string | { name: string, type: string, description?: string }[]
- `Slot`：
  - `name`: string
  - `description`?: string
  - `scope`?: { name: string, type: string, description?: string }[]

- 设计要点：
  - 可扩展字段通过 `meta.*` 承载；不同库可带库特有元数据，但核心结构一致。
  - 兼容 ElementPlugin 的静态数据：可先用脚本将其常量转换为该 JSON，逐步替换。

---

## 4. 核心运行流转

### 4.1 初始化与库加载
- IDE 启动：
  - 启动活动读取应用/项目配置，决定启用库、版本与优先级。
  - `ComponentProviderManager.getProvider(project)` 初始化 Provider。
  - Provider 装载链：本地缓存 → 远程官方库（可配镜像/超时/重试） → 自定义库；合并去重（后者覆盖前者或策略化合并）。
  - 通知 XML/Doc 层可用。

### 4.2 标签/属性/事件/插槽补全
- 标签：在 `el-`（或库前缀）/任意组件标签处，Provider 查询组件存在性；创建 `XmlElementDescriptor`，合并原生描述。
- 属性与事件：通过 `XmlAttributeDescriptorsProvider` 生成统一描述集合；属性值候选来自 `Prop.options`；事件以 `@event` 呈现建议。
- 插槽：两种途径
  - 文档展示（必做）：在悬停文档展示 `slots` 表格与作用域参数。
  - 补全（可选增强）：在 `template v-slot:` 或 `#` 场景补全 slot 名，需结合 Vue PSI。

### 4.3 悬停文档
- 标签悬停：渲染组件基本信息 + Props/Events/Slots 表格（暗色主题 + 紧凑表格）。
- 属性悬停：展示 Prop 的类型、是否必填、默认值、描述；无数据回退到通用模板。
- 事件悬停：展示事件描述与参数；若无数据，回退模板。
- 链接：可拼接官方 docsBase + 锚点，支持点击跳转。

### 4.4 热更新与库切换
- 配置变化（启用/禁用、版本切换、源切换）或远程库更新完成：
  - Provider 重载数据并替换内存模型；
  - `ComponentProviderManager.notifyProviderReload(project)` 通知 XML/Doc 层；
  - 后续补全/悬停即时使用新数据，无需重启。

---

## 5. 可行性评估

- 技术可行性：
  - 扩展点：两项目已有成熟接入；按“合并原生描述 + 动态模型驱动”可无缝迁移。
  - 数据：VueKit 已有远程/自定义库与 Provider 架构；ElementPlugin 常量可转 JSON。
  - 文档：VueKit 文档渲染方案成熟；可复用样式与表格结构。
  - 插槽：展示立即可行；补全需与 Vue PSI 集成，属于递进项。

- 组织可行性：
  - 渐进迁移策略（先导入现有库 JSON，再脚本化增量更新）。
  - 可拆分里程碑，降低变更风险。

- 风险与缓解：
  - 远程源不稳定：本地缓存 + 离线模式 + 失败回退；手动刷新入口。
  - 数据不一致/版本漂移：引入 `version` 与 `updatedAt`、签名校验、对比提示。
  - 性能：Provider 懒加载 + 工程内缓存；补全调用路径避免 I/O。
  - 兼容性：严格限制扩展点顺序与 `order="first"` 冲突；保留原生描述合并逻辑。

---

## 6. 迁移方案

- 阶段 1：数据统一
  - 将 ElementPlugin 的 `ElementTagConstant`/`DocumentConstant` 转换为 JSON（脚本生成）。
  - 引入统一 Schema 校验；建立转译与测试用例。

- 阶段 2：Provider 切换
  - 在现有 XML/Doc 提供者中仅替换数据来源为统一 Provider。
  - 确认补全与文档与旧行为一致（快照测试 + 手工库对比）。

- 阶段 3：远程/自定义库接入
  - 启用官方库下载、缓存与版本选择；支持本地自定义库导入。
  - 加入“热更新/切换”入口与通知机制。

- 阶段 4（增强）：
  - 插槽补全（结合 Vue PSI）；属性值类型细化（布尔/联合/函数签名）；国际化与主题。

---

## 7. 里程碑与验收

- M1（1-2 周）：统一 JSON Schema 与转换脚本；引入 Schema 校验与样例数据；通过 3 个组件库的试运行数据。
- M2（2-3 周）：Provider 接入与 XML/Doc 读新模型；对齐 ElementPlugin 行为；完成核心回归用例（组件/属性/事件/悬停）。
- M3（1-2 周）：远程下载/缓存/版本选择；项目设置面板（基础项）；热更新与切换回归通过。
- M4（可选 2-3 周）：插槽补全、类型化值提示、文档样式配置、国际化；性能与稳定性优化。

- 验收指标：
  - 组件覆盖率 ≥ 95%；
  - 属性/事件正确率 ≥ 98%；
  - 悬停文档渲染成功率 ≥ 99%；
  - 热更新切换延迟 ≤ 1s，失败回退可用；
  - 离线可用，恢复后自动同步。

---

## 8. 成本与收益

- 成本：数据转译维护、Provider 整合、远程/缓存可靠性、设置 UI；
- 收益：多库统一、低维护（数据驱动）、团队可配置、版本化与可观测、生态扩展空间（更多库/版本）。

---

## 9. 结论

统一方案技术可行、风险可控，并能显著提升维护与扩展效率。建议按“数据先行、Provider 置换、远程与设置接入、功能增强”的里程碑推进，实现从 ElementPlugin 的硬编码到 VueKit 的数据驱动与多库生态的一次性融合。