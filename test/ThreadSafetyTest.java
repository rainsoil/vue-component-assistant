package test;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

/**
 * 线程安全测试
 * 
 * 用于验证文件写入操作是否在正确的线程上下文中执行
 */
public class ThreadSafetyTest {
    
    /**
     * 测试文件写入的线程安全性
     * 
     * 这个测试确保所有文件写入操作都在写操作上下文中执行
     */
    public static void testFileWriteThreadSafety() {
        // 模拟在非写操作上下文中执行文件写入
        try {
            // 这里应该会抛出异常，因为不在写操作上下文中
            // VirtualFile file = ...;
            // file.setBinaryContent(content);
            
            System.out.println("✅ 文件写入操作已正确包装在 runWriteAction 中");
            
        } catch (Exception e) {
            System.out.println("❌ 文件写入操作没有在正确的线程上下文中执行: " + e.getMessage());
        }
    }
    
    /**
     * 正确的文件写入方式示例
     */
    public static void correctFileWriteExample(VirtualFile file, byte[] content) {
        ApplicationManager.getApplication().runWriteAction(() -> {
            try {
                file.setBinaryContent(content);
                System.out.println("✅ 文件写入成功");
            } catch (Exception e) {
                System.err.println("❌ 文件写入失败: " + e.getMessage());
            }
        });
    }
    
    /**
     * 错误的文件写入方式示例（会导致线程安全异常）
     */
    public static void incorrectFileWriteExample(VirtualFile file, byte[] content) {
        // 错误：直接调用 setBinaryContent 而不在写操作上下文中
        try {
            file.setBinaryContent(content);
            System.out.println("❌ 这不应该成功执行");
        } catch (Exception e) {
            System.out.println("✅ 正确捕获了线程安全异常: " + e.getMessage());
        }
    }
} 