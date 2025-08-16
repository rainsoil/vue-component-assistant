# 配置文件合并说明

## 📋 概述

为了简化配置管理，我们将原来分散的两个配置文件合并成一个统一的配置文件，并移动到项目的 `.idea` 目录下。

## 🔄 配置文件变更

### 旧配置文件（已废弃）
- `项目根目录/.vuekit-libraries.json` - 组件库配置
- `项目根目录/.vuekit-project-settings.json` - 项目设置配置

### 新配置文件
- `项目根目录/.idea/vuekit-project-config.json` - 合并后的统一配置文件

## 📁 新配置文件结构

```json
{
  "projectId": "项目唯一标识",
  "projectName": "项目名称",
  "enabledLibraryNames": [
    "ELEMENT_PLUS",
    "ANT_DESIGN_VUE"
  ],
  "enableComponentCompletion": true,
  "enableAttributeCompletion": true,
  "enableEventCompletion": true,
  "enableSlotCompletion": true,
  "enableHoverDocumentation": true,
  "enableRightClickDocumentation": true,
  "enableCaching": true,
  "enableDebugMode": false
}
```

### 配置项说明

#### 基本信息
- `projectId`: 项目的唯一标识符
- `projectName`: 项目名称

#### 组件库配置
- `enabledLibraryNames`: 启用的组件库列表
  - `ELEMENT_UI`: Element UI 组件库
  - `ELEMENT_PLUS`: Element Plus 组件库
  - `ANT_DESIGN_VUE`: Ant Design Vue 组件库
  - `VUETIFY`: Vuetify 组件库
  - `QUASAR`: Quasar 组件库

#### 功能配置
- `enableComponentCompletion`: 启用组件补全
- `enableAttributeCompletion`: 启用属性补全
- `enableEventCompletion`: 启用事件补全
- `enableSlotCompletion`: 启用插槽补全
- `enableHoverDocumentation`: 启用悬停文档
- `enableRightClickDocumentation`: 启用右键文档
- `enableCaching`: 启用缓存优化
- `enableDebugMode`: 启用调试模式

## 🛠️ 迁移工具

### 自动迁移
插件会自动检测旧配置文件并提供迁移选项。

### 手动迁移
1. 打开 "🔄 配置迁移" 动作
2. 查看配置路径信息
3. 确认执行迁移
4. 迁移完成后旧文件会被自动删除

### 迁移步骤
1. **检测旧配置**: 检查是否存在 `.vuekit-libraries.json` 或 `.vuekit-project-settings.json`
2. **读取配置**: 解析旧配置文件内容
3. **合并配置**: 将两个配置文件的内容合并到新的统一格式
4. **创建目录**: 确保 `.idea` 目录存在
5. **保存新配置**: 将合并后的配置保存到 `vuekit-project-config.json`
6. **清理旧文件**: 删除旧的配置文件

## 📍 配置文件位置

### 项目级配置
```
项目根目录/.idea/vuekit-project-config.json
```

### 全局配置（保持不变）
```
用户主目录/.vuekit/vuekit-libraries.json
```

## 🔧 使用方法

### 1. 自动迁移
- 插件启动时会自动检测并提示迁移
- 在设置页面提供迁移按钮

### 2. 手动迁移
- 使用 "🔄 配置迁移" 动作
- 使用 "🔍 组件库调试" 查看配置状态

### 3. 直接编辑
- 可以直接编辑 `.idea/vuekit-project-config.json` 文件
- 修改后需要重启 IDE 或重新加载项目

## ⚠️ 注意事项

1. **备份**: 迁移前建议备份旧配置文件
2. **权限**: 确保 IDE 有权限在 `.idea` 目录下创建文件
3. **版本控制**: 新的配置文件会被包含在版本控制中
4. **兼容性**: 旧配置文件格式不再支持

## 🚀 优势

1. **简化管理**: 只需要维护一个配置文件
2. **统一位置**: 所有项目配置都在 `.idea` 目录下
3. **版本控制**: 配置文件可以纳入版本控制
4. **IDE 集成**: 与 IntelliJ IDEA 的项目结构保持一致

## 🔍 故障排除

### 配置文件不存在
- 检查 `.idea` 目录是否存在
- 确认 IDE 有写入权限
- 尝试手动创建配置文件

### 迁移失败
- 检查旧配置文件格式是否正确
- 查看日志获取详细错误信息
- 手动复制配置内容

### 配置不生效
- 重启 IDE
- 重新加载项目
- 检查配置文件格式是否正确

## 📞 支持

如果遇到问题，可以：
1. 使用 "🔍 组件库调试" 查看详细状态
2. 检查日志文件获取错误信息
3. 手动编辑配置文件
4. 联系技术支持 