package com.chu7.vuecomponentassistant.completion;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.ProcessingContext;
import com.chu7.vuecomponentassistant.utils.CustomComponentLibraryManager;
import com.chu7.vuecomponentassistant.settings.PluginSettings;
import com.chu7.vuecomponentassistant.utils.VueKitLogger;
import com.chu7.vuecomponentassistant.constants.VueKitConstants;
import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Vue 组件智能补全提供者
 * 
 * 功能说明：
 * 1. 组件补全：输入 < 时提供当前组件库的组件列表
 * 2. 属性补全：在组件标签内输入空格时提供该组件的属性列表
 * 3. 事件补全：输入 @ 时提供该组件的事件列表
 * 4. 插槽补全：输入 sl 或 slot 时提供该组件的插槽列表
 * 
 * 智能特性：
 * - 根据前缀过滤，只显示匹配的选项
 * - 自动插入完整的标签结构
 * - 提供详细的中文描述和文档链接
 * - 支持作用域插槽的完整模板生成
 * - 动态检测项目使用的组件库（Element UI、Element Plus、Ant Design Vue）
 * 
 * @author VueKit Team
 * @version 2.0.0
 */
public class ElementPlusTestCompletionProvider extends CompletionProvider<CompletionParameters> {

    private static final Logger LOG = VueKitLogger.getLogger(ElementPlusTestCompletionProvider.class);

    /** 组件数据提供者，负责加载和管理组件数据 */
    private ComponentProvider componentProvider;

    /**
     * 构造函数
     * 组件提供者将在 addCompletions 中根据项目动态创建
     */
    public ElementPlusTestCompletionProvider() {
        // 组件提供者将在 addCompletions 中根据项目动态创建
    }

    /**
     * 主要的补全方法，由 IntelliJ IDEA 调用
     * 
     * 工作流程：
     * 1. 获取当前光标位置的上下文信息
     * 2. 分析用户输入的内容和位置
     * 3. 根据上下文类型提供相应的补全选项
     * 4. 将补全结果添加到结果集中
     * 
     * @param parameters 补全参数，包含光标位置等信息
     * @param context 处理上下文
     * @param result 补全结果集，用于添加补全选项
     */
    @Override
    protected void addCompletions(@NotNull CompletionParameters parameters,
                                  @NotNull ProcessingContext context,
                                  @NotNull CompletionResultSet result) {

        // 检查补全设置
        PluginSettings settings = PluginSettings.getInstance();
        if (!settings.isEnableComponentCompletion()) {
            return; // 如果组件补全被禁用，直接返回
        }

        // 获取当前项目
        Project project = parameters.getEditor().getProject();
        if (project == null) {
            return;
        }

        // 根据项目动态创建组件提供者
        if (componentProvider == null) {
            componentProvider = new ComponentProvider(project);
        }

        // 获取当前光标位置的 PSI 元素
        PsiElement element = parameters.getPosition();
        PsiFile file = element.getContainingFile();

        // 安全检查：确保文件存在
        if (file == null) {
            return;
        }

        // 分析当前位置的上下文，确定用户想要什么类型的补全
        CompletionContext completionContext = analyzeContext(file, element);

        // 添加详细的调试信息，帮助开发者了解补全过程
        VueKitLogger.debug(LOG, "=== 补全调试信息 ===");
        VueKitLogger.debug(LOG, "文件: " + file.getName());
        VueKitLogger.debug(LOG, "位置: " + element.getTextOffset());
        VueKitLogger.debug(LOG, "当前文本: " + element.getText());
        VueKitLogger.debug(LOG, "上下文类型: " + completionContext.getType());
        VueKitLogger.debug(LOG, "当前组件: " + completionContext.getCurrentComponent());
        VueKitLogger.debug(LOG, "前缀: " + completionContext.getPrefix());
        VueKitLogger.debug(LOG, "组件库: " + componentProvider.getLibraryDisplayName());

        // 根据上下文类型提供相应的补全选项
        switch (completionContext.getType()) {
            case COMPONENT:
                // 组件补全：用户输入 < 时显示组件列表
                addComponentCompletions(result, completionContext.getPrefix());
                break;
            case ATTRIBUTE:
                // 属性补全：在组件标签内输入空格时显示属性列表
                if (settings.isEnableAttributeCompletion()) {
                    addAttributeCompletions(result, completionContext.getCurrentComponent(), completionContext.getPrefix());
                }
                break;
            case EVENT:
                // 事件补全：输入 @ 时显示事件列表
                if (settings.isEnableEventCompletion()) {
                    addEventCompletions(result, completionContext.getCurrentComponent(), completionContext.getPrefix());
                }
                break;
            case SLOT:
                // 插槽补全：输入 sl 或 slot 时显示插槽列表
                if (settings.isEnableSlotCompletion()) {
                    addSlotCompletions(result, completionContext.getCurrentComponent(), completionContext.getPrefix());
                }
                break;
        }
    }

    /**
     * 添加组件补全选项
     * 
     * 功能说明：
     * - 当用户输入 < 时，提供当前组件库的组件列表
     * - 支持前缀过滤，只显示匹配的组件
     * - 自动插入完整的组件标签结构
     * - 限制显示数量，避免选项过多影响用户体验
     * 
     * @param result 补全结果集
     * @param prefix 用户输入的前缀，用于过滤组件
     */
    private void addComponentCompletions(CompletionResultSet result, String prefix) {
        List<ElementPlusComponent> components;

        // 根据用户输入的前缀过滤组件
        if (prefix != null && !prefix.isEmpty()) {
            // 有前缀时，只显示匹配的组件
            components = componentProvider.searchComponents(prefix);
        } else {
            // 无前缀时，显示所有可用组件
            components = componentProvider.getAllComponents();
        }

        // 添加调试信息
        VueKitLogger.debug(LOG, "=== 组件补全调试信息 ===");
        VueKitLogger.debug(LOG, "前缀: '" + prefix + "'");
        VueKitLogger.debug(LOG, "找到组件数量: " + components.size());
        
        // 显示前几个组件的详细信息
        for (int i = 0; i < Math.min(5, components.size()); i++) {
            ElementPlusComponent component = components.get(i);
            VueKitLogger.debug(LOG, "组件 " + (i + 1) + ": " + component.getName() + 
                             " (库: " + componentProvider.getComponentLibraryDisplayName(component.getName()) + ")");
        }
        
        // 检查自定义组件库
        List<CustomComponentLibraryManager.CustomLibraryConfig> customLibraries = 
            CustomComponentLibraryManager.getAllCustomLibraries();
        VueKitLogger.debug(LOG, "自定义组件库数量: " + customLibraries.size());
        for (CustomComponentLibraryManager.CustomLibraryConfig config : customLibraries) {
            VueKitLogger.debug(LOG, "自定义库: " + config.getDisplayName() + " (前缀: " + config.getComponentPrefix() + ")");
            VueKitLogger.debug(LOG, "  组件数量: " + config.getComponents().size());
            for (ElementPlusComponent component : config.getComponents()) {
                VueKitLogger.debug(LOG, "    - " + component.getName());
            }
        }

        // 限制显示数量，避免过多选项影响用户体验
        int count = 0;
        int maxCount = 30; // 最多显示30个组件

        for (ElementPlusComponent component : components) {
            if (count >= maxCount) break;

            // 创建组件补全元素
            LookupElementBuilder element = LookupElementBuilder.create(component.getName())
                    .withTypeText(componentProvider.getComponentLibraryDisplayName(component.getName()) + " Component") // 动态显示类型标识
                    .withTailText(" " + component.getDescription()) // 显示组件描述
                    .withIcon(ElementPlusIcons.COMPONENT_ICON) // 设置组件图标
                    .withInsertHandler((insertContext, item) -> {
                        // 插入处理器：自动补全组件标签
                        Editor editor = insertContext.getEditor();
                        int offset = insertContext.getTailOffset();
                        // 插入完整的结束标签，光标定位到标签内容位置
                        editor.getDocument().insertString(offset, "></" + component.getName() + ">");
                        editor.getCaretModel().moveToOffset(offset);
                    });

            result.addElement(element);
            count++;
        }
    }

    /**
     * 添加属性补全
     */
    private void addAttributeCompletions(CompletionResultSet result, String componentName, String prefix) {
        if (componentName == null) {
            return;
        }

        ElementPlusComponent component = componentProvider.getComponent(componentName);
        if (component == null) {
            return;
        }

        VueKitLogger.debug(LOG, "属性补全: 为组件 " + componentName + " 添加属性");
        VueKitLogger.debug(LOG, "组件属性数量: " + component.getProps().size());
        VueKitLogger.debug(LOG, "前缀: '" + prefix + "'");

        int count = 0;
        for (ElementPlusProp prop : component.getProps()) {
            // 根据前缀过滤属性
            if (prefix != null && !prefix.isEmpty() && !prop.getName().toLowerCase().contains(prefix.toLowerCase())) {
                VueKitLogger.debug(LOG, "属性过滤: " + prop.getName() + " 不包含前缀 '" + prefix + "'");
                continue;
            } else {
                VueKitLogger.debug(LOG, "属性匹配: " + prop.getName() + " 包含前缀 '" + prefix + "'");
            }

            LookupElementBuilder propElement = LookupElementBuilder.create(prop.getName())
                    .withTypeText("Property")
                    .withTailText(" " + prop.getDescription())
                    .withIcon(ElementPlusIcons.PROPERTY_ICON)
                    .withInsertHandler((insertContext, item) -> {
                        // 插入属性名和等号
                        Editor editor = insertContext.getEditor();
                        int offset = insertContext.getTailOffset();
                        String defaultValue = prop.getDefaultValueAsString();
                        editor.getDocument().insertString(offset, "=\"" + defaultValue + "\"");
                        editor.getCaretModel().moveToOffset(offset - 1);
                    });

            result.addElement(propElement);
            count++;
        }

        VueKitLogger.debug(LOG, "添加了 " + count + " 个属性补全");
    }

    /**
     * 添加事件补全
     */
    private void addEventCompletions(CompletionResultSet result, String componentName, String prefix) {
        if (componentName == null) {
                    VueKitLogger.debug(LOG, "事件补全: 组件名为空");
        return;
    }

    ElementPlusComponent component = componentProvider.getComponent(componentName);
    if (component == null) {
        VueKitLogger.debug(LOG, "事件补全: 找不到组件 " + componentName);
        return;
    }

    VueKitLogger.debug(LOG, "事件补全: 为组件 " + componentName + " 添加事件");
    VueKitLogger.debug(LOG, "组件事件数量: " + component.getEvents().size());
    VueKitLogger.debug(LOG, "前缀: '" + prefix + "'");

        int count = 0;
        for (ElementPlusEvent event : component.getEvents()) {
            // 根据前缀过滤事件
            if (prefix != null && !prefix.isEmpty()) {
                String eventName = event.getName().toLowerCase();
                String prefixLower = prefix.toLowerCase();
                if (!eventName.contains(prefixLower)) {
                    System.out.println("事件过滤: " + event.getName() + " 不包含前缀 '" + prefix + "'");
                    continue;
                } else {
                    System.out.println("事件匹配: " + event.getName() + " 包含前缀 '" + prefix + "'");
                }
            }

            LookupElementBuilder eventElement = LookupElementBuilder.create("@" + event.getName())
                    .withTypeText("Event")
                    .withTailText(" " + event.getDescription())
                    .withIcon(ElementPlusIcons.EVENT_ICON)
                    .withInsertHandler((insertContext, item) -> {
                        // 插入事件处理函数
                        Editor editor = insertContext.getEditor();
                        int offset = insertContext.getTailOffset();
                        String handlerName = "handle" + capitalize(event.getName());
                        editor.getDocument().insertString(offset, "=\"" + handlerName + "\"");
                        editor.getCaretModel().moveToOffset(offset - 1);
                    });

            result.addElement(eventElement);
            count++;
        }

        System.out.println("添加了 " + count + " 个事件补全");
    }

    /**
     * 添加插槽补全选项
     * 
     * 功能说明：
     * - 当用户输入 sl 或 slot 时，提供当前组件的插槽列表
     * - 只显示当前组件支持的插槽，避免混淆
     * - 自动生成完整的插槽模板，包括作用域支持
     * - 提供详细的中文描述和说明
     * 
     * 插槽模板格式：
     * - 有作用域：<template #slotName="scope"> <!-- 描述 --> </template>
     * - 无作用域：<template #slotName> <!-- 描述 --> </template>
     * 
     * @param result 补全结果集
     * @param componentName 当前组件名称
     * @param prefix 用户输入的前缀，用于过滤插槽
     */
    private void addSlotCompletions(CompletionResultSet result, String componentName, String prefix) {
        // 安全检查：确保组件名称不为空
        if (componentName == null) {
            System.out.println("插槽补全: 组件名为空");
            return;
        }

        // 获取组件详细信息
        ElementPlusComponent component = componentProvider.getComponent(componentName);
        if (component == null) {
            System.out.println("插槽补全: 找不到组件 " + componentName);
            return;
        }

        System.out.println("插槽补全: 为组件 " + componentName + " 添加插槽");
        System.out.println("组件插槽数量: " + component.getSlots().size());
        System.out.println("前缀: '" + prefix + "'");

        // 检查插槽列表是否为空
        if (component.getSlots() == null || component.getSlots().isEmpty()) {
            System.out.println("插槽补全: 组件没有插槽");
            return;
        }

        int count = 0;
        for (ElementPlusSlot slot : component.getSlots()) {
            // 跳过无效的插槽数据
            if (slot == null || slot.getName() == null) {
                continue;
            }
            
            // 构建插槽显示名称和描述
            String slotName = "slot:" + slot.getName();
            String description = slot.getDescription() != null ? slot.getDescription() : "";
            String scope = slot.getScope() != null ? slot.getScope() : "";
            
            // 根据是否有作用域构建不同的模板
            final String insertText;
            if (!scope.isEmpty()) {
                // 有作用域的插槽模板
                insertText = "<template #" + slot.getName() + "=\"" + scope + "\">\n  <!-- " + description + " -->\n</template>";
            } else {
                // 无作用域的插槽模板
                insertText = "<template #" + slot.getName() + ">\n  <!-- " + description + " -->\n</template>";
            }
            try {
                // 创建插槽补全元素
                result.addElement(
                        LookupElementBuilder.create(slotName)
                                .withTypeText("卡槽", true) // 显示类型为"卡槽"
                                .withTailText("  " + description + (scope.isEmpty() ? "" : " (作用域: " + scope + ")"), true) // 显示描述和作用域
                                .withPresentableText(slotName) // 设置显示文本
                                .withInsertHandler((insertionContext, item) -> {
                                    // 插入处理器：替换当前文本为完整的插槽模板
                                    insertionContext.getDocument().replaceString(
                                            insertionContext.getStartOffset(),
                                            insertionContext.getTailOffset(),
                                            insertText
                                    );
                                })
                                .withLookupString(slot.getName()) // 设置查找字符串
                );
                count++;
            } catch (Exception e) {
                // 如果创建失败，尝试创建一个简单的卡槽补全项
                try {
                    result.addElement(
                            LookupElementBuilder.create(slotName)
                                    .withTypeText("卡槽", true)
                                    .withTailText("  " + description, true)
                    );
                    count++;
                } catch (Exception ignored) {
                    // 如果连简单补全都失败，则跳过
                }
            }
        }

        System.out.println("添加了 " + count + " 个插槽补全");

        // 强制刷新补全结果集，确保插槽选项能够正确显示
        if (count > 0) {
            result.stopHere();
            System.out.println("强制停止补全，确保插槽选项显示");
        }
    }

    /**
     * 分析补全上下文，确定用户想要什么类型的补全
     * 
     * 分析逻辑：
     * 1. 检查当前文本是否以特殊字符开头（@、slot、sl）
     * 2. 检查 beforeText 是否包含特殊符号
     * 3. 检查是否在组件标签位置
     * 4. 检查是否在属性位置
     * 5. 检查是否在组件名称位置
     * 
     * 返回的上下文类型：
     * - COMPONENT: 组件补全（输入 < 时）
     * - ATTRIBUTE: 属性补全（在组件标签内输入空格时）
     * - EVENT: 事件补全（输入 @ 时）
     * - SLOT: 插槽补全（输入 sl 或 slot 时）
     * 
     * @param file 当前文件
     * @param element 当前光标位置的 PSI 元素
     * @return 补全上下文，包含类型、当前组件和前缀信息
     */
    private CompletionContext analyzeContext(PsiFile file, PsiElement element) {
        String fileText = file.getText();
        int offset = element.getTextOffset();
        String beforeText = fileText.substring(0, offset);
        String currentText = element.getText();

        System.out.println("分析上下文 - beforeText: '" + beforeText + "'");
        System.out.println("当前文本: '" + currentText + "'");

        // 检查当前文本是否以@开头
        if (currentText.startsWith("@")) {
            System.out.println("当前文本以@开头，直接检查事件");
            String currentComponent = getCurrentComponent(beforeText);
            // 去掉@符号，并处理IntelliJ IDEA的后缀
            String eventPrefix = extractValidEventPrefix(currentText.substring(1));

            if (currentComponent != null && eventPrefix != null) {
                System.out.println("返回事件上下文: " + currentComponent + " 前缀: " + eventPrefix);
                return new CompletionContext(CompletionType.EVENT, currentComponent, eventPrefix);
            } else {
                System.out.println("事件检测失败 - 组件: " + currentComponent + ", 前缀: " + eventPrefix);
            }
        }

        // 检查当前文本是否以slot或sl开头（先清理IntelliJ IDEA后缀）
        String cleanCurrentTextForSlot = currentText.replace("IntellijIdeaRulezzz", "");
        if (cleanCurrentTextForSlot.startsWith("slot") || cleanCurrentTextForSlot.startsWith("sl")) {
            System.out.println("当前文本以slot或sl开头，直接检查插槽");
            System.out.println("清理后的文本: '" + cleanCurrentTextForSlot + "'");
            String currentComponent = getCurrentComponent(beforeText);

            // 去掉前缀，获取插槽名称前缀
            String slotPrefix = "";
            if (cleanCurrentTextForSlot.startsWith("slot")) {
                slotPrefix = cleanCurrentTextForSlot.substring(4); // 去掉"slot"
            } else if (cleanCurrentTextForSlot.startsWith("sl")) {
                slotPrefix = cleanCurrentTextForSlot.substring(2); // 去掉"sl"
            }

            // 如果没有找到组件，尝试从beforeText中查找
            if (currentComponent == null) {
                currentComponent = getCurrentComponent(beforeText);
            }

            if (currentComponent != null) {
                System.out.println("返回插槽上下文: " + currentComponent + " 前缀: " + slotPrefix);
                return new CompletionContext(CompletionType.SLOT, currentComponent, slotPrefix);
            } else {
                System.out.println("插槽检测失败 - 找不到组件");
            }
        }


        // 检查beforeText是否包含@符号
        if (beforeText.contains("@")) {
            System.out.println("beforeText包含@符号，检查事件");
            String currentComponent = getCurrentComponent(beforeText);
            String eventPrefix = getEventPrefix(beforeText);

            if (currentComponent != null && eventPrefix != null) {
                System.out.println("返回事件上下文: " + currentComponent + " 前缀: " + eventPrefix);
                return new CompletionContext(CompletionType.EVENT, currentComponent, eventPrefix);
            } else {
                System.out.println("事件检测失败 - 组件: " + currentComponent + ", 前缀: " + eventPrefix);
            }
        }

        // 检查beforeText是否包含slot或sl符号
        if (beforeText.contains("slot") || beforeText.contains("sl")) {
            System.out.println("beforeText包含slot或sl符号，检查插槽");
            String currentComponent = getCurrentComponent(beforeText);
            String slotPrefix = getSlotPrefix(beforeText);

            if (currentComponent != null) {
                System.out.println("返回插槽上下文: " + currentComponent + " 前缀: " + slotPrefix);
                return new CompletionContext(CompletionType.SLOT, currentComponent, slotPrefix);
            } else {
                System.out.println("插槽检测失败 - 找不到组件");
            }
        }


        // 检查是否在组件标签位置
        if (beforeText.endsWith("<") || beforeText.matches(".*<\\s*$")) {
            System.out.println("检测到组件标签位置");
            
            // 检查当前文本是否包含组件前缀
            String cleanCurrentText = currentText.replace("IntellijIdeaRulezzz", "");
            System.out.println("清理后的当前文本: '" + cleanCurrentText + "'");
            
            // 如果当前文本看起来像组件前缀（包含字母、数字、连字符）
            if (cleanCurrentText.matches("[a-zA-Z][a-zA-Z0-9-]*")) {
                System.out.println("当前文本看起来像组件前缀，返回组件前缀上下文: '" + cleanCurrentText + "'");
                return new CompletionContext(CompletionType.COMPONENT, null, cleanCurrentText);
            }
            
            return new CompletionContext(CompletionType.COMPONENT, null, null);
        }

        // 获取当前组件
        String currentComponent = getCurrentComponent(beforeText);
        System.out.println("当前组件: " + currentComponent);

        // 检查是否在属性位置（在组件标签内，有空格但没有=）
        if (currentComponent != null && isInComponentTag(beforeText)) {
            String prefix = getAttributePrefix(beforeText);
            System.out.println("检测到属性位置，前缀: '" + prefix + "'");
            return new CompletionContext(CompletionType.ATTRIBUTE, currentComponent, prefix);
        }

        // 检查是否在组件名称位置
        String componentPrefix = getComponentPrefix(beforeText);
        if (componentPrefix != null) {
            System.out.println("检测到组件名称位置，前缀: '" + componentPrefix + "'");
            return new CompletionContext(CompletionType.COMPONENT, null, componentPrefix);
        }
        
        // 如果 beforeText 以 < 开头但不在组件标签位置，检查当前文本
        if (beforeText.contains("<") && !beforeText.endsWith("<")) {
            String cleanCurrentText = currentText.replace("IntellijIdeaRulezzz", "");
            System.out.println("beforeText 包含 < 但不以 < 结尾，检查当前文本: '" + cleanCurrentText + "'");
            
            // 如果当前文本看起来像组件前缀
            if (cleanCurrentText.matches("[a-zA-Z][a-zA-Z0-9-]*")) {
                System.out.println("当前文本看起来像组件前缀，返回组件前缀上下文: '" + cleanCurrentText + "'");
                return new CompletionContext(CompletionType.COMPONENT, null, cleanCurrentText);
            }
        }

        System.out.println("默认返回组件上下文");
        return new CompletionContext(CompletionType.COMPONENT, null, null);
    }

    /**
     * 获取当前组件名称
     */
    private String getCurrentComponent(String beforeText) {
        System.out.println("getCurrentComponent - 输入 beforeText: '" + beforeText + "'");
        
        Pattern componentPattern = Pattern.compile("<([a-zA-Z][a-zA-Z0-9-]*)\\b");
        Matcher matcher = componentPattern.matcher(beforeText);
        String lastComponent = null;
        while (matcher.find()) {
            lastComponent = matcher.group(1);
            System.out.println("getCurrentComponent - 找到组件: '" + lastComponent + "'");
        }

        // 使用ComponentProvider动态检测组件库
        if (lastComponent != null && componentProvider != null) {
            boolean isFromCurrentLibrary = componentProvider.isComponentFromCurrentLibrary(lastComponent);
            System.out.println("getCurrentComponent - 组件 '" + lastComponent + "' 是否来自当前库: " + isFromCurrentLibrary);
            if (isFromCurrentLibrary) {
                return lastComponent;
            }
        }

        System.out.println("getCurrentComponent - 返回 null");
        return null;
    }

    /**
     * 获取组件前缀
     */
    private String getComponentPrefix(String beforeText) {
        System.out.println("getComponentPrefix - 输入 beforeText: '" + beforeText + "'");
        
        if (beforeText.endsWith("<")) {
            System.out.println("getComponentPrefix - beforeText 以 < 结尾，返回空字符串");
            return "";
        }

        // 查找最近的<符号
        int lastOpenTag = beforeText.lastIndexOf('<');
        System.out.println("getComponentPrefix - lastOpenTag: " + lastOpenTag);
        
        if (lastOpenTag >= 0) {
            String afterOpenTag = beforeText.substring(lastOpenTag + 1);
            System.out.println("getComponentPrefix - afterOpenTag: '" + afterOpenTag + "'");
            
            // 检查是否包含空格或>符号，如果包含说明已经输入了属性或标签结束
            boolean containsSpace = afterOpenTag.contains(" ");
            boolean containsCloseTag = afterOpenTag.contains(">");
            System.out.println("getComponentPrefix - containsSpace: " + containsSpace + ", containsCloseTag: " + containsCloseTag);
            
            if (!containsSpace && !containsCloseTag) {
                System.out.println("getComponentPrefix - 返回前缀: '" + afterOpenTag + "'");
                return afterOpenTag;
            } else {
                System.out.println("getComponentPrefix - afterOpenTag 包含空格或>符号，返回 null");
            }
        }

        System.out.println("getComponentPrefix - 返回 null");
        return null;
    }

    /**
     * 检查是否在组件标签内
     */
    private boolean isInComponentTag(String beforeText) {
        // 查找最近的<和>符号
        int lastOpenTag = beforeText.lastIndexOf('<');
        int lastCloseTag = beforeText.lastIndexOf('>');

        System.out.println("检查是否在组件标签内 - lastOpenTag: " + lastOpenTag + ", lastCloseTag: " + lastCloseTag);

        // 如果最近的<在>之后，说明在标签内
        if (lastOpenTag > lastCloseTag) {
            // 检查是否在当前组件库的组件标签内
            String tagContent = beforeText.substring(lastOpenTag);
            System.out.println("标签内容: '" + tagContent + "'");
            
            // 如果标签内容以<开头但没有完整的组件名（如<my-），则不在组件标签内
            if (tagContent.startsWith("<") && !tagContent.contains(" ") && !tagContent.contains(">")) {
                // 检查是否有完整的组件名（至少包含一个字母数字字符）
                String afterOpenTag = tagContent.substring(1);
                if (afterOpenTag.matches("[a-zA-Z][a-zA-Z0-9-]*")) {
                    // 有完整的组件名，检查是否属于当前库
                    if (componentProvider != null) {
                        boolean isCurrentLibrary = componentProvider.isComponentFromCurrentLibrary(afterOpenTag);
                        System.out.println("组件: " + afterOpenTag + ", 是否当前库: " + isCurrentLibrary);
                        return isCurrentLibrary;
                    }
                } else {
                    // 没有完整的组件名，不在组件标签内
                    System.out.println("没有完整的组件名，不在组件标签内");
                    return false;
                }
            }
            
            // 检查是否包含已知的组件前缀（包括自定义组件库）
            boolean containsKnownPrefix = tagContent.contains("el-") || 
                                        tagContent.contains("my-") || 
                                        tagContent.contains("ant-");
            System.out.println("包含已知前缀: " + containsKnownPrefix);
            return containsKnownPrefix;
        }

        System.out.println("不在组件标签内");
        return false;
    }

    /**
     * 获取属性前缀
     */
    private String getAttributePrefix(String beforeText) {
        // 查找最近的<符号
        int lastOpenTag = beforeText.lastIndexOf('<');
        if (lastOpenTag >= 0) {
            String afterOpenTag = beforeText.substring(lastOpenTag + 1);

            // 如果已经输入了空格，说明在属性位置
            if (afterOpenTag.contains(" ")) {
                // 获取最后一个空格后的内容
                int lastSpace = afterOpenTag.lastIndexOf(' ');
                if (lastSpace >= 0) {
                    String afterLastSpace = afterOpenTag.substring(lastSpace + 1);

                    // 如果没有=，说明正在输入属性名
                    if (!afterLastSpace.contains("=")) {
                        return afterLastSpace;
                    }
                }
            }
        }
        return null;
    }

    /**
     * 获取事件前缀
     */
    private String getEventPrefix(String beforeText) {
        // 查找最近的@符号
        int lastAt = beforeText.lastIndexOf('@');
        System.out.println("查找@符号 - lastAt: " + lastAt);

        if (lastAt >= 0) {
            String afterAt = beforeText.substring(lastAt + 1);
            System.out.println("@符号后的内容: '" + afterAt + "'");

            // 如果没有=，说明正在输入事件名
            if (!afterAt.contains("=")) {
                // 简化逻辑：直接提取有效的事件名前缀
                String validPrefix = extractValidEventPrefix(afterAt);
                if (validPrefix != null) {
                    System.out.println("提取到有效事件前缀: '" + validPrefix + "'");
                    return validPrefix;
                } else {
                    System.out.println("无法提取有效事件前缀");
                }
            } else {
                System.out.println("事件前缀检测失败 - afterAt: '" + afterAt + "' 包含等号");
            }
        } else {
            System.out.println("事件前缀检测失败 - 未找到@符号");
        }
        return null;
    }

    /**
     * 提取有效的事件名前缀
     */
    private String extractValidEventPrefix(String text) {
        if (text == null || text.isEmpty()) {
            return text; // 返回空字符串而不是null
        }

        System.out.println("提取事件前缀 - 原始文本: '" + text + "'");

        // 直接替换掉IntelliJ IDEA的后缀
        String cleanText = text.replace("IntellijIdeaRulezzz", "");
        System.out.println("清理后的文本: '" + cleanText + "'");

        // 如果清理后为空，返回空字符串
        if (cleanText.isEmpty()) {
            return cleanText;
        }

        // 查找第一个非字母数字字符的位置
        for (int i = 0; i < cleanText.length(); i++) {
            char c = cleanText.charAt(i);
            if (!Character.isLetterOrDigit(c) && c != '-') {
                if (i > 0) {
                    String result = cleanText.substring(0, i);
                    System.out.println("提取到事件前缀: '" + result + "'");
                    return result;
                }
                return null;
            }
        }

        // 如果全部都是有效字符，返回整个字符串
        System.out.println("返回完整事件前缀: '" + cleanText + "'");
        return cleanText;
    }

    /**
     * 提取有效的插槽名前缀
     */
    private String extractValidSlotPrefix(String text) {
        if (text == null || text.isEmpty()) {
            return text; // 返回空字符串而不是null
        }

        System.out.println("提取插槽前缀 - 原始文本: '" + text + "'");

        // 直接替换掉IntelliJ IDEA的后缀
        String cleanText = text.replace("IntellijIdeaRulezzz", "");
        System.out.println("清理后的文本: '" + cleanText + "'");

        // 如果清理后为空，返回空字符串
        if (cleanText.isEmpty()) {
            return cleanText;
        }

        // 查找第一个非字母数字字符的位置
        for (int i = 0; i < cleanText.length(); i++) {
            char c = cleanText.charAt(i);
            if (!Character.isLetterOrDigit(c) && c != '-') {
                if (i > 0) {
                    String result = cleanText.substring(0, i);
                    System.out.println("提取到插槽前缀: '" + result + "'");
                    return result;
                }
                return null;
            }
        }

        // 如果全部都是有效字符，返回整个字符串
        System.out.println("返回完整插槽前缀: '" + cleanText + "'");
        return cleanText;
    }

    /**
     * 获取插槽前缀
     */
    private String getSlotPrefix(String beforeText) {
        // 查找最近的slot或sl符号
        int lastSlot = beforeText.lastIndexOf("slot");
        int lastSl = beforeText.lastIndexOf("sl");
        int lastIndex = Math.max(lastSlot, lastSl);

        System.out.println("查找slot或sl符号 - lastSlot: " + lastSlot + ", lastSl: " + lastSl + ", lastIndex: " + lastIndex);

        if (lastIndex >= 0) {
            String afterPrefix;
            if (lastSlot > lastSl) {
                afterPrefix = beforeText.substring(lastSlot + 4); // 4是"slot"的长度
                System.out.println("slot符号后的内容: '" + afterPrefix + "'");
            } else {
                afterPrefix = beforeText.substring(lastSl + 2); // 2是"sl"的长度
                System.out.println("sl符号后的内容: '" + afterPrefix + "'");
            }

            // 清理IntelliJ IDEA后缀
            afterPrefix = afterPrefix.replace("IntellijIdeaRulezzz", "");

            // 如果没有=，说明正在输入插槽名
            if (!afterPrefix.contains("=")) {
                // 直接返回清理后的前缀
                System.out.println("提取到插槽前缀: '" + afterPrefix + "'");
                return afterPrefix;
            } else {
                System.out.println("插槽前缀检测失败 - afterPrefix: '" + afterPrefix + "' 包含等号");
            }
        } else {
            System.out.println("插槽前缀检测失败 - 未找到slot或sl符号");
        }
        return null;
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    /**
     * 补全上下文类型枚举
     * 
     * 定义了四种补全类型：
     * - COMPONENT: 组件补全，用户输入 < 时触发
     * - ATTRIBUTE: 属性补全，在组件标签内输入空格时触发
     * - EVENT: 事件补全，用户输入 @ 时触发
     * - SLOT: 插槽补全，用户输入 sl 或 slot 时触发
     */
    private enum CompletionType {
        COMPONENT, ATTRIBUTE, EVENT, SLOT
    }

    /**
     * 补全上下文类
     * 
     * 用于封装补全相关的上下文信息，包括：
     * - 补全类型（组件、属性、事件、插槽）
     * - 当前组件名称
     * - 用户输入的前缀
     * 
     * 这个类帮助补全系统理解用户的意图并提供相应的补全选项
     */
    private static class CompletionContext {
        /** 补全类型 */
        private final CompletionType type;
        /** 当前组件名称 */
        private final String currentComponent;
        /** 用户输入的前缀，用于过滤补全选项 */
        private final String prefix;

        /**
         * 构造函数
         * 
         * @param type 补全类型
         * @param currentComponent 当前组件名称
         * @param prefix 用户输入的前缀
         */
        public CompletionContext(CompletionType type, String currentComponent, String prefix) {
            this.type = type;
            this.currentComponent = currentComponent;
            this.prefix = prefix;
        }

        /**
         * 获取补全类型
         * @return 补全类型枚举值
         */
        public CompletionType getType() {
            return type;
        }

        /**
         * 获取当前组件名称
         * @return 当前组件名称，可能为 null
         */
        public String getCurrentComponent() {
            return currentComponent;
        }

        /**
         * 获取用户输入的前缀
         * @return 用户输入的前缀，用于过滤补全选项
         */
        public String getPrefix() {
            return prefix;
        }
    }
}
