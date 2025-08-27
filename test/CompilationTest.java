// 简单的编译测试
public class CompilationTest {
    public static void main(String[] args) {
        System.out.println("B1 Component Tag Completion Test");
        
        // 测试数据模型
        com.chu7.vuecomponentassistant.library.model.ComponentLibrary library = 
            new com.chu7.vuecomponentassistant.library.model.ComponentLibrary();
        library.id = "test";
        library.name = "Test Library";
        library.componentPrefix = "el-";
        
        // 测试组件
        com.chu7.vuecomponentassistant.library.model.Component component = 
            new com.chu7.vuecomponentassistant.library.model.Component();
        component.name = "el-button";
        component.description = "Button component";
        
        System.out.println("Library: " + library.name);
        System.out.println("Component: " + component.name);
        System.out.println("Test completed successfully!");
    }
} 