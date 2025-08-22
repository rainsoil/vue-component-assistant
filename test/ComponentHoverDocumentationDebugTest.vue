<template>
  <div class="component-hover-debug-test">
    <h2>🔧 组件悬停文档调试测试</h2>
    
    <div class="test-info">
      <h3>🎯 测试说明</h3>
      <p>此文件用于调试组件悬停文档功能。</p>
      <p><strong>测试方法：</strong></p>
      <ol>
        <li>鼠标悬停在下面的 <code>&lt;el-button&gt;</code> 标签名上（不是属性上）</li>
        <li>查看是否显示组件的悬停文档</li>
        <li>检查 IDEA 的 Event Log 查看日志输出</li>
      </ol>
    </div>
    
    <div class="component-test">
      <h3>🧪 组件悬停测试</h3>
      
      <!-- 测试1: 基础组件 - 鼠标悬停在 el-button 标签名上 -->
      <div class="test-section">
        <h4>测试1: 悬停在组件标签名上</h4>
        <el-button type="primary">鼠标悬停在 el-button 标签名上</el-button>
      </div>
      
      <!-- 测试2: 带属性的组件 -->
      <div class="test-section">
        <h4>测试2: 带属性的组件</h4>
        <el-input placeholder="鼠标悬停在 el-input 标签名上" clearable></el-input>
      </div>
      
      <!-- 测试3: 表格组件 -->
      <div class="test-section">
        <h4>测试3: 复杂组件</h4>
        <el-table :data="[]" border>
          <el-table-column prop="name" label="姓名"></el-table-column>
        </el-table>
      </div>
      
      <!-- 测试4: 属性悬停（这个应该工作） -->
      <div class="test-section">
        <h4>测试4: 属性悬停（应该工作）</h4>
        <el-button type="primary" size="large">鼠标悬停在 type 或 size 属性上</el-button>
      </div>
    </div>
    
    <div class="debug-info">
      <h3>🐛 调试信息</h3>
      <p>如果组件悬停文档不工作，请检查：</p>
      <ul>
        <li><strong>Event Log</strong>: 查看是否有 "ComponentDocumentationProvider.generateDoc 被调用" 日志</li>
        <li><strong>元素类型</strong>: 查看日志中的 "element type" 和 "element full class name"</li>
        <li><strong>组件检测</strong>: 查看是否正确检测到 el-button 等组件名</li>
        <li><strong>文档生成</strong>: 查看 generateComponentDocument 是否被调用</li>
      </ul>
      
      <h4>🔍 预期日志输出：</h4>
      <pre>
=== ComponentDocumentationProvider.generateDoc 被调用 ===
element = XmlTag (或 HtmlTag)
generateDoc: XmlTag detected, tagName = el-button
generateComponentDocument: called for component: el-button
generateComponentDocument: found component: el-button
generateComponentHtml: called for component: el-button
      </pre>
    </div>
  </div>
</template>

<script>
export default {
  name: 'ComponentHoverDocumentationDebugTest',
  data() {
    return {
      tableData: [
        { name: '张三', age: 25 },
        { name: '李四', age: 30 }
      ]
    }
  }
}
</script>

<style scoped>
.component-hover-debug-test {
  padding: 20px;
  font-family: 'Microsoft YaHei', sans-serif;
}

.test-info {
  background: linear-gradient(135deg, #f0f9ff, #e0f2fe);
  padding: 15px;
  border-radius: 8px;
  margin-bottom: 20px;
  border-left: 4px solid #409EFF;
}

.test-section {
  margin: 15px 0;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 5px;
}

.debug-info {
  background: #fffbf0;
  padding: 15px;
  border-radius: 8px;
  margin-top: 20px;
  border-left: 4px solid #e6a23c;
}

pre {
  background: #f5f5f5;
  padding: 10px;
  border-radius: 5px;
  font-size: 12px;
  overflow-x: auto;
}

code {
  background: #f5f5f5;
  padding: 2px 4px;
  border-radius: 3px;
  font-family: 'Courier New', monospace;
}
</style>
