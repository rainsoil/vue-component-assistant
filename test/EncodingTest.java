import com.chu7.vuecomponentassistant.completion.ElementPlusComponent;
import com.chu7.vuecomponentassistant.completion.ElementPlusComponentProvider;
import com.chu7.vuecomponentassistant.documentation.DocumentationStyleGenerator;

/**
 * 编码测试类
 * 用于验证文档生成和显示的编码是否正确
 */
public class EncodingTest {
    
    public static void main(String[] args) {
        System.out.println("=== 开始编码测试 ===");
        
        // 测试基本中文字符
        System.out.println("测试中文字符: 你好世界");
        System.out.println("测试emoji: 📦🔧🎯🔌💡📖");
        
        // 测试组件数据加载
        ElementPlusComponentProvider provider = new ElementPlusComponentProvider();
        ElementPlusComponent buttonComponent = provider.getComponent("el-button");
        
        if (buttonComponent != null) {
            System.out.println("=== 按钮组件信息 ===");
            System.out.println("组件名称: " + buttonComponent.getName());
            System.out.println("组件描述: " + buttonComponent.getDescription());
            
            // 测试文档生成
            String documentation = DocumentationStyleGenerator.generateTextDocumentation(buttonComponent);
            System.out.println("=== 生成的文档内容 ===");
            System.out.println("文档长度: " + documentation.length());
            System.out.println("文档前200字符:");
            System.out.println(documentation.substring(0, Math.min(200, documentation.length())));
            
            // 测试字符编码
            System.out.println("=== 字符编码测试 ===");
            byte[] bytes = documentation.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            System.out.println("UTF-8字节长度: " + bytes.length);
            
            // 测试字体支持
            System.out.println("=== 字体支持测试 ===");
            java.awt.Font msyh = new java.awt.Font("Microsoft YaHei", java.awt.Font.PLAIN, 12);
            java.awt.Font simsun = new java.awt.Font("SimSun", java.awt.Font.PLAIN, 12);
            java.awt.Font dialog = new java.awt.Font("Dialog", java.awt.Font.PLAIN, 12);
            
            System.out.println("微软雅黑支持中文: " + msyh.canDisplay('中'));
            System.out.println("微软雅黑支持emoji: " + msyh.canDisplay('📦'));
            System.out.println("宋体支持中文: " + simsun.canDisplay('中'));
            System.out.println("宋体支持emoji: " + simsun.canDisplay('📦'));
            System.out.println("Dialog支持中文: " + dialog.canDisplay('中'));
            System.out.println("Dialog支持emoji: " + dialog.canDisplay('📦'));
            
        } else {
            System.out.println("无法加载按钮组件数据");
        }
        
        System.out.println("=== 编码测试完成 ===");
    }
}
