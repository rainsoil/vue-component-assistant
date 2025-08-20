package com.chu7.vuecomponentassistant.xml;

import com.chu7.vuecomponentassistant.completion2.ComponentProvider;
import com.chu7.vuecomponentassistant.completion2.ComponentProviderManager;
import com.chu7.vuecomponentassistant.completion2.ElementPlusComponent;
import com.chu7.vuecomponentassistant.settings.ProjectSettingsManager;
import com.chu7.vuecomponentassistant.utils.VueKitLogger;
import com.intellij.codeInsight.completion.XmlTagInsertHandler;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.source.xml.XmlElementDescriptorProvider;
import com.intellij.psi.xml.XmlTag;
import com.intellij.xml.XmlElementDescriptor;
import com.intellij.xml.XmlNSDescriptor;
import com.intellij.xml.XmlTagNameProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * VueKit XML 标签名称提供者
 * 
 * <p>功能说明：</p>
 * <ul>
 *   <li>提供高优先级的 XML/HTML 标签补全</li>
 *   <li>支持 Vue 组件库的标签自动完成</li>
 *   <li>与 Completion Contributor 配合，提供双重补全保障</li>
 *   <li>确保组件提示不被 IDEA 默认提示覆盖</li>
 * </ul>
 * 
 * <p>设计特点：</p>
 * <ul>
 *   <li>实现 XmlElementDescriptorProvider 和 XmlTagNameProvider 接口</li>
 *   <li>动态获取项目中的组件提供者</li>
 *   <li>支持多种组件库的标签补全</li>
 *   <li>提供智能的标签描述符</li>
 * </ul>
 * 
 * @author VueKit Team
 * @version 1.0.0
 * @since 1.0.0
 * @see com.intellij.xml.XmlTagNameProvider
 * @see com.intellij.psi.impl.source.xml.XmlElementDescriptorProvider
 * @see com.chu7.vuecomponentassistant.completion2.ComponentProviderManager
 */
public class VueKitTagNameProvider implements XmlElementDescriptorProvider, XmlTagNameProvider {

    private static final Logger LOG = VueKitLogger.getLogger(VueKitTagNameProvider.class);

    /**
     * 添加标签名称变体
     * 
     * <p>在输入 < 后执行，提供组件标签的自动完成建议</p>
     * 
     * @param list 补全建议列表
     * @param xmlTag 当前 XML 标签
     * @param s 搜索字符串
     */
    @Override
    public void addTagNameVariants(List<LookupElement> list, @NotNull XmlTag xmlTag, String s) {
        try {
            Project project = xmlTag.getProject();
            if (project == null) {
                return;
            }

            // 检查项目设置是否启用组件补全
            ProjectSettingsManager projectSettingsManager = ProjectSettingsManager.getInstance(project);
            ProjectSettingsManager.ProjectSettings projectSettings = projectSettingsManager.getProjectSettings(project);
            if (!projectSettings.isEnableComponentCompletion()) {
                return;
            }

            // 获取组件提供者
            ComponentProvider componentProvider = ComponentProviderManager.getProvider(project);
            if (componentProvider == null) {
                return;
            }

            // 获取所有可用的组件
            List<ElementPlusComponent> components = componentProvider.getAllComponents();
            
            for (ElementPlusComponent component : components) {
                String tagName = component.getName();
                if (tagName != null && !tagName.isEmpty()) {
                    // 创建带插入处理器的查找元素
                    LookupElementBuilder element = LookupElementBuilder.create(tagName)
                            .withInsertHandler(XmlTagInsertHandler.INSTANCE)
                            .withTypeText("VueKit Component")
                            .withIcon(null);
                    
                    list.add(element);
                }
            }

            VueKitLogger.info(LOG, "Added {} component tag variants", components.size());
            
        } catch (Exception e) {
            VueKitLogger.error(LOG, "Error adding tag name variants", e);
        }
    }

    /**
     * 获取标签描述符
     * 
     * <p>为 Vue 组件标签提供智能的描述符，支持属性补全和文档提示</p>
     * 
     * @param xmlTag XML 标签
     * @return 标签描述符，如果不是 Vue 组件则返回 null
     */
    @Nullable
    @Override
    public XmlElementDescriptor getDescriptor(XmlTag xmlTag) {
        try {
            Project project = xmlTag.getProject();
            if (project == null) {
                return null;
            }

            String tagName = xmlTag.getName();
            if (tagName == null || tagName.isEmpty()) {
                return null;
            }

            // 获取组件提供者
            ComponentProvider componentProvider = ComponentProviderManager.getProvider(project);
            if (componentProvider == null) {
                return null;
            }

            // 查找对应的组件
            ElementPlusComponent component = componentProvider.getComponent(tagName);
            if (component == null) {
                return null;
            }

            // 获取命名空间描述符
            final XmlNSDescriptor nsDescriptor = xmlTag.getNSDescriptor(xmlTag.getNamespace(), false);
            
            // 创建 VueKit 特定的元素描述符
            return new VueKitXmlElementDescriptor(component, nsDescriptor, tagName);
            
        } catch (Exception e) {
            VueKitLogger.error(LOG, "Error getting descriptor for tag: " + xmlTag.getName(), e);
            return null;
        }
    }
} 