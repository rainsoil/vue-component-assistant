# Vue Component Assistant (Vue组件助手)

一个专门为Vue.js开发者设计的IntelliJ IDEA插件，提供智能的Element Plus组件补全、文档提示和开发辅助功能。

## 🚀 主要功能

### 1. 智能组件补全
- **Element Plus组件库支持**: 完整的Element Plus组件补全，包括所有官方组件
- **组件描述**: 每个组件都显示详细的中文描述
- **前缀过滤**: 支持输入前缀快速过滤组件（如 `el-bu` 显示 `el-button`）
- **自动标签闭合**: 插入组件时自动生成完整的标签结构

### 2. 智能属性补全
- **组件特定属性**: 只显示当前组件支持的属性，不同组件的属性不会混淆
- **属性描述**: 每个属性都显示详细说明和用途
- **属性类型提示**: 显示属性的数据类型和默认值
- **必填属性标识**: 突出显示必填属性
- **属性选项提示**: 对于枚举类型属性，显示可选值

### 3. 智能事件补全
- **组件特定事件**: 只显示当前组件支持的事件
- **事件描述**: 每个事件都有详细说明和参数信息
- **自动生成处理函数**: 插入事件时自动生成处理函数名
- **事件参数提示**: 显示事件回调函数的参数

### 5. 文档提示功能
- **悬停文档**: 鼠标悬停在组件上显示详细文档
- **属性表格**: 以表格形式展示组件的所有属性
- **事件列表**: 显示组件支持的所有事件
- **示例代码**: 提供组件的使用示例

### 6. 右键菜单功能
- **快速文档访问**: 右键点击组件快速打开官方文档
- **智能菜单**: 只在Element Plus组件上显示相关菜单项

## 📋 支持的组件库

- **Element Plus** (最新版本)
- **Element UI** (经典版本)
- **Ant Design Vue**
- **自定义组件库** (支持导入和管理)

## 🎯 使用场景

- Vue.js项目开发
- 组件库文档查找
- 代码补全和智能提示
- 开发效率提升

## 🔧 安装和使用

### 安装方法

1. 下载插件包 (.jar文件)
2. 在IntelliJ IDEA中打开 `File` → `Settings` → `Plugins`
3. 点击齿轮图标，选择 `Install Plugin from Disk`
4. 选择下载的插件文件并安装
5. 重启IntelliJ IDEA

### 使用方法

#### 组件补全
1. 在Vue模板中输入 `<`
2. 开始输入组件名称，如 `el-button`
3. 选择需要的组件，自动生成完整标签

#### 属性补全
1. 在组件标签内输入空格
2. 开始输入属性名称
3. 选择属性，自动插入属性名和默认值

#### 事件补全
1. 在组件标签内输入 `@`
2. 开始输入事件名称
3. 选择事件，自动生成事件处理函数名

#### 文档查看
1. 鼠标悬停在组件上查看详细文档
2. 右键点击组件选择"查看 Element Plus 文档"

## 📝 示例

### 组件补全示例
```vue
<template>
  <el-button type="primary" @click="handleClick">按钮</el-button>
</template>
```

### 属性补全示例
```vue
<el-input 
  v-model="inputValue"
  placeholder="请输入内容"
  :disabled="false"
  clearable
/>
```

### 事件补全示例
```vue
<el-select 
  v-model="selectedValue"
  @change="handleChange"
  @visible-change="handleVisibleChange"
>
  <el-option label="选项1" value="1" />
</el-select>
```

## 🛠️ 技术特性

- **智能上下文分析**: 准确识别当前编辑位置的组件上下文
- **高性能**: 优化的数据加载和缓存机制
- **多语言支持**: 支持中文和英文界面
- **扩展性强**: 支持自定义组件库的导入和管理

## 📊 支持的Element Plus组件

插件支持Element Plus的所有官方组件，包括但不限于：

### 基础组件
- Button (按钮)
- Input (输入框)
- Select (选择器)
- Radio (单选框)
- Checkbox (多选框)
- Switch (开关)
- Slider (滑块)

### 表单组件
- Form (表单)
- FormItem (表单项)
- InputNumber (数字输入框)
- Cascader (级联选择器)
- DatePicker (日期选择器)
- TimePicker (时间选择器)

### 数据展示
- Table (表格)
- Tag (标签)
- Progress (进度条)
- Tree (树形控件)
- Pagination (分页)

### 导航组件
- Menu (导航菜单)
- Tabs (标签页)
- Breadcrumb (面包屑)
- Dropdown (下拉菜单)

### 反馈组件
- Alert (警告)
- Dialog (对话框)
- Message (消息提示)
- Notification (通知)
- Loading (加载)

### 其他组件
- Container (布局容器)
- Card (卡片)
- Drawer (抽屉)
- Tooltip (文字提示)
- Popover (弹出框)

## 🔄 版本更新

### v1.0.1
- 初始版本发布
- 支持Element Plus组件补全
- 支持属性和事件智能补全
- 支持文档提示功能
- 支持右键菜单快速访问文档

## 🤝 贡献

欢迎提交Issue和Pull Request来改进这个插件！

## 📄 许可证

本项目采用MIT许可证。

## 📞 联系方式

- 邮箱: luyanan0718@163.com
- GitHub: https://github.com/rainsoil/vue-component-assistant

---

**注意**: 本插件需要IntelliJ IDEA 2023.1或更高版本。