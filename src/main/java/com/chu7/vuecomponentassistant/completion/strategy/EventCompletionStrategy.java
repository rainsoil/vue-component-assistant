package com.chu7.vuecomponentassistant.completion.strategy;

import com.chu7.vuecomponentassistant.completion.CompletionContext;
import com.chu7.vuecomponentassistant.completion.ComponentProvider;
import com.chu7.vuecomponentassistant.remote.model.ComponentInfo;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 事件补全策略
 *
 * 负责处理Vue组件事件的智能补全，包括：
 * - 组件事件补全
 * - 事件类型提示
 * - 事件描述信息
 * - 事件处理函数生成
 * - 高优先级显示
 *
 * @author VueKit Team
 * @version 2.0.0
 */
public class EventCompletionStrategy implements CompletionStrategy {

    @NotNull
    @Override
    public CompletionContext.CompletionType getSupportedType() {
        return CompletionContext.CompletionType.EVENT;
    }

    @Override
    public boolean canHandle(@NotNull CompletionContext context,
                           @NotNull Project project,
                           @NotNull PsiFile file,
                           @NotNull PsiElement element) {

        // 检查上下文类型和当前组件
        return context.getType() == CompletionContext.CompletionType.EVENT &&
               context.getCurrentComponent() != null;
    }

    @Override
    public void complete(@NotNull CompletionContext context,
                        @NotNull Project project,
                        @NotNull PsiFile file,
                        @NotNull PsiElement element,
                        @NotNull ComponentProvider componentProvider,
                        @NotNull CompletionResultSet result) {

        long startTime = System.currentTimeMillis();

        try {
            String componentName = context.getCurrentComponent();
            String prefix = context.getPrefix();

            System.out.println("=== 事件补全策略开始执行 ===");
            System.out.println("组件: " + componentName + ", 前缀: " + prefix);

            // 获取组件信息
            ComponentInfo component = componentProvider.getComponent(componentName);
            if (component == null) {
                System.out.println("❌ 找不到组件: " + componentName);
                return;
            }

            System.out.println("✅ 找到组件: " + component.getName());
            System.out.println("组件描述: " + component.getDescription());

            // 获取组件事件
            List<ComponentInfo.ComponentEvent> events = componentProvider.getComponentEvents(componentName);
            if (events == null || events.isEmpty()) {
                System.out.println("❌ 组件没有事件数据");
                return;
            }

            System.out.println("✅ 找到事件数量: " + events.size());
            System.out.println("事件列表: " + events.stream().map(ComponentInfo.ComponentEvent::getName).collect(Collectors.joining(", ")));

            // 限制事件数量，避免性能问题
            List<ComponentInfo.ComponentEvent> limitedEvents = events.stream()
                    .limit(50) // 限制最多50个事件
                    .collect(Collectors.toList());

            System.out.println("✅ 限制后事件数量: " + limitedEvents.size());

            // 根据前缀过滤事件
            List<ComponentInfo.ComponentEvent> filteredEvents = limitedEvents.stream()
                    .filter(event -> prefix == null || prefix.isEmpty() ||
                            event.getName().toLowerCase().startsWith(prefix.toLowerCase()))
                    .limit(20) // 限制最终显示数量
                    .collect(Collectors.toList());

            System.out.println("✅ 过滤后事件数量: " + filteredEvents.size());
            if (prefix != null && !prefix.isEmpty()) {
                System.out.println("匹配前缀 '" + prefix + "' 的事件: " +
                    filteredEvents.stream().map(ComponentInfo.ComponentEvent::getName).collect(Collectors.joining(", ")));
            }

            // 创建事件补全选项
            for (ComponentInfo.ComponentEvent event : filteredEvents) {
                try {
                    LookupElementBuilder completion = createEventLookupElement(event, componentProvider);
                    result.addElement(completion);
                    System.out.println("✅ 添加事件补全选项: " + event.getName());
                } catch (Exception e) {
                    System.out.println("❌ 创建事件补全选项失败: " + event.getName() + ", 错误: " + e.getMessage());
                }
            }

            // 强制刷新补全结果集
            result.stopHere();

            long duration = System.currentTimeMillis() - startTime;
            System.out.println("=== 事件补全策略执行完成，耗时: " + duration + "ms ===");

        } catch (Exception e) {
            System.out.println("❌ 事件补全策略执行异常: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 创建事件查找元素
     */
    @NotNull
    private LookupElementBuilder createEventLookupElement(@NotNull ComponentInfo.ComponentEvent event,
                                                        @NotNull ComponentProvider componentProvider) {

        // 生成事件处理函数名：handle + 首字母大写的函数名
        String handlerName = "handle" + event.getName().substring(0, 1).toUpperCase() + event.getName().substring(1);

        // 创建事件补全选项，包含InsertHandler
        return LookupElementBuilder.create(event.getName())
                .withPresentableText(event.getName())
                .withTypeText("VueKit Event")
                .withTailText(" - " + (event.getDescription() != null ? event.getDescription() : "事件补全"))
                .withBoldness(true)
                .withInsertHandler((context, item) -> {
                    // 插入事件处理函数
                    Editor editor = context.getEditor();
                    int offset = context.getTailOffset();
                    editor.getDocument().insertString(offset, "=\"" + handlerName + "\"");
                    editor.getCaretModel().moveToOffset(offset + 1);
                });

    }

    @Override
    public int getPriority() {
        // 事件补全具有高优先级
        return 100;
    }

    @NotNull
    @Override
    public String getStrategyName() {
        return "EventCompletionStrategy";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "Vue组件事件智能补全策略";
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
