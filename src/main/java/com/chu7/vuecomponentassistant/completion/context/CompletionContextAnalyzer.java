package com.chu7.vuecomponentassistant.completion.context;

import com.chu7.vuecomponentassistant.completion.CompletionContext;
import com.chu7.vuecomponentassistant.utils.VueKitLogger;
import com.chu7.vuecomponentassistant.constants.VueKitConstants;
import com.chu7.vuecomponentassistant.cache.CacheManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 补全上下文分析器
 * 
 * 负责分析当前光标位置的上下文，确定用户需要什么类型的补全：
 * - 组件补全：用户输入 < 时
 * - 属性补全：在组件标签内输入空格时
 * - 事件补全：输入 @ 时
 * - 插槽补全：输入 # 或 slot 时
 * 
 * @author VueKit Team
 * @version 2.0.0
 */
public class CompletionContextAnalyzer {
    
    private static final Logger LOG = VueKitLogger.getLogger(CompletionContextAnalyzer.class);
    
    // 缓存管理器
    private final CacheManager cacheManager;
    
    // 正则表达式模式
    private static final Pattern COMPONENT_PATTERN = Pattern.compile(VueKitConstants.COMPONENT_TAG_REGEX);
    private static final Pattern ATTRIBUTE_PATTERN = Pattern.compile(VueKitConstants.ATTRIBUTE_REGEX);
    private static final Pattern EVENT_PATTERN = Pattern.compile(VueKitConstants.EVENT_REGEX);
    private static final Pattern SLOT_PATTERN = Pattern.compile(VueKitConstants.SLOT_REGEX);
    
    public CompletionContextAnalyzer() {
        this.cacheManager = CacheManager.getInstance();
    }
    
    /**
     * 分析补全上下文
     * 
     * @param file 当前文件
     * @param element 当前元素
     * @return 补全上下文信息
     */
    @NotNull
    public CompletionContext analyzeContext(@NotNull PsiFile file, @NotNull PsiElement element) {
        long startTime = System.currentTimeMillis();
        
        // 尝试从缓存获取
        CompletionContext cached = cacheManager.getCachedContext(file, element);
        if (cached != null) {
            VueKitLogger.logCacheOperation(LOG, "hit", "context");
            return cached;
        }
        
        VueKitLogger.debug(LOG, "开始分析补全上下文");
        VueKitLogger.debug(LOG, "元素类型: " + element.getClass().getSimpleName());
        VueKitLogger.debug(LOG, "元素文本: " + element.getText());
        
        CompletionContext context = performContextAnalysis(file, element);
        
        // 缓存结果
        cacheManager.cacheContext(file, element, context);
        
        long duration = System.currentTimeMillis() - startTime;
        VueKitLogger.performanceWithThreshold(LOG, "上下文分析", duration, 50);
        
        return context;
    }
    
    /**
     * 执行具体的上下文分析
     */
    @NotNull
    private CompletionContext performContextAnalysis(@NotNull PsiFile file, @NotNull PsiElement element) {
        // 获取当前元素的文本和位置信息
        String elementText = element.getText();
        String fileText = file.getText();
        int offset = element.getTextOffset();
        
        // 分析上下文类型
        CompletionContext.CompletionType type = determineCompletionType(element, elementText, fileText, offset);
        String currentComponent = extractCurrentComponent(element);
        String prefix = extractPrefix(element, elementText, type);
        
        VueKitLogger.debug(LOG, "上下文分析结果:");
        VueKitLogger.debug(LOG, "  类型: " + type);
        VueKitLogger.debug(LOG, "  当前组件: " + currentComponent);
        VueKitLogger.debug(LOG, "  前缀: " + prefix);
        
        return new CompletionContext(type, currentComponent, prefix);
    }
    
    /**
     * 确定补全类型
     */
    @NotNull
    private CompletionContext.CompletionType determineCompletionType(@NotNull PsiElement element, 
                                                                   @NotNull String elementText,
                                                                   @NotNull String fileText, 
                                                                   int offset) {
        
        // 检查是否在XML标签内
        if (isInXmlTag(element)) {
            return analyzeXmlTagContext(element, elementText, fileText, offset);
        }
        
        // 检查是否是组件补全（用户输入 <）
        if (isComponentCompletion(elementText, fileText, offset)) {
            return CompletionContext.CompletionType.COMPONENT;
        }
        
        // 检查文本模式
        return analyzeTextPatterns(elementText, fileText, offset);
    }
    
    /**
     * 检查是否在XML标签内
     */
    private boolean isInXmlTag(@NotNull PsiElement element) {
        PsiElement current = element;
        while (current != null) {
            if (current instanceof XmlTag) {
                return true;
            }
            current = current.getParent();
        }
        return false;
    }
    
    /**
     * 分析XML标签内的上下文
     */
    @NotNull
    private CompletionContext.CompletionType analyzeXmlTagContext(@NotNull PsiElement element,
                                                                @NotNull String elementText,
                                                                @NotNull String fileText,
                                                                int offset) {
        
        // 检查是否在属性位置
        if (element instanceof XmlAttribute || isInAttributePosition(element)) {
            // 进一步判断是事件还是普通属性
            if (elementText.startsWith("@") || elementText.contains("@")) {
                return CompletionContext.CompletionType.EVENT;
            } else if (elementText.startsWith("#") || elementText.contains("#")) {
                return CompletionContext.CompletionType.SLOT;
            } else {
                return CompletionContext.CompletionType.ATTRIBUTE;
            }
        }
        
        // 默认为组件补全
        return CompletionContext.CompletionType.COMPONENT;
    }
    
    /**
     * 检查是否在属性位置
     */
    private boolean isInAttributePosition(@NotNull PsiElement element) {
        // 检查父元素是否为XmlTag，且当前位置适合属性输入
        PsiElement parent = element.getParent();
        if (parent instanceof XmlTag) {
            String text = element.getText();
            return text.trim().isEmpty() || text.contains("=") || text.matches("\\s+\\w*");
        }
        return false;
    }
    
    /**
     * 检查是否是组件补全
     */
    private boolean isComponentCompletion(@NotNull String elementText, @NotNull String fileText, int offset) {
        // 检查前一个字符是否是 <
        if (offset > 0) {
            char prevChar = fileText.charAt(offset - 1);
            if (prevChar == '<') {
                return true;
            }
        }
        
        // 检查当前文本是否匹配组件模式
        return elementText.startsWith("<") || COMPONENT_PATTERN.matcher(elementText).find();
    }
    
    /**
     * 分析文本模式
     */
    @NotNull
    private CompletionContext.CompletionType analyzeTextPatterns(@NotNull String elementText,
                                                               @NotNull String fileText,
                                                               int offset) {
        
        // 检查事件模式 (@click, @input 等)
        if (EVENT_PATTERN.matcher(elementText).find() || elementText.contains("@")) {
            return CompletionContext.CompletionType.EVENT;
        }
        
        // 检查插槽模式 (#default, #header 等)
        if (SLOT_PATTERN.matcher(elementText).find() || elementText.contains("#")) {
            return CompletionContext.CompletionType.SLOT;
        }
        
        // 检查属性模式
        if (ATTRIBUTE_PATTERN.matcher(elementText).find()) {
            return CompletionContext.CompletionType.ATTRIBUTE;
        }
        
        // 默认为组件补全
        return CompletionContext.CompletionType.COMPONENT;
    }
    
    /**
     * 提取当前组件名称
     */
    @Nullable
    private String extractCurrentComponent(@NotNull PsiElement element) {
        // 向上查找最近的XML标签
        PsiElement current = element;
        while (current != null) {
            if (current instanceof XmlTag) {
                XmlTag tag = (XmlTag) current;
                String tagName = tag.getName();
                
                // 检查是否是Vue组件（包含连字符或以特定前缀开头）
                if (isVueComponent(tagName)) {
                    VueKitLogger.debug(LOG, "找到当前组件: " + tagName);
                    return tagName;
                }
            }
            current = current.getParent();
        }
        
        // 如果找不到XML标签，尝试从文本中解析
        return extractComponentFromText(element);
    }
    
    /**
     * 检查是否是Vue组件
     */
    private boolean isVueComponent(@NotNull String tagName) {
        // Vue组件通常包含连字符或以特定前缀开头
        return tagName.contains("-") || 
               tagName.startsWith("el-") || 
               tagName.startsWith("a-") || 
               tagName.startsWith("van-") ||
               tagName.matches("^[A-Z][a-zA-Z]*$"); // PascalCase组件
    }
    
    /**
     * 从文本中提取组件名称
     */
    @Nullable
    private String extractComponentFromText(@NotNull PsiElement element) {
        String text = element.getText();
        
        // 使用正则表达式匹配组件标签
        Matcher matcher = COMPONENT_PATTERN.matcher(text);
        if (matcher.find()) {
            String componentName = matcher.group(1);
            if (isVueComponent(componentName)) {
                VueKitLogger.debug(LOG, "从文本提取组件: " + componentName);
                return componentName;
            }
        }
        
        return null;
    }
    
    /**
     * 提取输入前缀
     */
    @Nullable
    private String extractPrefix(@NotNull PsiElement element, 
                               @NotNull String elementText,
                               @NotNull CompletionContext.CompletionType type) {
        
        String text = elementText.trim();
        
        switch (type) {
            case COMPONENT:
                return extractComponentPrefix(text);
            case ATTRIBUTE:
                return extractAttributePrefix(text);
            case EVENT:
                return extractEventPrefix(text);
            case SLOT:
                return extractSlotPrefix(text);
            default:
                return null;
        }
    }
    
    /**
     * 提取组件前缀
     */
    @Nullable
    private String extractComponentPrefix(@NotNull String text) {
        // 移除 < 符号
        if (text.startsWith("<")) {
            text = text.substring(1);
        }
        
        // 移除空格和特殊字符
        text = text.replaceAll("[^a-zA-Z0-9-]", "");
        
        return text.isEmpty() ? null : text;
    }
    
    /**
     * 提取属性前缀
     */
    @Nullable
    private String extractAttributePrefix(@NotNull String text) {
        // 查找最后一个空格后的内容作为前缀
        int lastSpace = text.lastIndexOf(' ');
        if (lastSpace >= 0 && lastSpace < text.length() - 1) {
            return text.substring(lastSpace + 1);
        }
        
        return text.isEmpty() ? null : text;
    }
    
    /**
     * 提取事件前缀
     */
    @Nullable
    private String extractEventPrefix(@NotNull String text) {
        // 移除 @ 符号
        if (text.startsWith("@")) {
            text = text.substring(1);
        }
        
        return text.isEmpty() ? null : text;
    }
    
    /**
     * 提取插槽前缀
     */
    @Nullable
    private String extractSlotPrefix(@NotNull String text) {
        // 移除 # 符号
        if (text.startsWith("#")) {
            text = text.substring(1);
        }
        
        return text.isEmpty() ? null : text;
    }
    
    /**
     * 清理缓存
     */
    public void clearCache() {
        // 缓存清理由CacheManager统一管理
        VueKitLogger.debug(LOG, "上下文分析器缓存已清理");
    }
}