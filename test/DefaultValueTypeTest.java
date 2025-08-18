import com.chu7.vuecomponentassistant.utils.DefaultValueConverter;

/**
 * 默认值类型转换测试
 * 验证各种类型的默认值是否能正确转换
 */
public class DefaultValueTypeTest {
    
    public static void main(String[] args) {
        System.out.println("=== 默认值类型转换测试 ===\n");
        
        // 测试字符串类型
        testStringType();
        
        // 测试布尔类型
        testBooleanType();
        
        // 测试数字类型
        testNumberType();
        
        // 测试混合类型
        testMixedType();
        
        // 测试格式化显示
        testFormatDisplay();
        
        // 测试类型比较
        testTypeComparison();
    }
    
    private static void testStringType() {
        System.out.println("--- 字符串类型测试 ---");
        
        String[] testValues = {"hello", "", "123", "true", "false"};
        for (String value : testValues) {
            String result = DefaultValueConverter.toString(value);
            String type = DefaultValueConverter.getTypeDescription(value);
            System.out.printf("输入: %-10s | 类型: %-8s | 结果: %s%n", 
                value, type, result);
        }
        System.out.println();
    }
    
    private static void testBooleanType() {
        System.out.println("--- 布尔类型测试 ---");
        
        Object[] testValues = {true, false, "true", "false", "1", "0", "yes", "no", "hello"};
        for (Object value : testValues) {
            Boolean result = DefaultValueConverter.toBoolean(value);
            String type = DefaultValueConverter.getTypeDescription(value);
            System.out.printf("输入: %-10s | 类型: %-8s | 布尔值: %s%n", 
                value, type, result);
        }
        System.out.println();
    }
    
    private static void testNumberType() {
        System.out.println("--- 数字类型测试 ---");
        
        Object[] testValues = {123, 3.14, "123", "3.14", "abc", true, false};
        for (Object value : testValues) {
            Number result = DefaultValueConverter.toNumber(value);
            String type = DefaultValueConverter.getTypeDescription(value);
            System.out.printf("输入: %-10s | 类型: %-8s | 数字值: %s%n", 
                value, type, result);
        }
        System.out.println();
    }
    
    private static void testMixedType() {
        System.out.println("--- 混合类型测试 ---");
        
        Object[] testValues = {
            "hello", true, 123, 3.14, "true", "123", null
        };
        
        for (Object value : testValues) {
            String type = DefaultValueConverter.getTypeDescription(value);
            String display = DefaultValueConverter.formatForDisplay(value);
            System.out.printf("输入: %-10s | 类型: %-8s | 显示: %s%n", 
                value, type, display);
        }
        System.out.println();
    }
    
    private static void testFormatDisplay() {
        System.out.println("--- 格式化显示测试 ---");
        
        Object[] testValues = {
            "hello world", "", 123, 3.14, true, false, null
        };
        
        for (Object value : testValues) {
            String display = DefaultValueConverter.formatForDisplay(value);
            System.out.printf("输入: %-15s | 格式化: %s%n", value, display);
        }
        System.out.println();
    }
    
    private static void testTypeComparison() {
        System.out.println("--- 类型比较测试 ---");
        
        Object[][] testPairs = {
            {"123", 123},
            {"true", true},
            {"false", false},
            {"hello", "hello"},
            {"1", true},
            {"0", false},
            {123, 123.0}
        };
        
        for (Object[] pair : testPairs) {
            Object value1 = pair[0];
            Object value2 = pair[1];
            boolean equals = DefaultValueConverter.equals(value1, value2);
            System.out.printf("比较: %-10s == %-10s | 结果: %s%n", 
                value1, value2, equals);
        }
        System.out.println();
    }
}
