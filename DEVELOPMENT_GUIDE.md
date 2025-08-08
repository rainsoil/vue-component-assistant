# Vue Component Assistant 开发指南

## 概述

本文档为 Vue Component Assistant 插件的开发者提供详细的开发指南，包括环境搭建、项目结构、开发流程、测试方法等。

## 环境要求

### 开发环境

- **IntelliJ IDEA**: 2023.1 或更高版本
- **Java**: JDK 17 或更高版本
- **Gradle**: 8.0 或更高版本
- **操作系统**: Windows 10+, macOS 10.15+, Linux

### 推荐工具

- **IntelliJ IDEA Ultimate**: 提供完整的插件开发支持
- **Git**: 版本控制
- **Postman**: API 测试（可选）

## 项目设置

### 1. 克隆项目

```bash
git clone https://github.com/rainsoil/vue-component-assistant.git
cd vue-component-assistant
```

### 2. 导入项目

1. 打开 IntelliJ IDEA
2. 选择 **File** → **Open**
3. 选择项目根目录
4. 等待 Gradle 同步完成

### 3. 配置 SDK

1. 打开 **File** → **Project Structure**
2. 在 **Project** 选项卡中设置 **Project SDK** 为 JDK 17
3. 在 **Modules** 选项卡中确保 **Language Level** 设置为 17

### 4. 配置 Gradle

确保 `build.gradle` 文件中的配置正确：

```gradle
plugins {
    id 'java'
    id 'org.jetbrains.intellij' version '1.17.2'
}

group 'com.chu7'
version '2.0.0'

repositories {
    mavenCentral()
}

dependencies {
    implementation 'com.google.code.gson:gson:2.10.1'
    testImplementation 'org.junit.jupiter:junit-jupiter-api:5.8.1'
    testRuntimeOnly 'org.junit.jupiter:junit-jupiter-engine:5.8.1'
}

intellij {
    version = '2023.1'
    plugins = ['com.intellij.java']
}

patchPluginXml {
    changeNotes = """
      新增自定义组件库支持
      新增数据持久化功能
      修复组件补全前缀识别问题
    """
}
```

## 项目结构

```
vue-component-assistant/
├── build.gradle                    # Gradle 构建配置
├── settings.gradle                 # Gradle 设置
├── README.md                       # 项目说明文档
├── API_DOCUMENTATION.md            # API 文档
├── DEVELOPMENT_GUIDE.md            # 开发指南
├── LICENSE                         # 许可证文件
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/chu7/vuecomponentassistant/
│   │   │       ├── action/         # 动作处理类
│   │   │       │   ├── ElementPlusDocumentationAction.java
│   │   │       │   └── CustomLibraryManagementAction.java
│   │   │       ├── completion/     # 补全相关类
│   │   │       │   ├── ElementPlusCompletionContributor.java
│   │   │       │   ├── ElementPlusTestCompletionProvider.java
│   │   │       │   ├── ElementPlusComponent.java
│   │   │       │   ├── ElementPlusProp.java
│   │   │       │   ├── ElementPlusEvent.java
│   │   │       │   ├── ElementPlusSlot.java
│   │   │       │   └── ComponentProvider.java
│   │   │       ├── documentation/  # 文档相关类
│   │   │       │   ├── ElementPlusDocumentationProvider.java
│   │   │       │   └── DocumentationStyleGenerator.java
│   │   │       ├── settings/       # 设置相关类
│   │   │       │   ├── ElementPlusSettings.java
│   │   │       │   └── ElementPlusSettingsConfigurable.java
│   │   │       ├── ui/             # UI 组件类
│   │   │       │   ├── ComponentDocumentationDialog.java
│   │   │       │   ├── CustomLibraryUploadDialog.java
│   │   │       │   └── ComponentLibraryManagementDialog.java
│   │   │       └── utils/          # 工具类
│   │   │           ├── CustomComponentLibraryManager.java
│   │   │           └── ComponentLibraryDetector.java
│   │   └── resources/
│   │       ├── META-INF/
│   │       │   └── plugin.xml      # 插件配置文件
│   │       ├── data/               # 组件数据文件
│   │       │   ├── element-plus-components.json
│   │       │   ├── element-ui-components.json
│   │       │   ├── ant-design-vue-components.json
│   │       │   └── custom-component-library-example.json
│   │       └── icons/              # 图标资源
│   │           ├── component.svg
│   │           ├── property.svg
│   │           ├── event.svg
│   │           └── slot.svg
│   └── test/                       # 测试代码
│       └── java/
│           └── com/chu7/vuecomponentassistant/
│               └── test/
│                   └── CustomComponentLibraryManagerTest.java
├── test/                           # 测试文件
│   ├── ElementPlusTest.vue
│   ├── CustomLibraryManagementTest.vue
│   └── package.json
└── build/                          # 构建输出目录
```

## 核心模块说明

### 1. 补全模块 (completion)

负责提供智能代码补全功能。

**主要类：**
- `ElementPlusCompletionContributor`: 补全贡献者，注册补全提供者
- `ElementPlusTestCompletionProvider`: 智能补全提供者，实现补全逻辑
- `ComponentProvider`: 组件数据提供者，管理组件数据

**开发要点：**
- 继承 `CompletionProvider` 类
- 实现 `addCompletions` 方法
- 分析用户输入上下文
- 提供相应的补全建议

### 2. 文档模块 (documentation)

负责生成和显示组件文档。

**主要类：**
- `ElementPlusDocumentationProvider`: 文档提供者，实现文档生成
- `DocumentationStyleGenerator`: 文档样式生成器，生成格式化文档

**开发要点：**
- 实现 `DocumentationProvider` 接口
- 生成 HTML 格式的文档
- 支持悬停和右键文档显示

### 3. 自定义组件库模块 (utils)

负责管理自定义组件库。

**主要类：**
- `CustomComponentLibraryManager`: 自定义组件库管理器
- `ComponentLibraryDetector`: 组件库检测器

**开发要点：**
- 实现数据持久化
- 支持 JSON 格式导入
- 提供验证和错误处理

### 4. UI 模块 (ui)

负责用户界面组件。

**主要类：**
- `ComponentDocumentationDialog`: 组件文档对话框
- `CustomLibraryUploadDialog`: 自定义组件库上传对话框
- `ComponentLibraryManagementDialog`: 组件库管理对话框

**开发要点：**
- 继承 `DialogWrapper` 类
- 使用 Swing 组件构建界面
- 处理用户交互

## 开发流程

### 1. 功能开发

#### 添加新功能

1. **创建功能分支**
   ```bash
   git checkout -b feature/new-feature
   ```

2. **编写代码**
   - 在相应的包中创建新类
   - 实现功能逻辑
   - 添加必要的注释

3. **更新配置**
   - 在 `plugin.xml` 中注册新的扩展点
   - 更新版本号和变更日志

4. **测试功能**
   - 编写单元测试
   - 进行手动测试
   - 验证功能正确性

5. **提交代码**
   ```bash
   git add .
   git commit -m "feat: 添加新功能"
   git push origin feature/new-feature
   ```

#### 修复 Bug

1. **创建修复分支**
   ```bash
   git checkout -b fix/bug-description
   ```

2. **定位问题**
   - 查看错误日志
   - 分析问题原因
   - 确定修复方案

3. **修复问题**
   - 修改相关代码
   - 添加测试用例
   - 验证修复效果

4. **提交修复**
   ```bash
   git add .
   git commit -m "fix: 修复问题描述"
   git push origin fix/bug-description
   ```

### 2. 代码规范

#### 命名规范

- **类名**: 使用 PascalCase，如 `ElementPlusComponent`
- **方法名**: 使用 camelCase，如 `getComponentByName`
- **常量**: 使用 UPPER_SNAKE_CASE，如 `CACHE_FILE_NAME`
- **包名**: 使用小写，如 `com.chu7.vuecomponentassistant`

#### 注释规范

- **类注释**: 包含功能说明、作者、版本等信息
- **方法注释**: 包含参数说明、返回值说明、示例代码
- **字段注释**: 说明字段的用途和含义
- **行内注释**: 解释复杂的业务逻辑

#### 代码风格

- 使用 4 个空格缩进
- 每行代码不超过 120 个字符
- 使用有意义的变量名
- 避免魔法数字，使用常量

### 3. 测试

#### 单元测试

创建测试类：

```java
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

public class CustomComponentLibraryManagerTest {
    
    @BeforeEach
    void setUp() {
        // 初始化测试环境
    }
    
    @Test
    void testLoadCustomLibrary() {
        // 测试加载自定义组件库
        String jsonContent = "{\"name\":\"test\",\"components\":[]}";
        boolean result = CustomComponentLibraryManager.loadCustomLibrary(jsonContent);
        assertTrue(result);
    }
}
```

#### 集成测试

1. **构建插件**
   ```bash
   ./gradlew buildPlugin
   ```

2. **安装插件**
   - 在 IntelliJ IDEA 中打开 **Settings** → **Plugins**
   - 点击齿轮图标，选择 **Install Plugin from Disk**
   - 选择构建的插件文件

3. **测试功能**
   - 创建 Vue 项目
   - 测试组件补全功能
   - 测试文档显示功能
   - 测试自定义组件库功能

#### 手动测试

1. **组件补全测试**
   - 在 Vue 文件中输入 `<`
   - 验证是否显示组件列表
   - 测试前缀过滤功能

2. **属性补全测试**
   - 在组件标签内输入空格
   - 验证是否显示属性列表
   - 测试属性描述显示

3. **事件补全测试**
   - 在组件标签内输入 `@`
   - 验证是否显示事件列表
   - 测试事件参数显示

4. **文档显示测试**
   - 悬停在组件上查看文档
   - 右键点击组件查看文档
   - 验证文档格式和内容

## 调试技巧

### 1. 日志调试

添加日志输出：

```java
private static final Logger LOG = Logger.getInstance(YourClass.class);

LOG.info("调试信息: " + variable);
LOG.error("错误信息", exception);
```

### 2. 断点调试

1. 在代码中设置断点
2. 以调试模式运行插件
3. 使用调试工具查看变量值

### 3. 控制台输出

使用 `System.out.println()` 输出调试信息：

```java
System.out.println("=== 调试信息 ===");
System.out.println("变量值: " + variable);
```

### 4. 插件调试

1. 运行 `./gradlew runIde` 启动测试 IDE
2. 在测试 IDE 中测试插件功能
3. 查看控制台输出和日志

## 性能优化

### 1. 缓存优化

- 使用内存缓存存储组件数据
- 实现文件缓存持久化数据
- 按需加载组件数据

### 2. 延迟加载

- 组件数据在首次使用时加载
- 文档内容在需要时生成
- 补全建议在用户输入时计算

### 3. 数据结构优化

- 使用 HashMap 进行快速查找
- 避免重复计算
- 优化字符串操作

## 发布流程

### 1. 版本管理

更新版本号：

```gradle
version '2.0.0'
```

更新变更日志：

```xml
<patchPluginXml>
    changeNotes = """
        新增自定义组件库支持
        新增数据持久化功能
        修复组件补全前缀识别问题
    """
</patchPluginXml>
```

### 2. 构建插件

```bash
./gradlew buildPlugin
```

### 3. 测试插件

1. 在测试环境中安装插件
2. 进行完整的功能测试
3. 验证所有功能正常工作

### 4. 发布插件

1. 创建发布标签
   ```bash
   git tag v2.0.0
   git push origin v2.0.0
   ```

2. 上传到插件市场
   - 登录 JetBrains 插件市场
   - 上传插件文件
   - 填写发布信息

## 常见问题

### 1. 编译错误

**问题**: 找不到某些类或方法
**解决**: 检查依赖配置，确保所有必要的依赖都已添加

**问题**: 版本兼容性问题
**解决**: 检查 IntelliJ IDEA 版本兼容性，更新 SDK 版本

### 2. 运行时错误

**问题**: 插件无法正常加载
**解决**: 检查 `plugin.xml` 配置，确保扩展点正确注册

**问题**: 功能无法正常工作
**解决**: 查看错误日志，检查代码逻辑

### 3. 性能问题

**问题**: 插件响应缓慢
**解决**: 优化数据加载逻辑，使用缓存机制

**问题**: 内存占用过高
**解决**: 检查内存泄漏，优化数据结构

## 贡献指南

### 1. 提交 Issue

- 使用清晰的标题描述问题
- 提供详细的复现步骤
- 包含错误日志和截图
- 说明期望的行为

### 2. 提交 Pull Request

- 创建功能分支
- 编写清晰的提交信息
- 添加必要的测试
- 更新相关文档

### 3. 代码审查

- 检查代码质量和规范
- 验证功能正确性
- 确保测试覆盖率
- 检查文档更新

## 资源链接

- [IntelliJ Platform SDK](https://plugins.jetbrains.com/docs/intellij/)
- [Plugin Development](https://www.jetbrains.org/intellij/sdk/docs/)
- [Gradle Plugin](https://github.com/JetBrains/gradle-intellij-plugin)
- [Vue.js Documentation](https://vuejs.org/guide/)

## 联系方式

- **邮箱**: luyanan0718@163.com
- **GitHub**: https://github.com/rainsoil/vue-component-assistant
- **Issues**: https://github.com/rainsoil/vue-component-assistant/issues

---

感谢您为 Vue Component Assistant 项目做出贡献！ 🚀
