package bak.completion.strategy;

import com.chu7.vuecomponentassistant.completion.CompletionContext;
import com.chu7.vuecomponentassistant.completion.ComponentProvider;
import com.chu7.vuecomponentassistant.completion.ElementPlusComponent;
import com.chu7.vuecomponentassistant.completion.ElementPlusIcons;
import com.chu7.vuecomponentassistant.completion.strategy.CompletionStrategy;
import com.chu7.vuecomponentassistant.constants.VueKitConstants;
import com.chu7.vuecomponentassistant.settings.PluginSettings;
import com.chu7.vuecomponentassistant.utils.CustomComponentLibraryManager;
import com.chu7.vuecomponentassistant.utils.VueKitLogger;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * 组件补全策略
 * 
 * 负责处理Vue组件的智能补全，包括：
 * - 标准组件库组件（Element Plus、Element UI、Ant Design Vue）
 * - 自定义组件库组件
 * - 基于前缀的智能过滤
 * - 组件标签自动补全
 * 
 * @author VueKit Team
 * @version 2.0.0
 */
public class ComponentCompletionStrategy implements CompletionStrategy {
    
    private static final Logger LOG = VueKitLogger.getLogger(ComponentCompletionStrategy.class);
    
    @NotNull
    @Override
    public CompletionContext.CompletionType getSupportedType() {
        return CompletionContext.CompletionType.COMPONENT;
    }
    
    @Override
    public boolean canHandle(@NotNull CompletionContext context, 
                           @NotNull Project project,
                           @NotNull PsiFile file, 
                           @NotNull PsiElement element) {
        
        // 检查是否启用组件补全
        PluginSettings settings = PluginSettings.getInstance();
        if (!settings.isEnableComponentCompletion()) {
            VueKitLogger.debug(LOG, "组件补全已禁用");
            return false;
        }
        
        // 检查上下文类型
        return context.getType() == CompletionContext.CompletionType.COMPONENT;
    }
    
    @Override
    public void complete(@NotNull CompletionContext context,
                        @NotNull Project project,
                        @NotNull PsiFile file,
                        @NotNull PsiElement element,
                        @NotNull ComponentProvider componentProvider,
                        @NotNull CompletionResultSet result) {
        
        long startTime = System.currentTimeMillis();
        
        VueKitLogger.debug(LOG, "开始组件补全，前缀: " + context.getPrefix());
        
        // 添加标准组件库组件
        addStandardComponents(context, componentProvider, result);
        
        // 添加自定义组件库组件
        addCustomComponents(context, result);
        
        long duration = System.currentTimeMillis() - startTime;
        VueKitLogger.logCompletion(LOG, "组件补全", result.getPrefixMatcher().getPrefix().length(), duration);
        VueKitLogger.performanceWithThreshold(LOG, "组件补全", duration, VueKitConstants.COMPLETION_THRESHOLD_MS);
    }
    
    /**
     * 添加标准组件库组件
     */
    private void addStandardComponents(@NotNull CompletionContext context,
                                     @NotNull ComponentProvider componentProvider,
                                     @NotNull CompletionResultSet result) {
        
        String prefix = context.getPrefix();
        List<ElementPlusComponent> components;
        
        // 根据前缀过滤组件
        if (prefix != null && !prefix.isEmpty()) {
            components = componentProvider.getComponentsByPrefix(prefix);
        } else {
            components = componentProvider.getAllComponents();
        }
        
        VueKitLogger.debug(LOG, "标准组件库找到 " + components.size() + " 个匹配组件");
        
        int addedCount = 0;
        int maxComponents = VueKitConstants.MAX_COMPLETION_RESULTS;
        
        for (ElementPlusComponent component : components) {
            if (addedCount >= maxComponents) {
                VueKitLogger.debug(LOG, "达到最大组件数量限制: " + maxComponents);
                break;
            }
            
            try {
                LookupElementBuilder element = createComponentLookupElement(component, componentProvider);
                result.addElement(element);
                addedCount++;
                
                VueKitLogger.debug(LOG, "添加组件: " + component.getName());
                
            } catch (Exception e) {
                VueKitLogger.logAndIgnore(LOG, "创建组件补全元素失败: " + component.getName(), e);
            }
        }
        
        VueKitLogger.debug(LOG, "添加了 " + addedCount + " 个标准组件");
    }
    
    /**
     * 添加自定义组件库组件
     */
    private void addCustomComponents(@NotNull CompletionContext context,
                                   @NotNull CompletionResultSet result) {
        
        PluginSettings settings = PluginSettings.getInstance();
        if (!settings.isEnableCustomLibrarySupport()) {
            VueKitLogger.debug(LOG, "自定义组件库支持已禁用");
            return;
        }
        
        try {
            List<CustomComponentLibraryManager.CustomLibraryConfig> customLibraries = 
                CustomComponentLibraryManager.getAllCustomLibraries();
            
            VueKitLogger.debug(LOG, "找到 " + customLibraries.size() + " 个自定义组件库");
            
            int addedCount = 0;
            String prefix = context.getPrefix();
            
            for (CustomComponentLibraryManager.CustomLibraryConfig config : customLibraries) {
                for (ElementPlusComponent component : config.getComponents()) {
                    // 前缀过滤
                    if (prefix != null && !prefix.isEmpty()) {
                        if (!component.getName().toLowerCase().contains(prefix.toLowerCase())) {
                            continue;
                        }
                    }
                    
                    try {
                        LookupElementBuilder element = createCustomComponentLookupElement(component, config);
                        result.addElement(element);
                        addedCount++;
                        
                        VueKitLogger.debug(LOG, "添加自定义组件: " + component.getName() + 
                                         " (来自: " + config.getDisplayName() + ")");
                        
                    } catch (Exception e) {
                        VueKitLogger.logAndIgnore(LOG, "创建自定义组件补全元素失败: " + component.getName(), e);
                    }
                }
            }
            
            VueKitLogger.debug(LOG, "添加了 " + addedCount + " 个自定义组件");
            
        } catch (Exception e) {
            VueKitLogger.logAndIgnore(LOG, "加载自定义组件库失败", e);
        }
    }
    
    /**
     * 创建标准组件的补全元素
     */
    @NotNull
    private LookupElementBuilder createComponentLookupElement(@NotNull ElementPlusComponent component,
                                                            @NotNull ComponentProvider componentProvider) {
        
        String componentName = component.getName();
        String description = component.getDescription();
        String libraryName = componentProvider.getLibraryDisplayName();
        
        // 构建插入文本 - 创建完整的组件标签
        String insertText = String.format("<%s></%s>", componentName, componentName);
        
        return LookupElementBuilder.create(componentName)
                .withTypeText(VueKitConstants.COMPONENT_TYPE_TEXT, true)
                .withTailText("  " + description + " (" + libraryName + ")", true)
                .withIcon(ElementPlusIcons.COMPONENT_ICON)
                .withBoldness(true)
                .withInsertHandler((insertionContext, item) -> {
                    // 自定义插入处理器 - 插入完整标签并定位光标
                    try {
                        insertionContext.getDocument().replaceString(
                            insertionContext.getStartOffset(),
                            insertionContext.getTailOffset(),
                            insertText
                        );
                        
                        // 将光标定位到开始标签和结束标签之间
                        int newOffset = insertionContext.getStartOffset() + componentName.length() + 2; // +2 for '<' and '>'
                        insertionContext.getEditor().getCaretModel().moveToOffset(newOffset);
                        
                    } catch (Exception e) {
                        VueKitLogger.logAndIgnore(LOG, "插入组件标签失败: " + componentName, e);
                    }
                })
                .withLookupString(componentName)
                .withLookupString(componentName.toLowerCase());
    }
    
    /**
     * 创建自定义组件的补全元素
     */
    @NotNull
    private LookupElementBuilder createCustomComponentLookupElement(@NotNull ElementPlusComponent component,
                                                                  @NotNull CustomComponentLibraryManager.CustomLibraryConfig config) {
        
        String componentName = component.getName();
        String description = component.getDescription();
        String libraryName = config.getDisplayName();
        
        // 应用组件前缀（如果有）
        String prefix = config.getComponentPrefix();
        final String finalComponentName = (prefix != null && !prefix.isEmpty() && !componentName.startsWith(prefix)) 
            ? prefix + componentName 
            : componentName;
        
        String insertText = String.format("<%s></%s>", finalComponentName, finalComponentName);
        
        return LookupElementBuilder.create(finalComponentName)
                .withTypeText("自定义组件", true)
                .withTailText("  " + description + " (" + libraryName + ")", true)
                .withIcon(ElementPlusIcons.COMPONENT_ICON)
                .withInsertHandler((insertionContext, item) -> {
                    try {
                        insertionContext.getDocument().replaceString(
                            insertionContext.getStartOffset(),
                            insertionContext.getTailOffset(),
                            insertText
                        );
                        
                        int newOffset = insertionContext.getStartOffset() + finalComponentName.length() + 2;
                        insertionContext.getEditor().getCaretModel().moveToOffset(newOffset);
                        
                    } catch (Exception e) {
                        VueKitLogger.logAndIgnore(LOG, "插入自定义组件标签失败: " + finalComponentName, e);
                    }
                })
                .withLookupString(componentName)
                .withLookupString(finalComponentName)
                .withLookupString(componentName.toLowerCase());
    }
    
    @Override
    public int getPriority() {
        return 100; // 组件补全具有高优先级
    }
    
    @NotNull
    @Override
    public String getStrategyName() {
        return "组件补全策略";
    }
    
    @NotNull
    @Override
    public String getDescription() {
        return "提供Vue组件的智能补全，支持标准组件库和自定义组件库";
    }
    
    @Override
    public boolean isEnabled() {
        return PluginSettings.getInstance().isEnableComponentCompletion();
    }
}