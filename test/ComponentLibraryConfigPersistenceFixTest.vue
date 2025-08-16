<!--
测试文件：ComponentLibraryConfigPersistenceFixTest.vue
用途：验证组件库配置持久化修复
-->

<template>
  <div class="debug-container">
    <h2>组件库配置持久化修复验证</h2>
    
    <!-- 问题分析 -->
    <div class="problem-analysis">
      <h3>问题分析：</h3>
      <div class="problem-details">
        <h4>问题现象：</h4>
        <ul>
          <li>配置保存后，重新打开时显示错误的组件库列表</li>
          <li>选中状态不一致：第一次显示未选中，重新打开时显示选中</li>
          <li>配置被意外重置为默认值</li>
          <li>显示"配置已重置为默认值"的日志</li>
        </ul>
        
        <h4>根本原因：</h4>
        <ul>
          <li><strong>配置加载问题</strong>：<code>getProjectConfig</code> 方法只从内存缓存获取，没有从文件加载</li>
          <li><strong>重置方法问题</strong>：IntelliJ 设置系统自动调用 <code>reset()</code> 方法，导致配置被重置</li>
          <li><strong>默认值问题</strong>：当项目配置为空时，回退到包含所有默认组件库的配置</li>
        </ul>
        
        <h4>修复方案：</h4>
        <ul>
          <li>修改 <code>getProjectConfig</code> 方法，自动从文件加载配置</li>
          <li>修改 <code>reset()</code> 方法，避免 IntelliJ 自动调用时重置配置</li>
          <li>添加 <code>performReset()</code> 方法，只在用户明确点击重置按钮时执行</li>
          <li>增强调试日志，便于排查问题</li>
        </ul>
      </div>
    </div>
    
    <!-- 修复内容 -->
    <div class="fix-content">
      <h3>修复内容：</h3>
      <pre><code>// 1. 修复 getProjectConfig 方法
private ProjectConfig getProjectConfig(Project project) {
    String projectId = getProjectId(project);
    
    // 如果缓存中没有，尝试从文件加载
    if (!projectConfigs.containsKey(projectId)) {
        loadProjectConfig(project);
    }
    
    return projectConfigs.get(projectId);
}

// 2. 修复 reset() 方法
@Override
public void reset() {
    // 重置为默认配置 - 只在用户明确点击重置按钮时执行
    // 这里不执行实际的重置操作，避免 IntelliJ 设置系统自动调用时重置用户配置
    System.out.println("reset() 方法被调用，但不执行重置操作以避免意外重置");
}

// 3. 添加 performReset() 方法
private void performReset() {
    // 用户明确点击重置按钮时的重置逻辑
    // 包含完整的重置操作和用户提示
}</code></pre>
    </div>
    
    <!-- 测试步骤 -->
    <div class="test-steps">
      <h3>测试步骤：</h3>
      <ol>
        <li>重新编译项目</li>
        <li>打开 Settings > Tools > VueKit > 组件库配置</li>
        <li>只选中 Element UI，不选中 Element Plus</li>
        <li>点击"保存配置"按钮</li>
        <li>关闭配置页面</li>
        <li>重新打开配置页面</li>
        <li>验证只有 Element UI 保持选中状态</li>
        <li>检查控制台日志，确认不再出现"配置已重置为默认值"</li>
        <li>检查项目根目录的 .vuekit-libraries.json 文件内容</li>
        <li>测试重置按钮功能是否正常工作</li>
      </ol>
    </div>
    
    <!-- 预期结果 -->
    <div class="expected-results">
      <h3>预期结果：</h3>
      <ul>
        <li>✅ 配置能够正确保存到文件</li>
        <li>✅ 配置能够正确从文件加载</li>
        <li>✅ 选中状态能够准确反映保存的配置</li>
        <li>✅ 不再出现意外的配置重置</li>
        <li>✅ 不再显示错误的组件库列表</li>
        <li>✅ 重置按钮功能正常工作</li>
        <li>✅ 调试日志清晰准确</li>
      </ul>
    </div>
    
    <!-- 调试信息 -->
    <div class="debug-info">
      <h3>调试信息：</h3>
      <div class="debug-details">
        <h4>保存时的日志：</h4>
        <pre><code>组件库配置已保存，启用的组件库: Element UI</code></pre>
        
        <h4>加载时的日志：</h4>
        <pre><code>项目配置已加载: test, 启用的组件库: Element UI
初始化配置，启用的组件库: Element UI
设置 element-ui 为 选中
设置 element-plus 为 未选中</code></pre>
        
        <h4>配置文件内容 (.vuekit-libraries.json)：</h4>
        <pre><code>{
  "projectId": "[项目ID]",
  "projectName": "test",
  "enabledLibraryNames": ["ELEMENT_UI"]
}</code></pre>
        
        <h4>不应该出现的日志：</h4>
        <ul>
          <li>❌ "配置已重置为默认值"</li>
          <li>❌ "初始化配置，启用的组件库: Element Plus, Element UI, Ant Design Vue"</li>
          <li>❌ "未找到对应 LibraryType"</li>
        </ul>
      </div>
    </div>
    
    <!-- 验证方法 -->
    <div class="verification">
      <h3>验证方法：</h3>
      <ol>
        <li>检查项目根目录的 <code>.vuekit-libraries.json</code> 文件</li>
        <li>确认文件内容只包含选中的组件库</li>
        <li>手动修改配置文件测试加载</li>
        <li>删除配置文件测试默认行为</li>
        <li>测试组件补全功能是否正常工作</li>
        <li>验证只有选中的组件库提供补全</li>
        <li>测试重置按钮是否正常工作</li>
      </ol>
    </div>
    
    <!-- 常见问题 -->
    <div class="common-issues">
      <h3>常见问题排查：</h3>
      <ul>
        <li><strong>问题1</strong>：配置仍然被重置
          <ul>
            <li>检查是否有其他地方调用了 <code>reset()</code> 方法</li>
            <li>确认 <code>getProjectConfig</code> 方法已修复</li>
            <li>检查文件权限和路径</li>
          </ul>
        </li>
        <li><strong>问题2</strong>：选中状态不正确
          <ul>
            <li>检查组件库名称映射是否正确</li>
            <li>确认配置文件内容正确</li>
            <li>检查 <code>getLibraryTypeByName</code> 方法</li>
          </ul>
        </li>
        <li><strong>问题3</strong>：显示错误的组件库列表
          <ul>
            <li>检查默认配置是否包含过多组件库</li>
            <li>确认项目配置加载逻辑正确</li>
            <li>检查缓存机制是否正常工作</li>
          </ul>
        </li>
      </ul>
    </div>
  </div>
</template>

<script>
export default {
  name: 'ComponentLibraryConfigPersistenceFixTest',
  data() {
    return {
      testResults: {
        configSaved: false,
        configLoaded: false,
        stateCorrect: false,
        noUnexpectedReset: false,
        resetButtonWorking: false
      }
    }
  },
  methods: {
    // 测试方法
    testConfigPersistence() {
      console.log('测试配置持久化');
    },
    testStateConsistency() {
      console.log('测试状态一致性');
    },
    testResetButton() {
      console.log('测试重置按钮');
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
.verification,
.common-issues {
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
.verification h3,
.common-issues h3 {
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

.common-issues ul {
  margin: 10px 0;
  padding-left: 20px;
}

.common-issues li {
  margin: 5px 0;
  line-height: 1.4;
}

.common-issues strong {
  color: #d32f2f;
}
</style> 