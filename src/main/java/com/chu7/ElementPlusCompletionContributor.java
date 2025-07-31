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


public class ElementPlusCompletionContributor extends CompletionContributor {
    public ElementPlusCompletionContributor() {
        extend(CompletionType.BASIC,
                PlatformPatterns.psiElement().withLanguage(com.intellij.lang.xml.XMLLanguage.INSTANCE),
                new CompletionProvider<CompletionParameters>() {
                    @Override
                    protected void addCompletions(@NotNull CompletionParameters parameters,
                                                  @NotNull ProcessingContext context,
                                                  @NotNull CompletionResultSet result) {
                        Project project = parameters.getPosition().getProject();
                        if (project == null) return;
                        ElementLibrarySettings settings = ElementLibrarySettings.getInstance(project);
                        if (settings == null) return;
                        String lib = settings.getLibrary();
                        List<ComponentMeta> components;
                        if ("element-plus".equals(lib)) {
                            components = loadComponents("data/element-plus-components.json");
                        } else if ("element-ui".equals(lib)) {
                            components = loadComponents("data/element-ui-components.json");
                        } else if ("ant-design-vue".equals(lib)) {
                            components = loadComponents("data/ant-design-vue-components.json");
                        } else {
                            // auto: 检查 package.json
                            String detected = detectLibrary(project);
                            if ("element-plus".equals(detected)) {
                                components = loadComponents("data/element-plus-components.json");
                            } else if ("element-ui".equals(detected)) {
                                components = loadComponents("data/element-ui-components.json");
                            } else if ("ant-design-vue".equals(detected)) {
                                components = loadComponents("data/ant-design-vue-components.json");
                            } else {
                                // 未检测到，全部补全
                                components = new ArrayList<>();
                                Set<String> seen = new HashSet<>();
                                components.addAll(loadComponents("data/element-plus-components.json", seen));
                                components.addAll(loadComponents("data/element-ui-components.json", seen));
                                components.addAll(loadComponents("data/ant-design-vue-components.json", seen));
                            }
                        }
                        for (ComponentMeta comp : components) {
                            String tailText = comp.description != null ? comp.description : "";
                            if (comp.docUrl != null) {
                                tailText += " 📖";
                            }
                            
                            result.addElement(
                                LookupElementBuilder.create(comp.name)
                                    .withTypeText(comp.version, true)
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
                                    .withBoldness(true) // 加粗显示
//                                    .withTypeIcon(com.intellij.icons.AllIcons.Nodes.Class) // 添加图标
                            );
                        }
                        
                        // 添加自定义组件
                        CustomComponentManager customManager = CustomComponentManager.getInstance(project);
                        if (customManager != null) {
                            List<ComponentMeta> customComponents = customManager.getCustomComponents();
                            for (ComponentMeta comp : customComponents) {
                                String tailText = comp.description != null ? comp.description : "";
                                if (comp.docUrl != null) {
                                    tailText += " 📖";
                                }
                                
                                result.addElement(
                                    LookupElementBuilder.create(comp.name)
                                        .withTypeText(comp.version != null ? comp.version : "自定义", true)
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
//                                        .withTypeIcon(com.intellij.icons.AllIcons.Nodes.Custom) // 自定义组件图标
                                );
                            }
                        }
                    }
                }
        );
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