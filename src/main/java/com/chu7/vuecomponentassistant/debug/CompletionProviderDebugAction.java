package com.chu7.vuecomponentassistant.debug;

import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;

/**
 * 补全提供者调试Action
 * 用于检查补全提供者的注册状态
 */
public class CompletionProviderDebugAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getData(CommonDataKeys.PROJECT);
        if (project == null) {
            Messages.showErrorDialog("No project found", "Debug Error");
            return;
        }
        
        try {
            StringBuilder info = new StringBuilder();
            info.append("补全提供者调试信息:\n\n");
            
            info.append("=== 检查步骤 ===\n");
            info.append("1. 确认plugin.xml中已注册completion.contributor\n");
            info.append("2. 确认UnifiedXmlAttributeValueProvider类存在\n");
            info.append("3. 确认构造函数中有LOG.info输出\n");
            info.append("4. 确认在XML文件中输入时触发补全\n");
            
            info.append("\n=== 测试方法 ===\n");
            info.append("1. 打开test/B2_SimpleAttributeValueTest.vue\n");
            info.append("2. 在type=\"\"的引号内输入t\n");
            info.append("3. 查看IDE日志是否有调试输出\n");
            info.append("4. 检查Tools菜单中的调试Action\n");
            
            Messages.showInfoMessage(info.toString(), "补全提供者调试");
        } catch (Exception ex) {
            Messages.showErrorDialog("Error: " + ex.getMessage(), "Debug Error");
        }
    }
    
    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setEnabledAndVisible(e.getData(CommonDataKeys.PROJECT) != null);
    }
}
