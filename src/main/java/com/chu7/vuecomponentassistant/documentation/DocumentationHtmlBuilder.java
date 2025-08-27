package com.chu7.vuecomponentassistant.documentation;

import com.chu7.vuecomponentassistant.library.model.Component;
import com.chu7.vuecomponentassistant.library.model.Prop;
import com.chu7.vuecomponentassistant.library.model.Event;
import com.chu7.vuecomponentassistant.library.model.Slot;
import org.jetbrains.annotations.NotNull;

public class DocumentationHtmlBuilder {
	@NotNull
	public static String buildComponentHtml(@NotNull Component component) {
		StringBuilder html = new StringBuilder();
		html.append(style());
		html.append("<div class='ep-doc'><div class='ep-content'>");
		html.append("<div class='ep-title'>").append(escape(component.name)).append("</div>");
		if (component.description != null && !component.description.isEmpty()) {
			html.append("<div class='ep-desc'>").append(escape(component.description)).append("</div>");
		}
		if (component.props != null && !component.props.isEmpty()) {
			html.append("<div class='ep-section'>");
			html.append("<div class='ep-section-title'>Attributes</div>");
			html.append("<table class='ep-table attributes-table'><thead><tr>");
			html.append("<th>参数</th><th>说明</th><th>类型</th><th>可选值</th><th>默认值</th>");
			html.append("</tr></thead><tbody>");
			for (Prop p : component.props) {
				html.append("<tr>");
				html.append("<td><span class='ep-name'>").append(escape(p.name)).append("</span></td>");
				html.append("<td>").append(escape(nullToEmpty(p.description))).append("</td>");
				html.append("<td>").append(p.type != null ? escape(String.valueOf(p.type)) : "").append("</td>");
				html.append("<td>").append(p.options != null && !p.options.isEmpty() ? escape(String.join(" / ", p.options)) : "—").append("</td>");
				html.append("<td>").append(p.defaultValue != null ? escape(String.valueOf(p.defaultValue)) : "—").append("</td>");
				html.append("</tr>");
			}
			html.append("</tbody></table></div>");
		}
		if (component.events != null && !component.events.isEmpty()) {
			html.append("<div class='ep-section'>");
			html.append("<div class='ep-section-title'>Events</div>");
			html.append("<table class='ep-table events-table'><thead><tr>");
			html.append("<th>事件名称</th><th>说明</th><th>回调参数</th>");
			html.append("</tr></thead><tbody>");
			for (Event e : component.events) {
				html.append("<tr>");
				html.append("<td><span class='ep-name'>@").append(escape(e.name)).append("</span></td>");
				html.append("<td>").append(escape(nullToEmpty(e.description))).append("</td>");
				html.append("<td>").append(e.parameters != null ? escape(String.valueOf(e.parameters)) : "—").append("</td>");
				html.append("</tr>");
			}
			html.append("</tbody></table></div>");
		}
		if (component.slots != null && !component.slots.isEmpty()) {
			html.append("<div class='ep-section'>");
			html.append("<div class='ep-section-title'>Slots</div>");
			html.append("<table class='ep-table slots-table'><thead><tr>");
			html.append("<th>插槽名</th><th>说明</th><th>作用域</th>");
			html.append("</tr></thead><tbody>");
			for (Slot s : component.slots) {
				html.append("<tr>");
				html.append("<td><span class='ep-name'>#").append(escape(s.name)).append("</span></td>");
				html.append("<td>").append(escape(nullToEmpty(s.description))).append("</td>");
				html.append("<td>").append("—").append("</td>");
				html.append("</tr>");
			}
			html.append("</tbody></table></div>");
		}
		html.append("<div class='ep-section'><div class='ep-section-title'>💡 使用示例</div>");
		html.append("<div class='ep-example'><pre>&lt;")
			.append(escape(component.name)).append("&gt;\n  &lt;!-- 组件内容 --&gt;\n&lt;/")
			.append(escape(component.name)).append("&gt;</pre></div></div>");
		if (component.docUrl != null && !component.docUrl.isEmpty()) {
			html.append("<div class='ep-section'><div class='ep-section-title'>📖 相关文档</div>");
			html.append("<a class='ep-link' href='").append(component.docUrl).append("' target='_blank'>🌐 查看官方文档</a>");
			html.append("</div>");
		}
		html.append("</div></div>");
		return html.toString();
	}

	@NotNull
	public static String buildPropHtml(@NotNull Prop prop) {
		StringBuilder html = new StringBuilder();
		html.append(style());
		html.append("<div class='ep-doc'><div class='ep-content'>");
		html.append("<div class='ep-section'>");
		html.append("<div class='ep-section-title'>Attribute</div>");
		html.append("<table class='ep-table attributes-table'><thead><tr>");
		html.append("<th>参数</th><th>说明</th><th>类型</th><th>可选值</th><th>默认值</th>");
		html.append("</tr></thead><tbody>");
		html.append("<tr>");
		html.append("<td><span class='ep-name'>").append(escape(prop.name)).append("</span></td>");
		html.append("<td>").append(escape(nullToEmpty(prop.description))).append("</td>");
		html.append("<td>").append(prop.type != null ? escape(String.valueOf(prop.type)) : "").append("</td>");
		html.append("<td>").append(prop.options != null && !prop.options.isEmpty() ? escape(String.join(" / ", prop.options)) : "—").append("</td>");
		html.append("<td>").append(prop.defaultValue != null ? escape(String.valueOf(prop.defaultValue)) : "—").append("</td>");
		html.append("</tr>");
		html.append("</tbody></table></div>");
		html.append("</div></div>");
		return html.toString();
	}

	@NotNull
	public static String buildEventHtml(@NotNull Event event) {
		StringBuilder html = new StringBuilder();
		html.append(style());
		html.append("<div class='ep-doc'><div class='ep-content'>");
		html.append("<div class='ep-section'>");
		html.append("<div class='ep-section-title'>Event</div>");
		html.append("<table class='ep-table events-table'><thead><tr>");
		html.append("<th>事件名称</th><th>说明</th><th>回调参数</th>");
		html.append("</tr></thead><tbody>");
		html.append("<tr>");
		html.append("<td><span class='ep-name'>@").append(escape(event.name)).append("</span></td>");
		html.append("<td>").append(escape(nullToEmpty(event.description))).append("</td>");
		html.append("<td>").append(event.parameters != null ? escape(String.valueOf(event.parameters)) : "—").append("</td>");
		html.append("</tr>");
		html.append("</tbody></table></div>");
		html.append("</div></div>");
		return html.toString();
	}

	private static String style() {
		StringBuilder css = new StringBuilder();
		css.append("<style>");
		css.append("* { box-sizing: border-box; }");
		css.append(".ep-doc { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', 'Helvetica Neue', Helvetica, Arial, sans-serif; max-width: 100%; background: transparent; padding: 8px; border-radius: 6px; }");
		css.append(".ep-content { background: transparent; border-radius: 4px; padding: 12px; }");
		css.append(".ep-title { color: #409EFF; font-size: 16px; font-weight: 700; margin: 0 0 10px 0; padding-bottom: 6px; border-bottom: 2px solid #409EFF; display: flex; align-items: center; gap: 6px; }");
		css.append(".ep-title::before { content: '📦'; font-size: 18px; }");
		css.append(".ep-desc { color: #606266; font-size: 11px; line-height: 1.4; margin: 0 0 12px 0; padding: 8px; background: rgba(64, 158, 255, 0.05); border-radius: 4px; border-left: 3px solid #409EFF; position: relative; }");
		css.append(".ep-desc::before { content: '💡'; position: absolute; top: -4px; left: -4px; background: #409EFF; color: white; border-radius: 50%; width: 16px; height: 16px; display: flex; align-items: center; justify-content: center; font-size: 9px; }");
		css.append(".ep-section { margin: 12px 0; }");
		css.append(".ep-section-title { color: #303133; font-size: 13px; font-weight: 600; margin: 0 0 6px 0; display: flex; align-items: center; gap: 6px; padding: 4px 0; }");
		css.append(".ep-section-title::before { content: ''; width: 3px; height: 16px; background: #409EFF; border-radius: 2px; }");
		css.append(".ep-table { width: 100%; border-collapse: collapse; margin: 6px 0; background: transparent; border-radius: 4px; overflow: hidden; }");
		css.append(".ep-table th { background: #409EFF; color: white; font-weight: 600; font-size: 10px; padding: 6px 8px; text-align: left; border: none; }");
		css.append(".ep-table td { padding: 6px 8px; font-size: 10px; line-height: 1.3; }");
		css.append(".ep-table.attributes-table th:nth-child(1), .ep-table.attributes-table td:nth-child(1) { width: 120px; min-width: 120px; max-width: 120px; }");
		css.append(".ep-table.attributes-table th:nth-child(2), .ep-table.attributes-table td:nth-child(2) { width: 300px; min-width: 300px; word-wrap: break-word; word-break: break-all; white-space: normal; }");
		css.append(".ep-table.attributes-table th:nth-child(3), .ep-table.attributes-table td:nth-child(3) { width: 100px; min-width: 100px; max-width: 100px; }");
		css.append(".ep-table.attributes-table th:nth-child(4), .ep-table.attributes-table td:nth-child(4) { width: 120px; min-width: 120px; max-width: 120px; word-wrap: break-word; word-break: break-all; white-space: normal; }");
		css.append(".ep-table.attributes-table th:nth-child(5), .ep-table.attributes-table td:nth-child(5) { width: 100px; min-width: 100px; max-width: 100px; word-wrap: break-word; word-break: break-all; white-space: normal; }");
		css.append(".ep-table.events-table th:nth-child(1), .ep-table.events-table td:nth-child(1) { width: 150px; min-width: 150px; max-width: 150px; }");
		css.append(".ep-table.events-table th:nth-child(2), .ep-table.events-table td:nth-child(2) { width: 350px; min-width: 350px; word-wrap: break-word; word-break: break-all; white-space: normal; }");
		css.append(".ep-table.events-table th:nth-child(3), .ep-table.events-table td:nth-child(3) { width: 200px; min-width: 200px; max-width: 200px; word-wrap: break-word; word-break: break-all; white-space: normal; }");
		css.append(".ep-table.slots-table th:nth-child(1), .ep-table.slots-table td:nth-child(1) { width: 150px; min-width: 150px; max-width: 150px; }");
		css.append(".ep-table.slots-table th:nth-child(2), .ep-table.slots-table td:nth-child(2) { width: 350px; min-width: 350px; word-wrap: break-word; word-break: break-all; white-space: normal; }");
		css.append(".ep-table.slots-table th:nth-child(3), .ep-table.slots-table td:nth-child(3) { width: 200px; min-width: 200px; max-width: 200px; word-wrap: break-word; word-break: break-all; white-space: normal; }");
		css.append(".ep-table tr:hover { background: rgba(64, 158, 255, 0.05); }");
		css.append(".ep-name { color: #409EFF; font-weight: 600; font-family: 'JetBrains Mono', 'Fira Code', 'Monaco', 'Menlo', monospace; font-size: 10px; background: rgba(64, 158, 255, 0.1); padding: 2px 4px; border-radius: 2px; display: inline-block; }");
		css.append(".ep-link { color: #409EFF; text-decoration: none; display: inline-flex; align-items: center; gap: 4px; padding: 4px 8px; background: rgba(64, 158, 255, 0.1); border-radius: 4px; transition: all 0.3s; font-weight: 500; font-size: 10px; }");
		css.append(".ep-link:hover { background: #409EFF; color: #ffffff; text-decoration: none; }");
		css.append(".ep-example { background: rgba(64, 158, 255, 0.05); border: 1px solid #e4e7ed; border-radius: 4px; padding: 8px; margin: 8px 0; position: relative; }");
		css.append(".ep-example::before { content: '💻'; position: absolute; top: -4px; left: 6px; background: #409EFF; color: white; border-radius: 50%; width: 16px; height: 16px; display: flex; align-items: center; justify-content: center; font-size: 9px; }");
		css.append(".ep-example pre { margin: 0; font-family: 'JetBrains Mono', 'Fira Code', 'Monaco', 'Menlo', monospace; font-size: 9px; color: #303133; line-height: 1.3; background: rgba(255,255,255,0.8); padding: 6px; border-radius: 2px; }");
		css.append("@keyframes fadeIn { from { opacity: 0; transform: translateY(8px); } to { opacity: 1; transform: translateY(0); } }");
		css.append(".ep-doc { animation: fadeIn 0.2s ease-out; }");
		css.append("</style>");
		return css.toString();
	}

	private static String nullToEmpty(String s) { return s == null ? "" : s; }
	private static String escape(String s) {
		if (s == null) return "";
		return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
	}
} 