package bak.completion;

import com.chu7.vuecomponentassistant.completion.CompletionCache;
import com.chu7.vuecomponentassistant.completion.CompletionContext;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 统一补全提供者
 * 
 * 整合了所有补全功能，包括：
 * - 组件补全
 * - 属性补全
 * - 事件补全
 * - 插槽补全
 * - 智能上下文分析
 * - 缓存优化
 * 
 * @author VueKit Team
 * @version 2.0.0
 */
public class UnifiedCompletionProvider extends CompletionProvider<CompletionParameters> {

    /** 组件数据提供者 */
    private com.chu7.vuecomponentassistant.completion.ComponentProvider componentProvider;
    
    /** 上下文分析器 */
    private final ElementPlusContextAnalyzer contextAnalyzer;
    
    /** 缓存管理器 */
    private final com.chu7.vuecomponentassistant.completion.CompletionCache cache;
    
    // 正则表达式模式
    private static final Pattern COMPONENT_TAG_PATTERN = Pattern.compile("<([a-zA-Z][a-zA-Z0-9-]*)\\b");
    private static final Pattern ATTRIBUTE_PATTERN = Pattern.compile("\\s([a-zA-Z][a-zA-Z0-9-]*)\\s*=");
    private static final Pattern EVENT_PATTERN = Pattern.compile("@([a-zA-Z][a-zA-Z0-9-]*)\\s*=");
    private static final Pattern SLOT_PATTERN = Pattern.compile("#([a-zA-Z][a-zA-Z0-9-]*)\\s*=");
    
    public UnifiedCompletionProvider() {
        this.contextAnalyzer = new ElementPlusContextAnalyzer();
        this.cache = new com.chu7.vuecomponentassistant.completion.CompletionCache();
    }
    
    @Override
    protected void addCompletions(@NotNull CompletionParameters parameters,
                                  @NotNull ProcessingContext context,
                                  @NotNull CompletionResultSet result) {
        
        // 获取项目信息
        Project project = parameters.getEditor().getProject();
        if (project == null) {
            return;
        }
        
        // 初始化组件提供者
        if (componentProvider == null) {
            componentProvider = new com.chu7.vuecomponentassistant.completion.ComponentProvider(project);
        }
        
        // 获取当前元素和文件
        PsiElement element = parameters.getPosition();
        PsiFile file = element.getContainingFile();
        
        if (file == null) {
            return;
        }
        
        // 检查文件类型
        if (!isVueFile(file)) {
            return;
        }
        
        // 分析上下文
        com.chu7.vuecomponentassistant.completion.CompletionContext completionContext = analyzeContext(file, element);
        
        // 尝试从缓存获取结果
        com.chu7.vuecomponentassistant.completion.CompletionCache.CacheKey cacheKey = new com.chu7.vuecomponentassistant.completion.CompletionCache.CacheKey(
            file, element, completionContext.getPrefix(), 
            componentProvider.getLibraryDisplayName()
        );
        
        com.chu7.vuecomponentassistant.completion.CompletionCache.CachedCompletionResult cachedResult =
            com.chu7.vuecomponentassistant.completion.CompletionCache.getCompletionResult(cacheKey);
        
        if (cachedResult != null) {
            // 使用缓存结果
            addCachedCompletions(result, cachedResult);
            return;
        }
        
        // 生成新的补全结果
        List<LookupElementBuilder> completions = generateCompletions(completionContext);
        
        // 缓存结果
        com.chu7.vuecomponentassistant.completion.CompletionCache.cacheCompletionResult(cacheKey, completions);
        
        // 添加补全选项
        for (LookupElementBuilder completion : completions) {
            result.addElement(completion);
        }
    }
    
    /**
     * 分析上下文
     */
    private com.chu7.vuecomponentassistant.completion.CompletionContext analyzeContext(PsiFile file, PsiElement element) {
        String fileText = file.getText();
        int offset = element.getTextOffset();
        String beforeText = fileText.substring(0, offset);
        String currentText = element.getText();
        
        // 清理当前文本
        String cleanCurrentText = currentText.replace("IntellijIdeaRulezzz", "");
        
        // 检查组件标签位置
        if (beforeText.endsWith("<") || beforeText.matches(".*<\\s*$")) {
            if (cleanCurrentText.matches("[a-zA-Z][a-zA-Z0-9-]*")) {
                return new com.chu7.vuecomponentassistant.completion.CompletionContext(com.chu7.vuecomponentassistant.completion.CompletionContext.CompletionType.COMPONENT, null, cleanCurrentText);
            }
            return new com.chu7.vuecomponentassistant.completion.CompletionContext(com.chu7.vuecomponentassistant.completion.CompletionContext.CompletionType.COMPONENT, null, null);
        }
        
        // 获取当前组件
        String currentComponent = getCurrentComponent(beforeText);
        
        // 检查事件位置
        if (currentComponent != null && isInComponentTag(beforeText) && beforeText.contains("@")) {
            String eventPrefix = getEventPrefix(beforeText);
            if (eventPrefix != null) {
                return new com.chu7.vuecomponentassistant.completion.CompletionContext(com.chu7.vuecomponentassistant.completion.CompletionContext.CompletionType.EVENT, currentComponent, eventPrefix);
            }
        }
        
        // 检查属性位置
        if (currentComponent != null && isInComponentTag(beforeText)) {
            String prefix = getAttributePrefix(beforeText);
            if (prefix != null) {
                return new com.chu7.vuecomponentassistant.completion.CompletionContext(com.chu7.vuecomponentassistant.completion.CompletionContext.CompletionType.ATTRIBUTE, currentComponent, prefix);
            }
        }
        
        // 检查组件名称位置
        String componentPrefix = getComponentPrefix(beforeText);
        if (componentPrefix != null) {
            return new com.chu7.vuecomponentassistant.completion.CompletionContext(com.chu7.vuecomponentassistant.completion.CompletionContext.CompletionType.COMPONENT, null, componentPrefix);
        }
        
        return new com.chu7.vuecomponentassistant.completion.CompletionContext(com.chu7.vuecomponentassistant.completion.CompletionContext.CompletionType.COMPONENT, null, null);
    }
    
    /**
     * 生成补全选项
     */
    private List<LookupElementBuilder> generateCompletions(CompletionContext context) {
        switch (context.getType()) {
            case COMPONENT:
                return generateComponentCompletions(context.getPrefix());
            case ATTRIBUTE:
                return generateAttributeCompletions(context.getCurrentComponent(), context.getPrefix());
            case EVENT:
                return generateEventCompletions(context.getCurrentComponent(), context.getPrefix());
            case SLOT:
                return generateSlotCompletions(context.getCurrentComponent(), context.getPrefix());
            default:
                return generateComponentCompletions(context.getPrefix());
        }
    }
    
    /**
     * 生成组件补全选项
     */
    private List<LookupElementBuilder> generateComponentCompletions(String prefix) {
        List<ElementPlusComponent> components;
        
        if (prefix != null && !prefix.isEmpty()) {
            components = componentProvider.searchComponents(prefix);
        } else {
            components = componentProvider.getAllComponents();
        }
        
        return components.stream()
            .limit(20) // 限制显示数量
            .map(this::createComponentLookupElement)
            .collect(java.util.stream.Collectors.toList());
    }
    
    /**
     * 生成属性补全选项
     */
    private List<LookupElementBuilder> generateAttributeCompletions(String componentName, String prefix) {
        if (componentName == null) {
            return new java.util.ArrayList<>();
        }
        
        List<ElementPlusProp> props = componentProvider.getComponentProps(componentName);
        
        return props.stream()
            .filter(prop -> prefix == null || prop.getName().toLowerCase().contains(prefix.toLowerCase()))
            .map(this::createAttributeLookupElement)
            .collect(java.util.stream.Collectors.toList());
    }
    
    /**
     * 生成事件补全选项
     */
    private List<LookupElementBuilder> generateEventCompletions(String componentName, String prefix) {
        if (componentName == null) {
            return new java.util.ArrayList<>();
        }
        
        List<ElementPlusEvent> events = componentProvider.getComponentEvents(componentName);
        
        return events.stream()
            .filter(event -> prefix == null || event.getName().toLowerCase().contains(prefix.toLowerCase()))
            .map(this::createEventLookupElement)
            .collect(java.util.stream.Collectors.toList());
    }
    
    /**
     * 生成插槽补全选项
     */
    private List<LookupElementBuilder> generateSlotCompletions(String componentName, String prefix) {
        if (componentName == null) {
            return new java.util.ArrayList<>();
        }
        
        List<ElementPlusSlot> slots = componentProvider.getComponentSlots(componentName);
        
        return slots.stream()
            .filter(slot -> prefix == null || slot.getName().toLowerCase().contains(prefix.toLowerCase()))
            .map(this::createSlotLookupElement)
            .collect(java.util.stream.Collectors.toList());
    }
    
    /**
     * 创建组件查找元素
     */
    private LookupElementBuilder createComponentLookupElement(ElementPlusComponent component) {
        return LookupElementBuilder.create(component.getName())
            .withTypeText("Component")
            .withTailText(" " + component.getDescription())
            .withIcon(ElementPlusIcons.COMPONENT_ICON)
            .withInsertHandler((insertContext, item) -> {
                Editor editor = insertContext.getEditor();
                int offset = insertContext.getTailOffset();
                editor.getDocument().insertString(offset, "></" + component.getName() + ">");
                editor.getCaretModel().moveToOffset(offset);
            });
    }
    
    /**
     * 创建属性查找元素
     */
    private LookupElementBuilder createAttributeLookupElement(ElementPlusProp prop) {
        return LookupElementBuilder.create(prop.getName())
            .withTypeText("Property")
            .withTailText(" " + prop.getDescription())
            .withIcon(ElementPlusIcons.PROPERTY_ICON);
    }
    
    /**
     * 创建事件查找元素
     */
    private LookupElementBuilder createEventLookupElement(ElementPlusEvent event) {
        return LookupElementBuilder.create(event.getName())
            .withTypeText("Event")
            .withTailText(" " + event.getDescription())
            .withIcon(ElementPlusIcons.EVENT_ICON);
    }
    
    /**
     * 创建插槽查找元素
     */
    private LookupElementBuilder createSlotLookupElement(ElementPlusSlot slot) {
        return LookupElementBuilder.create(slot.getName())
            .withTypeText("Slot")
            .withTailText(" " + slot.getDescription())
            .withIcon(ElementPlusIcons.SLOT_ICON);
    }
    
    /**
     * 添加缓存的补全选项
     */
    @SuppressWarnings("unchecked")
    private void addCachedCompletions(CompletionResultSet result, CompletionCache.CachedCompletionResult cachedResult) {
        List<LookupElementBuilder> completions = (List<LookupElementBuilder>) cachedResult.getResult();
        for (LookupElementBuilder completion : completions) {
            result.addElement(completion);
        }
    }
    
    // 辅助方法
    private String getCurrentComponent(String beforeText) {
        Matcher matcher = COMPONENT_TAG_PATTERN.matcher(beforeText);
        String lastComponent = null;
        while (matcher.find()) {
            lastComponent = matcher.group(1);
        }
        return lastComponent;
    }
    
    private boolean isInComponentTag(String beforeText) {
        return beforeText.matches(".*<[^>]*$");
    }
    
    private String getAttributePrefix(String beforeText) {
        Matcher matcher = ATTRIBUTE_PATTERN.matcher(beforeText);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
    
    private String getEventPrefix(String beforeText) {
        Matcher matcher = EVENT_PATTERN.matcher(beforeText);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
    
    private String getComponentPrefix(String beforeText) {
        if (beforeText.matches(".*<\\s*[a-zA-Z][a-zA-Z0-9-]*$")) {
            String[] parts = beforeText.split("<");
            String lastPart = parts[parts.length - 1].trim();
            if (lastPart.matches("[a-zA-Z][a-zA-Z0-9-]*")) {
                return lastPart;
            }
        }
        return null;
    }
    
    private boolean isVueFile(PsiFile file) {
        String fileName = file.getName().toLowerCase();
        return fileName.endsWith(".vue") || fileName.endsWith(".html");
    }
} 