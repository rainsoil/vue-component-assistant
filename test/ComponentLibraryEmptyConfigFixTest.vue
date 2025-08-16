<!--
测试文件：ComponentLibraryEmptyConfigFixTest.vue
用途：验证组件库空配置修复
-->

<template>
  <div class="debug-container">
    <h2>组件库空配置修复验证</h2>
    
    <!-- 问题分析 -->
    <div class="problem-analysis">
      <h3>问题分析：</h3>
      <div class="problem-details">
        <h4>问题现象：</h4>
        <ul>
          <li>用户没有选中任何组件库，保存时显示：<code>组件库配置已保存，启用的组件库: </code></li>
          <li>重新打开时显示所有组件都被选中：<code>初始化配置，启用的组件库: Element Plus, Ant Design Vue, Element UI</code></li>
          <li>系统回退到默认配置，而不是保持用户的选择</li>
        </ul>
        
        <h4>根本原因：</h4>
        <ul>
          <li><strong>配置回退逻辑问题</strong>：<code>getEnabledLibraries</code> 方法在项目配置为空时回退到默认配置</li>
          <li><strong>空配置处理不当</strong>：没有区分"用户明确选择不启用"和"没有配置"的情况</li>
          <li><strong>保存逻辑问题</strong>：空集合可能没有被正确保存到文件</li>
        </ul>
        
        <h4>修复方案：</h4>
        <ul>
          <li>修改 <code>getEnabledLibraries</code> 方法，如果项目配置存在就直接返回（即使是空的）</li>
          <li>修改 <code>setProjectEnabledLibraries</code> 方法，确保空集合也被保存</li>
          <li>增强日志输出，明确显示空配置的情况</li>
        </ul>
      </div>
    </div>
    
    <!-- 修复内容 -->
    <div class="fix-content">
      <h3>修复内容：</h3>
      <pre><code>// 1. 修复 getEnabledLibraries 方法
public Set&lt;ComponentLibraryDetector.LibraryType&gt; getEnabledLibraries(Project project) {
    try {
        String projectId = getProjectId(project);
        
        // 1. 检查项目级配置
        ProjectConfig projectConfig = getProjectConfig(project);
        if (projectConfig != null) {
            // 如果项目配置存在，直接返回（即使是空的，也表示用户明确选择了不启用任何组件库）
            Set&lt;ComponentLibraryDetector.LibraryType&gt; enabledLibraries = projectConfig.getEnabledLibraries();
            VueKitLogger.debug(LOG, "使用项目级配置，启用的组件库: " + 
                (enabledLibraries.isEmpty() ? "无" : enabledLibraries.stream()
                    .map(ComponentLibraryDetector.LibraryType::getDisplayName)
                    .collect(java.util.stream.Collectors.joining(", "))));
            return new HashSet&lt;&gt;(enabledLibraries);
        }
        
        // 2. 检查全局配置（只有在没有项目配置时才回退）
        // ...
    }
}

// 2. 修复 setProjectEnabledLibraries 方法
public void setProjectEnabledLibraries(Project project, Set&lt;ComponentLibraryDetector.LibraryType&gt; enabledLibraries) {
    // ...
    // 保存项目配置（即使为空集合也要保存，表示用户明确选择不启用任何组件库）
    saveProjectConfig(project, projectConfig);
    // ...
    VueKitLogger.info(LOG, "项目 " + project.getName() + " 的组件库配置已更新: " + 
        (enabledLibraries.isEmpty() ? "无" : enabledLibraries.stream()
            .map(ComponentLibraryDetector.LibraryType::getDisplayName)
            .collect(java.util.stream.Collectors.joining(", "))));
}</code></pre>
    </div>
    
    <!-- 测试步骤 -->
    <div class="test-steps">
      <h3>测试步骤：</h3>
      <ol>
        <li>重新编译项目</li>
        <li>打开 Settings > Tools > VueKit > 组件库配置</li>
        <li>确保所有组件库都未选中（取消所有勾选）</li>
        <li>点击"保存配置"按钮</li>
        <li>检查控制台日志，应该显示：<code>组件库配置已保存，启用的组件库: 无</code></li>
        <li>关闭配置页面</li>
        <li>重新打开配置页面</li>
        <li>验证所有组件库仍然保持未选中状态</li>
        <li>检查控制台日志，应该显示：<code>初始化配置，启用的组件库: 无</code></li>
        <li>检查项目根目录的 .vuekit-libraries.json 文件内容</li>
      </ol>
    </div>
    
    <!-- 预期结果 -->
    <div class="expected-results">
      <h3>预期结果：</h3>
      <ul>
        <li>✅ 空配置能够正确保存到文件</li>
        <li>✅ 空配置能够正确从文件加载</li>
        <li>✅ 重新打开时所有组件库保持未选中状态</li>
        <li>✅ 不再回退到默认配置</li>
        <li>✅ 日志显示"无"而不是空字符串</li>
        <li>✅ 配置文件包含空的 enabledLibraryNames 数组</li>
      </ul>
    </div>
    
    <!-- 调试信息 -->
    <div class="debug-info">
      <h3>调试信息：</h3>
      <div class="debug-details">
        <h4>保存时的正确日志：</h4>
        <pre><code>组件库配置已保存，启用的组件库: 无
功能设置已保存，项目: test</code></pre>
        
        <h4>加载时的正确日志：</h4>
        <pre><code>项目配置已加载: test, 启用的组件库: 无
初始化配置，启用的组件库: 无
设置 element-ui 为 未选中
设置 element-plus 为 未选中</code></pre>
        
        <h4>配置文件内容 (.vuekit-libraries.json)：</h4>
        <pre><code>{
  "projectId": "[项目ID]",
  "projectName": "test",
  "enabledLibraryNames": []
}</code></pre>
        
        <h4>不应该出现的日志：</h4>
        <ul>
          <li>❌ "初始化配置，启用的组件库: Element Plus, Ant Design Vue, Element UI"</li>
          <li>❌ "使用默认配置，启用的组件库: ..."</li>
          <li>❌ "使用全局配置，启用的组件库: ..."</li>
          <li>❌ "设置 element-ui 为 选中"</li>
          <li>❌ "设置 element-plus 为 选中"</li>
        </ul>
      </div>
    </div>
    
    <!-- 验证方法 -->
    <div class="verification">
      <h3>验证方法：</h3>
      <ol>
        <li>检查项目根目录的 <code>.vuekit-libraries.json</code> 文件</li>
        <li>确认 <code>enabledLibraryNames</code> 是空数组 <code>[]</code></li>
        <li>手动修改配置文件，添加一些组件库名称测试加载</li>
        <li>删除配置文件，测试默认行为</li>
        <li>测试组件补全功能，确认没有组件被提供</li>
        <li>选中一个组件库，保存，然后取消选中，再次保存</li>
        <li>验证状态能够正确切换</li>
      </ol>
    </div>
    
    <!-- 常见问题 -->
    <div class="common-issues">
      <h3>常见问题排查：</h3>
      <ul>
        <li><strong>问题1</strong>：仍然回退到默认配置
          <ul>
            <li>检查 <code>getProjectConfig</code> 方法是否正确加载了配置文件</li>
            <li>确认 <code>getEnabledLibraries</code> 方法的逻辑已修复</li>
            <li>检查配置文件是否被正确保存</li>
          </ul>
        </li>
        <li><strong>问题2</strong>：空配置没有被保存
          <ul>
            <li>检查 <code>setProjectEnabledLibraries</code> 方法</li>
            <li>确认空集合也被传递到 <code>saveProjectConfig</code></li>
            <li>检查文件写入权限</li>
          </ul>
        </li>
        <li><strong>问题3</strong>：日志显示不正确
          <ul>
            <li>检查日志输出逻辑</li>
            <li>确认空集合的处理方式</li>
            <li>验证字符串拼接逻辑</li>
          </ul>
        </li>
      </ul>
    </div>
  </div>
</template>

<script>
export default {
  name: 'ComponentLibraryEmptyConfigFixTest',
  data() {
    return {
      testResults: {
        emptyConfigSaved: false,
        emptyConfigLoaded: false,
        noFallbackToDefault: false,
        correctLogging: false,
        correctFileContent: false
      }
    }
  },
  methods: {
    // 测试方法
    testEmptyConfigPersistence() {
      console.log('测试空配置持久化');
    },
    testNoFallbackToDefault() {
      console.log('测试不回退到默认配置');
    },
    testCorrectLogging() {
      console.log('测试正确的日志输出');
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