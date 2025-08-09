package com.chu7.vuecomponentassistant.completion.strategy;

import com.chu7.vuecomponentassistant.completion.CompletionContext;
import com.chu7.vuecomponentassistant.completion.ComponentProvider;
import com.chu7.vuecomponentassistant.completion.ElementPlusComponent;
import com.chu7.vuecomponentassistant.completion.ElementPlusIcons;
import com.chu7.vuecomponentassistant.completion.ElementPlusProp;
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
        ElementPlusComponent component = componentProvider.getComponent(componentName);
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
    private void addComponentAttributes(@NotNull ElementPlusComponent component,
                                      @Nullable String prefix,
                                      @NotNull CompletionResultSet result) {
        
        VueKitLogger.debug(LOG, "组件 " + component.getName() + " 有 " + component.getProps().size() + " 个属性");
        
        int addedCount = 0;
        
        for (ElementPlusProp prop : component.getProps()) {
            // 前缀过滤
            if (prefix != null && !prefix.isEmpty()) {
                String propName = prop.getName().toLowerCase();
                String prefixLower = prefix.toLowerCase();
                
                if (!propName.contains(prefixLower)) {
                    VueKitLogger.debug(LOG, "属性过滤: " + prop.getName() + " 不匹配前缀 " + prefix);
                    continue;
                }
            }
            
            try {
                LookupElementBuilder propElement = createAttributeLookupElement(prop, component);
                result.addElement(propElement);
                addedCount++;
                
                VueKitLogger.debug(LOG, "添加属性: " + prop.getName());
                
            } catch (Exception e) {
                VueKitLogger.logAndIgnore(LOG, "创建属性补全元素失败: " + prop.getName(), e);
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
                if (!attr.toLowerCase().contains(prefix.toLowerCase())) {
                    continue;
                }
            }
            
            try {
                LookupElementBuilder element = createCommonAttributeLookupElement(attr);
                result.addElement(element);
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
    @NotNull
    private LookupElementBuilder createAttributeLookupElement(@NotNull ElementPlusProp prop,
                                                            @NotNull ElementPlusComponent component) {
        
        String propName = prop.getName();
        String propType = prop.getType();
        String propDescription = prop.getDescription();
        String defaultValue = prop.getDefaultValueAsString();
        boolean required = prop.isRequired();
        
        // 构建尾部文本（类型和描述）
        StringBuilder tailText = new StringBuilder();
        if (propType != null && !propType.isEmpty()) {
            tailText.append(" : ").append(propType);
        }
        if (propDescription != null && !propDescription.isEmpty()) {
            tailText.append(" - ").append(propDescription);
        }
        if (required) {
            tailText.append(" (必需)");
        }
        if (defaultValue != null && !defaultValue.isEmpty()) {
            tailText.append(" [默认: ").append(defaultValue).append("]");
        }
        
        // 构建插入文本
        String insertText = propName + "=\"\"";
        
        return LookupElementBuilder.create(propName)
                .withTypeText(VueKitConstants.PROPERTY_TYPE_TEXT, true)
                .withTailText(tailText.toString(), true)
                .withIcon(ElementPlusIcons.PROPERTY_ICON)
                .withBoldness(required) // 必需属性加粗显示
                .withInsertHandler((insertionContext, item) -> {
                    try {
                        // 插入属性名和等号引号
                        insertionContext.getDocument().replaceString(
                            insertionContext.getStartOffset(),
                            insertionContext.getTailOffset(),
                            insertText
                        );
                        
                        // 将光标定位到引号内
                        int newOffset = insertionContext.getStartOffset() + propName.length() + 2; // +2 for '="'
                        insertionContext.getEditor().getCaretModel().moveToOffset(newOffset);
                        
                    } catch (Exception e) {
                        VueKitLogger.logAndIgnore(LOG, "插入属性失败: " + propName, e);
                    }
                })
                .withLookupString(propName)
                .withLookupString(propName.toLowerCase());
    }
    
    /**
     * 创建通用属性补全元素
     */
    @NotNull
    private LookupElementBuilder createCommonAttributeLookupElement(@NotNull String attributeName) {
        
        String description = getCommonAttributeDescription(attributeName);
        String insertText = getCommonAttributeInsertText(attributeName);
        
        return LookupElementBuilder.create(attributeName)
                .withTypeText("Vue属性", true)
                .withTailText("  " + description, true)
                .withIcon(ElementPlusIcons.PROPERTY_ICON)
                .withInsertHandler((insertionContext, item) -> {
                    try {
                        insertionContext.getDocument().replaceString(
                            insertionContext.getStartOffset(),
                            insertionContext.getTailOffset(),
                            insertText
                        );
                        
                        // 对于需要值的属性，将光标定位到引号内
                        if (insertText.contains("=\"\"")) {
                            int newOffset = insertionContext.getStartOffset() + attributeName.length() + 2;
                            insertionContext.getEditor().getCaretModel().moveToOffset(newOffset);
                        }
                        
                    } catch (Exception e) {
                        VueKitLogger.logAndIgnore(LOG, "插入通用属性失败: " + attributeName, e);
                    }
                })
                .withLookupString(attributeName)
                .withLookupString(attributeName.toLowerCase());
    }
    
    /**
     * 获取通用属性的描述
     */
    @NotNull
    private String getCommonAttributeDescription(@NotNull String attributeName) {
        switch (attributeName) {
            case "key": return "Vue列表渲染的唯一标识";
            case "ref": return "元素或组件的引用";
            case "is": return "动态组件";
            case "v-if": return "条件渲染";
            case "v-else": return "v-if的else分支";
            case "v-else-if": return "v-if的else-if分支";
            case "v-show": return "条件显示（CSS display）";
            case "v-for": return "列表渲染";
            case "v-model": return "双向数据绑定";
            case "v-bind": return "属性绑定";
            case "v-on": return "事件监听";
            case "class": return "CSS类名";
            case "style": return "内联样式";
            case "id": return "元素ID";
            default: return "Vue属性";
        }
    }
    
    /**
     * 获取通用属性的插入文本
     */
    @NotNull
    private String getCommonAttributeInsertText(@NotNull String attributeName) {
        switch (attributeName) {
            case "v-if":
            case "v-else-if":
            case "v-show":
            case "v-for":
            case "v-model":
                return attributeName + "=\"\"";
            case "v-bind":
                return ":=\"\"";
            case "v-on":
                return "@=\"\"";
            case "key":
            case "ref":
            case "is":
            case "class":
            case "style":
            case "id":
                return attributeName + "=\"\"";
            default:
                return attributeName + "=\"\"";
        }
    }
    
    @Override
    public int getPriority() {
        return 80; // 属性补全具有较高优先级
    }
    
    @NotNull
    @Override
    public String getStrategyName() {
        return "属性补全策略";
    }
    
    @NotNull
    @Override
    public String getDescription() {
        return "提供Vue组件属性的智能补全，包括组件特定属性和通用Vue属性";
    }
    
    @Override
    public boolean isEnabled() {
        return PluginSettings.getInstance().isEnableAttributeCompletion();
    }
}