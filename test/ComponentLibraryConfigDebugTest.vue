<!--
测试文件：ComponentLibraryConfigDebugTest.vue
用途：调试组件库配置保存功能
-->

<template>
  <div class="debug-container">
    <h2>组件库配置保存功能调试</h2>
    
    <!-- 调试步骤 -->
    <div class="debug-steps">
      <h3>调试步骤：</h3>
      <ol>
        <li>打开 Settings > Tools > VueKit > 组件库配置</li>
        <li>查看控制台输出的调试信息</li>
        <li>选中一个组件库（如 Element Plus）</li>
        <li>点击"保存配置"按钮</li>
        <li>查看是否显示成功消息</li>
        <li>关闭配置页面</li>
        <li>重新打开配置页面</li>
        <li>查看之前选中的组件库是否仍然选中</li>
        <li>检查项目根目录是否生成了 .vuekit-libraries.json 文件</li>
      </ol>
    </div>
    
    <!-- 预期调试信息 -->
    <div class="expected-debug-info">
      <h3>预期的调试信息：</h3>
      <div class="debug-info">
        <h4>1. 页面创建时：</h4>
        <pre><code>组件库配置页面已创建，当前项目: [项目名称]
初始化配置，启用的组件库: [已启用的组件库列表]
设置 [组件库名称] 为 [选中/未选中]</code></pre>
        
        <h4>2. 保存配置时：</h4>
        <pre><code>组件库配置已保存，启用的组件库: [选中的组件库列表]
[显示成功消息对话框]</code></pre>
        
        <h4>3. 重新打开时：</h4>
        <pre><code>组件库配置页面已创建，当前项目: [项目名称]
初始化配置，启用的组件库: [之前保存的组件库列表]
设置 [组件库名称] 为 选中</code></pre>
      </div>
    </div>
    
    <!-- 配置文件格式 -->
    <div class="config-file-format">
      <h3>配置文件格式 (.vuekit-libraries.json)：</h3>
      <pre><code>{
  "projectId": "[项目ID]",
  "projectName": "[项目名称]",
  "enabledLibraryNames": [
    "ELEMENT_PLUS",
    "ANT_DESIGN_VUE"
  ]
}</code></pre>
    </div>
    
    <!-- 常见问题 -->
    <div class="common-issues">
      <h3>常见问题排查：</h3>
      <ul>
        <li><strong>问题1</strong>：配置保存后立即失效
          <ul>
            <li>检查 JSON 序列化是否正确</li>
            <li>检查文件写入权限</li>
            <li>检查项目路径是否正确</li>
          </ul>
        </li>
        <li><strong>问题2</strong>：组件库名称映射失败
          <ul>
            <li>检查 getLibraryTypeByName 方法</li>
            <li>检查组件库名称格式</li>
            <li>检查枚举值定义</li>
          </ul>
        </li>
        <li><strong>问题3</strong>：配置文件未生成
          <ul>
            <li>检查项目根目录权限</li>
            <li>检查 VirtualFile API 调用</li>
            <li>检查异常处理</li>
          </ul>
        </li>
      </ul>
    </div>
    
    <!-- 手动验证 -->
    <div class="manual-verification">
      <h3>手动验证步骤：</h3>
      <ol>
        <li>在项目根目录查找 .vuekit-libraries.json 文件</li>
        <li>检查文件内容是否正确</li>
        <li>手动修改文件内容测试加载</li>
        <li>删除文件测试默认配置</li>
      </ol>
    </div>
  </div>
</template>

<script>
export default {
  name: 'ComponentLibraryConfigDebugTest',
  data() {
    return {
      debugInfo: {
        pageCreated: false,
        configLoaded: false,
        configSaved: false,
        fileGenerated: false
      }
    }
  },
  methods: {
    // 调试方法
    debugPageCreation() {
      console.log('组件库配置页面创建调试');
    },
    debugConfigLoading() {
      console.log('组件库配置加载调试');
    },
    debugConfigSaving() {
      console.log('组件库配置保存调试');
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

.debug-steps,
.expected-debug-info,
.config-file-format,
.common-issues,
.manual-verification {
  margin-bottom: 30px;
  padding: 15px;
  border: 1px solid #ddd;
  border-radius: 5px;
  background-color: #f9f9f9;
}

.debug-steps h3,
.expected-debug-info h3,
.config-file-format h3,
.common-issues h3,
.manual-verification h3 {
  color: #333;
  margin-top: 0;
}

.debug-steps ol,
.manual-verification ol {
  margin: 10px 0;
  padding-left: 20px;
}

.debug-steps li,
.manual-verification li {
  margin: 5px 0;
  line-height: 1.4;
}

.debug-info h4 {
  color: #555;
  margin-top: 15px;
  margin-bottom: 8px;
}

.debug-info pre,
.config-file-format pre {
  background-color: #2d2d2d;
  color: #f8f8f2;
  padding: 10px;
  border-radius: 3px;
  overflow-x: auto;
  margin: 5px 0;
}

.debug-info code,
.config-file-format code {
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