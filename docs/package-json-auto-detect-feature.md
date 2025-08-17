# Package.json 自动检测功能

## 功能概述

VueKit 插件现在支持自动检测项目中的 `package.json` 文件，并根据其中声明的依赖项自动匹配和启用相应的组件库。这个功能特别适用于首次使用插件的项目，能够智能地为用户提供默认的组件库配置。

## 功能特性

### 1. 自动检测机制
- **首次使用检测**：当 `vuekit-project-config.json` 文件为空或不存在时触发
- **依赖项解析**：读取 `package.json` 中的 `dependencies`、`devDependencies` 和 `peerDependencies`
- **智能匹配**：将包名与已安装的组件库进行匹配

### 2. 支持的组件库
- **Element Plus** (`element-plus`)
- **Element UI** (`element-ui`)
- **Ant Design Vue** (`ant-design-vue`)
- **Vuetify** (`vuetify`)
- **Quasar** (`quasar`)
- **Naive UI** (`naive-ui`)
- **PrimeVue** (`primevue`)
- **其他自定义组件库**

### 3. 检测逻辑
1. 检查项目配置文件是否为空
2. 查找项目根目录下的 `package.json` 文件
3. 解析 JSON 内容，提取依赖项
4. 匹配已安装的组件库
5. 自动启用匹配到的组件库
6. 保存配置到项目配置文件

## 使用场景

### 场景一：新项目首次使用
```
项目结构：
├── package.json (包含 element-plus 依赖)
├── .idea/
│   └── vuekit-project-config.json (不存在或为空)
└── src/
    └── components/
```

**结果**：系统自动检测到 `element-plus` 依赖，自动启用 Element Plus 组件库。

### 场景二：已有配置的项目
```
项目结构：
├── package.json (包含 ant-design-vue 依赖)
├── .idea/
│   └── vuekit-project-config.json (已存在配置)
└── src/
    └── components/
```

**结果**：跳过自动检测，保持现有配置不变。

### 场景三：无组件库依赖的项目
```
项目结构：
├── package.json (不包含组件库依赖)
├── .idea/
│   └── vuekit-project-config.json (不存在)
└── src/
    └── components/
```

**结果**：未检测到支持的组件库，提示用户手动配置。

## 配置界面

### 自动检测状态显示
在组件库配置对话框中，系统会显示自动检测的状态：

- **✅ 已自动检测并启用项目中的组件库**
  - 绿色状态指示器
  - 显示检测到的组件库信息
  - 提供调整选项

- **ℹ️ 手动配置模式**
  - 灰色状态指示器
  - 说明未检测到依赖或配置文件已存在
  - 提示用户手动选择

### 组件库选择界面
- 自动检测到的组件库会显示为选中状态
- 用户可以手动调整启用/禁用状态
- 支持实时预览配置变更

## 技术实现

### 核心类
- `PackageJsonAutoDetector`：自动检测器主类
- `ComponentLibraryStartupActivity`：启动活动集成
- `ComponentLibraryConfigDialog`：配置界面增强

### 检测流程
```java
// 1. 检查配置文件状态
if (!isProjectConfigEmpty(project)) {
    return false; // 跳过检测
}

// 2. 读取 package.json
VirtualFile packageJsonFile = findPackageJsonFile(project);
JsonObject packageJson = parsePackageJson(packageJsonFile);

// 3. 检测依赖项
Set<String> detectedLibraries = detectLibrariesFromDependencies(packageJson);

// 4. 启用检测到的组件库
enableDetectedLibraries(project, detectedLibraries);
```

### 依赖项检查
```java
// 检查各种依赖类型
String[] dependencyTypes = {"dependencies", "devDependencies", "peerDependencies"};

for (String dependencyType : dependencyTypes) {
    if (packageJson.has(dependencyType)) {
        JsonObject dependencies = packageJson.getAsJsonObject(dependencyType);
        
        // 检查已安装的组件库
        for (ComponentLibrary library : installedLibraries) {
            if (dependencies.has(library.getName())) {
                detectedLibraries.add(library.getName());
            }
        }
    }
}
```

## 配置示例

### package.json 示例
```json
{
  "name": "my-vue-project",
  "version": "1.0.0",
  "dependencies": {
    "vue": "^3.3.0",
    "element-plus": "^2.3.0",
    "ant-design-vue": "^4.0.0"
  },
  "devDependencies": {
    "@vitejs/plugin-vue": "^4.2.0",
    "vite": "^4.3.0"
  }
}
```

### 自动生成的配置
```json
{
  "projectId": "project_hash",
  "projectName": "my-vue-project",
  "enabledLibraryNames": [
    "element-plus",
    "ant-design-vue"
  ]
}
```

## 最佳实践

### 1. 项目初始化
- 确保 `package.json` 中包含正确的组件库依赖
- 首次打开项目时，系统会自动检测并配置
- 检查配置对话框中的自动检测状态

### 2. 配置管理
- 自动检测仅在首次使用时执行
- 后续可以通过配置对话框手动调整
- 配置会持久化保存到项目文件中

### 3. 团队协作
- 配置文件会包含在版本控制中
- 团队成员打开项目时会自动应用配置
- 支持不同项目使用不同的组件库配置

## 故障排除

### 问题：自动检测未生效
**可能原因**：
- 项目配置文件已存在且不为空
- `package.json` 文件不存在或格式错误
- 依赖的组件库未在插件中安装

**解决方案**：
1. 删除 `.idea/vuekit-project-config.json` 文件
2. 检查 `package.json` 文件格式
3. 确保组件库已正确安装

### 问题：检测到错误的组件库
**可能原因**：
- 包名与组件库名称不匹配
- 依赖项版本不兼容

**解决方案**：
1. 手动调整配置对话框中的选择
2. 检查组件库的兼容性
3. 更新依赖项版本

## 更新日志

### v3.0.0
- ✅ 新增 Package.json 自动检测功能
- ✅ 支持多种组件库的智能匹配
- ✅ 增强配置界面显示自动检测状态
- ✅ 优化首次使用体验
- ✅ 提供详细的检测日志和错误处理

## 未来计划

- [ ] 支持更多组件库的自动检测
- [ ] 添加检测结果的详细报告
- [ ] 支持自定义检测规则
- [ ] 提供检测历史记录
- [ ] 支持批量项目的自动配置 