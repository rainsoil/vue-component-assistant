package com.chu7.vuecomponentassistant.documentation;

import com.chu7.vuecomponentassistant.completion.ElementPlusComponent;
import com.chu7.vuecomponentassistant.completion.ElementPlusProp;
import com.chu7.vuecomponentassistant.completion.ElementPlusEvent;
import com.chu7.vuecomponentassistant.completion.ElementPlusSlot;

import java.util.List;

/**
 * Element Plus 文档样式生成器
 * 
 * 功能说明：
 * - 统一生成 Element Plus 风格的文档样式
 * - 支持 HTML 格式（用于悬浮提示）和纯文本格式（用于右键菜单）
 * - 参照 Element Plus 官网的设计风格
 * 
 * @author Vue Component Assistant Team
 * @version 1.0.0
 */
public class DocumentationStyleGenerator {

    /**
     * 生成 HTML 格式的组件文档（用于悬浮提示）
     * 
     * @param component 组件信息
     * @return HTML 格式的文档内容
     */
    public static String generateHtmlDocumentation(ElementPlusComponent component) {
        StringBuilder html = new StringBuilder();
        
        // 添加现代化的 Element Plus 风格 CSS 样式
        html.append("<style>");
        html.append("* { box-sizing: border-box; }");
        html.append(".ep-doc { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', 'Helvetica Neue', Helvetica, Arial, sans-serif; max-width: 1300px; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); padding: 12px; border-radius: 8px; box-shadow: 0 12px 24px rgba(0,0,0,0.1); }");
        html.append(".ep-content { background: rgba(255,255,255,0.95); backdrop-filter: blur(10px); border-radius: 6px; padding: 16px; box-shadow: 0 6px 20px rgba(0,0,0,0.1); }");
        html.append(".ep-title { background: linear-gradient(135deg, #409EFF, #67C23A); -webkit-background-clip: text; -webkit-text-fill-color: transparent; background-clip: text; font-size: 18px; font-weight: 700; margin: 0 0 12px 0; padding-bottom: 8px; border-bottom: 2px solid; border-image: linear-gradient(135deg, #409EFF, #67C23A) 1; display: flex; align-items: center; gap: 6px; }");
        html.append(".ep-title::before { content: '📦'; font-size: 20px; }");
        html.append(".ep-desc { color: #606266; font-size: 12px; line-height: 1.5; margin: 0 0 16px 0; padding: 12px; background: linear-gradient(135deg, #f8f9fa, #e9ecef); border-radius: 6px; border-left: 3px solid #409EFF; position: relative; }");
        html.append(".ep-desc::before { content: '💡'; position: absolute; top: -6px; left: -6px; background: #409EFF; color: white; border-radius: 50%; width: 18px; height: 18px; display: flex; align-items: center; justify-content: center; font-size: 10px; }");
        html.append(".ep-section { margin: 16px 0; }");
        html.append(".ep-section-title { color: #303133; font-size: 14px; font-weight: 600; margin: 0 0 8px 0; display: flex; align-items: center; gap: 6px; padding: 6px 0; }");
        html.append(".ep-section-title::before { content: ''; width: 4px; height: 20px; background: linear-gradient(135deg, #409EFF, #67C23A); border-radius: 2px; }");
        html.append(".ep-table { width: 100%; border-collapse: collapse; margin: 8px 0; background: white; border-radius: 6px; overflow: hidden; box-shadow: 0 2px 8px rgba(0,0,0,0.1); }");
        html.append(".ep-table th { background: linear-gradient(135deg, #409EFF, #67C23A); color: white; font-weight: 600; font-size: 11px; padding: 8px 12px; text-align: left; border: none; }");
        html.append(".ep-table td { padding: 8px 12px; border-bottom: 1px solid #e4e7ed; font-size: 11px; line-height: 1.4; }");
        html.append(".ep-table tr:last-child td { border-bottom: none; }");
        html.append(".ep-table tr:hover { background: linear-gradient(135deg, #f8f9fa, #e9ecef); }");
        html.append(".ep-name { color: #409EFF; font-weight: 600; font-family: 'JetBrains Mono', 'Fira Code', 'Monaco', 'Menlo', monospace; font-size: 11px; background: rgba(64, 158, 255, 0.1); padding: 3px 6px; border-radius: 3px; display: inline-block; }");
        html.append(".ep-desc-text { color: #606266; font-size: 11px; line-height: 1.4; }");
        html.append(".ep-default { color: #909399; font-size: 10px; background: linear-gradient(135deg, #f5f7fa, #e4e7ed); padding: 3px 6px; border-radius: 4px; margin-left: 6px; display: inline-block; font-weight: 500; }");
        html.append(".ep-scope { color: #67C23A; font-size: 10px; background: linear-gradient(135deg, #f0f9ff, #e1f5fe); padding: 3px 6px; border-radius: 4px; margin-left: 6px; display: inline-block; font-weight: 500; }");
        html.append(".ep-link { color: #409EFF; text-decoration: none; display: inline-flex; align-items: center; gap: 4px; padding: 6px 10px; background: linear-gradient(135deg, #ecf5ff, #e1f5fe); border-radius: 6px; transition: all 0.3s; font-weight: 500; font-size: 11px; }");
        html.append(".ep-link:hover { background: linear-gradient(135deg, #409EFF, #67C23A); color: #ffffff; text-decoration: none; transform: translateY(-1px); box-shadow: 0 3px 8px rgba(64, 158, 255, 0.3); }");
        html.append(".ep-example { background: linear-gradient(135deg, #f8f9fa, #e9ecef); border: 1px solid #e4e7ed; border-radius: 6px; padding: 12px; margin: 12px 0; position: relative; }");
        html.append(".ep-example::before { content: '💻'; position: absolute; top: -6px; left: 8px; background: #409EFF; color: white; border-radius: 50%; width: 18px; height: 18px; display: flex; align-items: center; justify-content: center; font-size: 10px; }");
        html.append(".ep-example pre { margin: 0; font-family: 'JetBrains Mono', 'Fira Code', 'Monaco', 'Menlo', monospace; font-size: 10px; color: #303133; line-height: 1.4; background: rgba(255,255,255,0.8); padding: 8px; border-radius: 4px; }");
        html.append(".ep-badge { display: inline-block; padding: 2px 6px; border-radius: 8px; font-size: 9px; font-weight: 600; margin-left: 6px; text-transform: uppercase; letter-spacing: 0.3px; }");
        html.append(".ep-badge-prop { background: linear-gradient(135deg, #ecf5ff, #e1f5fe); color: #409EFF; border: 1px solid rgba(64, 158, 255, 0.2); }");
        html.append(".ep-badge-event { background: linear-gradient(135deg, #f0f9ff, #e8f5e8); color: #67C23A; border: 1px solid rgba(103, 194, 58, 0.2); }");
        html.append(".ep-badge-slot { background: linear-gradient(135deg, #fdf6ec, #fff3e0); color: #E6A23C; border: 1px solid rgba(230, 162, 60, 0.2); }");
        html.append(".ep-footer { margin-top: 12px; padding-top: 10px; border-top: 1px solid #e4e7ed; text-align: center; }");
        html.append(".ep-footer-text { color: #909399; font-size: 10px; }");
        html.append("@keyframes fadeIn { from { opacity: 0; transform: translateY(10px); } to { opacity: 1; transform: translateY(0); } }");
        html.append(".ep-doc { animation: fadeIn 0.3s ease-out; }");
        html.append("</style>");

        // 开始文档内容
        html.append("<div class='ep-doc'>");
        html.append("<div class='ep-content'>");
        
        // 组件标题
        html.append("<div class='ep-title'>").append(component.getName()).append("</div>");
        
        // 组件描述
        if (component.getDescription() != null && !component.getDescription().isEmpty()) {
            html.append("<div class='ep-desc'>").append(component.getDescription()).append("</div>");
        }

        // 属性列表
        List<ElementPlusProp> props = component.getProps();
        if (props != null && !props.isEmpty()) {
            html.append("<div class='ep-section'>");
            html.append("<div class='ep-section-title'>🔧 属性 (Attributes)</div>");
            html.append("<table class='ep-table'>");
            html.append("<thead><tr>");
            html.append("<th>参数</th>");
            html.append("<th>说明</th>");
            html.append("<th>类型</th>");
            html.append("<th>可选值</th>");
            html.append("<th>默认值</th>");
            html.append("</tr></thead>");
            html.append("<tbody>");
            for (ElementPlusProp prop : props) {
                html.append("<tr>");
                html.append("<td><span class='ep-name'>").append(prop.getName()).append("</span></td>");
                html.append("<td>").append(prop.getDescription() != null ? prop.getDescription() : "").append("</td>");
                html.append("<td>").append(prop.getType() != null ? prop.getType() : "—").append("</td>");
                html.append("<td>").append(prop.getOptions() != null ? String.join(" / ", prop.getOptions()) : "—").append("</td>");
                html.append("<td>").append(prop.getDefaultValue() != null ? prop.getDefaultValueAsString() : "—").append("</td>");
                html.append("</tr>");
            }
            html.append("</tbody></table>");
            html.append("</div>");
        }

        // 事件列表
        List<ElementPlusEvent> events = component.getEvents();
        if (events != null && !events.isEmpty()) {
            html.append("<div class='ep-section'>");
            html.append("<div class='ep-section-title'>🎯 事件 (Events)</div>");
            html.append("<table class='ep-table'>");
            html.append("<thead><tr>");
            html.append("<th>事件名称</th>");
            html.append("<th>说明</th>");
            html.append("<th>回调参数</th>");
            html.append("</tr></thead>");
            html.append("<tbody>");
            for (ElementPlusEvent event : events) {
                html.append("<tr>");
                html.append("<td><span class='ep-name'>@").append(event.getName()).append("</span></td>");
                html.append("<td>").append(event.getDescription() != null ? event.getDescription() : "").append("</td>");
                html.append("<td>").append(event.getParameters() != null ? event.getParameters() : "—").append("</td>");
                html.append("</tr>");
            }
            html.append("</tbody></table>");
            html.append("</div>");
        }

        // 插槽列表
        List<ElementPlusSlot> slots = component.getSlots();
        if (slots != null && !slots.isEmpty()) {
            html.append("<div class='ep-section'>");
            html.append("<div class='ep-section-title'>🔌 插槽 (Slots)</div>");
            html.append("<table class='ep-table'>");
            html.append("<thead><tr>");
            html.append("<th>插槽名</th>");
            html.append("<th>说明</th>");
            html.append("<th>作用域</th>");
            html.append("</tr></thead>");
            html.append("<tbody>");
            for (ElementPlusSlot slot : slots) {
                html.append("<tr>");
                html.append("<td><span class='ep-name'>#").append(slot.getName()).append("</span></td>");
                html.append("<td>").append(slot.getDescription() != null ? slot.getDescription() : "").append("</td>");
                html.append("<td>").append(slot.getScope() != null ? slot.getScope() : "—").append("</td>");
                html.append("</tr>");
            }
            html.append("</tbody></table>");
            html.append("</div>");
        }

        // 使用示例
        html.append("<div class='ep-section'>");
        html.append("<div class='ep-section-title'>💡 使用示例</div>");
        html.append("<div class='ep-example'>");
        html.append("<pre>&lt;").append(component.getName()).append("&gt;\n  &lt;!-- 组件内容 --&gt;\n&lt;/").append(component.getName()).append("&gt;</pre>");
        html.append("</div>");
        html.append("</div>");

        // 文档链接
        html.append("<div class='ep-section'>");
        html.append("<div class='ep-section-title'>📖 相关文档</div>");
        html.append("<a href='https://element-plus.org/zh-CN/component/").append(component.getName().substring(3)).append(".html' class='ep-link' target='_blank'>");
        html.append("🌐 查看官方文档");
        html.append("</a>");
        html.append("</div>");

        // 添加页脚
        html.append("<div class='ep-footer'>");
        html.append("<div class='ep-footer-text'>Element Plus Component Assistant v1.0.0</div>");
        html.append("</div>");

        html.append("</div>");
        html.append("</div>");
        
        return html.toString();
    }

    /**
     * 生成表格格式的组件文档（用于右键菜单）
     * 
     * @param component 组件信息
     * @return 表格格式的文档内容
     */
    public static String generateTextDocumentation(ElementPlusComponent component) {
        StringBuilder text = new StringBuilder();
        
        // 添加编码测试信息
        System.out.println("=== 文档生成编码测试 ===");
        System.out.println("组件名称: " + component.getName());
        System.out.println("组件描述: " + component.getDescription());
        
        // 组件标题
        text.append("📦 ").append(component.getName()).append("\n");
        text.append("=".repeat(80)).append("\n\n");
        
        // 组件描述
        if (component.getDescription() != null && !component.getDescription().isEmpty()) {
            text.append("📝 组件描述:\n");
            text.append(component.getDescription()).append("\n\n");
        }

        // 属性表格
        List<ElementPlusProp> props = component.getProps();
        if (props != null && !props.isEmpty()) {
            text.append("🔧 属性 (Attributes)\n");
            text.append("-".repeat(80)).append("\n");
            
            // 表头
            text.append(String.format("%-15s %-25s %-12s %-20s %-12s\n", 
                "参数", "说明", "类型", "可选值", "默认值"));
            text.append("-".repeat(80)).append("\n");
            
            // 表格内容
            for (ElementPlusProp prop : props) {
                String name = prop.getName() != null ? prop.getName() : "";
                String desc = prop.getDescription() != null ? prop.getDescription() : "";
                String type = prop.getType() != null ? prop.getType() : "—";
                String options = prop.getOptions() != null ? String.join(" / ", prop.getOptions()) : "—";
                String defaultValue = prop.getDefaultValue() != null ? prop.getDefaultValueAsString() : "—";
                
                // 截断过长的描述
                if (desc.length() > 23) {
                    desc = desc.substring(0, 20) + "...";
                }
                if (options.length() > 18) {
                    options = options.substring(0, 15) + "...";
                }
                
                text.append(String.format("%-15s %-25s %-12s %-20s %-12s\n", 
                    name, desc, type, options, defaultValue));
            }
            text.append("\n");
        }

        // 事件表格
        List<ElementPlusEvent> events = component.getEvents();
        if (events != null && !events.isEmpty()) {
            text.append("🎯 事件 (Events)\n");
            text.append("-".repeat(80)).append("\n");
            
            // 表头
            text.append(String.format("%-20s %-35s %-25s\n", 
                "事件名称", "说明", "回调参数"));
            text.append("-".repeat(80)).append("\n");
            
            // 表格内容
            for (ElementPlusEvent event : events) {
                String name = "@" + (event.getName() != null ? event.getName() : "");
                String desc = event.getDescription() != null ? event.getDescription() : "";
                String params = event.getParameters() != null ? event.getParameters() : "—";
                
                // 截断过长的描述
                if (desc.length() > 33) {
                    desc = desc.substring(0, 30) + "...";
                }
                if (params.length() > 23) {
                    params = params.substring(0, 20) + "...";
                }
                
                text.append(String.format("%-20s %-35s %-25s\n", 
                    name, desc, params));
            }
            text.append("\n");
        }

        // 插槽表格
        List<ElementPlusSlot> slots = component.getSlots();
        if (slots != null && !slots.isEmpty()) {
            text.append("🔌 插槽 (Slots)\n");
            text.append("-".repeat(80)).append("\n");
            
            // 表头
            text.append(String.format("%-20s %-40s %-20s\n", 
                "插槽名", "说明", "作用域"));
            text.append("-".repeat(80)).append("\n");
            
            // 表格内容
            for (ElementPlusSlot slot : slots) {
                String name = "#" + (slot.getName() != null ? slot.getName() : "");
                String desc = slot.getDescription() != null ? slot.getDescription() : "";
                String scope = slot.getScope() != null ? slot.getScope() : "—";
                
                // 截断过长的描述
                if (desc.length() > 38) {
                    desc = desc.substring(0, 35) + "...";
                }
                if (scope.length() > 18) {
                    scope = scope.substring(0, 15) + "...";
                }
                
                text.append(String.format("%-20s %-40s %-20s\n", 
                    name, desc, scope));
            }
            text.append("\n");
        }

        // 使用示例
        text.append("💡 使用示例:\n");
        text.append("-".repeat(80)).append("\n");
        text.append("<").append(component.getName()).append(">\n");
        text.append("  <!-- 组件内容 -->\n");
        text.append("</").append(component.getName()).append(">\n\n");

        // 文档链接
        text.append("📖 相关文档:\n");
        text.append("-".repeat(80)).append("\n");
        text.append("官方文档: https://element-plus.org/zh-CN/component/")
           .append(component.getName().substring(3)).append(".html\n");

        return text.toString();
    }
}
