package com.chu7.vuecomponentassistant.settings;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.ui.FormBuilder;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Vue Component 插件设置配置页面
 * 
 * <p>功能说明：</p>
 * <ul>
 *   <li>提供Vue组件助手的图形化设置界面</li>
 *   <li>管理插件的核心功能开关和配置选项</li>
 *   <li>集成组件库管理功能</li>
 *   <li>支持设置的实时预览和修改检测</li>
 *   <li>提供直观的用户配置体验</li>
 * </ul>
 * 
 * <p>设计特点：</p>
 * <ul>
 *   <li>IntelliJ IDEA集成：实现Configurable接口</li>
 *   <li>响应式界面：使用FormBuilder构建布局</li>
 *   <li>实时反馈：支持修改检测和状态同步</li>
 *   <li>组件库集成：提供组件库管理入口</li>
 *   <li>用户友好：使用中文标签和图标</li>
 * </ul>
 * 
 * <p>配置选项：</p>
 * <ol>
 *   <li>自动补全：控制代码补全功能的启用状态</li>
 *   <li>文档提示：控制组件文档的显示功能</li>
 *   <li>智能上下文：控制上下文分析功能的启用</li>
 *   <li>右键菜单：控制右键菜单功能的启用</li>
 *   <li>组件库管理：提供组件库的管理入口</li>
 * </ol>
 * 
 * <p>使用场景：</p>
 * <ul>
 *   <li>插件设置界面的显示</li>
 *   <li>用户配置的修改和保存</li>
 *   <li>组件库管理的快速访问</li>
 *   <li>插件功能的开关控制</li>
 *   <li>设置状态的实时预览</li>
 * </ul>
 * 
 * @author VueKit Team
 * @version 2.0.0
 * @since 1.0.0
 * @see com.intellij.openapi.options.Configurable
 * @see com.intellij.openapi.options.ConfigurationException
 * @see com.intellij.util.ui.FormBuilder
 * @see com.intellij.ui.components.JBCheckBox
 * @see com.intellij.ui.components.JBLabel
 */
public class VueComponentSettingsConfigurable implements Configurable {
    
    /**
     * 自动补全功能启用复选框
     * 控制Vue组件代码补全功能的启用状态
     */
    private JBCheckBox enableAutoCompletion;
    
    /**
     * 文档提示功能启用复选框
     * 控制组件文档显示功能的启用状态
     */
    private JBCheckBox enableDocumentation;
    
    /**
     * 智能上下文分析功能启用复选框
     * 控制基于上下文的智能分析功能启用状态
     */
    private JBCheckBox enableSmartContext;
    
    /**
     * 右键菜单功能启用复选框
     * 控制右键菜单中Vue组件相关功能的启用状态
     */
    private JBCheckBox enableRightClickMenu;
    
    /**
     * 组件库管理按钮
     * 提供快速访问组件库管理功能的入口
     */
    private JButton manageComponentLibrariesButton;
    
    /**
     * 组件库信息显示标签
     * 显示当前组件库的统计信息和状态
     */
    private JBLabel componentLibrariesInfoLabel;
    
    /**
     * 主设置面板
     * 包含所有设置选项和组件库管理功能的容器
     */
    private JPanel mainPanel;
    
    /**
     * 获取设置页面的显示名称
     * 
     * <p>该方法返回在IntelliJ IDEA设置界面中显示的页面标题。
     * 使用@Nls注解确保国际化支持。</p>
     * 
     * @return 设置页面的显示名称，用于在设置树中标识此页面
     * 
     * @see org.jetbrains.annotations.Nls
     * @see com.intellij.openapi.options.Configurable#getDisplayName()
     */
    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getDisplayName() {
        return "Vue Component Assistant";
    }
    
    /**
     * 创建设置界面的主要组件
     * 
     * <p>该方法构建并返回设置界面的主要UI组件，包括功能开关复选框、
     * 组件库管理面板和相关信息显示。</p>
     * 
     * <p>界面构建流程：</p>
     * <ol>
     *   <li>创建功能开关复选框</li>
     *   <li>构建组件库管理面板</li>
     *   <li>设置组件库管理按钮的事件处理</li>
     *   <li>使用FormBuilder组装主面板</li>
     *   <li>返回完整的设置界面</li>
     * </ol>
     * 
     * <p>界面组件：</p>
     * <ul>
     *   <li>功能开关：4个复选框控制核心功能</li>
     *   <li>组件库管理：带边框的管理面板</li>
     *   <li>信息显示：组件库统计信息标签</li>
     *   <li>操作按钮：组件库管理入口按钮</li>
     * </ul>
     * 
     * @return 包含所有设置选项的JComponent面板
     * 
     * @see com.intellij.util.ui.FormBuilder
     * @see com.intellij.ui.components.JBCheckBox
     * @see com.intellij.ui.components.JBLabel
     */
    @Nullable
    @Override
    public JComponent createComponent() {
        enableAutoCompletion = new JBCheckBox("启用自动补全", true);
        enableDocumentation = new JBCheckBox("启用文档提示", true);
        enableSmartContext = new JBCheckBox("启用智能上下文分析", true);
        enableRightClickMenu = new JBCheckBox("启用右键菜单", true);
        
        // 组件库管理按钮
        manageComponentLibrariesButton = new JButton("📚 组件库管理");
        manageComponentLibrariesButton.setPreferredSize(new Dimension(200, 30));
        manageComponentLibrariesButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 获取当前打开的项目
                Project[] projects = com.intellij.openapi.project.ProjectManager.getInstance().getOpenProjects();
                if (projects.length > 0) {
                    // 打开组件库管理对话框
                    com.chu7.vuecomponentassistant.ui.ComponentLibraryManagementDialog dialog = 
                        new com.chu7.vuecomponentassistant.ui.ComponentLibraryManagementDialog(projects[0]);
                    dialog.show();
                    // 更新信息显示
                    updateComponentLibrariesInfo();
                } else {
                    JOptionPane.showMessageDialog(
                        mainPanel,
                        "请先打开一个项目，然后再次尝试。",
                        "提示",
                        JOptionPane.INFORMATION_MESSAGE
                    );
                }
            }
        });
        
        // 组件库信息标签
        componentLibrariesInfoLabel = new JBLabel("正在加载组件库信息...");
        updateComponentLibrariesInfo();
        
        // 创建组件库管理面板
        JPanel componentLibraryPanel = new JPanel(new BorderLayout());
        componentLibraryPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        JPanel componentLibraryHeaderPanel = new JPanel(new BorderLayout());
        componentLibraryHeaderPanel.add(new JBLabel("📚 组件库管理"), BorderLayout.WEST);
        componentLibraryHeaderPanel.add(manageComponentLibrariesButton, BorderLayout.EAST);
        
        JPanel componentLibraryContentPanel = new JPanel(new BorderLayout());
        componentLibraryContentPanel.add(componentLibrariesInfoLabel, BorderLayout.CENTER);
        componentLibraryContentPanel.add(new JBLabel("功能：查看、新增、删除、导出组件库，导出模板"), BorderLayout.SOUTH);
        
        componentLibraryPanel.add(componentLibraryHeaderPanel, BorderLayout.NORTH);
        componentLibraryPanel.add(componentLibraryContentPanel, BorderLayout.CENTER);
        
        // 创建主面板
        mainPanel = FormBuilder.createFormBuilder()
            .addComponent(enableAutoCompletion)
            .addComponent(enableDocumentation)
            .addComponent(enableSmartContext)
            .addComponent(enableRightClickMenu)
            .addComponent(componentLibraryPanel)
            .addComponentFillVertically(new JPanel(), 0)
            .getPanel();
        
        return mainPanel;
    }
    
    /**
     * 更新组件库信息显示
     * 
     * <p>该方法更新组件库信息标签的显示内容，包括统计信息和状态。
     * 目前显示静态信息，为未来扩展预留接口。</p>
     * 
     * <p>显示内容：</p>
     * <ul>
     *   <li>总组件数量统计</li>
     *   <li>组件库数量统计</li>
     *   <li>各库组件分布信息</li>
     *   <li>加载状态和错误信息</li>
     * </ul>
     * 
     * <p>错误处理：</p>
     * <ul>
     *   <li>捕获所有异常并显示错误信息</li>
     *   <li>确保界面不会因错误而崩溃</li>
     *   <li>提供用户友好的错误提示</li>
     * </ul>
     * 
     * @see com.intellij.ui.components.JBLabel#setText(String)
     */
    private void updateComponentLibrariesInfo() {
        try {
            // 暂时显示静态信息
            StringBuilder info = new StringBuilder();
            info.append("📊 组件库统计信息：\n");
            info.append("• 总组件数量：0\n");
            info.append("• 组件库数量：0\n");
            info.append("• 各库组件分布：暂无数据\n");
            
            componentLibrariesInfoLabel.setText("<html>" + info.toString().replace("\n", "<br>") + "</html>");
            
        } catch (Exception e) {
            componentLibrariesInfoLabel.setText("加载组件库信息失败: " + e.getMessage());
        }
    }
    
    /**
     * 检查设置是否已被修改
     * 
     * <p>该方法比较当前界面上的设置值与已保存的设置值，判断用户是否进行了修改。
     * 用于在设置界面中显示"应用"按钮的状态。</p>
     * 
     * <p>检查项目：</p>
     * <ul>
     *   <li>自动补全功能的启用状态</li>
     *   <li>文档提示功能的启用状态</li>
     *   <li>智能上下文功能的启用状态</li>
     *   <li>右键菜单功能的启用状态</li>
     * </ul>
     * 
     * @return 如果设置已被修改则返回true，否则返回false
     * 
     * @see VueComponentSettings#getInstance()
     * @see VueComponentSettings#isAutoCompletionEnabled()
     * @see VueComponentSettings#isDocumentationEnabled()
     * @see VueComponentSettings#isSmartContextEnabled()
     * @see VueComponentSettings#isRightClickMenuEnabled()
     */
    @Override
    public boolean isModified() {
        VueComponentSettings settings = VueComponentSettings.getInstance();
        return enableAutoCompletion.isSelected() != settings.isAutoCompletionEnabled() ||
               enableDocumentation.isSelected() != settings.isDocumentationEnabled() ||
               enableSmartContext.isSelected() != settings.isSmartContextEnabled() ||
               enableRightClickMenu.isSelected() != settings.isRightClickMenuEnabled();
    }
    
    /**
     * 应用设置更改
     * 
     * <p>该方法将界面上的设置值保存到VueComponentSettings中，实现设置的持久化。
     * 在用户点击"应用"按钮时调用。</p>
     * 
     * <p>保存流程：</p>
     * <ol>
     *   <li>获取当前设置实例</li>
     *   <li>读取界面上的复选框状态</li>
     *   <li>更新设置对象的相应属性</li>
     *   <li>设置自动保存到磁盘</li>
     * </ol>
     * 
     * @throws ConfigurationException 当应用设置失败时抛出，目前不会抛出此异常
     * 
     * @see VueComponentSettings#getInstance()
     * @see VueComponentSettings#setAutoCompletionEnabled(boolean)
     * @see VueComponentSettings#setDocumentationEnabled(boolean)
     * @see VueComponentSettings#setSmartContextEnabled(boolean)
     * @see VueComponentSettings#setRightClickMenuEnabled(boolean)
     */
    @Override
    public void apply() throws ConfigurationException {
        VueComponentSettings settings = VueComponentSettings.getInstance();
        settings.setAutoCompletionEnabled(enableAutoCompletion.isSelected());
        settings.setDocumentationEnabled(enableDocumentation.isSelected());
        settings.setSmartContextEnabled(enableSmartContext.isSelected());
        settings.setRightClickMenuEnabled(enableRightClickMenu.isSelected());
    }
    
    /**
     * 重置设置到已保存的值
     * 
     * <p>该方法将界面上的设置值重置为已保存的设置值，用于取消用户的修改。
     * 在用户点击"重置"按钮时调用。</p>
     * 
     * <p>重置流程：</p>
     * <ol>
     *   <li>获取当前设置实例</li>
     *   <li>读取已保存的设置值</li>
     *   <li>更新界面上的复选框状态</li>
     *   <li>同步界面显示</li>
     * </ol>
     * 
     * @see VueComponentSettings#getInstance()
     * @see VueComponentSettings#isAutoCompletionEnabled()
     * @see VueComponentSettings#isDocumentationEnabled()
     * @see VueComponentSettings#isSmartContextEnabled()
     * @see VueComponentSettings#isRightClickMenuEnabled()
     */
    @Override
    public void reset() {
        VueComponentSettings settings = VueComponentSettings.getInstance();
        enableAutoCompletion.setSelected(settings.isAutoCompletionEnabled());
        enableDocumentation.setSelected(settings.isDocumentationEnabled());
        enableSmartContext.setSelected(settings.isSmartContextEnabled());
        enableRightClickMenu.setSelected(settings.isRightClickMenuEnabled());
    }
    
    /**
     * 释放UI资源
     * 
     * <p>该方法在设置界面关闭时调用，用于清理UI组件和释放内存资源。
     * 防止内存泄漏和资源占用。</p>
     * 
     * <p>清理内容：</p>
     * <ul>
     *   <li>将主面板引用设置为null</li>
     *   <li>允许垃圾回收器回收相关资源</li>
     *   <li>确保界面组件的正确释放</li>
     * </ul>
     * 
     * @see com.intellij.openapi.options.Configurable#disposeUIResources()
     */
    @Override
    public void disposeUIResources() {
        mainPanel = null;
    }
} 