package com.chu7.vuecomponentassistant.documentation;

import com.chu7.vuecomponentassistant.remote.model.ComponentInfo;
import com.chu7.vuecomponentassistant.completion2.ElementPlusComponent;
import com.chu7.vuecomponentassistant.completion2.ElementPlusProp;
import com.chu7.vuecomponentassistant.completion2.ElementPlusEvent;
import com.chu7.vuecomponentassistant.completion2.ElementPlusSlot;
import com.chu7.vuecomponentassistant.utils.DefaultValueConverter;

import java.util.ArrayList;
import java.util.List;

/**
 * Element Plus 文档样式生成器
 *
 * <p>功能说明：</p>
 * <ul>
 *   <li>统一生成 Element Plus 风格的文档样式</li>
 *   <li>支持 HTML 格式（用于悬浮提示）和纯文本格式（用于右键菜单）</li>
 *   <li>参照 Element Plus 官网的设计风格和配色方案</li>
 *   <li>提供完整的组件属性、事件、插槽信息展示</li>
 *   <li>支持智能的文档URL生成和组件库识别</li>
 *   <li>集成现代化的CSS动画和交互效果</li>
 * </ul>
 *
 * <p>设计特点：</p>
 * <ul>
 *   <li>响应式设计：适配不同屏幕尺寸和显示环境</li>
 *   <li>现代化UI：使用Element Plus官方配色和字体</li>
 *   <li>智能布局：自动调整表格列宽和内容换行</li>
 *   <li>交互体验：支持悬停效果和动画过渡</li>
 *   <li>多格式支持：HTML和纯文本两种输出格式</li>
 *   <li>组件库适配：支持多种组件库的文档生成</li>
 * </ul>
 *
 * <p>使用场景：</p>
 * <ul>
 *   <li>IntelliJ IDEA中的组件悬浮提示文档</li>
 *   <li>右键菜单中的组件信息展示</li>
 *   <li>组件库文档的在线预览</li>
 *   <li>开发调试时的组件信息查阅</li>
 *   <li>团队协作中的组件文档共享</li>
 * </ul>
 *
 * @author VueKit Team
 * @version 1.0.0
 * @since 1.0.0
 * @see com.chu7.vuecomponentassistant.remote.model.ComponentInfo
 * @see com.chu7.vuecomponentassistant.completion2.ElementPlusComponent
 * @see com.chu7.vuecomponentassistant.utils.DefaultValueConverter
 * @see com.chu7.vuecomponentassistant.utils.DynamicLibraryInfoProvider
 */
public class DocumentationStyleGenerator {

    /**
     * 生成 HTML 格式的组件文档（用于悬浮提示）
     *
     * <p>功能说明：</p>
     * <ul>
     *   <li>生成完整的HTML格式组件文档</li>
     *   <li>包含现代化的CSS样式和动画效果</li>
     *   <li>支持组件属性、事件、插槽的表格展示</li>
     *   <li>提供使用示例和文档链接</li>
     *   <li>集成Element Plus官方设计风格</li>
     * </ul>
     *
     * <p>文档结构：</p>
     * <ul>
     *   <li>组件标题和描述</li>
     *   <li>属性表格（参数、说明、类型、可选值、默认值）</li>
     *   <li>事件表格（事件名称、说明、回调参数）</li>
     *   <li>插槽表格（插槽名、说明、作用域）</li>
     *   <li>使用示例代码</li>
     *   <li>相关文档链接</li>
     * </ul>
     *
     * @param component 组件信息对象，不能为null
     * @return HTML 格式的文档内容
     * @throws IllegalArgumentException 如果component参数为null
     * @see #generateDocumentationUrl(String)
     * @see com.chu7.vuecomponentassistant.utils.DefaultValueConverter#formatForDisplay(Object)
     */
    public static String generateHtmlDocumentation(ComponentInfo component) {
        if (component == null) {
            throw new IllegalArgumentException("组件信息对象不能为null");
        }
        
        StringBuilder html = new StringBuilder();

        // 添加现代化的 Element Plus 风格 CSS 样式，使用 IDEA 背景色
        html.append("<style>");
        html.append("* { box-sizing: border-box; }");
        html.append(".ep-doc { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', 'Helvetica Neue', Helvetica, Arial, sans-serif; max-width: 100%; background: transparent; padding: 8px; border-radius: 6px; }");
        html.append(".ep-content { background: transparent; border-radius: 4px; padding: 12px; }");
        html.append(".ep-title { color: #409EFF; font-size: 16px; font-weight: 700; margin: 0 0 10px 0; padding-bottom: 6px; border-bottom: 2px solid #409EFF; display: flex; align-items: center; gap: 6px; }");
        html.append(".ep-title::before { content: '📦'; font-size: 18px; }");
        html.append(".ep-desc { color: #606266; font-size: 11px; line-height: 1.4; margin: 0 0 12px 0; padding: 8px; background: rgba(64, 158, 255, 0.05); border-radius: 4px; border-left: 3px solid #409EFF; position: relative; }");
        html.append(".ep-desc::before { content: '💡'; position: absolute; top: -4px; left: -4px; background: #409EFF; color: white; border-radius: 50%; width: 16px; height: 16px; display: flex; align-items: center; justify-content: center; font-size: 9px; }");
        html.append(".ep-section { margin: 12px 0; }");
        html.append(".ep-section-title { color: #303133; font-size: 13px; font-weight: 600; margin: 0 0 6px 0; display: flex; align-items: center; gap: 6px; padding: 4px 0; }");
        html.append(".ep-section-title::before { content: ''; width: 3px; height: 16px; background: #409EFF; border-radius: 2px; }");
        html.append(".ep-section-subtitle { font-weight: 600; margin: 6px 0; color: #409EFF; font-size: 11px; }");
        html.append(".ep-table { width: 100%; border-collapse: collapse; margin: 6px 0; background: transparent; border-radius: 4px; overflow: hidden; }");
        html.append(".ep-table th { background: #409EFF; color: white; font-weight: 600; font-size: 10px; padding: 6px 8px; text-align: left; border: none; }");
        html.append(".ep-table td { padding: 6px 8px; font-size: 10px; line-height: 1.3; }");
                 // Attributes 表格列宽设置 - 固定宽度，允许换行
         html.append(".ep-table.attributes-table th:nth-child(1), .ep-table.attributes-table td:nth-child(1) { width: 120px; min-width: 120px; max-width: 120px; color: red !important; }"); // 参数列
         html.append(".ep-table.attributes-table th:nth-child(2), .ep-table.attributes-table td:nth-child(2) { width: 300px; min-width: 300px; word-wrap: break-word; word-break: break-all; white-space: normal; }"); // 说明列
         html.append(".ep-table.attributes-table th:nth-child(3), .ep-table.attributes-table td:nth-child(3) { width: 100px; min-width: 100px; max-width: 100px; }"); // 类型列
         html.append(".ep-table.attributes-table th:nth-child(4), .ep-table.attributes-table td:nth-child(4) { width: 120px; min-width: 120px; max-width: 120px; word-wrap: break-word; word-break: break-all; white-space: normal; }"); // 可选值列
         html.append(".ep-table.attributes-table th:nth-child(5), .ep-table.attributes-table td:nth-child(5) { width: 100px; min-width: 100px; max-width: 100px; word-wrap: break-word; word-break: break-all; white-space: normal; }"); // 默认值列

                 // Events 表格列宽设置 - 固定宽度，允许换行
         html.append(".ep-table.events-table th:nth-child(1), .ep-table.events-table td:nth-child(1) { width: 150px; min-width: 150px; max-width: 150px; }"); // 事件名称列
         html.append(".ep-table.events-table th:nth-child(2), .ep-table.events-table td:nth-child(2) { width: 350px; min-width: 350px; word-wrap: break-word; word-break: break-all; white-space: normal; }"); // 说明列
         html.append(".ep-table.events-table th:nth-child(3), .ep-table.events-table td:nth-child(3) { width: 200px; min-width: 200px; max-width: 200px; word-wrap: break-word; word-break: break-all; white-space: normal; }"); // 回调参数列
         
         // Slots 表格列宽设置 - 固定宽度，允许换行
         html.append(".ep-table.slots-table th:nth-child(1), .ep-table.slots-table td:nth-child(1) { width: 150px; min-width: 150px; max-width: 150px; }"); // 插槽名列
         html.append(".ep-table.slots-table th:nth-child(2), .ep-table.slots-table td:nth-child(2) { width: 350px; min-width: 350px; word-wrap: break-word; word-break: break-all; white-space: normal; }"); // 说明列
         html.append(".ep-table.slots-table th:nth-child(3), .ep-table.slots-table td:nth-child(3) { width: 200px; min-width: 200px; max-width: 200px; word-wrap: break-word; word-break: break-all; white-space: normal; }"); // 作用域列
        html.append(".ep-table tr:last-child td { border-bottom: none; }");
        html.append(".ep-table tr:hover { background: rgba(64, 158, 255, 0.05); }");
                 html.append(".ep-name { color: #409EFF; font-weight: 600; font-family: 'JetBrains Mono', 'Fira Code', 'Monaco', 'Menlo', monospace; font-size: 10px; background: rgba(64, 158, 255, 0.1); padding: 2px 4px; border-radius: 2px; display: inline-block; }");
         html.append(".ep-table.attributes-table td:nth-child(1) .ep-name { color: red !important; }"); // 参数列中的名称样式
        html.append(".ep-desc-text { color: #606266; font-size: 10px; line-height: 1.3; }");
        html.append(".ep-default { color: #909399; font-size: 9px; background: rgba(144, 147, 153, 0.1); padding: 2px 4px; border-radius: 2px; margin-left: 4px; display: inline-block; font-weight: 500; }");
        html.append(".ep-scope { color: #67C23A; font-size: 9px; background: rgba(103, 194, 58, 0.1); padding: 2px 4px; border-radius: 2px; margin-left: 4px; display: inline-block; font-weight: 500; }");
        html.append(".ep-link { color: #409EFF; text-decoration: none; display: inline-flex; align-items: center; gap: 4px; padding: 4px 8px; background: rgba(64, 158, 255, 0.1); border-radius: 4px; transition: all 0.3s; font-weight: 500; font-size: 10px; }");
        html.append(".ep-link:hover { background: #409EFF; color: #ffffff; text-decoration: none; }");
        html.append(".ep-example { background: rgba(64, 158, 255, 0.05); border: 1px solid #e4e7ed; border-radius: 4px; padding: 8px; margin: 8px 0; position: relative; }");
        html.append(".ep-example::before { content: '💻'; position: absolute; top: -4px; left: 6px; background: #409EFF; color: white; border-radius: 50%; width: 16px; height: 16px; display: flex; align-items: center; justify-content: center; font-size: 9px; }");
        html.append(".ep-example pre { margin: 0; font-family: 'JetBrains Mono', 'Fira Code', 'Monaco', 'Menlo', monospace; font-size: 9px; color: #303133; line-height: 1.3; background: rgba(255,255,255,0.8); padding: 6px; border-radius: 2px; }");
        html.append(".ep-badge { display: inline-block; padding: 1px 4px; border-radius: 4px; font-size: 8px; font-weight: 600; margin-left: 4px; text-transform: uppercase; letter-spacing: 0.2px; }");
        html.append(".ep-badge-prop { background: rgba(64, 158, 255, 0.1); color: #409EFF; border: 1px solid rgba(64, 158, 255, 0.2); }");
        html.append(".ep-badge-event { background: rgba(103, 194, 58, 0.1); color: #67C23A; border: 1px solid rgba(103, 194, 58, 0.2); }");
        html.append(".ep-badge-slot { background: rgba(230, 162, 60, 0.1); color: #E6A23C; border: 1px solid rgba(230, 162, 60, 0.2); }");
        html.append(".ep-footer { margin-top: 8px; padding-top: 6px; border-top: 1px solid #e4e7ed; text-align: center; }");
        html.append(".ep-footer-text { color: #909399; font-size: 9px; }");
        html.append("@keyframes fadeIn { from { opacity: 0; transform: translateY(8px); } to { opacity: 1; transform: translateY(0); } }");
        html.append(".ep-doc { animation: fadeIn 0.2s ease-out; }");
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
        List<ComponentInfo.ComponentProp> props = component.getProps();
        if (props != null && !props.isEmpty()) {
            html.append("<div class='ep-section'>");
            html.append("<div class='ep-section-title'>Attributes</div>");
            html.append("<table class='ep-table attributes-table'>");
            html.append("<thead><tr>");
            html.append("<th>参数</th>");
            html.append("<th>说明</th>");
            html.append("<th>类型</th>");
            html.append("<th>可选值</th>");
            html.append("<th>默认值</th>");
            html.append("</tr></thead>");
            html.append("<tbody>");
            for (ComponentInfo.ComponentProp prop : props) {
                html.append("<tr>");
                html.append("<td><span class='ep-name'>").append(prop.getName()).append("</span></td>");
                html.append("<td>").append(prop.getDescription() != null ? prop.getDescription() : "").append("</td>");
                html.append("<td>").append(prop.getType() != null ? prop.getType() : "").append("</td>");
                html.append("<td>").append(prop.getOptions() != null ? String.join(" / ", prop.getOptions()) : "—").append("</td>");
                html.append("<td>").append(prop.getDefaultValue() != null ? DefaultValueConverter.formatForDisplay(prop.getDefaultValue()) : "—").append("</td>");
                html.append("</tr>");
            }
            html.append("</tbody></table>");
            html.append("</div>");
        }

        // 事件列表
        List<ComponentInfo.ComponentEvent> events = component.getEvents();
        if (events != null && !events.isEmpty()) {
            html.append("<div class='ep-section'>");
            html.append("<div class='ep-section-title'>Events</div>");
            html.append("<table class='ep-table events-table'>");
            html.append("<thead><tr>");
            html.append("<th>事件名称</th>");
            html.append("<th>说明</th>");
            html.append("<th>回调参数</th>");
            html.append("</tr></thead>");
            html.append("<tbody>");
            for (ComponentInfo.ComponentEvent event : events) {
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
        List<ComponentInfo.ComponentSlot> slots = component.getSlots();
        if (slots != null && !slots.isEmpty()) {
            html.append("<div class='ep-section'>");
            html.append("<div class='ep-section-title'>Slots</div>");
            html.append("<table class='ep-table slots-table'>");
            html.append("<thead><tr>");
            html.append("<th>插槽名</th>");
            html.append("<th>说明</th>");
            html.append("<th>作用域</th>");
            html.append("</tr></thead>");
            html.append("<tbody>");
            for (ComponentInfo.ComponentSlot slot : slots) {
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
        String docUrl = generateDocumentationUrl(component.getName());
        html.append("<a href='").append(docUrl).append("' class='ep-link' target='_blank'>");
        html.append("🌐 查看官方文档");
        html.append("</a>");
        html.append("</div>");

        // 添加页脚
        html.append("<div class='ep-footer'>");
        html.append("<div class='ep-footer-text'>vuekit</div>");
        html.append("</div>");

        html.append("</div>");
        html.append("</div>");

        return html.toString();
    }

    /**
     * 生成表格格式的组件文档（用于右键菜单）
     *
     * <p>功能说明：</p>
     * <ul>
     *   <li>生成纯文本格式的组件文档</li>
     *   <li>适用于不支持HTML的环境</li>
     *   <li>提供表格化的信息展示</li>
     *   <li>支持长文本的智能截断</li>
     *   <li>包含编码测试和调试信息</li>
     * </ul>
     *
     * <p>文档结构：</p>
     * <ul>
     *   <li>组件标题和分隔线</li>
     *   <li>属性表格（固定列宽格式）</li>
     *   <li>事件表格（事件信息展示）</li>
     *   <li>插槽表格（插槽信息展示）</li>
     *   <li>使用示例代码</li>
     *   <li>相关文档链接</li>
     * </ul>
     *
     * @param component 组件信息对象，不能为null
     * @return 表格格式的文档内容
     * @throws IllegalArgumentException 如果component参数为null
     * @see #generateDocumentationUrl(String)
     * @see com.chu7.vuecomponentassistant.utils.DefaultValueConverter#formatForDisplay(Object)
     */
    public static String generateTextDocumentation(ComponentInfo component) {
        if (component == null) {
            throw new IllegalArgumentException("组件信息对象不能为null");
        }
        
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
        List<ComponentInfo.ComponentProp> props = component.getProps();
        if (props != null && !props.isEmpty()) {
            text.append("Attributes\n");
            text.append("-".repeat(80)).append("\n");

            // 表头
            text.append(String.format("%-12s %-20s %-10s %-15s %-10s\n",
                    "参数", "说明", "类型", "可选值", "默认值"));
            text.append("-".repeat(70)).append("\n");

            // 表格内容
            for (ComponentInfo.ComponentProp prop : props) {
                String name = prop.getName() != null ? prop.getName() : "";
                String desc = prop.getDescription() != null ? prop.getDescription() : "";
                String type = prop.getType() != null ? prop.getType() : "";
                String options = prop.getOptions() != null ? String.join(" / ", prop.getOptions()) : "—";
                String defaultValue = prop.getDefaultValue() != null ? DefaultValueConverter.formatForDisplay(prop.getDefaultValue()) : "—";

                // 截断过长的描述
                if (desc.length() > 18) {
                    desc = desc.substring(0, 15) + "...";
                }
                if (options.length() > 13) {
                    options = options.substring(0, 10) + "...";
                }

                text.append(String.format("%-12s %-20s %-10s %-15s %-10s\n",
                        name, desc, type, options, defaultValue));
            }
            text.append("\n");
        }

        // 事件表格
        List<ComponentInfo.ComponentEvent> events = component.getEvents();
        if (events != null && !events.isEmpty()) {
            text.append("Events\n");
            text.append("-".repeat(80)).append("\n");

            // 表头
            text.append(String.format("%-15s %-25s %-20s\n",
                    "事件名称", "说明", "回调参数"));
            text.append("-".repeat(65)).append("\n");

            // 表格内容
            for (ComponentInfo.ComponentEvent event : events) {
                String name = "@" + (event.getName() != null ? event.getName() : "");
                String desc = event.getDescription() != null ? event.getDescription() : "";
                String params = event.getParameters() != null ? event.getParameters() : "—";

                // 截断过长的描述
                if (desc.length() > 23) {
                    desc = desc.substring(0, 20) + "...";
                }
                if (params.length() > 18) {
                    params = params.substring(0, 15) + "...";
                }

                text.append(String.format("%-15s %-25s %-20s\n",
                        name, desc, params));
            }
            text.append("\n");
        }

        // 插槽表格
        List<ComponentInfo.ComponentSlot> slots = component.getSlots();
        if (slots != null && !slots.isEmpty()) {
            text.append("Slots\n");
            text.append("-".repeat(65)).append("\n");

            // 表头
            text.append(String.format("%-15s %-30s %-15s\n",
                    "插槽名", "说明", "作用域"));
            text.append("-".repeat(65)).append("\n");

            // 表格内容
            for (ComponentInfo.ComponentSlot slot : slots) {
                String name = "#" + (slot.getName() != null ? slot.getName() : "");
                String desc = slot.getDescription() != null ? slot.getDescription() : "";
                String scope = slot.getScope() != null ? slot.getScope() : "—";

                // 截断过长的描述
                if (desc.length() > 28) {
                    desc = desc.substring(0, 25) + "...";
                }
                if (scope.length() > 13) {
                    scope = scope.substring(0, 10) + "...";
                }

                text.append(String.format("%-15s %-30s %-15s\n",
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
        String docUrl = generateDocumentationUrl(component.getName());
        if (!docUrl.isEmpty()) {
            text.append("官方文档: ").append(docUrl).append("\n");
        } else {
            text.append("官方文档: 暂无可用\n");
        }

                return text.toString();
    }
    
    /**
     * 生成组件文档URL
     *
     * <p>功能说明：</p>
     * <ul>
     *   <li>智能识别组件所属的组件库</li>
     *   <li>生成对应的官方文档链接</li>
     *   <li>支持多种组件库的URL模板</li>
     *   <li>提供智能推断作为后备方案</li>
     * </ul>
     *
     * <p>生成策略：</p>
     * <ol>
     *   <li>优先从已安装的组件库中获取文档URL模板</li>
     *   <li>检查组件是否属于指定的组件库</li>
     *   <li>使用智能推断生成通用文档URL</li>
     *   <li>返回可用的文档链接或空字符串</li>
     * </ol>
     *
     * @param componentName 组件名称，不能为null或空字符串
     * @return 组件的官方文档URL，如果无法生成则返回空字符串
     * @throws IllegalArgumentException 如果componentName参数为null或空字符串
     * @see #isComponentFromLibrary(String, ComponentLibrary)
     * @see #inferDocumentationUrl(String, String)
     * @see com.chu7.vuecomponentassistant.remote.ComponentLibraryManager#getAllLibraries()
     */
    private static String generateDocumentationUrl(String componentName) {
        if (componentName == null || componentName.trim().isEmpty()) {
            throw new IllegalArgumentException("组件名称不能为null或空字符串");
        }
        
        // 优先从已安装的组件库中获取文档URL模板
        try {
            com.chu7.vuecomponentassistant.remote.ComponentLibraryManager libraryManager = 
                new com.chu7.vuecomponentassistant.remote.ComponentLibraryManager();
            java.util.List<com.chu7.vuecomponentassistant.remote.model.ComponentLibrary> installedLibraries = 
                libraryManager.getAllLibraries();
            
            for (com.chu7.vuecomponentassistant.remote.model.ComponentLibrary library : installedLibraries) {
                // 检查组件是否属于该组件库
                if (isComponentFromLibrary(componentName, library)) {
                    // 如果组件库有自定义的文档URL模板，使用它
                    // 注意：ComponentLibrary 类目前没有 getDocumentationUrlTemplate 方法
                    // 这里可以后续扩展，暂时使用智能推断
                    return inferDocumentationUrl(componentName, library.getName());
                }
            }
        } catch (Exception e) {
            // 如果获取失败，使用智能推断
        }
        
        // 使用智能推断作为后备方案
        return inferDocumentationUrl(componentName, null);
    }
    
    /**
     * 检查组件是否属于指定的组件库
     *
     * <p>功能说明：</p>
     * <ul>
     *   <li>根据组件库提供的前缀进行匹配</li>
     *   <li>支持智能推断组件库前缀</li>
     *   <li>避免硬编码的组件库识别逻辑</li>
     * </ul>
     *
     * <p>匹配规则：</p>
     * <ul>
     *   <li>优先使用组件库的componentPrefix属性</li>
     *   <li>如果前缀为空，则智能推断库名第一段的前两位 + "-"</li>
     *   <li>检查组件名称是否以推断的前缀开头</li>
     * </ul>
     *
     * @param componentName 组件名称，不能为null
     * @param library 组件库对象，不能为null
     * @return 如果组件属于指定组件库则返回true，否则返回false
     * @throws IllegalArgumentException 如果componentName或library参数为null
     * @see com.chu7.vuecomponentassistant.remote.model.ComponentLibrary#getComponentPrefix()
     * @see com.chu7.vuecomponentassistant.remote.model.ComponentLibrary#getName()
     */
    private static boolean isComponentFromLibrary(String componentName, com.chu7.vuecomponentassistant.remote.model.ComponentLibrary library) {
        if (componentName == null) {
            throw new IllegalArgumentException("组件名称不能为null");
        }
        if (library == null) {
            throw new IllegalArgumentException("组件库对象不能为null");
        }
        
        // 根据组件库提供的前缀或从名称智能推断，避免硬编码
        String prefix = library.getComponentPrefix();
        if (prefix == null || prefix.trim().isEmpty()) {
            String libName = library.getName();
            if (libName == null) return false;
            // 简单智能推断：库名第一段的前两位 + "-"
            String[] parts = libName.split("-");
            if (parts.length > 0) {
                String first = parts[0].toLowerCase();
                prefix = first.length() >= 2 ? first.substring(0, 2) + "-" : first + "-";
            }
        }
        return prefix != null && !prefix.isEmpty() && componentName.startsWith(prefix);
    }
    
    /**
     * 推断文档URL
     *
     * <p>功能说明：</p>
     * <ul>
     *   <li>智能移除组件名称中的常见前缀</li>
     *   <li>根据组件库名称推断文档基础URL</li>
     *   <li>构建完整的组件文档链接</li>
     * </ul>
     *
     * <p>推断逻辑：</p>
     * <ol>
     *   <li>移除常见的组件前缀（el-、a-、v-、q-、n-、p-）</li>
     *   <li>获取组件库的文档基础URL</li>
     *   <li>拼接组件键名和基础URL</li>
     *   <li>返回完整的文档链接</li>
     * </ol>
     *
     * @param componentName 组件名称，不能为null或空字符串
     * @param libraryName 组件库名称，可能为null
     * @return 推断的文档URL，如果无法推断则返回空字符串
     * @throws IllegalArgumentException 如果componentName参数为null或空字符串
     * @see com.chu7.vuecomponentassistant.utils.DynamicLibraryInfoProvider#getDocumentationBaseUrlFromName(String)
     */
    private static String inferDocumentationUrl(String componentName, String libraryName) {
        if (componentName == null || componentName.trim().isEmpty()) {
            throw new IllegalArgumentException("组件名称不能为null或空字符串");
        }
        
        // 移除组件前缀
        String componentKey = componentName;
        String[] prefixes = {"el-", "a-", "v-", "q-", "n-", "p-"};
        for (String prefix : prefixes) {
            if (componentName.startsWith(prefix)) {
                componentKey = componentName.substring(prefix.length());
                break;
            }
        }
        
        // 根据组件库推断文档URL
        if (libraryName != null) {
            // 优先使用组件信息中自带的 docUrl（如果未来模型支持）或构建通用 URL
            String base = com.chu7.vuecomponentassistant.utils.DynamicLibraryInfoProvider.getDocumentationBaseUrlFromName(libraryName);
            if (base != null && !base.isEmpty()) {
                return base + componentKey;
            }
        }
        
        // 默认返回空字符串
        return "";
    }
    
    /**
     * 生成 HTML 格式的组件文档（用于悬浮提示）- ElementPlusComponent 重载
     *
     * <p>功能说明：</p>
     * <ul>
     *   <li>将 ElementPlusComponent 转换为 ComponentInfo 格式</li>
     *   <li>支持 ElementPlus 组件的文档生成</li>
     *   <li>保持与 ComponentInfo 版本的一致性</li>
     * </ul>
     *
     * <p>转换过程：</p>
     * <ol>
     *   <li>创建新的 ComponentInfo 对象</li>
     *   <li>转换组件基本属性（名称、描述）</li>
     *   <li>转换属性列表（ElementPlusProp → ComponentProp）</li>
     *   <li>转换事件列表（ElementPlusEvent → ComponentEvent）</li>
     *   <li>转换插槽列表（ElementPlusSlot → ComponentSlot）</li>
     *   <li>调用原有的 generateHtmlDocumentation 方法</li>
     * </ol>
     *
     * @param component ElementPlusComponent 组件信息，不能为null
     * @return HTML 格式的文档内容
     * @throws IllegalArgumentException 如果component参数为null
     * @see #generateHtmlDocumentation(ComponentInfo)
     * @see com.chu7.vuecomponentassistant.completion2.ElementPlusComponent
     * @see com.chu7.vuecomponentassistant.completion2.ElementPlusProp
     * @see com.chu7.vuecomponentassistant.completion2.ElementPlusEvent
     * @see com.chu7.vuecomponentassistant.completion2.ElementPlusSlot
     */
    public static String generateHtmlDocumentation(ElementPlusComponent component) {
        if (component == null) {
            throw new IllegalArgumentException("ElementPlusComponent对象不能为null");
        }
        
        // 将 ElementPlusComponent 转换为 ComponentInfo 格式
        ComponentInfo componentInfo = new ComponentInfo();
        componentInfo.setName(component.getName());
        componentInfo.setDescription(component.getDescription());

        // 转换属性
        if (component.getProps() != null) {
            List<ComponentInfo.ComponentProp> props = new ArrayList<>();
            for (ElementPlusProp prop : component.getProps()) {
                ComponentInfo.ComponentProp componentProp = new ComponentInfo.ComponentProp();
                componentProp.setName(prop.getName());
                componentProp.setType(prop.getType());
                componentProp.setDescription(prop.getDescription());
                componentProp.setDefaultValue(prop.getDefaultValueAsString());
                props.add(componentProp);
            }
            componentInfo.setProps(props);
        }

        // 转换事件
        if (component.getEvents() != null) {
            List<ComponentInfo.ComponentEvent> events = new ArrayList<>();
            for (ElementPlusEvent event : component.getEvents()) {
                ComponentInfo.ComponentEvent componentEvent = new ComponentInfo.ComponentEvent();
                componentEvent.setName(event.getName());
                componentEvent.setDescription(event.getDescription());
                events.add(componentEvent);
            }
            componentInfo.setEvents(events);
        }

        // 转换插槽
        if (component.getSlots() != null) {
            List<ComponentInfo.ComponentSlot> slots = new ArrayList<>();
            for (ElementPlusSlot slot : component.getSlots()) {
                ComponentInfo.ComponentSlot componentSlot = new ComponentInfo.ComponentSlot();
                componentSlot.setName(slot.getName());
                componentSlot.setDescription(slot.getDescription());
                slots.add(componentSlot);
            }
            componentInfo.setSlots(slots);
        }

        // 调用原有的方法
        return generateHtmlDocumentation(componentInfo);
    }
}
