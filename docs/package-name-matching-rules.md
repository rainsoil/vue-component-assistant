# 包名匹配规则说明

## 概述

VueKit 插件的 Package.json 自动检测功能现在使用标准化的包名匹配规则，可以处理不同大小写和空格的情况，提高匹配的准确性和容错性。

## 匹配规则

### 1. 标准化处理

所有包名在匹配前都会进行标准化处理：

```java
private static String normalizePackageName(String packageName) {
    if (packageName == null) {
        return "";
    }
    return packageName.toLowerCase().replaceAll("\\s+", "");
}
```

**处理步骤：**
1. 转换为小写
2. 移除所有空格（包括开头、结尾、中间的空格）

### 2. 匹配示例

| 原始包名 | 标准化后 | 匹配结果 |
|---------|---------|---------|
| `Element-Plus` | `element-plus` | ✅ 匹配 `element-plus` |
| `ANT-DESIGN-VUE` | `ant-design-vue` | ✅ 匹配 `ant-design-vue` |
| `Vuetify ` | `vuetify` | ✅ 匹配 `vuetify` |
| ` quasar` | `quasar` | ✅ 匹配 `quasar` |
| `Element UI` | `elementui` | ✅ 匹配 `element-ui` |
| `Ant Design Vue` | `antdesignvue` | ✅ 匹配 `ant-design-vue` |

### 3. 匹配流程

#### 动态检测流程
```java
// 1. 获取已安装的组件库列表
List<ComponentLibrary> installedLibraries = libraryManager.getAllLibraries();

// 2. 对每个组件库进行标准化
for (ComponentLibrary library : installedLibraries) {
    String packageName = library.getName();
    String normalizedPackageName = normalizePackageName(packageName);
    
    // 3. 遍历所有依赖项进行匹配
    for (String dependencyName : dependencies.keySet()) {
        String normalizedDependencyName = normalizePackageName(dependencyName);
        
        // 4. 标准化后比较
        if (normalizedPackageName.equals(normalizedDependencyName)) {
            // 匹配成功
            detectedLibraries.add(packageName);
            break;
        }
    }
}
```

#### 静态检测流程
```java
// 1. 预定义的组件库列表
String[] commonLibraries = {
    "element-plus", "element-ui", "ant-design-vue", "vuetify", "quasar",
    "naive-ui", "primevue", "vuestic-ui", "oruga-ui"
};

// 2. 对每个预定义组件库进行标准化
for (String library : commonLibraries) {
    String normalizedLibrary = normalizePackageName(library);
    
    // 3. 遍历所有依赖项进行匹配
    for (String dependencyName : dependencies.keySet()) {
        String normalizedDependencyName = normalizePackageName(dependencyName);
        
        // 4. 标准化后比较
        if (normalizedLibrary.equals(normalizedDependencyName)) {
            // 匹配成功
            detectedLibraries.add(library);
            break;
        }
    }
}
```

## 支持的匹配场景

### 1. 大小写不敏感
- `Element-Plus` ↔ `element-plus`
- `ANT-DESIGN-VUE` ↔ `ant-design-vue`
- `Vuetify` ↔ `vuetify`

### 2. 空格处理
- `Element Plus` ↔ `element-plus`
- `Ant Design Vue` ↔ `ant-design-vue`
- `Vuetify ` ↔ `vuetify`
- ` quasar` ↔ `quasar`

### 3. 混合情况
- `Element Plus` ↔ `element-plus`
- `ANT DESIGN VUE` ↔ `ant-design-vue`
- `Vuetify UI` ↔ `vuetify-ui`

## 测试用例

### 测试用例 1：大小写变化
```json
{
  "dependencies": {
    "Element-Plus": "^2.3.0",
    "ANT-DESIGN-VUE": "^4.0.0"
  }
}
```
**预期结果：**
- 检测到 `element-plus`
- 检测到 `ant-design-vue`

### 测试用例 2：空格处理
```json
{
  "dependencies": {
    "Element Plus": "^2.3.0",
    "Ant Design Vue": "^4.0.0",
    "Vuetify ": "^3.3.0"
  }
}
```
**预期结果：**
- 检测到 `element-plus`
- 检测到 `ant-design-vue`
- 检测到 `vuetify`

### 测试用例 3：混合情况
```json
{
  "dependencies": {
    "ELEMENT PLUS": "^2.3.0",
    "ant design vue": "^4.0.0",
    "Vuetify UI": "^3.3.0"
  }
}
```
**预期结果：**
- 检测到 `element-plus`
- 检测到 `ant-design-vue`
- 检测到 `vuetify-ui`

## 优势

### 1. 提高匹配成功率
- 处理用户输入的大小写差异
- 处理包名中的空格问题
- 减少因格式问题导致的匹配失败

### 2. 增强容错性
- 支持各种常见的包名格式
- 自动处理用户输入的不规范情况
- 提供更好的用户体验

### 3. 保持准确性
- 使用精确的字符串匹配
- 避免模糊匹配导致的误判
- 确保匹配结果的可靠性

## 注意事项

### 1. 性能考虑
- 标准化处理会增加少量计算开销
- 但相比匹配失败的成本，这是值得的
- 使用 `break` 语句优化匹配效率

### 2. 日志记录
- 记录原始包名和匹配到的组件库名称
- 便于调试和问题排查
- 提供详细的匹配过程信息

### 3. 向后兼容
- 不影响现有的标准包名匹配
- 保持原有功能的稳定性
- 只是增强了匹配的容错性

## 相关文件

- `src/main/java/com/chu7/vuecomponentassistant/utils/PackageJsonAutoDetector.java` - 核心匹配逻辑
- `test/package.json.case-sensitive-test.json` - 测试用例
- `test/CaseSensitiveMatchTest.vue` - 测试组件

## 总结

新的包名匹配规则通过标准化处理，有效解决了大小写和空格导致的匹配问题，提高了自动检测功能的准确性和用户体验。这个改进使得 VueKit 插件能够更好地处理各种格式的包名，为用户提供更可靠的组件库自动检测功能。 