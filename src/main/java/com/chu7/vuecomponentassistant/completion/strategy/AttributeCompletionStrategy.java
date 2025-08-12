package com.chu7.vuecomponentassistant.completion.strategy;

import com.chu7.vuecomponentassistant.completion.CompletionContext;
import com.chu7.vuecomponentassistant.completion.ComponentProvider;
import com.chu7.vuecomponentassistant.remote.model.ComponentInfo;
import com.chu7.vuecomponentassistant.settings.PluginSettings;
import com.chu7.vuecomponentassistant.utils.VueKitLogger;
import com.chu7.vuecomponentassistant.constants.VueKitConstants;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

/**
 * 属性补全策略
 * 
 * 负责处理Vue组件属性的智能补全，包括：
 * - 组件属性补全
 * - 属性类型提示
 * - 属性默认值提示
 * - 必需属性标识
 * - 属性描述信息
 * 
 * @author VueKit Team
 * @version 2.0.0
 */
public class AttributeCompletionStrategy implements CompletionStrategy {
    
    private static final Logger LOG = VueKitLogger.getLogger(AttributeCompletionStrategy.class);
    
    @NotNull
    @Override
    public CompletionContext.CompletionType getSupportedType() {
        return CompletionContext.CompletionType.ATTRIBUTE;
    }
    
    @Override
    public boolean canHandle(@NotNull CompletionContext context, 
                           @NotNull Project project,
                           @NotNull PsiFile file, 
                           @NotNull PsiElement element) {
        
        // 检查是否启用属性补全
        PluginSettings settings = PluginSettings.getInstance();
        if (!settings.isEnableAttributeCompletion()) {
            VueKitLogger.debug(LOG, "属性补全已禁用");
            return false;
        }
        
        // 检查上下文类型和当前组件
        return context.getType() == CompletionContext.CompletionType.ATTRIBUTE && 
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
        
        String componentName = context.getCurrentComponent();
        String prefix = context.getPrefix();
        
        VueKitLogger.debug(LOG, "开始属性补全，组件: " + componentName + ", 前缀: " + prefix);
        
        // 获取组件信息
        ComponentInfo component = componentProvider.getComponent(componentName);
        if (component == null) {
            VueKitLogger.debug(LOG, "找不到组件: " + componentName);
            return;
        }
        
        // 添加组件属性
        addComponentAttributes(component, prefix, result);
        
        // 添加通用Vue属性
        addCommonVueAttributes(prefix, result);
        
        long duration = System.currentTimeMillis() - startTime;
        VueKitLogger.logCompletion(LOG, "属性补全", component.getProps().size(), duration);
        VueKitLogger.performanceWithThreshold(LOG, "属性补全", duration, VueKitConstants.COMPLETION_THRESHOLD_MS);
    }
    
        /**
     * 添加组件特定属性
     */
    private void addComponentAttributes(@NotNull ComponentInfo component,
                                       @Nullable String prefix,
                                       @NotNull CompletionResultSet result) {
        
        VueKitLogger.debug(LOG, "组件 " + component.getName() + " 有 " + component.getProps().size() + " 个属性");
        
        int addedCount = 0;
        
        for (ComponentInfo.ComponentProp prop : component.getProps()) {
            String propName = prop.getName();
            String propType = prop.getType();
            String propDefaultValue = prop.getDefaultValue();
            
            // 前缀过滤
            if (prefix != null && !prefix.isEmpty()) {
                String propNameLower = propName.toLowerCase();
                String prefixLower = prefix.toLowerCase();
                
                if (!propNameLower.contains(prefixLower)) {
                    VueKitLogger.debug(LOG, "属性过滤: " + propName + " 不匹配前缀 " + prefix);
                    continue;
                }
            }
            
            try {
                LookupElementBuilder propElement = createAttributeLookupElement(propName, propType, propDefaultValue, component.getName());
                result.addElement(propElement);
                addedCount++;
                
                VueKitLogger.debug(LOG, "添加属性: " + propName + " (默认值: " + propDefaultValue + ")");
                
            } catch (Exception e) {
                VueKitLogger.logAndIgnore(LOG, "创建属性补全元素失败: " + propName, e);
            }
        }
        
        VueKitLogger.debug(LOG, "添加了 " + addedCount + " 个组件属性");
    }
    
    /**
     * 添加通用Vue属性
     */
    private void addCommonVueAttributes(@Nullable String prefix,
                                      @NotNull CompletionResultSet result) {
        
        // 常见的Vue指令和属性
        String[] commonAttributes = {
            "key", "ref", "is",
            "v-if", "v-else", "v-else-if", "v-show",
            "v-for", "v-model", "v-bind", "v-on",
            "class", "style", "id"
        };
        
        int addedCount = 0;
        
        for (String attr : commonAttributes) {
            // 前缀过滤
            if (prefix != null && !prefix.isEmpty()) {
                String attrLower = attr.toLowerCase();
                String prefixLower = prefix.toLowerCase();
                
                if (!attrLower.contains(prefixLower)) {
                    continue;
                }
            }
            
            try {
                LookupElementBuilder attrElement = createCommonAttributeLookupElement(attr);
                result.addElement(attrElement);
                addedCount++;
                
            } catch (Exception e) {
                VueKitLogger.logAndIgnore(LOG, "创建通用属性补全元素失败: " + attr, e);
            }
        }
        
        VueKitLogger.debug(LOG, "添加了 " + addedCount + " 个通用Vue属性");
    }
    
    /**
     * 创建属性补全元素
     */
    private LookupElementBuilder createAttributeLookupElement(String propName, String propType, String defaultValue, String componentName) {
        String displayText = propName;
        
        // 使用组件属性的真实默认值，如果没有则使用"defaultValue"
        String actualDefaultValue = (defaultValue != null && !defaultValue.isEmpty()) ? defaultValue : "defaultValue";
        String insertText = propName + "=\"" + actualDefaultValue + "\"";
        
        // 构建描述信息
        StringBuilder description = new StringBuilder();
        description.append("属性: ").append(propName);
        
        if (propType != null && !propType.isEmpty()) {
            description.append(" (类型: ").append(propType).append(")");
        }
        
        if (defaultValue != null && !defaultValue.isEmpty()) {
            description.append(" (默认值: ").append(defaultValue).append(")");
        }
        
        description.append(" - ").append(componentName).append(" 组件");
        
        return LookupElementBuilder.create(displayText)
                .withInsertHandler((context, item) -> {
                    // 插入属性后，将光标定位到引号内
                    context.getDocument().insertString(context.getTailOffset(), "=\"" + actualDefaultValue + "\"");
                    context.getEditor().getCaretModel().moveToOffset(context.getTailOffset() + 1);
                })
                .withTypeText(description.toString())
                .withIcon(null);
    }
    
    /**
     * 创建通用属性补全元素
     */
    private LookupElementBuilder createCommonAttributeLookupElement(String attr) {
        String displayText = attr;
        String insertText = attr + "=\"defaultValue\"";
        
        String description = "Vue " + (attr.startsWith("v-") ? "指令" : "属性") + ": " + attr;
        
        return LookupElementBuilder.create(displayText)
                .withInsertHandler((context, item) -> {
                    // 所有属性都插入默认值
                    context.getDocument().insertString(context.getTailOffset(), "=\"defaultValue\"");
                    context.getEditor().getCaretModel().moveToOffset(context.getTailOffset() + 1);
                })
                .withTypeText(description)
                .withIcon(null);
    }
    
    @Override
    public int getPriority() {
        return 100; // 属性补全优先级
    }
    
    @Override
    public String getStrategyName() {
        return "AttributeCompletionStrategy";
    }
    
    @Override
    public boolean isEnabled() {
        return PluginSettings.getInstance().isEnableAttributeCompletion();
    }
}