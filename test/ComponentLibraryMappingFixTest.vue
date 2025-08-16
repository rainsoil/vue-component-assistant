<!--
测试文件：ComponentLibraryMappingFixTest.vue
用途：验证组件库映射修复
-->

<template>
  <div class="debug-container">
    <h2>组件库映射修复验证</h2>
    
    <!-- 问题分析 -->
    <div class="problem-analysis">
      <h3>问题分析：</h3>
      <div class="problem-details">
        <h4>问题现象：</h4>
        <ul>
          <li>配置保存成功：<code>组件库配置已保存，启用的组件库: Ant Design Vue, Element Plus, Element UI</code></li>
          <li>重新加载失败：<code>未找到 element-ui 对应的 LibraryType，设置为未选中</code></li>
          <li>重新加载失败：<code>未找到 element-plus 对应的 LibraryType，设置为未选中</code></li>
        </ul>
        
        <h4>根本原因：</h4>
        <ul>
          <li>组件库实际名称：<code>element-plus</code>、<code>element-ui</code>（带连字符）</li>
          <li>映射方法期望：<code>element plus</code>、<code>element ui</code>（带空格）</li>
          <li>名称不匹配导致映射失败</li>
        </ul>
        
        <h4>修复方案：</h4>
        <ul>
          <li>在 <code>getLibraryTypeByName</code> 方法中添加连字符版本的映射</li>
          <li>支持多种命名格式：空格分隔和连字符分隔</li>
          <li>添加调试日志以便排查问题</li>
        </ul>
      </div>
    </div>
    
    <!-- 修复内容 -->
    <div class="fix-content">
      <h3>修复内容：</h3>
      <pre><code>// 修复前
switch (cleanName.toLowerCase()) {
    case "element plus":
        return ComponentLibraryDetector.LibraryType.ELEMENT_PLUS;
    case "element ui":
        return ComponentLibraryDetector.LibraryType.ELEMENT_UI;
    // ...
}

// 修复后
switch (cleanName.toLowerCase()) {
    case "element plus":
    case "element-plus":
        return ComponentLibraryDetector.LibraryType.ELEMENT_PLUS;
    case "element ui":
    case "element-ui":
        return ComponentLibraryDetector.LibraryType.ELEMENT_UI;
    case "ant design vue":
    case "ant-design-vue":
        return ComponentLibraryDetector.LibraryType.ANT_DESIGN_VUE;
    // ...
    default:
        System.out.println("未找到组件库映射: " + cleanName);
        return null;
}</code></pre>
    </div>
    
    <!-- 测试步骤 -->
    <div class="test-steps">
      <h3>测试步骤：</h3>
      <ol>
        <li>重新编译项目</li>
        <li>打开 Settings > Tools > VueKit > 组件库配置</li>
        <li>选中 Element Plus 和 Element UI 组件库</li>
        <li>点击"保存配置"按钮</li>
        <li>关闭配置页面</li>
        <li>重新打开配置页面</li>
        <li>验证 Element Plus 和 Element UI 是否保持选中状态</li>
        <li>检查控制台日志，确认不再出现"未找到对应 LibraryType"的错误</li>
      </ol>
    </div>
    
    <!-- 预期结果 -->
    <div class="expected-results">
      <h3>预期结果：</h3>
      <ul>
        <li>✅ 组件库映射正常工作</li>
        <li>✅ 配置能够正确保存</li>
        <li>✅ 配置能够正确加载</li>
        <li>✅ 选中状态能够保持</li>
        <li>✅ 不再出现映射失败的错误</li>
        <li>✅ 支持多种命名格式（空格和连字符）</li>
      </ul>
    </div>
    
    <!-- 调试信息 -->
    <div class="debug-info">
      <h3>调试信息：</h3>
      <div class="debug-details">
        <h4>保存时的日志：</h4>
        <pre><code>组件库配置已保存，启用的组件库: Element Plus, Element UI</code></pre>
        
        <h4>加载时的日志：</h4>
        <pre><code>初始化配置，启用的组件库: Element Plus, Element UI
设置 element-plus 为 选中
设置 element-ui 为 选中</code></pre>
        
        <h4>组件库名称映射：</h4>
        <table>
          <tr>
            <th>实际名称</th>
            <th>映射到</th>
          </tr>
          <tr>
            <td>element-plus</td>
            <td>ELEMENT_PLUS</td>
          </tr>
          <tr>
            <td>element-ui</td>
            <td>ELEMENT_UI</td>
          </tr>
          <tr>
            <td>ant-design-vue</td>
            <td>ANT_DESIGN_VUE</td>
          </tr>
        </table>
      </div>
    </div>
    
    <!-- 验证方法 -->
    <div class="verification">
      <h3>验证方法：</h3>
      <ol>
        <li>检查项目根目录的 <code>.vuekit-libraries.json</code> 文件</li>
        <li>确认文件内容包含正确的组件库配置</li>
        <li>手动修改配置文件测试加载</li>
        <li>测试组件补全功能是否正常工作</li>
        <li>验证只有选中的组件库提供补全</li>
      </ol>
    </div>
  </div>
</template>

<script>
export default {
  name: 'ComponentLibraryMappingFixTest',
  data() {
    return {
      testResults: {
        mappingFixed: false,
        configSaved: false,
        configLoaded: false,
        stateMaintained: false
      }
    }
  },
  methods: {
    // 测试方法
    testMappingFix() {
      console.log('测试组件库映射修复');
    },
    testConfigPersistence() {
      console.log('测试配置持久化');
    },
    testStateMaintenance() {
      console.log('测试状态保持');
    }
  }
}
</script>

<style scoped>
.debug-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
  font-family: Arial, sans-serif;
}

.problem-analysis,
.fix-content,
.test-steps,
.expected-results,
.debug-info,
.verification {
  margin-bottom: 30px;
  padding: 15px;
  border: 1px solid #ddd;
  border-radius: 5px;
  background-color: #f9f9f9;
}

.problem-analysis h3,
.fix-content h3,
.test-steps h3,
.expected-results h3,
.debug-info h3,
.verification h3 {
  color: #333;
  margin-top: 0;
}

.problem-details h4,
.debug-details h4 {
  color: #555;
  margin-top: 15px;
  margin-bottom: 8px;
}

.problem-details ul,
.debug-details ul {
  margin: 10px 0;
  padding-left: 20px;
}

.problem-details li,
.debug-details li {
  margin: 5px 0;
  line-height: 1.4;
}

.problem-details code,
.debug-details code {
  background-color: #f0f0f0;
  padding: 2px 4px;
  border-radius: 3px;
  font-family: 'Courier New', monospace;
}

.test-steps ol,
.verification ol {
  margin: 10px 0;
  padding-left: 20px;
}

.test-steps li,
.verification li {
  margin: 5px 0;
  line-height: 1.4;
}

.expected-results ul {
  margin: 10px 0;
  padding-left: 20px;
}

.expected-results li {
  margin: 5px 0;
  line-height: 1.4;
}

.expected-results li:before {
  content: "✅ ";
  color: #4caf50;
}

.fix-content pre,
.debug-details pre {
  background-color: #2d2d2d;
  color: #f8f8f2;
  padding: 10px;
  border-radius: 3px;
  overflow-x: auto;
  margin: 5px 0;
}

.fix-content code,
.debug-details code {
  font-family: 'Courier New', monospace;
  font-size: 12px;
}

.debug-details table {
  width: 100%;
  border-collapse: collapse;
  margin: 10px 0;
}

.debug-details th,
.debug-details td {
  border: 1px solid #ddd;
  padding: 8px;
  text-align: left;
}

.debug-details th {
  background-color: #f2f2f2;
  font-weight: bold;
}
</style> 