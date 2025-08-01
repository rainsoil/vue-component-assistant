package com.chu7;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

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
                        
                        // 添加组件补全项
                        for (ComponentMeta comp : components) {
                            addComponentCompletion(result, comp, false);
                        }
                        
                        // 添加自定义组件库中的组件
                        ComponentLibraryManager libraryManager = ComponentLibraryManager.getInstance(project);
                        if (libraryManager != null) {
                            List<ComponentMeta> customComponents = libraryManager.getAllComponents();
                            for (ComponentMeta comp : customComponents) {
                                addComponentCompletion(result, comp, true);
                            }
                        }
                        
                        // 添加属性补全
                        addPropsCompletion(result, components, libraryManager);
                        
                        // 添加事件补全
                        addEventsCompletion(result, components, libraryManager);
                        
                        // 添加卡槽补全
                        addSlotsCompletion(result, components, libraryManager);
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
    }
    
    /**
     * 添加属性补全
     * @param result 补全结果集
     * @param components 组件列表
     * @param libraryManager 组件库管理器
     */
    private void addPropsCompletion(CompletionResultSet result, List<ComponentMeta> components, ComponentLibraryManager libraryManager) {
        for (ComponentMeta comp : components) {
            for (ComponentMeta.ComponentProp prop : comp.props) {
                String propName = prop.name;
                String description = prop.description != null ? prop.description : "";
                String type = prop.type != null ? prop.type : "";
                String defaultValue = prop.defaultValue != null ? "默认值: " + prop.defaultValue : "";
                
                result.addElement(
                    LookupElementBuilder.create(propName)
                        .withTypeText(type, true)
                        .withTailText("  " + description + (defaultValue.isEmpty() ? "" : " (" + defaultValue + ")"), true)
                        .withPresentableText(propName)
                        .withLookupString(propName)
                );
            }
        }
        
        // 添加自定义组件的属性
        if (libraryManager != null) {
            List<ComponentMeta> customComponents = libraryManager.getAllComponents();
            for (ComponentMeta comp : customComponents) {
                for (ComponentMeta.ComponentProp prop : comp.props) {
                    String propName = prop.name;
                    String description = prop.description != null ? prop.description : "";
                    String type = prop.type != null ? prop.type : "";
                    
                    result.addElement(
                        LookupElementBuilder.create(propName)
                            .withTypeText(type, true)
                            .withTailText("  " + description, true)
                            .withPresentableText(propName)
                            .withLookupString(propName)
                    );
                }
            }
        }
    }
    
    /**
     * 添加事件补全
     * @param result 补全结果集
     * @param components 组件列表
     * @param libraryManager 组件库管理器
     */
    private void addEventsCompletion(CompletionResultSet result, List<ComponentMeta> components, ComponentLibraryManager libraryManager) {
        for (ComponentMeta comp : components) {
            for (ComponentMeta.ComponentEvent event : comp.events) {
                String eventName = event.name;
                String description = event.description != null ? event.description : "";
                String parameters = event.parameters != null ? "参数: " + event.parameters : "";
                
                result.addElement(
                    LookupElementBuilder.create(eventName)
                        .withTypeText("事件", true)
                        .withTailText("  " + description + (parameters.isEmpty() ? "" : " (" + parameters + ")"), true)
                        .withPresentableText(eventName)
                        .withLookupString(eventName)
                );
            }
        }
        
        // 添加自定义组件的事件
        if (libraryManager != null) {
            List<ComponentMeta> customComponents = libraryManager.getAllComponents();
            for (ComponentMeta comp : customComponents) {
                for (ComponentMeta.ComponentEvent event : comp.events) {
                    String eventName = event.name;
                    String description = event.description != null ? event.description : "";
                    
                    result.addElement(
                        LookupElementBuilder.create(eventName)
                            .withTypeText("事件", true)
                            .withTailText("  " + description, true)
                            .withPresentableText(eventName)
                            .withLookupString(eventName)
                    );
                }
            }
        }
    }
    
    /**
     * 添加卡槽补全
     * @param result 补全结果集
     * @param components 组件列表
     * @param libraryManager 组件库管理器
     */
    private void addSlotsCompletion(CompletionResultSet result, List<ComponentMeta> components, ComponentLibraryManager libraryManager) {
        for (ComponentMeta comp : components) {
            for (ComponentMeta.ComponentSlot slot : comp.slots) {
                String slotName = slot.name;
                String description = slot.description != null ? slot.description : "";
                String scope = slot.scope != null ? "作用域: " + slot.scope : "";
                
                result.addElement(
                    LookupElementBuilder.create(slotName)
                        .withTypeText("卡槽", true)
                        .withTailText("  " + description + (scope.isEmpty() ? "" : " (" + scope + ")"), true)
                        .withPresentableText(slotName)
                        .withLookupString(slotName)
                );
            }
        }
        
        // 添加自定义组件的卡槽
        if (libraryManager != null) {
            List<ComponentMeta> customComponents = libraryManager.getAllComponents();
            for (ComponentMeta comp : customComponents) {
                for (ComponentMeta.ComponentSlot slot : comp.slots) {
                    String slotName = slot.name;
                    String description = slot.description != null ? slot.description : "";
                    
                    result.addElement(
                        LookupElementBuilder.create(slotName)
                            .withTypeText("卡槽", true)
                            .withTailText("  " + description, true)
                            .withPresentableText(slotName)
                            .withLookupString(slotName)
                    );
                }
            }
        }
    }

    private static List<ComponentMeta> loadComponents(String resourcePath) {
        try (InputStream is = ElementPlusCompletionContributor.class.getClassLoader()
                .getResourceAsStream(resourcePath)) {
            if (is == null) return List.of();
            String json = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            return new Gson().fromJson(json, new TypeToken<List<ComponentMeta>>(){}.getType());
        } catch (Exception e) {
            return List.of();
        }
    }

    private static List<ComponentMeta> loadComponents(String resourcePath, Set<String> seen) {
        try (InputStream is = ElementPlusCompletionContributor.class.getClassLoader()
                .getResourceAsStream(resourcePath)) {
            if (is == null) return List.of();
            String json = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            List<ComponentMeta> list = new Gson().fromJson(json, new TypeToken<List<ComponentMeta>>(){}.getType());
            return list.stream().filter(c -> seen.add(c.name)).collect(Collectors.toList());
        } catch (Exception e) {
            return List.of();
        }
    }

    private static String detectLibrary(Project project) {
        if (project == null) return "";
        try {
            Path pkgPath = Path.of(project.getBasePath(), "package.json");
            if (!Files.exists(pkgPath)) return "";
            String json = Files.readString(pkgPath);
            JsonObject obj = new Gson().fromJson(json, JsonObject.class);
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
            
        } catch (Exception ignored) {}
        return "";
    }
}