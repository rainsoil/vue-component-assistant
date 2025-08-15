<template>
  <div class="component-library-loading-debug">
    <h2>🔍 组件库加载调试</h2>
    
    <div class="debug-info">
      <h3>🐛 调试信息</h3>
      <p>此文件用于调试组件库加载问题。</p>
      <p>请查看 IntelliJ IDEA 的日志输出，应该能看到详细的加载信息。</p>
    </div>
    
    <div class="test-instructions">
      <h3>📋 测试步骤</h3>
      <ol>
        <li>打开此文件</li>
        <li>在 IntelliJ IDEA 中查看日志输出（View → Tool Windows → Event Log）</li>
        <li>查找包含 "开始加载组件数据" 的日志条目</li>
        <li>检查组件库加载的详细过程</li>
      </ol>
    </div>
    
    <div class="expected-log-output">
      <h3>📝 期望的日志输出</h3>
      <pre>
=== 开始加载组件数据 ===
检测到的组件库类型: Element UI
组件库ID: element-ui
尝试从本地缓存加载组件库
本地缓存管理器返回的组件库数量: X
检查本地缓存组件库: element-ui vs element-ui
✅ 找到本地缓存的组件库: Element UI
从本地缓存成功加载 X 个组件
      </pre>
    </div>
    
    <div class="troubleshooting">
      <h3>🔧 故障排除</h3>
      <ul>
        <li><strong>如果显示 "本地缓存中未找到匹配的组件库"</strong>：检查下载的组件库是否正确保存</li>
        <li><strong>如果显示 "远程管理器中未找到匹配的组件库"</strong>：检查组件库管理器的状态</li>
        <li><strong>如果显示 "使用内置资源作为备用"</strong>：说明下载的组件库没有被正确识别</li>
        <li><strong>如果组件库数量为 0</strong>：检查组件库管理器的缓存目录</li>
      </ul>
    </div>
    
    <div class="debug-actions">
      <h3>🧪 调试操作</h3>
      <p>在下面的位置输入组件标签来触发组件库加载：</p>
      <div class="completion-test">
        <!-- 在这里输入 <el- 来触发组件库加载 -->
        <!-- 应该能看到详细的加载日志 -->
      </div>
    </div>
    
    <div class="cache-directory-info">
      <h3>📁 缓存目录信息</h3>
      <p>组件库缓存通常保存在以下位置：</p>
      <ul>
        <li><strong>Windows</strong>: %USERPROFILE%\.vuekit\cache\</li>
        <li><strong>macOS</strong>: ~/.vuekit/cache/</li>
        <li><strong>Linux</strong>: ~/.vuekit/cache/</li>
      </ul>
      <p>检查该目录下是否有 <code>component-library-element-ui.json</code> 文件</p>
    </div>
  </div>
</template>

<script>
export default {
  name: 'ComponentLibraryLoadingDebug',
  mounted() {
    // 触发组件库加载
    console.log('组件库加载调试页面已加载');
    console.log('请查看 IntelliJ IDEA 的日志输出');
  }
}
</script>

<style scoped>
.component-library-loading-debug {
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
  font-size: 12px;
  overflow-x: auto;
}

.troubleshooting {
  background: linear-gradient(135deg, #fff3cd, #ffeaa7);
  padding: 15px;
  border-radius: 8px;
  margin-bottom: 20px;
  border-left: 4px solid #ffc107;
}

.debug-actions {
  background: linear-gradient(135deg, #d4edda, #c3e6cb);
  padding: 15px;
  border-radius: 8px;
  margin-bottom: 20px;
  border-left: 4px solid #28a745;
}

.completion-test {
  background: #fff;
  border: 2px dashed #dee2e6;
  border-radius: 4px;
  padding: 20px;
  margin-top: 10px;
  min-height: 60px;
  color: #6c757d;
  font-style: italic;
}

.cache-directory-info {
  background: linear-gradient(135deg, #e2e3e5, #d6d8db);
  padding: 15px;
  border-radius: 8px;
  border-left: 4px solid #6c757d;
}

.cache-directory-info code {
  background: #f8f9fa;
  padding: 2px 4px;
  border-radius: 3px;
  font-family: 'Courier New', monospace;
}
</style>
