package com.chu7.vuecomponentassistant.completion.context;

import com.chu7.vuecomponentassistant.completion.CompletionContext;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 补全上下文分析器
 * 
 * 负责分析Vue文件中的上下文，确定补全类型和相关信息：
 * - 组件补全上下文
 * - 属性补全上下文
 * - 事件补全上下文
 * - 插槽补全上下文
 * - 智能上下文识别
 * 
 * @author VueKit Team
 * @version 2.0.0
 */
public class CompletionContextAnalyzer {
    
    // 正则表达式模式（简化后只保留必要的）
    private static final Pattern V_MODEL_PATTERN = Pattern.compile("v-model\\s*=\\s*\"([^\"]*)\"");
    
    /**
     * 分析补全上下文
     * 
     * @param file 当前文件
     * @param element 当前元素
     * @return 补全上下文
     */
    @NotNull
    public CompletionContext analyzeContext(@NotNull PsiFile file, @NotNull PsiElement element) {
        String fileText = file.getText();
        int offset = element.getTextOffset();
        String beforeText = fileText.substring(0, offset);
        String currentText = element.getText();
        
        // 清理当前文本
        String cleanCurrentText = currentText.replace("IntellijIdeaRulezzz", "");
        
        // 调试信息
        System.out.println("=== 上下文分析调试 ===");
        System.out.println("beforeText: '" + beforeText.substring(Math.max(0, beforeText.length() - 50)) + "'");
        System.out.println("currentText: '" + cleanCurrentText + "'");
        System.out.println("offset: " + offset);
        
        // 检查组件标签位置
        if (isComponentTagPosition(beforeText, cleanCurrentText)) {
            System.out.println("✅ 检测到组件补全上下文");
            return analyzeComponentContext(beforeText, cleanCurrentText);
        }
        
        // 获取当前组件
        String currentComponent = getCurrentComponent(beforeText);
        System.out.println("当前组件: " + currentComponent);
        
        // 检查事件位置
        if (isEventPosition(beforeText, cleanCurrentText)) {
            System.out.println("✅ 检测到事件补全上下文");
            return analyzeEventContext(beforeText, cleanCurrentText, currentComponent);
        }
        
        // 检查插槽位置
        if (isSlotPosition(beforeText, cleanCurrentText)) {
            System.out.println("✅ 检测到插槽补全上下文");
            return analyzeSlotContext(beforeText, cleanCurrentText, currentComponent);
        }
        
        // 检查属性位置
        if (isAttributePosition(beforeText, cleanCurrentText, currentComponent)) {
            System.out.println("✅ 检测到属性补全上下文");
            return analyzeAttributeContext(beforeText, cleanCurrentText, currentComponent);
        }
        
        // 检查v-bind位置
        if (isVBindPosition(beforeText, cleanCurrentText)) {
            System.out.println("✅ 检测到v-bind补全上下文");
            return analyzeVBindContext(beforeText, cleanCurrentText, currentComponent);
        }
        
        System.out.println("❌ 未检测到特定上下文，返回组件补全");
        // 默认返回组件补全上下文
        return new CompletionContext(CompletionContext.CompletionType.COMPONENT, null, null);
    }
    
    /**
     * 检查是否在组件标签位置
     */
    private boolean isComponentTagPosition(String beforeText, String currentText) {
        return beforeText.endsWith("<") || 
               beforeText.matches(".*<\\s*$") ||
               (currentText.matches("[a-zA-Z][a-zA-Z0-9-]*") && 
                (beforeText.endsWith("<") || beforeText.matches(".*<\\s+$")));
    }
    
    /**
     * 检查是否在事件位置
     */
    private boolean isEventPosition(String beforeText, String currentText) {
        // 检查当前文本是否以 @ 开头
        if (currentText.startsWith("@")) {
            return true;
        }
        
        // 检查 beforeText 中是否包含 @ 符号（用于处理 @ 后面没有字符的情况）
        if (beforeText.contains("@")) {
            // 找到最后一个 @ 的位置
            int lastAtSign = beforeText.lastIndexOf('@');
            String afterAtSign = beforeText.substring(lastAtSign + 1);
            
            // 如果 @ 后面是空的或者只有空格，也认为是事件位置
            if (afterAtSign.trim().isEmpty()) {
                return true;
            }
            
            // 如果 @ 后面有字符但没有 = 号，也认为是事件位置
            if (!afterAtSign.contains("=")) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 检查是否在插槽位置
     */
    private boolean isSlotPosition(String beforeText, String currentText) {
        // 简单规则：当前文本以 # 开头就是插槽位置
        return currentText.startsWith("#");
    }
    
    /**
     * 检查是否在属性位置
     */
    private boolean isAttributePosition(String beforeText, String currentText, String currentComponent) {
        System.out.println("=== 属性位置检测调试 ===");
        System.out.println("currentComponent: " + currentComponent);
        System.out.println("currentText: '" + currentText + "'");
        System.out.println("beforeText: '" + beforeText.substring(Math.max(0, beforeText.length() - 30)) + "'");
        
        // 确保有当前组件
        if (currentComponent == null) {
            System.out.println("❌ 没有当前组件");
            return false;
        }
        
        // 确保在组件标签内
        if (!isInComponentTag(beforeText)) {
            System.out.println("❌ 不在组件标签内");
            return false;
        }
        
        // 排除事件位置（@开头）
        if (currentText.startsWith("@")) {
            System.out.println("❌ 排除事件位置");
            return false;
        }
        
        // 排除插槽位置（#开头）
        if (currentText.startsWith("#")) {
            System.out.println("❌ 排除插槽位置");
            return false;
        }
        
        // 排除v-bind位置（:开头）
        if (currentText.startsWith(":")) {
            System.out.println("❌ 排除v-bind位置");
            return false;
        }
        
        // 检查是否在属性输入位置
        // 1. 当前文本不为空（正在输入属性名）
        if (!currentText.isEmpty()) {
            System.out.println("✅ 当前文本不为空，检测到属性位置");
            return true;
        }
        
        // 2. 检查前面是否有空格，表示准备输入属性
        if (beforeText.endsWith(" ") || beforeText.matches(".*\\s$")) {
            System.out.println("✅ 前面有空格，检测到属性位置");
            return true;
        }
        
        // 3. 检查是否在属性名中间（比如输入了部分属性名）
        if (beforeText.matches(".*\\s[a-zA-Z][a-zA-Z0-9-]*$")) {
            System.out.println("✅ 在属性名中间，检测到属性位置");
            return true;
        }
        
        System.out.println("❌ 未检测到属性位置");
        return false;
    }
    
    /**
     * 检查是否在v-bind位置
     */
    private boolean isVBindPosition(String beforeText, String currentText) {
        // 简单规则：当前文本以 : 开头就是v-bind位置
        return currentText.startsWith(":");
    }
    
    /**
     * 分析组件补全上下文
     */
    private CompletionContext analyzeComponentContext(String beforeText, String currentText) {
        String prefix = null;
        
        if (currentText.matches("[a-zA-Z][a-zA-Z0-9-]*")) {
            prefix = currentText;
        }
        
        return new CompletionContext(CompletionContext.CompletionType.COMPONENT, null, prefix);
    }
    
    /**
     * 分析事件补全上下文
     */
    private CompletionContext analyzeEventContext(String beforeText, String currentText, String currentComponent) {
        String prefix = null;
        
        // 如果当前文本以 @ 开头，去掉 @ 作为前缀
        if (currentText.startsWith("@")) {
            prefix = currentText.substring(1);
        } else if (beforeText.contains("@")) {
            // 如果当前文本不以 @ 开头，但从 beforeText 中检测到事件位置
            // 找到最后一个 @ 的位置
            int lastAtSign = beforeText.lastIndexOf('@');
            String afterAtSign = beforeText.substring(lastAtSign + 1);
            
            // 如果 @ 后面有字符，提取作为前缀
            if (!afterAtSign.trim().isEmpty() && !afterAtSign.contains("=")) {
                prefix = afterAtSign.trim();
            }
        }
        
        return new CompletionContext(CompletionContext.CompletionType.EVENT, currentComponent, prefix);
    }
    
    /**
     * 分析插槽补全上下文
     */
    private CompletionContext analyzeSlotContext(String beforeText, String currentText, String currentComponent) {
        String prefix = null;
        
        // 简单规则：如果当前文本以 # 开头，去掉 # 作为前缀
        if (currentText.startsWith("#")) {
            prefix = currentText.substring(1);
        }
        
        return new CompletionContext(CompletionContext.CompletionType.SLOT, currentComponent, prefix);
    }
    
    /**
     * 分析属性补全上下文
     */
    private CompletionContext analyzeAttributeContext(String beforeText, String currentText, String currentComponent) {
        String prefix = null;
        
        // 简单规则：如果当前文本不为空，直接作为前缀
        if (!currentText.isEmpty()) {
            prefix = currentText;
        }
        
        return new CompletionContext(CompletionContext.CompletionType.ATTRIBUTE, currentComponent, prefix);
    }
    
    /**
     * 分析v-bind补全上下文
     */
    private CompletionContext analyzeVBindContext(String beforeText, String currentText, String currentComponent) {
        String prefix = null;
        
        // 简单规则：如果当前文本以 : 开头，去掉 : 作为前缀
        if (currentText.startsWith(":")) {
            prefix = currentText.substring(1);
        }
        
        return new CompletionContext(CompletionContext.CompletionType.ATTRIBUTE, currentComponent, prefix);
    }
    
    /**
     * 获取当前组件名称
     */
    private String getCurrentComponent(String beforeText) {
        System.out.println("=== 获取当前组件调试 ===");
        System.out.println("beforeText: '" + beforeText.substring(Math.max(0, beforeText.length() - 50)) + "'");
        
        // 使用更可靠的方法：找到最后一个 < 符号，然后提取组件名
        int lastOpenTag = beforeText.lastIndexOf('<');
        if (lastOpenTag == -1) {
            System.out.println("❌ 未找到 < 符号");
            return null;
        }
        
        // 从 < 后面开始查找组件名
        String afterOpenTag = beforeText.substring(lastOpenTag + 1);
        System.out.println("afterOpenTag: '" + afterOpenTag + "'");
        
        // 使用正则表达式匹配组件名，更宽松的匹配
        Matcher matcher = Pattern.compile("^([a-zA-Z][a-zA-Z0-9-]*)").matcher(afterOpenTag);
        if (matcher.find()) {
            String componentName = matcher.group(1);
            System.out.println("✅ 找到组件: " + componentName);
            return componentName;
        }
        
        // 如果正则匹配失败，尝试更简单的方法
        String[] parts = afterOpenTag.split("\\s+");
        if (parts.length > 0 && parts[0].matches("[a-zA-Z][a-zA-Z0-9-]*")) {
            String componentName = parts[0];
            System.out.println("✅ 通过分割找到组件: " + componentName);
            return componentName;
        }
        
        System.out.println("❌ 未找到组件名");
        return null;
    }
    
    /**
     * 检查是否在组件标签内
     */
    private boolean isInComponentTag(String beforeText) {
        // 检查是否在开始标签内（< 和 > 之间）
        int lastOpenTag = beforeText.lastIndexOf('<');
        int lastCloseTag = beforeText.lastIndexOf('>');
        
        System.out.println("=== 组件标签检测调试 ===");
        System.out.println("lastOpenTag: " + lastOpenTag);
        System.out.println("lastCloseTag: " + lastCloseTag);
        System.out.println("beforeText: '" + beforeText.substring(Math.max(0, beforeText.length() - 50)) + "'");
        
        // 如果在 < 之后且在 > 之前，说明在标签内
        if (lastOpenTag > lastCloseTag) {
            System.out.println("✅ 在 < 和 > 之间，在组件标签内");
            return true;
        }
        
        // 特殊情况：如果还没有 > 符号，但已经输入了 < 和组件名
        if (lastOpenTag >= 0 && lastCloseTag == -1) {
            // 检查 < 后面是否有组件名
            String afterOpenTag = beforeText.substring(lastOpenTag + 1);
            System.out.println("afterOpenTag: '" + afterOpenTag + "'");
            
            // 更宽松的匹配：只要包含组件名模式就认为在标签内
            if (afterOpenTag.matches("[a-zA-Z][a-zA-Z0-9-]*.*")) {
                System.out.println("✅ 有组件名，在组件标签内");
                return true;
            }
            
            // 额外检查：如果包含属性或事件，也认为在标签内
            if (afterOpenTag.contains("=") || afterOpenTag.contains("@") || afterOpenTag.contains(":")) {
                System.out.println("✅ 包含属性或事件，在组件标签内");
                return true;
            }
        }
        
        System.out.println("❌ 不在组件标签内");
        return false;
    }
    
    /**
     * 获取组件前缀
     */
    public String getComponentPrefix(String beforeText) {
        // 检查是否在组件标签开始位置
        if (beforeText.endsWith("<") || beforeText.matches(".*<\\s*$")) {
            return "";
        }
        
        // 检查是否有部分组件名称
        Matcher matcher = Pattern.compile("<([a-zA-Z][a-zA-Z0-9-]*)$").matcher(beforeText);
        if (matcher.find()) {
            return matcher.group(1);
        }
        
        return null;
    }
    
    /**
     * 检查是否在v-model指令中
     */
    public boolean isInVModelDirective(String beforeText) {
        Matcher matcher = V_MODEL_PATTERN.matcher(beforeText);
        return matcher.find();
    }
    
    /**
     * 获取v-model绑定的变量名
     */
    public String getVModelVariable(String beforeText) {
        Matcher matcher = V_MODEL_PATTERN.matcher(beforeText);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
}