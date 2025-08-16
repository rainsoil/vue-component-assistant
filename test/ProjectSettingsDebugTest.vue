<!--
测试文件：ProjectSettingsDebugTest.vue
用途：调试项目级功能设置
-->

<template>
  <div class="debug-container">
    <h2>项目级功能设置调试</h2>
    
    <!-- 调试步骤 -->
    <div class="debug-steps">
      <h3>调试步骤：</h3>
      <ol>
        <li>打开 Settings > Tools > VueKit > 组件库配置</li>
        <li>查看功能设置面板（在组件库配置上方）</li>
        <li>修改功能设置选项</li>
        <li>点击"保存配置"按钮</li>
        <li>查看是否显示成功消息</li>
        <li>关闭配置页面</li>
        <li>重新打开配置页面</li>
        <li>查看之前修改的功能设置是否保持</li>
        <li>检查项目根目录是否生成了 .vuekit-project-settings.json 文件</li>
      </ol>
    </div>
    
    <!-- 功能设置说明 -->
    <div class="function-settings-info">
      <h3>功能设置说明：</h3>
      <div class="settings-info">
        <h4>补全功能设置：</h4>
        <ul>
          <li><strong>启用组件补全</strong>：控制是否启用组件名称补全</li>
          <li><strong>启用属性补全</strong>：控制是否启用组件属性补全</li>
          <li><strong>启用事件补全</strong>：控制是否启用组件事件补全</li>
          <li><strong>启用插槽补全</strong>：控制是否启用组件插槽补全</li>
        </ul>
        
        <h4>文档功能设置：</h4>
        <ul>
          <li><strong>启用悬停文档</strong>：控制是否启用鼠标悬停显示组件文档</li>
          <li><strong>启用右键文档</strong>：控制是否启用右键菜单显示组件文档</li>
        </ul>
        
        <h4>性能优化设置：</h4>
        <ul>
          <li><strong>启用缓存优化</strong>：控制是否启用组件数据缓存</li>
          <li><strong>启用调试模式</strong>：控制是否启用调试日志输出</li>
        </ul>
      </div>
    </div>
    
    <!-- 配置文件格式 -->
    <div class="config-file-format">
      <h3>配置文件格式 (.vuekit-project-settings.json)：</h3>
      <pre><code>{
  "projectId": "[项目ID]",
  "projectName": "[项目名称]",
  "enableComponentCompletion": true,
  "enableAttributeCompletion": true,
  "enableEventCompletion": true,
  "enableSlotCompletion": true,
  "enableHoverDocumentation": true,
  "enableRightClickDocumentation": true,
  "enableCaching": true,
  "enableDebugMode": false
}</code></pre>
    </div>
    
    <!-- 预期调试信息 -->
    <div class="expected-debug-info">
      <h3>预期的调试信息：</h3>
      <div class="debug-info">
        <h4>1. 页面创建时：</h4>
        <pre><code>组件库配置页面已创建，当前项目: [项目名称]
功能设置已初始化，项目: [项目名称]
初始化配置，启用的组件库: [已启用的组件库列表]</code></pre>
        
        <h4>2. 保存配置时：</h4>
        <pre><code>组件库配置已保存，启用的组件库: [选中的组件库列表]
功能设置已保存，项目: [项目名称]
[显示成功消息对话框]</code></pre>
        
        <h4>3. 重新打开时：</h4>
        <pre><code>组件库配置页面已创建，当前项目: [项目名称]
功能设置已初始化，项目: [项目名称]
初始化配置，启用的组件库: [之前保存的组件库列表]</code></pre>
      </div>
    </div>
    
    <!-- 常见问题 -->
    <div class="common-issues">
      <h3>常见问题排查：</h3>
      <ul>
        <li><strong>问题1</strong>：功能设置保存后立即失效
          <ul>
            <li>检查 JSON 序列化是否正确</li>
            <li>检查文件写入权限</li>
            <li>检查项目路径是否正确</li>
          </ul>
        </li>
        <li><strong>问题2</strong>：功能设置未生效
          <ul>
            <li>检查补全提供者是否正确读取设置</li>
            <li>检查文档提供者是否正确读取设置</li>
            <li>检查缓存管理器是否正确读取设置</li>
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
        <li>在项目根目录查找 .vuekit-project-settings.json 文件</li>
        <li>检查文件内容是否正确</li>
        <li>手动修改文件内容测试加载</li>
        <li>删除文件测试默认配置</li>
        <li>测试补全功能是否根据设置生效</li>
        <li>测试文档功能是否根据设置生效</li>
      </ol>
    </div>
  </div>
</template>

<script>
export default {
  name: 'ProjectSettingsDebugTest',
  data() {
    return {
      debugInfo: {
        pageCreated: false,
        settingsLoaded: false,
        settingsSaved: false,
        fileGenerated: false
      }
    }
  },
  methods: {
    // 调试方法
    debugPageCreation() {
      console.log('项目设置页面创建调试');
    },
    debugSettingsLoading() {
      console.log('项目设置加载调试');
    },
    debugSettingsSaving() {
      console.log('项目设置保存调试');
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
.function-settings-info,
.config-file-format,
.expected-debug-info,
.common-issues,
.manual-verification {
  margin-bottom: 30px;
  padding: 15px;
  border: 1px solid #ddd;
  border-radius: 5px;
  background-color: #f9f9f9;
}

.debug-steps h3,
.function-settings-info h3,
.config-file-format h3,
.expected-debug-info h3,
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

.settings-info h4 {
  color: #555;
  margin-top: 15px;
  margin-bottom: 8px;
}

.settings-info ul {
  margin: 10px 0;
  padding-left: 20px;
}

.settings-info li {
  margin: 5px 0;
  line-height: 1.4;
}

.settings-info strong {
  color: #2c3e50;
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