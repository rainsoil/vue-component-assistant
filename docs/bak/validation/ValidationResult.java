package bak.validation;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 验证结果类
 * 
 * 封装数据验证的结果信息，包括：
 * - 错误信息列表
 * - 警告信息列表
 * - 验证状态
 * - 结果合并功能
 * 
 * @author VueKit Team
 * @version 2.0.0
 */
public class ValidationResult {
    
    private final List<String> errors;
    private final List<String> warnings;
    
    public ValidationResult() {
        this.errors = new ArrayList<>();
        this.warnings = new ArrayList<>();
    }
    
    /**
     * 添加错误信息
     * 
     * @param error 错误信息
     */
    public void addError(@NotNull String error) {
        errors.add(error);
    }
    
    /**
     * 添加警告信息
     * 
     * @param warning 警告信息
     */
    public void addWarning(@NotNull String warning) {
        warnings.add(warning);
    }
    
    /**
     * 检查是否有错误
     * 
     * @return 如果有错误返回true
     */
    public boolean hasErrors() {
        return !errors.isEmpty();
    }
    
    /**
     * 检查是否有警告
     * 
     * @return 如果有警告返回true
     */
    public boolean hasWarnings() {
        return !warnings.isEmpty();
    }
    
    /**
     * 检查验证是否通过（无错误）
     * 
     * @return 如果验证通过返回true
     */
    public boolean isValid() {
        return !hasErrors();
    }
    
    /**
     * 获取错误列表
     * 
     * @return 只读的错误列表
     */
    @NotNull
    public List<String> getErrors() {
        return Collections.unmodifiableList(errors);
    }
    
    /**
     * 获取警告列表
     * 
     * @return 只读的警告列表
     */
    @NotNull
    public List<String> getWarnings() {
        return Collections.unmodifiableList(warnings);
    }
    
    /**
     * 获取错误数量
     * 
     * @return 错误数量
     */
    public int getErrorCount() {
        return errors.size();
    }
    
    /**
     * 获取警告数量
     * 
     * @return 警告数量
     */
    public int getWarningCount() {
        return warnings.size();
    }
    
    /**
     * 合并另一个验证结果
     * 
     * @param other 要合并的验证结果
     */
    public void merge(@NotNull ValidationResult other) {
        errors.addAll(other.errors);
        warnings.addAll(other.warnings);
    }
    
    /**
     * 清空所有结果
     */
    public void clear() {
        errors.clear();
        warnings.clear();
    }
    
    /**
     * 获取第一个错误信息
     * 
     * @return 第一个错误信息，如果没有错误返回null
     */
    public String getFirstError() {
        return errors.isEmpty() ? null : errors.get(0);
    }
    
    /**
     * 获取第一个警告信息
     * 
     * @return 第一个警告信息，如果没有警告返回null
     */
    public String getFirstWarning() {
        return warnings.isEmpty() ? null : warnings.get(0);
    }
    
    /**
     * 获取格式化的摘要信息
     * 
     * @return 摘要信息
     */
    @NotNull
    public String getSummary() {
        if (isValid()) {
            if (hasWarnings()) {
                return String.format("验证通过，但有 %d 个警告", getWarningCount());
            } else {
                return "验证通过";
            }
        } else {
            return String.format("验证失败：%d 个错误，%d 个警告", getErrorCount(), getWarningCount());
        }
    }
    
    /**
     * 获取详细的错误报告
     * 
     * @return 详细报告
     */
    @NotNull
    public String getDetailedReport() {
        StringBuilder report = new StringBuilder();
        
        report.append("=== 验证结果 ===\n");
        report.append("状态: ").append(isValid() ? "通过" : "失败").append("\n");
        report.append("错误数量: ").append(getErrorCount()).append("\n");
        report.append("警告数量: ").append(getWarningCount()).append("\n\n");
        
        if (hasErrors()) {
            report.append("=== 错误信息 ===\n");
            for (int i = 0; i < errors.size(); i++) {
                report.append(String.format("%d. %s\n", i + 1, errors.get(i)));
            }
            report.append("\n");
        }
        
        if (hasWarnings()) {
            report.append("=== 警告信息 ===\n");
            for (int i = 0; i < warnings.size(); i++) {
                report.append(String.format("%d. %s\n", i + 1, warnings.get(i)));
            }
        }
        
        return report.toString();
    }
    
    @Override
    public String toString() {
        return getSummary();
    }
}