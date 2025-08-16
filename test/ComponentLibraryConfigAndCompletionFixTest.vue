<!--
测试文件：ComponentLibraryConfigAndCompletionFixTest.vue
用途：验证组件库配置和组件提示的修复
-->

<template>
  <div class="debug-container">
    <h2>组件库配置和组件提示修复验证</h2>
    
    <!-- 问题分析 -->
    <div class="problem-analysis">
      <h3>问题分析：</h3>
      <div class="problem-details">
        <h4>问题1：组件库配置页面显示默认组件</h4>
        <ul>
          <li>当删除所有下载的组件库后，配置页面仍然显示默认组件</li>
          <li>显示 Element Plus、Ant Design Vue、Vuetify 等示例组件</li>
          <li>这些组件不应该显示，因为没有实际安装</li>
        </ul>
        
        <h4>问题2：组件提示与选中状态不关联</h4>
        <ul>
          <li>即使没有选中任何组件库，组件提示仍然工作</li>
          <li>提示的组件库与配置页面选中的组件库没有关联</li>
          <li>应该只提示被选中的组件库的组件</li>
        </ul>
        
        <h4>根本原因：</h4>
        <ul>
          <li><strong>默认组件显示</strong>：<code>getInstalledLibraries</code> 方法在没有组件时返回示例数据</li>
          <li><strong>组件提示过滤</strong>：<code>getEnabledLibrariesForProject</code> 方法在配置失败时返回默认组件库</li>
        </ul>
      </div>
    </div>
    
    <!-- 修复内容 -->
    <div class="fix-content">
      <h3>修复内容：</h3>
      <pre><code>// 1. 修复 getInstalledLibraries 方法
private List&lt;ComponentLibrary&gt; getInstalledLibraries() {
    try {
        Project currentProject = getCurrentProject();
        if (currentProject != null) {
            ComponentLibraryManager libraryManager = new ComponentLibraryManager();
            List&lt;ComponentLibrary&gt; libraries = libraryManager.getAllLibraries();
            if (libraries != null && !libraries.isEmpty()) {
                return libraries;
            } else {
                System.out.println("没有找到已安装的组件库，返回空列表");
            }
        }
    } catch (Exception e) {
        System.err.println("获取已安装组件库失败: " + e.getMessage());
    }
    // 返回空列表（不显示默认组件）
    return new ArrayList&lt;&gt;();
}

// 2. 修复 getEnabledLibrariesForProject 方法
private Set&lt;ComponentLibraryDetector.LibraryType&gt; getEnabledLibrariesForProject() {
    try {
        ComponentLibraryConfigManager configManager = ComponentLibraryConfigManager.getInstance(project);
        Set&lt;ComponentLibraryDetector.LibraryType&gt; enabledLibraries = configManager.getEnabledLibraries(project);
        
        VueKitLogger.debug(LOG, "获取到项目启用的组件库: " + 
            (enabledLibraries.isEmpty() ? "无" : enabledLibraries.stream()
                .map(ComponentLibraryDetector.LibraryType::getDisplayName)
                .collect(java.util.stream.Collectors.joining(", "))));
        
        return enabledLibraries;
    } catch (Exception e) {
        VueKitLogger.error(LOG, "获取项目启用的组件库配置失败", e);
        // 如果获取失败，返回空集合（不提供任何组件）
        return new HashSet&lt;&gt;();
    }
}</code></pre>
    </div>
    
    <!-- 测试步骤 -->
    <div class="test-steps">
      <h3>测试步骤：</h3>
      <ol>
        <li>重新编译项目</li>
        <li>打开 Settings > Tools > VueKit > 组件库管理</li>
        <li>删除所有已下载的组件库</li>
        <li>打开 Settings > Tools > VueKit > 组件库配置</li>
        <li>验证配置页面不显示任何组件库（应该显示"暂无已安装的组件库"）</li>
        <li>在 Vue 文件中测试组件提示，确认没有组件被提示</li>
        <li>在组件库管理中重新下载一个组件库</li>
        <li>在组件库配置中选中该组件库</li>
        <li>测试组件提示，确认只有选中的组件库提供提示</li>
        <li>取消选中组件库，再次测试组件提示</li>
      </ol>
    </div>
    
    <!-- 预期结果 -->
    <div class="expected-results">
      <h3>预期结果：</h3>
      <ul>
        <li>✅ 删除所有组件库后，配置页面不显示默认组件</li>
        <li>✅ 配置页面显示"暂无已安装的组件库"或空列表</li>
        <li>✅ 没有选中组件库时，组件提示不工作</li>
        <li>✅ 只有选中的组件库提供组件提示</li>
        <li>✅ 取消选中后，组件提示立即停止工作</li>
        <li>✅ 日志显示正确的组件库状态</li>
      </ul>
    </div>
    
    <!-- 调试信息 -->
    <div class="debug-info">
      <h3>调试信息：</h3>
      <div class="debug-details">
        <h4>配置页面日志（无组件库时）：</h4>
        <pre><code>获取到已安装组件库数量: 0
没有找到已安装的组件库，返回空列表
返回空组件库列表</code></pre>
        
        <h4>组件提示日志（无选中组件库时）：</h4>
        <pre><code>获取到项目启用的组件库: 无
开始获取所有可用组件...
跳过内置组件库组件 (类型: Element Plus 未启用)
添加自定义组件库组件: 0 个
添加官方组件库组件: 0 个
总共获取到 0 个组件 (内置: 0, 自定义: 0, 官方: 0)</code></pre>
        
        <h4>组件提示日志（有选中组件库时）：</h4>
        <pre><code>获取到项目启用的组件库: Element Plus
开始获取所有可用组件...
添加内置组件库组件: 85 个 (类型: Element Plus)
添加自定义组件库组件: 0 个
添加官方组件库组件: 0 个
总共获取到 85 个组件 (内置: 85, 自定义: 0, 官方: 0)</code></pre>
        
        <h4>不应该出现的日志：</h4>
        <ul>
          <li>❌ "返回示例组件库数据"</li>
          <li>❌ "将显示示例数据"</li>
          <li>❌ "返回默认启用的组件库"</li>
          <li>❌ 显示默认的 Element Plus、Ant Design Vue、Vuetify 组件</li>
        </ul>
      </div>
    </div>
    
    <!-- 验证方法 -->
    <div class="verification">
      <h3>验证方法：</h3>
      <ol>
        <li>检查组件库配置页面的显示内容</li>
        <li>在 Vue 文件中输入 <code>&lt;el-</code> 测试组件提示</li>
        <li>检查控制台日志确认组件库状态</li>
        <li>验证组件提示的数量与选中的组件库一致</li>
        <li>测试不同组件库的切换</li>
        <li>验证空配置时的行为</li>
      </ol>
    </div>
    
    <!-- 常见问题 -->
    <div class="common-issues">
      <h3>常见问题排查：</h3>
      <ul>
        <li><strong>问题1</strong>：仍然显示默认组件
          <ul>
            <li>检查 <code>getInstalledLibraries</code> 方法是否已修复</li>
            <li>确认 <code>getSampleLibraries</code> 方法已被删除</li>
            <li>检查组件库管理器的 <code>getAllLibraries</code> 方法</li>
          </ul>
        </li>
        <li><strong>问题2</strong>：组件提示仍然工作
          <ul>
            <li>检查 <code>getEnabledLibrariesForProject</code> 方法</li>
            <li>确认配置管理器返回正确的状态</li>
            <li>验证组件过滤逻辑</li>
          </ul>
        </li>
        <li><strong>问题3</strong>：配置状态不一致
          <ul>
            <li>检查配置保存和加载逻辑</li>
            <li>确认项目配置正确同步</li>
            <li>验证缓存机制</li>
          </ul>
        </li>
      </ul>
    </div>
  </div>
</template>

<script>
export default {
  name: 'ComponentLibraryConfigAndCompletionFixTest',
  data() {
    return {
      testResults: {
        noDefaultComponents: false,
        completionFiltered: false,
        stateConsistent: false,
        correctLogging: false
      }
    }
  },
  methods: {
    // 测试方法
    testNoDefaultComponents() {
      console.log('测试不显示默认组件');
    },
    testCompletionFiltering() {
      console.log('测试组件提示过滤');
    },
    testStateConsistency() {
      console.log('测试状态一致性');
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