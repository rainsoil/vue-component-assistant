package com.chu7.vuecomponentassistant.xml;

import com.chu7.vuecomponentassistant.completion2.ComponentProvider;
import com.chu7.vuecomponentassistant.completion2.ComponentProviderManager;
import com.chu7.vuecomponentassistant.completion2.ElementPlusComponent;
import com.chu7.vuecomponentassistant.completion2.ElementPlusProp;
import com.chu7.vuecomponentassistant.service.FrameworkDetectService;
import com.chu7.vuecomponentassistant.utils.VueKitLogger;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

/**
 * VueKit XML 属性值提供者
 * 
 * @author VueKit Team
 * @version 1.0.0
 * @since 1.0.0
 */
public class VueKitXmlAttributeValueProvider extends CompletionProvider<CompletionParameters> {

    private static final Logger LOG = VueKitLogger.getLogger(VueKitXmlAttributeValueProvider.class);

    @Override
    protected void addCompletions(@NotNull CompletionParameters parameters,
                                  @NotNull ProcessingContext context,
                                  @NotNull CompletionResultSet result) {
        try {
            PsiElement position = parameters.getPosition();
            if (!(position.getParent() instanceof XmlAttribute)) {
                return;
            }

            XmlAttribute attribute = (XmlAttribute) position.getParent();
            XmlTag tag = attribute.getParent();
            if (tag == null) {
                return;
            }

            Project project = tag.getProject();
            if (project == null) {
                return;
            }

            // 检查框架检测服务是否支持当前项目
            FrameworkDetectService frameworkDetectService = FrameworkDetectService.getInstance(project);
            if (!frameworkDetectService.isSupportedFramework()) {
                return;
            }

            String tagName = tag.getName();
            String attributeName = attribute.getName();
            if (tagName == null || attributeName == null) {
                return;
            }

            // 获取组件提供者
            ComponentProvider componentProvider = ComponentProviderManager.getProvider(project);
            if (componentProvider == null) {
                return;
            }

            // 查找对应的组件
            ElementPlusComponent component = componentProvider.getComponent(tagName);
            if (component == null) {
                return;
            }

            // 查找对应的属性
            ElementPlusProp prop = findPropertyByName(component, attributeName);
            if (prop == null) {
                return;
            }

            // 提供属性值建议
            provideAttributeValueSuggestions(prop, result);

        } catch (Exception e) {
            VueKitLogger.error(LOG, "Error providing attribute value completions", e);
        }
    }

    /**
     * 根据名称查找属性
     */
    private ElementPlusProp findPropertyByName(ElementPlusComponent component, String attributeName) {
        if (component.getProps() == null) {
            return null;
        }

        for (ElementPlusProp prop : component.getProps()) {
            if (attributeName.equals(prop.getName())) {
                return prop;
            }
        }
        return null;
    }

    /**
     * 提供属性值建议
     */
    private void provideAttributeValueSuggestions(ElementPlusProp prop, CompletionResultSet result) {
        // 如果有默认值，提供默认值建议
        if (prop.getDefaultValue() != null) {
            String defaultValue = String.valueOf(prop.getDefaultValue());
            if (!defaultValue.isEmpty()) {
                LookupElementBuilder defaultElement = LookupElementBuilder.create(defaultValue)
                        .withTypeText("默认值")
                        .withTailText(" - " + prop.getDescription(), true);
                result.addElement(defaultElement);
            }
        }

        // 如果有选项值，提供选项建议
        if (prop.getOptions() != null && !prop.getOptions().isEmpty()) {
            for (String option : prop.getOptions()) {
                LookupElementBuilder optionElement = LookupElementBuilder.create(option)
                        .withTypeText("可选值")
                        .withTailText(" - " + prop.getDescription(), true);
                result.addElement(optionElement);
            }
        }

        // 根据类型提供常用值建议
        String type = prop.getType();
        if (type != null) {
            switch (type.toLowerCase()) {
                case "boolean":
                    result.addElement(LookupElementBuilder.create("true").withTypeText("布尔值"));
                    result.addElement(LookupElementBuilder.create("false").withTypeText("布尔值"));
                    break;
                case "array":
                    result.addElement(LookupElementBuilder.create("[]").withTypeText("空数组"));
                    result.addElement(LookupElementBuilder.create("[{}]").withTypeText("对象数组"));
                    break;
                case "object":
                    result.addElement(LookupElementBuilder.create("{}").withTypeText("空对象"));
                    break;
                case "string":
                    if (prop.getName().equals("data")) {
                        result.addElement(LookupElementBuilder.create("[]").withTypeText("表格数据数组"));
                        result.addElement(LookupElementBuilder.create("[{}]").withTypeText("表格数据对象数组"));
                    }
                    break;
            }
        }
    }
}
