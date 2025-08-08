# Vue Component Assistant 测试指南

## 概述

本文档提供了 Vue Component Assistant 插件的完整测试指南，包括单元测试、集成测试和手动测试的方法。

## 测试环境

### 环境要求
- **IntelliJ IDEA**: 2023.1 或更高版本
- **Java**: JDK 17 或更高版本
- **Gradle**: 8.0 或更高版本
- **操作系统**: Windows 10+, macOS 10.15+, Linux

### 测试数据
- 测试用的 Vue 文件位于 `test/` 目录
- 测试用的 package.json 文件位于 `test/` 目录
- 自定义组件库示例文件位于 `src/main/resources/data/`

## 单元测试

### 运行单元测试

```bash
# 运行所有测试
./gradlew test

# 运行特定测试类
./gradlew test --tests CustomComponentLibraryManagerTest

# 运行特定测试方法
./gradlew test --tests CustomComponentLibraryManagerTest.testLoadCustomLibrary
```

### 测试覆盖率

```bash
# 生成测试覆盖率报告
./gradlew jacocoTestReport

# 查看覆盖率报告
# 报告位置: build/reports/jacoco/test/html/index.html
```

### 主要测试类

#### CustomComponentLibraryManagerTest
测试自定义组件库管理器的核心功能：

```java
@Test
void testLoadCustomLibrary() {
    // 测试加载自定义组件库
    String jsonContent = "{\"name\":\"test\",\"components\":[]}";
    boolean result = CustomComponentLibraryManager.loadCustomLibrary(jsonContent);
    assertTrue(result);
}

@Test
void testGetAllCustomLibraries() {
    // 测试获取所有自定义组件库
    List<CustomLibraryConfig> libraries = CustomComponentLibraryManager.getAllCustomLibraries();
    assertNotNull(libraries);
}

@Test
void testRemoveCustomLibrary() {
    // 测试删除自定义组件库
    boolean result = CustomComponentLibraryManager.removeCustomLibrary("test");
    assertTrue(result);
}
```

#### ComponentLibraryDetectorTest
测试组件库检测功能：

```java
@Test
void testDetectElementPlus() {
    // 测试检测 Element Plus
    String result = ComponentLibraryDetector.detectComponentLibrary(project);
    assertEquals("Element Plus", result);
}

@Test
void testDetectElementUI() {
    // 测试检测 Element UI
    String result = ComponentLibraryDetector.detectComponentLibrary(project);
    assertEquals("Element UI", result);
}
```

## 集成测试

### 构建和安装插件

1. **构建插件**
   ```bash
   ./gradlew buildPlugin
   ```

2. **安装插件**
   - 打开 IntelliJ IDEA
   - 进入 **File** → **Settings** → **Plugins**
   - 点击齿轮图标 → **Install Plugin from Disk**
   - 选择 `build/distributions/vue-component-assistant-*.zip`
   - 重启 IntelliJ IDEA

### 测试项目设置

1. **创建测试项目**
   - 创建新的 Vue.js 项目
   - 在项目根目录放置 `package.json` 文件

2. **配置组件库**
   ```json
   {
     "dependencies": {
       "element-plus": "^2.4.0"
     }
   }
   ```

3. **创建测试文件**
   - 在 `src/` 目录下创建 `.vue` 文件
   - 使用测试用例进行功能验证

## 手动测试

### 1. 组件补全测试

#### 基础组件补全
1. 打开 Vue 文件
2. 在 `<template>` 中输入 `<`
3. 验证是否显示组件列表
4. 选择组件并验证是否正确插入

**测试用例**:
```vue
<template>
  <!-- 输入 < 时应该显示组件列表 -->
  <el-button type="primary">按钮</el-button>
  <el-input v-model="value" placeholder="请输入内容" />
  <el-select v-model="selected" placeholder="请选择">
    <el-option label="选项1" value="1" />
  </el-select>
</template>
```

#### 前缀过滤测试
1. 输入 `<el-bu` 验证是否显示 `el-button`
2. 输入 `<el-in` 验证是否显示 `el-input`
3. 输入 `<my-` 验证是否显示自定义组件

#### 自定义组件测试
1. 导入自定义组件库
2. 输入 `<my-` 验证是否显示自定义组件
3. 验证自定义组件的属性和事件补全

### 2. 属性补全测试

#### 属性列表显示
1. 在组件标签内输入空格
2. 验证是否显示该组件的属性列表
3. 验证属性描述是否正确

**测试用例**:
```vue
<template>
  <el-button 
    type="primary"           <!-- 应该显示按钮类型属性 -->
    size="medium"            <!-- 应该显示按钮尺寸属性 -->
    disabled                 <!-- 应该显示禁用属性 -->
    loading                  <!-- 应该显示加载属性 -->
  >
    按钮
  </el-button>
</template>
```

#### 属性过滤测试
1. 在属性列表中输入 `typ` 验证是否显示 `type`
2. 在属性列表中输入 `siz` 验证是否显示 `size`
3. 验证必填属性是否突出显示

### 3. 事件补全测试

#### 事件列表显示
1. 在组件标签内输入 `@`
2. 验证是否显示该组件的事件列表
3. 验证事件描述和参数是否正确

**测试用例**:
```vue
<template>
  <el-button @click="handleClick">按钮</el-button>
  <el-input 
    @input="handleInput"     <!-- 应该显示输入事件 -->
    @change="handleChange"   <!-- 应该显示值改变事件 -->
    @focus="handleFocus"     <!-- 应该显示获得焦点事件 -->
    @blur="handleBlur"       <!-- 应该显示失去焦点事件 -->
  />
</template>
```

#### 事件过滤测试
1. 在事件列表中输入 `cli` 验证是否显示 `click`
2. 在事件列表中输入 `cha` 验证是否显示 `change`
3. 验证事件处理函数名是否正确生成

### 4. 插槽补全测试

#### 插槽列表显示
1. 输入 `sl` 或 `slot` 验证是否显示插槽列表
2. 验证插槽描述是否正确

**测试用例**:
```vue
<template>
  <el-card>
    <template #header>      <!-- 应该显示头部插槽 -->
      卡片标题
    </template>
    <template #default>     <!-- 应该显示默认插槽 -->
      卡片内容
    </template>
  </el-card>
</template>
```

### 5. 文档显示测试

#### 悬停文档
1. 将鼠标悬停在组件名称上
2. 验证是否显示详细文档
3. 验证文档格式是否正确（表格格式）
4. 验证文档内容是否完整

#### 右键文档
1. 右键点击组件名称
2. 选择 **📚 查看组件文档**
3. 验证是否弹出文档对话框
4. 验证对话框内容是否正确

### 6. 自定义组件库测试

#### 导入测试
1. 打开 **Tools** → **📚 组件库管理**
2. 点击 **上传组件库**
3. 选择自定义组件库JSON文件
4. 验证是否成功导入

#### 使用测试
1. 导入自定义组件库后
2. 输入 `<my-` 验证是否显示自定义组件
3. 验证自定义组件的属性和事件补全
4. 验证自定义组件的文档显示

#### 持久化测试
1. 导入自定义组件库
2. 重启 IntelliJ IDEA
3. 验证自定义组件库是否仍然存在
4. 验证自定义组件是否仍然可以补全

### 7. 多组件库测试

#### 自动检测测试
1. 在项目根目录放置不同的 `package.json` 文件
2. 验证插件是否正确检测组件库
3. 验证补全功能是否使用正确的组件库

**测试用例**:
```json
// Element Plus
{
  "dependencies": {
    "element-plus": "^2.4.0"
  }
}

// Element UI
{
  "dependencies": {
    "element-ui": "^2.15.0"
  }
}

// Ant Design Vue
{
  "dependencies": {
    "ant-design-vue": "^4.0.0"
  }
}
```

#### 动态切换测试
1. 修改 `package.json` 文件
2. 验证插件是否自动切换组件库
3. 验证补全功能是否使用新的组件库

## 性能测试

### 响应时间测试
1. **补全响应时间**
   - 测量输入 `<` 到显示组件列表的时间
   - 目标：< 100ms

2. **文档加载时间**
   - 测量悬停到显示文档的时间
   - 目标：< 200ms

3. **数据导入时间**
   - 测量导入自定义组件库的时间
   - 目标：< 1s

### 内存占用测试
1. **插件内存占用**
   - 监控插件运行时的内存占用
   - 目标：< 50MB

2. **CPU使用率**
   - 监控插件运行时的CPU使用率
   - 目标：< 5%

## 兼容性测试

### IDE版本兼容性
- IntelliJ IDEA 2023.1
- IntelliJ IDEA 2023.2
- IntelliJ IDEA 2023.3
- IntelliJ IDEA 2024.1

### 操作系统兼容性
- Windows 10/11
- macOS 10.15+
- Ubuntu 20.04+
- CentOS 7+

### Java版本兼容性
- Java 17
- Java 18
- Java 19
- Java 20
- Java 21

## 错误处理测试

### 异常情况测试
1. **无效JSON文件**
   - 尝试导入格式错误的JSON文件
   - 验证是否正确显示错误信息

2. **缺失依赖**
   - 删除项目中的 `package.json` 文件
   - 验证插件是否正常处理

3. **权限问题**
   - 设置缓存目录为只读
   - 验证插件是否正常处理权限错误

### 恢复机制测试
1. **数据损坏**
   - 手动损坏缓存文件
   - 验证插件是否能够恢复

2. **网络问题**
   - 模拟网络连接问题
   - 验证插件是否正常处理

## 测试报告

### 测试结果记录
记录以下测试结果：
- 测试日期和时间
- 测试环境信息
- 测试用例执行结果
- 发现的问题和缺陷
- 性能测试数据

### 问题跟踪
- 使用 GitHub Issues 跟踪问题
- 记录问题的详细描述
- 提供复现步骤
- 标记问题的优先级和严重程度

## 自动化测试

### CI/CD 集成
```yaml
# .github/workflows/test.yml
name: Test

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    
    steps:
    - uses: actions/checkout@v3
    
    - name: Set up JDK 17
      uses: actions/setup-java@v3
      with:
        java-version: '17'
        distribution: 'temurin'
    
    - name: Run tests
      run: ./gradlew test
    
    - name: Build plugin
      run: ./gradlew buildPlugin
```

### 测试脚本
```bash
#!/bin/bash
# test.sh

echo "Running Vue Component Assistant tests..."

# 运行单元测试
./gradlew test

# 构建插件
./gradlew buildPlugin

# 检查构建结果
if [ -f "build/distributions/vue-component-assistant-*.zip" ]; then
    echo "✅ 测试通过，插件构建成功"
    exit 0
else
    echo "❌ 测试失败，插件构建失败"
    exit 1
fi
```

## 测试工具

### 推荐工具
- **JUnit 5**: 单元测试框架
- **Mockito**: Mock 框架
- **JaCoCo**: 代码覆盖率工具
- **Gradle**: 构建工具

### 调试工具
- **IntelliJ IDEA Debugger**: 调试工具
- **Log4j**: 日志记录
- **JProfiler**: 性能分析

---

**测试是质量保证的重要环节，请认真执行所有测试用例！** 🧪
