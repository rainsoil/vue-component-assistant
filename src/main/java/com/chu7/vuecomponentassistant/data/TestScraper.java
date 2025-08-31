package com.chu7.vuecomponentassistant.data;

/**
 * 测试ElementDataScraper的简单类
 */
public class TestScraper {
    public static void main(String[] args) {
        System.out.println("开始测试ElementDataScraper...");
        
        try {
            ElementDataScraper scraper = new ElementDataScraper();
            
            // 测试Element Plus抓取
            System.out.println("测试Element Plus抓取...");
            scraper.scrapeElementPlus();
            
            // 测试Element UI抓取
            System.out.println("测试Element UI抓取...");
            scraper.scrapeElementUI();
            
            System.out.println("测试完成！");
            
        } catch (Exception e) {
            System.err.println("测试失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 