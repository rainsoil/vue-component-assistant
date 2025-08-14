package com.chu7.vuecomponentassistant.completion;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Element Plus 上下文分析器
 * 用于分析当前编辑位置的组件上下文
 */
public class ElementPlusContextAnalyzer {
    
    private static final Logger LOG = Logger.getInstance(ElementPlusContextAnalyzer.class);
    
    // 匹配Vue模板中的组件标签
    private static final Pattern COMPONENT_PATTERN = Pattern.compile("<([a-zA-Z][a-zA-Z0-9-]*)\\b");
    // 匹配Element Plus组件前缀
    private static final Pattern ELEMENT_PLUS_PATTERN = Pattern.compile("el-[a-zA-Z-]+");
    
    /**
     * 获取当前组件名称
     */
    public String getCurrentComponent(PsiElement element) {
        if (element == null) {
            return null;
        }
        
        try {
            // 获取当前文件内容
            PsiFile file = element.getContainingFile();
            if (file == null) {
                return null;
            }
            
            String fileText = file.getText();
            int offset = element.getTextOffset();
            
            // 向前查找最近的组件标签
            String beforeText = fileText.substring(0, offset);
            Matcher matcher = COMPONENT_PATTERN.matcher(beforeText);
            
            String lastComponent = null;
            while (matcher.find()) {
                lastComponent = matcher.group(1);
            }
            
            // 检查是否是Element Plus组件
            if (lastComponent != null && ELEMENT_PLUS_PATTERN.matcher(lastComponent).matches()) {
                return lastComponent;
            }
            
        } catch (Exception e) {
            LOG.warn("Error analyzing component context", e);
        }
        
        return null;
    }
    
    /**
     * 检查当前是否在Vue模板中
     */
    public boolean isInVueTemplate(PsiElement element) {
        if (element == null) {
            return false;
        }
        
        PsiFile file = element.getContainingFile();
        if (file == null) {
            return false;
        }
        
        String fileName = file.getName().toLowerCase();
        return fileName.endsWith(".vue") || fileName.endsWith(".html");
    }
    
    /**
     * 检查当前是否在组件标签内
     */
    public boolean isInsideComponentTag(PsiElement element) {
        if (element == null) {
            return false;
        }
        
        try {
            PsiFile file = element.getContainingFile();
            if (file == null) {
                return false;
            }
            
            String fileText = file.getText();
            int offset = element.getTextOffset();
            
            // 检查当前位置是否在标签内
            String beforeText = fileText.substring(0, offset);
            String afterText = fileText.substring(offset);
            
            // 查找最近的开始标签和结束标签
            int lastOpenTag = beforeText.lastIndexOf('<');
            int nextCloseTag = afterText.indexOf('>');
            
            if (lastOpenTag >= 0 && nextCloseTag >= 0) {
                String tagContent = beforeText.substring(lastOpenTag) + afterText.substring(0, nextCloseTag + 1);
                return tagContent.contains(" ") && !tagContent.contains("</");
            }
            
        } catch (Exception e) {
            LOG.warn("Error checking if inside component tag", e);
        }
        
        return false;
    }
    
    /**
     * 检查当前是否在事件绑定位置
     */
    public boolean isInEventBinding(PsiElement element) {
        if (element == null) {
            return false;
        }
        
        try {
            PsiFile file = element.getContainingFile();
            if (file == null) {
                return false;
            }
            
            String fileText = file.getText();
            int offset = element.getTextOffset();
            
            // 检查当前位置前后是否有@符号
            String beforeText = fileText.substring(Math.max(0, offset - 10), offset);
            return beforeText.contains("@");
            
        } catch (Exception e) {
            LOG.warn("Error checking if in event binding", e);
        }
        
        return false;
    }
    
    /**
     * 获取当前属性名称
     */
    public String getCurrentAttribute(PsiElement element) {
        if (element == null) {
            return null;
        }
        
        try {
            PsiFile file = element.getContainingFile();
            if (file == null) {
                return null;
            }
            
            String fileText = file.getText();
            int offset = element.getTextOffset();
            
            // 向前查找最近的属性
            String beforeText = fileText.substring(0, offset);
            Pattern attrPattern = Pattern.compile("\\s([a-zA-Z][a-zA-Z0-9-]*)\\s*=");
            Matcher matcher = attrPattern.matcher(beforeText);
            
            String lastAttribute = null;
            while (matcher.find()) {
                lastAttribute = matcher.group(1);
            }
            
            return lastAttribute;
            
        } catch (Exception e) {
            LOG.warn("Error getting current attribute", e);
        }
        
        return null;
    }
    
    /**
     * 检查是否是Element Plus项目
     */
    public boolean isElementPlusProject(PsiElement element) {
        if (element == null) {
            return false;
        }
        
        try {
            PsiFile file = element.getContainingFile();
            if (file == null) {
                return false;
            }
            
            String fileText = file.getText();
            
            // 检查是否包含Element Plus相关的导入或使用
            return fileText.contains("element-plus") || 
                   fileText.contains("El") ||
                   fileText.contains("el-");
            
        } catch (Exception e) {
            LOG.warn("Error checking if Element Plus project", e);
        }
        
        return false;
    }
}
