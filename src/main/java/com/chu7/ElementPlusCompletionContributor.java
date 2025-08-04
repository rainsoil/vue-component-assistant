package com.chu7;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;
import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.XmlTag;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.JsonObject;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Element Plus 组件补全贡献者
 * 负责为 XML 文件中的 Vue 组件提供智能补全功能
 */
public class ElementPlusCompletionContributor extends CompletionContributor {

    /**
     * 构造函数，注册补全提供者
     */
    public ElementPlusCompletionContributor() {
        // 为 XML 语言注册补全提供者
        extend(CompletionType.BASIC,
                PlatformPatterns.psiElement().withLanguage(com.intellij.lang.xml.XMLLanguage.INSTANCE),
                new CompletionProvider<CompletionParameters>() {
                    @Override
                    protected void addCompletions(@NotNull CompletionParameters parameters,
                                                  @NotNull ProcessingContext context,
                                                  @NotNull CompletionResultSet result) {
                        // 获取当前项目
                        Project project = parameters.getPosition().getProject();
                        if (project == null) return;

                        // 获取组件库设置
                        ElementLibrarySettings settings = ElementLibrarySettings.getInstance(project);
                        if (settings == null) return;

                        List<ComponentMeta> components = new ArrayList<>();

                        // 获取选中的组件库
                        List<String> selectedLibraries = settings.getSelectedLibraries();
                        boolean autoDetect = settings.isAutoDetect();

                        if (autoDetect && selectedLibraries.isEmpty()) {
                            // 自动检测模式：根据项目的 package.json 自动检测使用的组件库
                            String detected = detectLibrary(project);
                            if ("element-plus".equals(detected)) {
                                components.addAll(loadComponents("data/element-plus-components.json"));
                            } else if ("element-ui".equals(detected)) {
                                components.addAll(loadComponents("data/element-ui-components.json"));
                            } else if ("ant-design-vue".equals(detected)) {
                                components.addAll(loadComponents("data/ant-design-vue-components.json"));
                            } else {
                                // 未检测到，提供所有组件库的补全
                                Set<String> seen = new HashSet<>();
                                components.addAll(loadComponents("data/element-plus-components.json", seen));
                                components.addAll(loadComponents("data/element-ui-components.json", seen));
                                components.addAll(loadComponents("data/ant-design-vue-components.json", seen));
                            }
                        } else {
                            // 手动选择模式：根据用户选择的组件库提供补全
                            for (String lib : selectedLibraries) {
                                if ("element-plus".equals(lib)) {
                                    components.addAll(loadComponents("data/element-plus-components.json"));
                                } else if ("element-ui".equals(lib)) {
                                    components.addAll(loadComponents("data/element-ui-components.json"));
                                } else if ("ant-design-vue".equals(lib)) {
                                    components.addAll(loadComponents("data/ant-design-vue-components.json"));
                                }
                            }
                        }

                        // 获取组件库管理器
                        ComponentLibraryManager libraryManager = ComponentLibraryManager.getInstance(project);

                        // 获取当前标签名称
                        String currentTagName = getCurrentTagName(parameters);
                        ComponentMeta currentComponent = findComponentByTagName(currentTagName, components);

                        // 获取当前输入的文本
                        String currentText = getCurrentText(parameters);

                        // 根据输入内容决定显示什么补全
                        if (currentText != null && currentText.startsWith(":")) {
                            // 输入 : 时显示属性 - 只显示当前组件的属性
                            addPropsCompletion(result, components, libraryManager, currentComponent);
                        } else if (currentText != null && currentText.startsWith("@")) {
                            // 输入 @ 时显示事件 - 只显示当前组件的事件
                            addEventsCompletion(result, components, libraryManager, currentComponent);
                        } else if (currentText != null && currentText.startsWith("slot:")) {
                            // 输入 slot: 时显示卡槽 - 只显示当前组件的卡槽
                            addSlotsCompletion(result, components, libraryManager, currentComponent);
                        } else {
                            // 默认显示所有类型的补全
                            // 添加组件补全项
                            for (ComponentMeta comp : components) {
                                addComponentCompletion(result, comp, false);
                            }

                            // 添加自定义组件库中的组件
                            if (libraryManager != null) {
                                List<ComponentMeta> customComponents = libraryManager.getAllComponents();
                                for (ComponentMeta comp : customComponents) {
                                    addComponentCompletion(result, comp, true);
                                }
                            }

                            // 添加属性补全（不限制当前组件）
                            addPropsCompletion(result, components, libraryManager, null);

                            // 添加事件补全（不限制当前组件）
                            addEventsCompletion(result, components, libraryManager, null);

                            // 添加卡槽补全（不限制当前组件）
                            addSlotsCompletion(result, components, libraryManager, null);
                        }
                    }
                }
        );

        // 为属性值提供枚举补全
        extend(CompletionType.BASIC,
                PlatformPatterns.psiElement().withLanguage(com.intellij.lang.xml.XMLLanguage.INSTANCE),
                new CompletionProvider<CompletionParameters>() {
                    @Override
                    protected void addCompletions(@NotNull CompletionParameters parameters,
                                                  @NotNull ProcessingContext context,
                                                  @NotNull CompletionResultSet result) {
                        // 获取当前项目
                        Project project = parameters.getPosition().getProject();
                        if (project == null) return;

                        // 获取组件库设置
                        ElementLibrarySettings settings = ElementLibrarySettings.getInstance(project);
                        if (settings == null) return;

                        List<ComponentMeta> components = new ArrayList<>();

                        // 获取选中的组件库
                        List<String> selectedLibraries = settings.getSelectedLibraries();
                        boolean autoDetect = settings.isAutoDetect();

                        if (autoDetect && selectedLibraries.isEmpty()) {
                            // 自动检测模式
                            String detected = detectLibrary(project);
                            if ("element-plus".equals(detected)) {
                                components.addAll(loadComponents("data/element-plus-components.json"));
                            } else if ("element-ui".equals(detected)) {
                                components.addAll(loadComponents("data/element-ui-components.json"));
                            } else if ("ant-design-vue".equals(detected)) {
                                components.addAll(loadComponents("data/ant-design-vue-components.json"));
                            } else {
                                // 未检测到，提供所有组件库的补全
                                Set<String> seen = new HashSet<>();
                                components.addAll(loadComponents("data/element-plus-components.json", seen));
                                components.addAll(loadComponents("data/element-ui-components.json", seen));
                                components.addAll(loadComponents("data/ant-design-vue-components.json", seen));
                            }
                        } else {
                            // 手动选择模式
                            for (String lib : selectedLibraries) {
                                if ("element-plus".equals(lib)) {
                                    components.addAll(loadComponents("data/element-plus-components.json"));
                                } else if ("element-ui".equals(lib)) {
                                    components.addAll(loadComponents("data/element-ui-components.json"));
                                } else if ("ant-design-vue".equals(lib)) {
                                    components.addAll(loadComponents("data/ant-design-vue-components.json"));
                                }
                            }
                        }

                        // 添加自定义组件库中的组件
                        ComponentLibraryManager libraryManager = ComponentLibraryManager.getInstance(project);
                        if (libraryManager != null) {
                            List<ComponentMeta> customComponents = libraryManager.getAllComponents();
                            components.addAll(customComponents);
                        }

                        // 添加属性值枚举补全
                        addPropValueCompletion(result, components);
                    }
                }
        );
    }

    /**
     * 添加组件补全项
     * @param result 补全结果集
     * @param comp 组件元数据
     * @param isCustom 是否为自定义组件
     */
    private void addComponentCompletion(CompletionResultSet result, ComponentMeta comp, boolean isCustom) {
        String tailText = comp.description != null ? comp.description : "";
        if (comp.docUrl != null) {
            tailText += " 📖";
        }

        try {
            result.addElement(
                    LookupElementBuilder.create(comp.name)
                            .withTypeText(comp.version != null ? comp.version : (isCustom ? "自定义" : ""), true)
                            .withTailText("  " + tailText, true)
                            .withPresentableText(comp.name)
                            .withInsertHandler((insertionContext, item) -> {
                                if (comp.example != null && !comp.example.isEmpty()) {
                                    insertionContext.getDocument().replaceString(
                                            insertionContext.getStartOffset(),
                                            insertionContext.getTailOffset(),
                                            comp.example
                                    );
                                }
                            })
                            .withLookupString(comp.name)
                            .withBoldness(true)
            );
        } catch (Exception e) {
            // 如果创建失败，尝试创建一个简单的组件补全项
            try {
                result.addElement(
                        LookupElementBuilder.create(comp.name)
                                .withTypeText(comp.version != null ? comp.version : (isCustom ? "自定义" : ""), true)
                                .withTailText("  " + tailText, true)
                );
            } catch (Exception ignored) {
                // 如果连简单补全都失败，则跳过
            }
        }
    }

    /**
     * 添加属性补全
     * @param result 补全结果集
     * @param components 组件列表
     * @param libraryManager 组件库管理器
     * @param currentComponent 当前组件，如果为 null 则显示所有组件的属性
     */
    private void addPropsCompletion(CompletionResultSet result, List<ComponentMeta> components, ComponentLibraryManager libraryManager, ComponentMeta currentComponent) {
        // 如果找到了当前组件，只显示该组件的属性
        if (currentComponent != null) {
            addComponentProps(result, currentComponent);

            // 如果当前组件是自定义组件，也显示其属性
            if (libraryManager != null) {
                List<ComponentMeta> customComponents = libraryManager.getAllComponents();
                for (ComponentMeta comp : customComponents) {
                    if (comp.name.equals(currentComponent.name)) {
                        addComponentProps(result, comp);
                        break;
                    }
                }
            }
        } else {
            // 否则显示所有组件的属性
            for (ComponentMeta comp : components) {
                addComponentProps(result, comp);
            }

            // 添加自定义组件的属性
            if (libraryManager != null) {
                List<ComponentMeta> customComponents = libraryManager.getAllComponents();
                for (ComponentMeta comp : customComponents) {
                    addComponentProps(result, comp);
                }
            }
        }
    }

    /**
     * 添加单个组件的属性补全
     * @param result 补全结果集
     * @param comp 组件
     */
    private void addComponentProps(CompletionResultSet result, ComponentMeta comp) {
        // 检查属性列表是否为空
        if (comp.props == null || comp.props.isEmpty()) {
            return;
        }

        for (ComponentMeta.ComponentProp prop : comp.props) {
            if (prop == null || prop.name == null) {
                continue;
            }
            String propName = prop.name;
            String description = prop.description != null ? prop.description : "";
            String type = prop.type != null ? prop.type : "";
            String defaultValue = prop.defaultValue != null ? "默认值: " + prop.defaultValue : "";

            // 构建详细的提示信息
            StringBuilder tailText = new StringBuilder(description);
            if (!defaultValue.isEmpty()) {
                tailText.append(" (").append(defaultValue).append(")");
            }

            // 根据类型添加特殊提示
            final String insertText;
            if ("boolean".equals(type)) {
                tailText.append(" [true/false]");
                insertText = propName + "=\"true\"";
            } else if ("string".equals(type) && prop.options != null && !prop.options.isEmpty()) {
                tailText.append(" [").append(String.join("/", prop.options)).append("]");
                if (!prop.options.isEmpty()) {
                    insertText = propName + "=\"" + prop.options.get(0) + "\"";
                } else {
                    insertText = propName + "=\"\"";
                }
            } else if ("number".equals(type)) {
                tailText.append(" [数字]");
                insertText = propName + "=\"0\"";
            } else if ("function".equals(type)) {
                tailText.append(" [函数]");
                insertText = propName + "=\"() => {}\"";
            } else if ("object".equals(type)) {
                tailText.append(" [对象]");
                insertText = propName + "=\"{}\"";
            } else if ("array".equals(type)) {
                tailText.append(" [数组]");
                insertText = propName + "=\"[]\"";
            } else {
                insertText = propName + "=\"\"";
            }
            try {
                LookupElementBuilder lookupElementBuilder = LookupElementBuilder.create(propName)
                        .withTypeText(type, true)
                        .withTailText("  " + tailText.toString(), true)
                        .withPresentableText(propName)
                        .withInsertHandler((insertionContext, item) -> {
                            insertionContext.getDocument().replaceString(
                                    insertionContext.getStartOffset(),
                                    insertionContext.getTailOffset(),
                                    insertText
                            );
                        })
                        .withLookupString(propName);

                // 添加普通属性
                result.addElement(lookupElementBuilder);
            } catch (Exception e) {
                // 如果创建失败，尝试创建一个简单的补全项
                try {
                    result.addElement(
                            LookupElementBuilder.create(propName)
                                    .withTypeText(type, true)
                                    .withTailText("  " + tailText.toString(), true)
                    );
                } catch (Exception ignored) {
                    // 如果连简单补全都失败，则跳过
                }
            }

//            // 添加带 : 前缀的属性（动态绑定）
//            result.addElement(
//                    LookupElementBuilder.create(":" + propName)
//                            .withTypeText(type + " (动态绑定)", true)
//                            .withTailText("  " + tailText.toString(), true)
//                            .withPresentableText(":" + propName)
//                            .withInsertHandler((insertionContext, item) -> {
//                                insertionContext.getDocument().replaceString(
//                                        insertionContext.getStartOffset(),
//                                        insertionContext.getTailOffset(),
//                                        ":" + insertText
//                                );
//                            })
//                            .withLookupString(":" + propName)
//            );
        }
    }

    /**
     * 添加事件补全
     * @param result 补全结果集
     * @param components 组件列表
     * @param libraryManager 组件库管理器
     * @param currentComponent 当前组件，如果为 null 则显示所有组件的事件
     */
    private void addEventsCompletion(CompletionResultSet result, List<ComponentMeta> components, ComponentLibraryManager libraryManager, ComponentMeta currentComponent) {
        // 如果找到了当前组件，只显示该组件的事件
        if (currentComponent != null) {
            addComponentEvents(result, currentComponent);

            // 如果当前组件是自定义组件，也显示其事件
            if (libraryManager != null) {
                List<ComponentMeta> customComponents = libraryManager.getAllComponents();
                for (ComponentMeta comp : customComponents) {
                    if (comp.name.equals(currentComponent.name)) {
                        addComponentEvents(result, comp);
                        break;
                    }
                }
            }
        } else {
            // 否则显示所有组件的事件
            for (ComponentMeta comp : components) {
                addComponentEvents(result, comp);
            }

            // 添加自定义组件的事件
            if (libraryManager != null) {
                List<ComponentMeta> customComponents = libraryManager.getAllComponents();
                for (ComponentMeta comp : customComponents) {
                    addComponentEvents(result, comp);
                }
            }
        }
    }

    /**
     * 添加单个组件的事件补全
     * @param result 补全结果集
     * @param comp 组件
     */
    private void addComponentEvents(CompletionResultSet result, ComponentMeta comp) {
        // 检查事件列表是否为空
        if (comp.events == null || comp.events.isEmpty()) {
            return;
        }

        for (ComponentMeta.ComponentEvent event : comp.events) {
            if (event == null || event.name == null) {
                continue;
            }

            String eventName = "@" + event.name;
            String description = event.description != null ? event.description : "";
            String parameters = event.parameters != null ? event.parameters : "";

            // 构建示例代码
            final String insertText;
            if (!parameters.isEmpty()) {
                insertText = "@" + event.name + "=\"" + event.name + "Handler\"";
            } else {
                insertText = "@" + event.name + "=\"" + event.name + "Handler\"";
            }

            try {
                result.addElement(
                        LookupElementBuilder.create(eventName)
                                .withTypeText("事件", true)
                                .withTailText("  " + description + (parameters.isEmpty() ? "" : " (参数: " + parameters + ")"), true)
                                .withPresentableText(eventName)
                                .withInsertHandler((insertionContext, item) -> {
                                    insertionContext.getDocument().replaceString(
                                            insertionContext.getStartOffset(),
                                            insertionContext.getTailOffset(),
                                            insertText
                                    );
                                })
                                .withLookupString(event.name)
                );
            } catch (Exception e) {
                // 如果创建失败，尝试创建一个简单的事件补全项
                try {
                    result.addElement(
                            LookupElementBuilder.create(eventName)
                                    .withTypeText("事件", true)
                                    .withTailText("  " + description, true)
                    );
                } catch (Exception ignored) {
                    // 如果连简单补全都失败，则跳过
                }
            }
        }
    }

    /**
     * 添加卡槽补全
     * @param result 补全结果集
     * @param components 组件列表
     * @param libraryManager 组件库管理器
     * @param currentComponent 当前组件，如果为 null 则显示所有组件的卡槽
     */
    private void addSlotsCompletion(CompletionResultSet result, List<ComponentMeta> components, ComponentLibraryManager libraryManager, ComponentMeta currentComponent) {
        // 如果找到了当前组件，只显示该组件的卡槽
        if (currentComponent != null) {
            addComponentSlots(result, currentComponent);

            // 如果当前组件是自定义组件，也显示其卡槽
            if (libraryManager != null) {
                List<ComponentMeta> customComponents = libraryManager.getAllComponents();
                for (ComponentMeta comp : customComponents) {
                    if (comp.name.equals(currentComponent.name)) {
                        addComponentSlots(result, comp);
                        break;
                    }
                }
            }
        } else {
            // 否则显示所有组件的卡槽
            for (ComponentMeta comp : components) {
                addComponentSlots(result, comp);
            }

            // 添加自定义组件的卡槽
            if (libraryManager != null) {
                List<ComponentMeta> customComponents = libraryManager.getAllComponents();
                for (ComponentMeta comp : customComponents) {
                    addComponentSlots(result, comp);
                }
            }
        }
    }

    /**
     * 添加单个组件的卡槽补全
     * @param result 补全结果集
     * @param comp 组件
     */
    private void addComponentSlots(CompletionResultSet result, ComponentMeta comp) {
        // 检查卡槽列表是否为空
        if (comp.slots == null || comp.slots.isEmpty()) {
            return;
        }

        for (ComponentMeta.ComponentSlot slot : comp.slots) {
            if (slot == null || slot.name == null) {
                continue;
            }
            String slotName = "slot:" + slot.name;
            String description = slot.description != null ? slot.description : "";
            String scope = slot.scope != null ? slot.scope : "";

            // 构建示例代码
            final String insertText;
            if (!scope.isEmpty()) {
                insertText = "<template #" + slot.name + "=\"" + scope + "\">\n  <!-- " + description + " -->\n</template>";
            } else {
                insertText = "<template #" + slot.name + ">\n  <!-- " + description + " -->\n</template>";
            }

            try {
                result.addElement(
                        LookupElementBuilder.create(slotName)
                                .withTypeText("卡槽", true)
                                .withTailText("  " + description + (scope.isEmpty() ? "" : " (作用域: " + scope + ")"), true)
                                .withPresentableText(slotName)
                                .withInsertHandler((insertionContext, item) -> {
                                    insertionContext.getDocument().replaceString(
                                            insertionContext.getStartOffset(),
                                            insertionContext.getTailOffset(),
                                            insertText
                                    );
                                })
                                .withLookupString(slot.name)
                );
            } catch (Exception e) {
                // 如果创建失败，尝试创建一个简单的卡槽补全项
                try {
                    result.addElement(
                            LookupElementBuilder.create(slotName)
                                    .withTypeText("卡槽", true)
                                    .withTailText("  " + description, true)
                    );
                } catch (Exception ignored) {
                    // 如果连简单补全都失败，则跳过
                }
            }
        }
    }

    /**
     * 从资源文件加载组件数据
     * @param resourcePath 资源文件路径
     * @return 组件列表
     */
    private static List<ComponentMeta> loadComponents(String resourcePath) {
        try (InputStream is = ElementPlusCompletionContributor.class.getClassLoader()
                .getResourceAsStream(resourcePath)) {
            if (is == null) return List.of();
            String json = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            return new Gson().fromJson(json, new TypeToken<List<ComponentMeta>>() {
            }.getType());
        } catch (Exception e) {
            return List.of();
        }
    }

    /**
     * 从资源文件加载组件数据，并过滤重复的组件名称
     * @param resourcePath 资源文件路径
     * @param seen 已见过的组件名称集合
     * @return 过滤后的组件列表
     */
    private static List<ComponentMeta> loadComponents(String resourcePath, Set<String> seen) {
        try (InputStream is = ElementPlusCompletionContributor.class.getClassLoader()
                .getResourceAsStream(resourcePath)) {
            if (is == null) return List.of();
            String json = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            List<ComponentMeta> list = new Gson().fromJson(json, new TypeToken<List<ComponentMeta>>() {
            }.getType());
            return list.stream().filter(c -> seen.add(c.name)).collect(Collectors.toList());
        } catch (Exception e) {
            return List.of();
        }
    }

    /**
     * 获取当前标签名称
     * @param parameters 补全参数
     * @return 当前标签名称，如果无法获取则返回空字符串
     */
    private String getCurrentTagName(CompletionParameters parameters) {
        PsiElement element = parameters.getPosition();
        if (element == null) return "";


        // 向上查找最近的 XmlTag
        PsiElement parent = null;
        try {
            parent = element.getParent();
        } catch (Exception e) {
            e.printStackTrace();
//            throw new RuntimeException(e);
            return "";
        }
        while (parent != null && !(parent instanceof XmlTag)) {
            parent = parent.getParent();
        }

        if (parent instanceof XmlTag) {
            return ((XmlTag) parent).getName();
        }

        return "";
    }

    /**
     * 根据标签名称查找对应的组件
     * @param tagName 标签名称
     * @param components 组件列表
     * @return 对应的组件，如果未找到则返回 null
     */
    private ComponentMeta findComponentByTagName(String tagName, List<ComponentMeta> components) {
        if (tagName == null || tagName.isEmpty()) return null;

        for (ComponentMeta comp : components) {
            if (tagName.equals(comp.name)) {
                return comp;
            }
        }

        return null;
    }

    /**
     * 添加属性值枚举补全
     * @param result 补全结果集
     * @param components 组件列表
     */
    private void addPropValueCompletion(CompletionResultSet result, List<ComponentMeta> components) {
        for (ComponentMeta comp : components) {
            for (ComponentMeta.ComponentProp prop : comp.props) {
                // 只为有枚举值的属性提供值补全
                if (prop.options != null && !prop.options.isEmpty()) {
                    for (String option : prop.options) {
                        try {
                            result.addElement(
                                    LookupElementBuilder.create(option)
                                            .withTypeText("枚举值", true)
                                            .withTailText("  " + prop.description + " (" + prop.name + ")", true)
                                            .withPresentableText(option)
                                            .withLookupString(option)
                            );
                        } catch (Exception e) {
                            // 如果创建失败，尝试创建一个简单的枚举值补全项
                            try {
                                result.addElement(
                                        LookupElementBuilder.create(option)
                                                .withTypeText("枚举值", true)
                                                .withTailText("  " + prop.name, true)
                                );
                            } catch (Exception ignored) {
                                // 如果连简单补全都失败，则跳过
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * 获取当前输入的文本
     * @param parameters 补全参数
     * @return 当前输入的文本，如果无法获取则返回 null
     */
    private String getCurrentText(CompletionParameters parameters) {
        PsiElement element = parameters.getPosition();
        if (element == null) return null;

        // 获取当前文档
        com.intellij.openapi.editor.Document document = parameters.getEditor().getDocument();
        int offset = parameters.getOffset();

        // 获取当前行的文本
        int lineStart = document.getLineStartOffset(document.getLineNumber(offset));
        String lineText = document.getText().substring(lineStart, offset);

        // 检查是否在属性值位置（在 = 之后）
        if (lineText.contains("=")) {
            String[] parts = lineText.split("=");
            if (parts.length > 1) {
                String lastPart = parts[parts.length - 1].trim();
                if (lastPart.startsWith("\"") || lastPart.startsWith("'")) {
                    // 在属性值中，不触发特殊补全
                    return null;
                }
            }
        }

        // 简化逻辑：直接检查最后一个字符
        if (lineText.endsWith(":")) {
            return ":";
        } else if (lineText.endsWith("@")) {
            return "@";
        } else if (lineText.endsWith("slot:")) {
            return "slot:";
        }

        // 检查最后一个单词，支持带前缀的属性名和事件名
        String[] words = lineText.split("\\s+");
        if (words.length > 0) {
            String lastWord = words[words.length - 1];

            // 检查是否以 : 开头（属性）
            if (lastWord.startsWith(":")) {
                return ":";
            }

            // 检查是否以 @ 开头（事件）
            if (lastWord.startsWith("@")) {
                return "@";
            }

            // 检查是否以 slot: 开头（卡槽）
            if (lastWord.startsWith("slot:")) {
                return "slot:";
            }
        }

        return null;
    }

    /**
     * 检测项目中使用的组件库
     * 通过读取 package.json 文件来检测项目依赖的组件库
     * @param project 项目实例
     * @return 检测到的组件库名称，如果未检测到则返回空字符串
     */
    private static String detectLibrary(Project project) {
        if (project == null) return "";
        try {
            // 读取项目的 package.json 文件
            Path pkgPath = Path.of(project.getBasePath(), "package.json");
            if (!Files.exists(pkgPath)) return "";
            String json = Files.readString(pkgPath);
            JsonObject obj = new Gson().fromJson(json, JsonObject.class);

            // 获取依赖信息
            JsonObject deps = obj.has("dependencies") ? obj.getAsJsonObject("dependencies") : null;
            JsonObject devDeps = obj.has("devDependencies") ? obj.getAsJsonObject("devDependencies") : null;

            // 检查 Element Plus
            if (deps != null && deps.has("element-plus")) return "element-plus";
            if (devDeps != null && devDeps.has("element-plus")) return "element-plus";

            // 检查 Element UI
            if (deps != null && deps.has("element-ui")) return "element-ui";
            if (devDeps != null && devDeps.has("element-ui")) return "element-ui";

            // 检查 Ant Design Vue
            if (deps != null && deps.has("ant-design-vue")) return "ant-design-vue";
            if (devDeps != null && devDeps.has("ant-design-vue")) return "ant-design-vue";

        } catch (Exception ignored) {
            // 忽略解析异常，返回空字符串表示未检测到
        }
        return "";
    }
}