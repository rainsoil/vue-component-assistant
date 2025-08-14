import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * 简单的编码测试类
 */
public class SimpleEncodingTest {
    
    public static void main(String[] args) {
        System.out.println("=== 简单编码测试 ===");
        
        // 测试基本中文字符
        String testChinese = "你好世界";
        String testEmoji = "📦🔧🎯";
        
        System.out.println("中文字符: " + testChinese);
        System.out.println("Emoji字符: " + testEmoji);
        System.out.println("中文字符字节长度: " + testChinese.getBytes(StandardCharsets.UTF_8).length);
        System.out.println("Emoji字符字节长度: " + testEmoji.getBytes(StandardCharsets.UTF_8).length);
        
        // 测试从资源文件读取
        try {
            InputStream inputStream = SimpleEncodingTest.class.getResourceAsStream("/data/element-plus-components.json");
            if (inputStream != null) {
                byte[] bytes = inputStream.readAllBytes();
                String content = new String(bytes, StandardCharsets.UTF_8);
                inputStream.close();
                
                System.out.println("JSON文件大小: " + bytes.length + " 字节");
                System.out.println("JSON内容长度: " + content.length() + " 字符");
                System.out.println("JSON前100字符: " + content.substring(0, Math.min(100, content.length())));
                System.out.println("是否包含'按钮': " + content.contains("按钮"));
                System.out.println("是否包含'输入框': " + content.contains("输入框"));
                
            } else {
                System.out.println("无法找到JSON文件");
            }
        } catch (Exception e) {
            System.out.println("读取文件时出错: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("=== 测试完成 ===");
    }
}
