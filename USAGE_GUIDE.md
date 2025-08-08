# Vue Component Assistant 使用指南

## 快速开始

### 安装插件

1. **下载插件**
   - 从 [GitHub Releases](https://github.com/rainsoil/vue-component-assistant/releases) 下载最新版本
   - 或从 JetBrains 插件市场搜索 "Vue Component Assistant"

2. **安装插件**
   - 打开 IntelliJ IDEA
   - 进入 **File** → **Settings** (Windows/Linux) 或 **IntelliJ IDEA** → **Preferences** (macOS)
   - 选择 **Plugins**
   - 点击齿轮图标 → **Install Plugin from Disk**
   - 选择下载的插件文件
   - 重启 IntelliJ IDEA

### 基本配置

1. **打开设置**
   - 进入 **File** → **Settings** → **Tools** → **Element Plus Assistant**

2. **配置组件库**
   - 启用 **自动检测组件库**
   - 选择默认组件库（Element Plus/Element UI/Ant Design Vue）
   - 配置自定义组件库路径（可选）

## 功能使用

### 1. 组件补全

#### 基础组件补全
```vue
<template>
  <!-- 输入 < 时显示组件列表 -->
  <el-button type="primary">按钮</el-button>
  <el-input v-model="value" placeholder="请输入内容" />
  <el-select v-model="selected" placeholder="请选择">
    <el-option label="选项1" value="1" />
  </el-select>
</template>
```

#### 前缀过滤
- 输入 `<el-bu` 快速找到 `el-button`
- 输入 `<el-in` 快速找到 `el-input`
- 输入 `<my-` 显示自定义组件

### 2. 属性补全

在组件标签内输入空格时，会显示该组件的所有属性：

```vue
<template>
  <el-button 
    type="primary"           <!-- 按钮类型 -->
    size="medium"            <!-- 按钮尺寸 -->
    disabled                 <!-- 是否禁用 -->
    loading                  <!-- 是否加载中 -->
    @click="handleClick"     <!-- 点击事件 -->
  >
    按钮
  </el-button>
</template>
```

### 3. 事件补全

在组件标签内输入 `@` 时，会显示该组件的所有事件：

```vue
<template>
  <el-input 
    v-model="value"
    @input="handleInput"     <!-- 输入事件 -->
    @change="handleChange"   <!-- 值改变事件 -->
    @focus="handleFocus"     <!-- 获得焦点事件 -->
    @blur="handleBlur"       <!-- 失去焦点事件 -->
  />
</template>
```

### 4. 插槽补全

输入 `sl` 或 `slot` 时，会显示该组件的所有插槽：

```vue
<template>
  <el-card>
    <template #header>      <!-- 头部插槽 -->
      卡片标题
    </template>
    <template #default>     <!-- 默认插槽 -->
      卡片内容
    </template>
  </el-card>
</template>
```

### 5. 文档查看

#### 悬停文档
- 将鼠标悬停在组件名称上
- 会显示详细的组件文档，包括：
  - 组件描述
  - 属性列表（表格格式）
  - 事件列表（表格格式）
  - 插槽列表（表格格式）
  - 使用示例

#### 右键文档
- 右键点击组件名称
- 选择 **📚 查看组件文档**
- 会弹出详细的文档对话框

### 6. 自定义组件库

#### 导入自定义组件库

1. **准备JSON文件**
   ```json
   {
     "name": "my-custom-library",
     "displayName": "我的自定义组件库",
     "version": "1.0.0",
     "description": "这是一个示例自定义组件库",
     "componentPrefix": "my-",
     "components": [
       {
         "name": "my-button",
         "description": "自定义按钮组件",
         "props": [
           {
             "name": "type",
             "type": "string",
             "description": "按钮类型",
             "defaultValue": "default",
             "required": false,
             "options": ["primary", "success", "warning", "danger"]
           }
         ],
         "events": [
           {
             "name": "click",
             "description": "点击事件",
             "parameters": "event"
           }
         ],
         "slots": [
           {
             "name": "default",
             "description": "按钮内容"
           }
         ]
       }
     ]
   }
   ```

2. **导入组件库**
   - 打开 **Tools** → **📚 组件库管理**
   - 点击 **上传组件库**
   - 选择JSON文件
   - 点击 **确定** 完成导入

#### 使用自定义组件

导入后，可以像使用内置组件一样使用自定义组件：

```vue
<template>
  <!-- 输入 <my- 时会显示自定义组件 -->
  <my-button type="primary" @click="handleClick">
    自定义按钮
  </my-button>
</template>
```

## 高级功能

### 1. 组件库管理

#### 查看组件库
- 打开 **Tools** → **📚 组件库管理**
- 查看所有已加载的组件库
- 显示组件库名称、版本、组件数量等信息

#### 删除组件库
- 在组件库管理界面选择要删除的组件库
- 点击 **删除** 按钮
- 注意：内置组件库（Element Plus、Element UI、Ant Design Vue）不能删除

#### 导出组件库
- 选择要导出的组件库
- 点击 **导出** 按钮
- 保存为JSON文件

#### 导出模板
- 点击 **导出模板** 按钮
- 获取自定义组件库的模板文件
- 可以基于模板创建自己的组件库

### 2. 智能检测

插件会自动检测项目中使用的组件库：

1. **读取 package.json**
   - 检查 `dependencies` 和 `devDependencies`
   - 识别 Element Plus、Element UI、Ant Design Vue 等

2. **自动加载数据**
   - 根据检测结果加载对应的组件数据
   - 提供相应的补全和文档功能

3. **动态切换**
   - 支持在多个组件库间动态切换
   - 根据项目配置自动调整

### 3. 性能优化

#### 缓存机制
- 组件数据自动缓存到内存
- 自定义组件库数据持久化到文件
- 启动时自动加载缓存数据

#### 智能加载
- 按需加载组件数据
- 延迟生成文档内容
- 优化补全建议计算

## 常见问题

### Q: 为什么输入 `<el-` 时没有显示组件补全？

**A:** 请检查以下几点：
1. 确保项目中有 `package.json` 文件
2. 确保 `package.json` 中包含了 Element Plus 依赖
3. 确保插件已正确安装并启用
4. 检查插件设置中的组件库配置

### Q: 自定义组件库导入后重启IDEA就丢失了？

**A:** 请检查以下几点：
1. 确保缓存目录有写入权限
2. 检查控制台是否有错误日志
3. 尝试重新导入组件库
4. 验证JSON文件格式是否正确

### Q: 如何查看插件的调试信息？

**A:** 在 IntelliJ IDEA 中：
1. 打开 **Help** → **Diagnostic Tools** → **Debug Log Settings**
2. 添加日志配置：`com.chu7.vuecomponentassistant`
3. 重启IDEA查看控制台输出

### Q: 支持哪些组件库？

**A:** 目前支持：
- Element Plus（最新版本）
- Element UI（经典版本）
- Ant Design Vue（最新版本）
- 自定义组件库（任意版本）

### Q: 如何获取自定义组件库模板？

**A:** 
1. 打开 **Tools** → **📚 组件库管理**
2. 点击 **导出模板** 按钮
3. 保存模板文件
4. 参考模板创建自己的组件库

## 快捷键

| 功能 | 快捷键 | 说明 |
|------|--------|------|
| 组件补全 | `<` | 在Vue模板中输入 `<` 触发组件补全 |
| 属性补全 | `空格` | 在组件标签内输入空格触发属性补全 |
| 事件补全 | `@` | 在组件标签内输入 `@` 触发事件补全 |
| 插槽补全 | `sl` 或 `slot` | 输入 `sl` 或 `slot` 触发插槽补全 |
| 查看文档 | `Ctrl+Q` (Windows) / `F1` (macOS) | 悬停时查看组件文档 |

## 更新日志

### v2.0.0 (2024-01-XX)
- ✨ 新增自定义组件库支持
- ✨ 新增数据持久化功能
- ✨ 新增组件库管理界面
- 🐛 修复组件补全前缀识别问题
- 🐛 修复文档显示编码问题
- 📚 完善文档和注释

### v1.0.2 (2024-01-XX)
- ✨ 新增多组件库支持
- ✨ 新增智能组件库检测
- ✨ 新增表格格式文档显示
- 🐛 修复各种编译错误

### v1.0.0 (2024-01-XX)
- 🎉 首次发布
- ✨ 基础组件补全功能
- ✨ 基础文档显示功能
- ✨ 支持 Element Plus 组件库

## 技术支持

如果您在使用过程中遇到问题，可以通过以下方式获取帮助：

- **GitHub Issues**: https://github.com/rainsoil/vue-component-assistant/issues
- **邮箱支持**: luyanan0718@163.com
- **文档**: 查看项目中的 README.md 和 API_DOCUMENTATION.md

---

**Vue Component Assistant** - 让 Vue.js 开发更高效！ 🚀
