# 组件库管理指南

## 概述

VueKit 支持多种组件库的管理，包括内置组件库和自定义组件库。本指南将详细介绍如何使用和管理这些组件库。

## 内置组件库

### 支持的组件库

| 组件库 | 版本 | 状态 | 说明 |
|--------|------|------|------|
| Element Plus | 最新版本 | ✅ 支持 | 现代化的Vue 3组件库 |
| Element UI | 经典版本 | ✅ 支持 | Vue 2经典组件库 |
| Ant Design Vue | 最新版本 | ✅ 支持 | 企业级UI组件库 |

### 自动检测

插件会自动检测项目中使用的组件库：

1. **读取 package.json**
   - 检查项目根目录的 `package.json` 文件
   - 分析 `dependencies` 和 `devDependencies` 中的组件库

2. **智能识别**
   - 识别 `element-plus` → Element Plus
   - 识别 `element-ui` → Element UI
   - 识别 `ant-design-vue` → Ant Design Vue

3. **自动加载**
   - 根据检测结果自动加载对应的组件数据
   - 提供相应的补全和文档功能

### 手动配置

如果自动检测失败，可以手动配置：

1. 打开 **File** → **Settings** → **Tools** → **Element Plus Assistant**
2. 禁用 **自动检测组件库**
3. 选择默认组件库
4. 点击 **Apply** 保存设置

## 自定义组件库

### JSON格式说明

#### 单个组件库格式

```json
{
  "name": "my-custom-library",
  "displayName": "我的自定义组件库",
  "version": "1.0.0",
  "description": "这是一个示例自定义组件库",
  "componentPrefix": "my-",
  "documentationUrlTemplate": "https://example.com/docs/%s",
  "components": [
    {
      "name": "my-button",
      "description": "自定义按钮组件",
      "version": "1.0.0",
      "example": "<my-button type=\"primary\">按钮</my-button>",
      "docUrl": "https://example.com/my-button",
      "props": [
        {
          "name": "type",
          "type": "string",
          "description": "按钮类型",
          "defaultValue": "default",
          "required": false,
          "options": ["primary", "success", "warning", "danger", "info", "default"]
        },
        {
          "name": "size",
          "type": "string",
          "description": "按钮尺寸",
          "defaultValue": "medium",
          "required": false,
          "options": ["large", "medium", "small"]
        },
        {
          "name": "disabled",
          "type": "boolean",
          "description": "是否禁用",
          "defaultValue": false,
          "required": false
        }
      ],
      "events": [
        {
          "name": "click",
          "description": "点击事件",
          "parameters": "event"
        },
        {
          "name": "dblclick",
          "description": "双击事件",
          "parameters": "event"
        }
      ],
      "slots": [
        {
          "name": "default",
          "description": "按钮内容"
        },
        {
          "name": "icon",
          "description": "按钮图标"
        }
      ]
    },
    {
      "name": "my-input",
      "description": "自定义输入框组件",
      "version": "1.0.0",
      "example": "<my-input v-model=\"value\" placeholder=\"请输入内容\" />",
      "docUrl": "https://example.com/my-input",
      "props": [
        {
          "name": "value",
          "type": "string",
          "description": "输入值",
          "defaultValue": "",
          "required": false
        },
        {
          "name": "placeholder",
          "type": "string",
          "description": "占位符",
          "defaultValue": "",
          "required": false
        },
        {
          "name": "disabled",
          "type": "boolean",
          "description": "是否禁用",
          "defaultValue": false,
          "required": false
        }
      ],
      "events": [
        {
          "name": "input",
          "description": "输入事件",
          "parameters": "value"
        },
        {
          "name": "change",
          "description": "值改变事件",
          "parameters": "value"
        },
        {
          "name": "focus",
          "description": "获得焦点事件",
          "parameters": "event"
        },
        {
          "name": "blur",
          "description": "失去焦点事件",
          "parameters": "event"
        }
      ],
      "slots": [
        {
          "name": "prefix",
          "description": "输入框前缀"
        },
        {
          "name": "suffix",
          "description": "输入框后缀"
        }
      ]
    }
  ]
}
```

#### 多个组件库格式

```json
[
  {
    "name": "library1",
    "displayName": "组件库1",
    "version": "1.0.0",
    "description": "第一个自定义组件库",
    "componentPrefix": "lib1-",
    "components": [...]
  },
  {
    "name": "library2",
    "displayName": "组件库2",
    "version": "1.0.0",
    "description": "第二个自定义组件库",
    "componentPrefix": "lib2-",
    "components": [...]
  }
]
```

### 字段说明

#### 组件库级别字段

| 字段 | 类型 | 必需 | 说明 |
|------|------|------|------|
| `name` | string | ✅ | 组件库唯一标识名称 |
| `displayName` | string | ❌ | 显示名称，默认为 `name` |
| `version` | string | ❌ | 版本号，默认为 "1.0.0" |
| `description` | string | ❌ | 描述信息 |
| `componentPrefix` | string | ❌ | 组件前缀，如 "my-" |
| `documentationUrlTemplate` | string | ❌ | 文档URL模板，支持 %s 占位符 |

#### 组件级别字段

| 字段 | 类型 | 必需 | 说明 |
|------|------|------|------|
| `name` | string | ✅ | 组件名称 |
| `description` | string | ❌ | 组件描述 |
| `version` | string | ❌ | 组件版本 |
| `example` | string | ❌ | 使用示例 |
| `docUrl` | string | ❌ | 文档URL |
| `props` | array | ❌ | 属性列表 |
| `events` | array | ❌ | 事件列表 |
| `slots` | array | ❌ | 插槽列表 |

#### 属性字段

| 字段 | 类型 | 必需 | 说明 |
|------|------|------|------|
| `name` | string | ✅ | 属性名称 |
| `type` | string | ❌ | 属性类型 |
| `description` | string | ❌ | 属性描述 |
| `defaultValue` | any | ❌ | 默认值 |
| `required` | boolean | ❌ | 是否必填 |
| `options` | array | ❌ | 可选值列表 |

#### 事件字段

| 字段 | 类型 | 必需 | 说明 |
|------|------|------|------|
| `name` | string | ✅ | 事件名称 |
| `description` | string | ❌ | 事件描述 |
| `parameters` | string | ❌ | 事件参数 |

#### 插槽字段

| 字段 | 类型 | 必需 | 说明 |
|------|------|------|------|
| `name` | string | ✅ | 插槽名称 |
| `description` | string | ❌ | 插槽描述 |

### 导入自定义组件库

#### 方法一：通过组件库管理界面

1. 打开 **Tools** → **📚 组件库管理**
2. 点击 **上传组件库** 按钮
3. 选择JSON文件
4. 点击 **确定** 完成导入

#### 方法二：通过设置页面

1. 打开 **File** → **Settings** → **Tools** → **Element Plus Assistant**
2. 点击 **管理自定义组件库** 按钮
3. 在弹出窗口中点击 **上传组件库**
4. 选择JSON文件并导入

### 使用自定义组件

导入成功后，可以像使用内置组件一样使用自定义组件：

```vue
<template>
  <!-- 输入 <my- 时会显示自定义组件 -->
  <my-button type="primary" @click="handleClick">
    自定义按钮
  </my-button>
  
  <my-input 
    v-model="value"
    placeholder="请输入内容"
    @input="handleInput"
    @change="handleChange"
  />
</template>

<script>
export default {
  data() {
    return {
      value: ''
    }
  },
  methods: {
    handleClick() {
      console.log('按钮被点击')
    },
    handleInput(value) {
      console.log('输入值:', value)
    },
    handleChange(value) {
      console.log('值改变:', value)
    }
  }
}
</script>
```

## 组件库管理

### 查看组件库

1. 打开 **Tools** → **📚 组件库管理**
2. 查看所有已加载的组件库列表
3. 显示组件库名称、版本、组件数量等信息

### 删除组件库

1. 在组件库管理界面选择要删除的组件库
2. 点击 **删除** 按钮
3. 确认删除操作
4. **注意**：内置组件库（Element Plus、Element UI、Ant Design Vue）不能删除

### 导出组件库

1. 选择要导出的组件库
2. 点击 **导出** 按钮
3. 选择保存位置
4. 保存为JSON文件

### 导出模板

1. 点击 **导出模板** 按钮
2. 选择保存位置
3. 保存模板文件
4. 可以基于模板创建自己的组件库

## 数据持久化

### 缓存机制

- **缓存位置**：`用户主目录/.intellij_idea_system/vuekit/custom_component_libraries.json`
- **自动保存**：数据变更时自动保存到缓存文件
- **自动加载**：启动时自动从缓存文件加载数据
- **跨会话**：重启IDE后数据不丢失

### 缓存文件格式

```json
[
  {
    "name": "my-custom-library",
    "displayName": "我的自定义组件库",
    "version": "1.0.0",
    "description": "这是一个示例自定义组件库",
    "componentPrefix": "my-",
    "documentationUrlTemplate": "https://example.com/docs/%s",
    "components": [...]
  }
]
```

### 手动管理缓存

如果需要手动管理缓存文件：

1. **备份缓存**：复制缓存文件到安全位置
2. **恢复缓存**：将备份文件复制回缓存位置
3. **清空缓存**：删除缓存文件，重启IDE后重新导入

## 最佳实践

### 组件库设计

1. **命名规范**
   - 使用有意义的组件库名称
   - 使用统一的组件前缀
   - 避免与内置组件库冲突

2. **文档完善**
   - 为每个组件提供详细描述
   - 提供使用示例
   - 完善属性和事件说明

3. **版本管理**
   - 使用语义化版本号
   - 记录版本变更历史
   - 保持向后兼容性

### 团队协作

1. **统一标准**
   - 团队使用统一的组件库格式
   - 建立组件库开发规范
   - 统一命名和文档标准

2. **共享机制**
   - 将组件库文件纳入版本控制
   - 建立组件库发布流程
   - 提供组件库更新机制

3. **质量保证**
   - 建立组件库审查机制
   - 进行充分的测试验证
   - 保持文档的及时更新

### 性能优化

1. **数据优化**
   - 避免冗余的组件数据
   - 优化JSON文件大小
   - 使用压缩格式存储

2. **加载优化**
   - 按需加载组件数据
   - 实现增量更新机制
   - 优化缓存策略

## 故障排除

### 常见问题

#### Q: 导入自定义组件库失败

**A:** 请检查以下几点：
1. JSON文件格式是否正确
2. 必需字段是否完整
3. 组件名称是否重复
4. 文件编码是否为UTF-8

#### Q: 自定义组件不显示

**A:** 请检查以下几点：
1. 组件库是否成功导入
2. 组件前缀是否正确
3. 输入的前缀是否匹配
4. 缓存是否正常加载

#### Q: 重启后组件库丢失

**A:** 请检查以下几点：
1. 缓存目录是否有写入权限
2. 缓存文件是否被删除
3. 是否有其他程序占用缓存文件
4. 尝试重新导入组件库

#### Q: 组件库冲突

**A:** 请检查以下几点：
1. 组件库名称是否重复
2. 组件前缀是否冲突
3. 组件名称是否重复
4. 删除冲突的组件库

### 调试方法

#### 查看日志

1. 打开 **Help** → **Diagnostic Tools** → **Debug Log Settings**
2. 添加日志配置：`com.chu7.vuecomponentassistant`
3. 重启IDEA查看控制台输出

#### 检查缓存

1. 查看缓存文件是否存在
2. 检查缓存文件内容是否正确
3. 验证缓存文件权限

#### 验证数据

1. 使用JSON验证工具检查格式
2. 手动解析JSON数据
3. 验证组件数据结构

## 示例项目

### 完整示例

参考 `src/main/resources/data/custom-component-library-example.json` 文件，这是一个完整的自定义组件库示例。

### 模板文件

使用 **导出模板** 功能获取标准的组件库模板文件，可以基于模板快速创建自己的组件库。

---

**组件库管理是插件的重要功能，合理使用可以大大提高开发效率！** 📚
