package com.chu7.vuecomponentassistant.completion;

import com.chu7.vuecomponentassistant.completion.context.CompletionContextAnalyzer;
import com.chu7.vuecomponentassistant.remote.model.ComponentInfo;
import com.intellij.codeInsight.completion.*;
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
 * <p>
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

    /**
     * 组件数据提供者
     */
    private ComponentProvider componentProvider;

    /**
     * 上下文分析器
     */
    private final CompletionContextAnalyzer contextAnalyzer;

    /**
     * 缓存管理器
     */
    private final CompletionCache cache;

    // 正则表达式模式 - 支持属性前缀匹配
    private static final Pattern COMPONENT_TAG_PATTERN = Pattern.compile("<([a-zA-Z][a-zA-Z0-9-]*)\\b");
    private static final Pattern ATTRIBUTE_PATTERN = Pattern.compile("\\s([a-zA-Z][a-zA-Z0-9-]*)\\s*[=]?");
    private static final Pattern EVENT_PATTERN = Pattern.compile("@([a-zA-Z][a-zA-Z0-9-]*)\\s*[=]?");
    private static final Pattern SLOT_PATTERN = Pattern.compile("#([a-zA-Z][a-zA-Z0-9-]*)\\s*[=]?");

    public UnifiedCompletionProvider() {
        this.contextAnalyzer = new CompletionContextAnalyzer();
        this.cache = new CompletionCache();
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

        // 每次补全都重新初始化组件提供者，确保获取最新数据
        componentProvider = new ComponentProvider(project);

        // 添加调试日志
        System.out.println("=== 代码补全开始 ===");
        System.out.println("检测到的组件库类型: " + componentProvider.getLibraryType().getDisplayName());
        System.out.println("组件总数: " + componentProvider.getComponentCount());
        System.out.println("组件库显示名称: " + componentProvider.getLibraryDisplayName());

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
        CompletionContext completionContext = analyzeContext(file, element);

        // 尝试从缓存获取结果
        CompletionCache.CacheKey cacheKey = new CompletionCache.CacheKey(
                file, element, completionContext.getPrefix(),
                componentProvider.getLibraryDisplayName()
        );

        CompletionCache.CachedCompletionResult cachedResult =
                CompletionCache.getCompletionResult(cacheKey);

        if (cachedResult != null) {
            // 使用缓存结果
            addCachedCompletions(result, cachedResult);
            return;
        }

        // 生成新的补全结果
        List<LookupElementBuilder> completions = generateCompletions(completionContext);

        // 缓存结果
        CompletionCache.cacheCompletionResult(cacheKey, completions);

        // 添加补全选项
        for (LookupElementBuilder completion : completions) {
            result.addElement(completion);
        }
    }

    /**
     * 分析上下文 - 参考备份代码的简单逻辑
     */
    private CompletionContext analyzeContext(PsiFile file, PsiElement element) {
        String fileText = file.getText();
        int offset = element.getTextOffset();
        String beforeText = fileText.substring(0, offset);
        String currentText = element.getText();

        // 清理当前文本
        String cleanCurrentText = currentText.replace("IntellijIdeaRulezzz", "");

        // 添加调试日志
        System.out.println("=== 分析补全上下文 ===");
        System.out.println("beforeText: '" + beforeText + "'");
        System.out.println("currentText: '" + currentText + "'");
        System.out.println("cleanCurrentText: '" + cleanCurrentText + "'");

        // 检查是否在组件标签位置
        if (beforeText.endsWith("<") || beforeText.matches(".*<\\s*$")) {
            System.out.println("✅ 匹配到组件标签位置");
            if (cleanCurrentText.matches("[a-zA-Z][a-zA-Z0-9-]*")) {
                System.out.println("✅ 匹配到组件名称模式，前缀: " + cleanCurrentText);
                return new CompletionContext(CompletionContext.CompletionType.COMPONENT, null, cleanCurrentText);
            }
            return new CompletionContext(CompletionContext.CompletionType.COMPONENT, null, null);
        }

        // 获取当前组件
        String currentComponent = getCurrentComponent(beforeText);
        System.out.println("当前组件: " + currentComponent);

        // 检查是否在属性位置 - 使用简单的空格检测
        if (currentComponent != null && beforeText.contains("<" + currentComponent) && beforeText.contains(" ")) {
            // 检查是否在属性位置（空格后）
            String lastSpace = beforeText.substring(beforeText.lastIndexOf(" ")).trim();
            System.out.println("=== 属性位置检测调试 ===");
            System.out.println("lastSpace: '" + lastSpace + "'");
            System.out.println("lastSpace.isEmpty(): " + lastSpace.isEmpty());
            System.out.println("lastSpace.contains('='): " + lastSpace.contains("="));
            System.out.println("lastSpace.contains('@'): " + lastSpace.contains("@"));
            System.out.println("lastSpace.contains('#'): " + lastSpace.contains("#"));
            
            if (!lastSpace.isEmpty() && !lastSpace.contains("=") && !lastSpace.contains("@") && !lastSpace.contains("#")) {
                System.out.println("✅ 检测到属性补全场景，前缀: " + lastSpace);
                return new CompletionContext(CompletionContext.CompletionType.ATTRIBUTE, currentComponent, lastSpace);
            } else {
                // 如果最后一个空格后没有内容，检查当前输入的文本
                if (cleanCurrentText.matches("[a-zA-Z][a-zA-Z0-9-]*")) {
                    System.out.println("✅ 检测到属性补全场景，当前输入前缀: " + cleanCurrentText);
                    return new CompletionContext(CompletionContext.CompletionType.ATTRIBUTE, currentComponent, cleanCurrentText);
                }
            }
        }

        // 检查是否在事件位置
        if (currentComponent != null && beforeText.contains("@")) {
            String eventPrefix = getEventPrefix(beforeText);
            if (eventPrefix != null) {
                System.out.println("✅ 检测到事件补全场景");
                return new CompletionContext(CompletionContext.CompletionType.EVENT, currentComponent, eventPrefix);
            }
        }

        // 检查是否在插槽位置
        if (currentComponent != null && beforeText.contains("#")) {
            String slotPrefix = getSlotPrefix(beforeText);
            if (slotPrefix != null) {
                System.out.println("✅ 检测到插槽补全场景");
                return new CompletionContext(CompletionContext.CompletionType.SLOT, currentComponent, slotPrefix);
            }
        }

        // 检查组件名称位置
        String componentPrefix = getComponentPrefix(beforeText);
        if (componentPrefix != null) {
            System.out.println("✅ 检测到组件补全场景");
            return new CompletionContext(CompletionContext.CompletionType.COMPONENT, null, componentPrefix);
        }

        System.out.println("默认返回组件补全场景");
        return new CompletionContext(CompletionContext.CompletionType.COMPONENT, null, null);
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
        List<ComponentInfo> components;

        // 添加调试日志
        System.out.println("生成组件补全选项，前缀: " + prefix);
        System.out.println("当前组件库类型: " + componentProvider.getLibraryType().getDisplayName());
        System.out.println("组件总数: " + componentProvider.getComponentCount());

        if (prefix != null && !prefix.isEmpty()) {
            // 使用 startsWith 进行前缀匹配
            components = componentProvider.getAllComponents().stream()
                    .filter(comp -> comp.getName().toLowerCase().startsWith(prefix.toLowerCase()))
                    .collect(java.util.stream.Collectors.toList());
            System.out.println("根据前缀 '" + prefix + "' 搜索到 " + components.size() + " 个组件");
        } else {
            components = componentProvider.getAllComponents();
            System.out.println("获取所有组件，共 " + components.size() + " 个");
        }

        // 打印前几个组件名称用于调试
        if (!components.isEmpty()) {
            System.out.println("前5个组件: " + components.stream()
                    .limit(5)
                    .map(ComponentInfo::getName)
                    .collect(java.util.stream.Collectors.joining(", ")));
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
        System.out.println("=== 生成属性补全选项 ===");
        System.out.println("组件名称: " + componentName);
        System.out.println("前缀: " + prefix);

        if (componentName == null) {
            System.out.println("❌ 组件名称为空，返回空列表");
            return new java.util.ArrayList<>();
        }

        List<ComponentInfo.ComponentProp> props = componentProvider.getComponentProps(componentName);
        System.out.println("找到属性数量: " + (props != null ? props.size() : 0));

        if (props == null || props.isEmpty()) {
            System.out.println("❌ 没有找到属性，返回空列表");
            return new java.util.ArrayList<>();
        }

                List<LookupElementBuilder> result = props.stream()
                .filter(prop -> prefix == null || prop.getName().toLowerCase().startsWith(prefix.toLowerCase()))
                .map(this::createAttributeLookupElement)
                .collect(java.util.stream.Collectors.toList());
        
        System.out.println("✅ 生成属性补全选项: " + result.size() + " 个");
        if (prefix != null && !prefix.isEmpty()) {
            System.out.println("匹配前缀 '" + prefix + "' 的属性: " + 
                result.stream().map(item -> item.getLookupString()).collect(java.util.stream.Collectors.joining(", ")));
        }
        return result;
    }

    /**
     * 生成事件补全选项
     */
    private List<LookupElementBuilder> generateEventCompletions(String componentName, String prefix) {
        System.out.println("=== 生成事件补全选项 ===");
        System.out.println("组件名称: " + componentName);
        System.out.println("前缀: " + prefix);

        if (componentName == null) {
            System.out.println("❌ 组件名称为空，返回空列表");
            return new java.util.ArrayList<>();
        }

        List<ComponentInfo.ComponentEvent> events = componentProvider.getComponentEvents(componentName);
        System.out.println("找到事件数量: " + (events != null ? events.size() : 0));

        if (events == null || events.isEmpty()) {
            System.out.println("❌ 没有找到事件，返回空列表");
            return new java.util.ArrayList<>();
        }

        List<LookupElementBuilder> result = events.stream()
                .filter(event -> prefix == null || event.getName().toLowerCase().startsWith(prefix.toLowerCase()))
                .map(this::createEventLookupElement)
                .collect(java.util.stream.Collectors.toList());

        System.out.println("✅ 生成事件补全选项: " + result.size() + " 个");
        return result;
    }

    /**
     * 生成插槽补全选项
     */
    private List<LookupElementBuilder> generateSlotCompletions(String componentName, String prefix) {
        System.out.println("=== 生成插槽补全选项 ===");
        System.out.println("组件名称: " + componentName);
        System.out.println("前缀: " + prefix);

        if (componentName == null) {
            System.out.println("❌ 组件名称为空，返回空列表");
            return new java.util.ArrayList<>();
        }

        List<ComponentInfo.ComponentSlot> slots = componentProvider.getComponentSlots(componentName);
        System.out.println("找到插槽数量: " + (slots != null ? slots.size() : 0));

        if (slots == null || slots.isEmpty()) {
            System.out.println("❌ 没有找到插槽，返回空列表");
            return new java.util.ArrayList<>();
        }

        List<LookupElementBuilder> result = slots.stream()
                .filter(slot -> prefix == null || slot.getName().toLowerCase().startsWith(prefix.toLowerCase()))
                .map(this::createSlotLookupElement)
                .collect(java.util.stream.Collectors.toList());

        System.out.println("✅ 生成插槽补全选项: " + result.size() + " 个");
        return result;
    }

    /**
     * 创建组件查找元素
     */
    private LookupElementBuilder createComponentLookupElement(ComponentInfo component) {
        // 添加调试信息
        System.out.println("=== 创建组件查找元素 ===");
        System.out.println("组件名称: " + component.getName());
        System.out.println("组件描述: " + component.getDescription());
        System.out.println("组件属性数量: " + (component.getProps() != null ? component.getProps().size() : 0));
        System.out.println("组件事件数量: " + (component.getEvents() != null ? component.getEvents().size() : 0));
        System.out.println("组件插槽数量: " + (component.getSlots() != null ? component.getSlots().size() : 0));

        // 构建类型文本：组件库 >= 版本号
        String typeText = componentProvider.getLibraryDisplayName();
        if (component.getVersion() != null && !component.getVersion().trim().isEmpty()) {
//            typeText += " >= " + component.getVersion();
            typeText = component.getVersion();
        }

        // 使用组件名称作为插入文本，描述只用于显示
        return LookupElementBuilder.create(component.getName())
                .withTypeText(typeText)
                .withTailText(component.getDescription() != null && !component.getDescription().trim().isEmpty()
                        ? " - " + component.getDescription()
                        : "")
                .withIcon(null)
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
    private LookupElementBuilder createAttributeLookupElement(ComponentInfo.ComponentProp prop) {
        // 添加调试信息
        System.out.println("=== 创建属性查找元素 ===");
        System.out.println("属性名称: " + prop.getName());
        System.out.println("属性描述: " + prop.getDescription());
        System.out.println("属性类型: " + prop.getType());
        System.out.println("是否必需: " + prop.isRequired());
        System.out.println("默认值: " + prop.getDefaultValue());

        // 构建类型文本：组件库 < Property >
        String typeText = componentProvider.getLibraryDisplayName() + " < Property >";

        // 使用属性名称作为插入文本，描述只用于显示
        return LookupElementBuilder.create(prop.getName())
                .withTypeText(typeText)
                .withTailText(prop.getDescription() != null && !prop.getDescription().trim().isEmpty()
                        ? " - " + prop.getDescription()
                        : "")
                .withIcon(null)
                .withInsertHandler((insertContext, item) -> {
                    // 插入属性名、等号和默认值
                    Editor editor = insertContext.getEditor();
                    int offset = insertContext.getTailOffset();
                    
                    // 构建属性值字符串
                    String attributeValue = "";
                    if (prop.getDefaultValue() != null && !prop.getDefaultValue().toString().trim().isEmpty()) {
                        // 所有类型的属性值都加上双引号
                        attributeValue = "=\"" + prop.getDefaultValue() + "\"";
                    } else {
                        // 没有默认值，只插入等号和引号
                        attributeValue = "=\"\"";
                    }
                    
                    editor.getDocument().insertString(offset, attributeValue);
                    // 将光标移动到引号中间，方便用户输入
                    if (attributeValue.contains("\"")) {
                        editor.getCaretModel().moveToOffset(offset + attributeValue.indexOf("\"") + 1);
                    } else {
                        editor.getCaretModel().moveToOffset(offset + attributeValue.length());
                    }
                });
    }

    /**
     * 创建事件查找元素
     */
    private LookupElementBuilder createEventLookupElement(ComponentInfo.ComponentEvent event) {
        // 构建类型文本：组件库 < Event >
        String typeText = componentProvider.getLibraryDisplayName() + " < Event >";

        // 使用事件名称作为插入文本，描述只用于显示
        return LookupElementBuilder.create(event.getName())
                .withTypeText(typeText)
                .withTailText(event.getDescription() != null && !event.getDescription().trim().isEmpty()
                        ? " - " + event.getDescription()
                        : "")
                .withIcon(null)
                .withInsertHandler((insertContext, item) -> {
                    // 插入事件名、等号和默认的事件处理函数名
                    Editor editor = insertContext.getEditor();
                    int offset = insertContext.getTailOffset();
                    
                    // 生成事件处理函数名：handle + 首字母大写的函数名
                    String handlerName = "handle" + event.getName().substring(0, 1).toUpperCase() + event.getName().substring(1);
                    String eventValue = "=\"" + handlerName + "\"";
                    
                    editor.getDocument().insertString(offset, eventValue);
                    // 将光标移动到引号中间，方便用户修改
                    editor.getCaretModel().moveToOffset(offset + eventValue.indexOf("\"") + 1);
                });
    }

    /**
     * 创建插槽查找元素
     */
    private LookupElementBuilder createSlotLookupElement(ComponentInfo.ComponentSlot slot) {
        // 构建类型文本：组件库 < Slot >
        String typeText = componentProvider.getLibraryDisplayName() + " < Slot >";

        // 使用插槽名称作为插入文本，描述只用于显示
        return LookupElementBuilder.create(slot.getName())
                .withTypeText(typeText)
                .withTailText(slot.getDescription() != null && !slot.getDescription().trim().isEmpty()
                        ? " - " + slot.getDescription()
                        : "")
                .withIcon(null)
                .withInsertHandler((insertContext, item) -> {
                    // 插入插槽名、等号和默认的插槽内容
                    Editor editor = insertContext.getEditor();
                    int offset = insertContext.getTailOffset();
                    
                    // 为插槽插入默认内容
                    String slotValue = "=\"slotContent\">";
                    
                    editor.getDocument().insertString(offset, slotValue);
                    // 将光标移动到插槽内容中间，方便用户修改
                    editor.getCaretModel().moveToOffset(offset + slotValue.indexOf("slotContent"));
                });
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

    // 辅助方法 - 参考备份代码的实现
    private String getCurrentComponent(String beforeText) {
        Matcher matcher = COMPONENT_TAG_PATTERN.matcher(beforeText);
        String lastComponent = null;
        while (matcher.find()) {
            lastComponent = matcher.group(1);
        }
        
        // 检查是否是Element Plus组件
        if (lastComponent != null && lastComponent.startsWith("el-")) {
            return lastComponent;
        }
        
        return null;
    }

        // 删除复杂的正则匹配方法，使用简单的字符串检测

    private String getEventPrefix(String beforeText) {
        // 简单的事件前缀检测
        if (beforeText.contains("@")) {
            int atIndex = beforeText.lastIndexOf("@");
            String afterAt = beforeText.substring(atIndex + 1);
            if (!afterAt.contains("=") && !afterAt.contains(" ")) {
                return afterAt;
            }
        }
        return null;
    }

    private String getSlotPrefix(String beforeText) {
        // 简单的插槽前缀检测
        if (beforeText.contains("#")) {
            int hashIndex = beforeText.lastIndexOf("#");
            String afterHash = beforeText.substring(hashIndex + 1);
            if (!afterHash.contains("=") && !afterHash.contains(" ")) {
                return afterHash;
            }
        }
        return null;
    }

    private String getComponentPrefix(String beforeText) {
        System.out.println("=== getComponentPrefix 调试 ===");
        System.out.println("beforeText: '" + beforeText + "'");
        
        if (beforeText.endsWith("<")) {
            return "";
        }
        
        // 查找最近的<符号
        int lastOpenTag = beforeText.lastIndexOf('<');
        if (lastOpenTag >= 0) {
            String afterOpenTag = beforeText.substring(lastOpenTag + 1);
            if (!afterOpenTag.contains(" ") && !afterOpenTag.contains(">")) {
                System.out.println("✅ 返回组件前缀: " + afterOpenTag);
                return afterOpenTag;
            }
        }
        
        System.out.println("❌ 未找到组件前缀");
        return null;
    }

    private boolean isVueFile(PsiFile file) {
        String fileName = file.getName().toLowerCase();
        return fileName.endsWith(".vue") || fileName.endsWith(".html");
    }
} 