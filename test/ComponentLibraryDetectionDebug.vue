<template>
  <div class="component-library-detection-debug">
    <h2>🔍 组件库检测调试</h2>
    
    <div class="debug-info">
      <h3>🐛 调试信息</h3>
      <p>此文件用于调试组件库检测问题。</p>
      <p>请查看 IntelliJ IDEA 的日志输出，应该能看到详细的检测信息。</p>
    </div>
    
    <div class="test-instructions">
      <h3>📋 测试步骤</h3>
      <ol>
        <li>打开此文件</li>
        <li>在 IntelliJ IDEA 中查看日志输出（View → Tool Windows → Event Log）</li>
        <li>查找包含 "组件库检测详情" 的日志条目</li>
        <li>检查检测到的组件库类型是否正确</li>
      </ol>
    </div>
    
    <div class="expected-log-output">
      <h3>📝 期望的日志输出</h3>
      <pre>
=== 组件库检测详情 ===
检测结果: Element UI
枚举名称: ELEMENT_UI
包名: element-ui
数据路径: /data/element-ui-components.json
========================
      </pre>
    </div>
    
    <div class="troubleshooting">
      <h3>🔧 故障排除</h3>
      <ul>
        <li><strong>如果显示 UNKNOWN</strong>：检查 package.json 中是否有 element-ui 依赖</li>
        <li><strong>如果显示 Element Plus</strong>：检查 package.json 中是否有 element-plus 依赖</li>
        <li><strong>如果没有日志输出</strong>：检查插件是否正确加载</li>
      </ul>
    </div>
    
    <div class="package-json-example">
      <h3>📦 package.json 示例</h3>
      <p>确保您的 package.json 包含正确的依赖：</p>
      <pre>
{
  "dependencies": {
    "element-ui": "^2.15.0"
  }
}
      </pre>
    </div>
  </div>
</template>

<script>
export default {
  name: 'ComponentLibraryDetectionDebug',
  mounted() {
    // 触发组件库检测
    console.log('组件库检测调试页面已加载');
    console.log('请查看 IntelliJ IDEA 的日志输出');
  }
}
</script>

<style scoped>
.component-library-detection-debug {
  padding: 20px;
  font-family: 'Microsoft YaHei', sans-serif;
}

.debug-info {
  background: linear-gradient(135deg, #f8d7da, #f5c6cb);
  padding: 15px;
  border-radius: 8px;
  margin-bottom: 20px;
  border-left: 4px solid #dc3545;
}

.test-instructions {
  background: linear-gradient(135deg, #d1ecf1, #bee5eb);
  padding: 15px;
  border-radius: 8px;
  margin-bottom: 20px;
  border-left: 4px solid #17a2b8;
}

.expected-log-output {
  background: #f8f9fa;
  padding: 15px;
  border-radius: 8px;
  margin-bottom: 20px;
  border-left: 4px solid #6c757d;
}

.expected-log-output pre {
  background: #e9ecef;
  padding: 10px;
  border-radius: 4px;
  margin: 0;
  font-family: 'Courier New', monospace;
}

.troubleshooting {
  background: linear-gradient(135deg, #fff3cd, #ffeaa7);
  padding: 15px;
  border-radius: 8px;
  margin-bottom: 20px;
  border-left: 4px solid #ffc107;
}

.package-json-example {
  background: linear-gradient(135deg, #d4edda, #c3e6cb);
  padding: 15px;
  border-radius: 8px;
  border-left: 4px solid #28a745;
}

.package-json-example pre {
  background: #e9ecef;
  padding: 10px;
  border-radius: 4px;
  margin: 0;
  font-family: 'Courier New', monospace;
}
</style>
