package com.chu7.spider;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

/**
 * element plus 组件库数据抓取
 * @author luyanan
 * @since 2025/9/3
 */
public class ElementPlusSpider {


    public String getComponent(ComponentInfo componentInfo) {
        String url = "https://element-plus.org/zh-CN/component/" + componentInfo.getUrl();
        String html = HttpUtil.createGet(url).execute().body();
        if (StrUtil.isBlank(html)) {
            throw new NullPointerException("获取到的页面为空");
        }
        Document document = Jsoup.parse(html);
        Elements vpTables = document.select("div.vp-table");
        for (Element vpTable : vpTables) {

            String title = vpTable.previousElementSibling().text();


            System.out.println(title);
        }
        return null;
    }

    public static class ComponentInfo {

        private String name;

        private String desc;

        private String url;

        public ComponentInfo(String name, String desc, String url) {
            this.name = name;
            this.desc = desc;
            this.url = url;
        }

        public String getName() {
            return name;
        }

        public String getDesc() {
            return desc;
        }

        public String getUrl() {
            return url;
        }
    }

    public static void main(String[] args) {
        List<ComponentInfo> componentInfos = new ArrayList<>();
        componentInfos.add(new ComponentInfo("Button", "常用的操作按钮", "button.html"));
        componentInfos.add(new ComponentInfo("ButtonGroup", "以按钮组的方式出现，常用于多项类似操作。", "button.html"));

        componentInfos.add(new ComponentInfo("Container", "外层容器。 当子元素中包含 <el-header> 或 <el-footer> 时，全部子元素会垂直上下排列， 否则会水平左右排列。", "container.html"));
        componentInfos.add(new ComponentInfo("Header", "顶栏容器。", "container.html"));
        componentInfos.add(new ComponentInfo("Aside", "侧边栏容器", "container.html"));
        componentInfos.add(new ComponentInfo("Main", "主要区域容器。", "container.html"));
        componentInfos.add(new ComponentInfo("Footer", "底栏容器。", "container.html"));

        componentInfos.add(new ComponentInfo("Icon", "常用的图标集合", "icon.html"));

        componentInfos.add(new ComponentInfo("Row", "通过基础的 24 分栏，迅速简便地创建布局", "layout.html"));

        componentInfos.add(new ComponentInfo("Col", "通过基础的 24 分栏，迅速简便地创建布局", "layout.html"));

        componentInfos.add(new ComponentInfo("Link", "文字超链接", "link.html"));

        componentInfos.add(new ComponentInfo("Text", "文本-文本的常见操作", "text.html"));

        componentInfos.add(new ComponentInfo("Scrollbar", "滚动条-用于替换浏览器原生滚动条", "scrollbar.html"));


        componentInfos.add(new ComponentInfo("Splitter", "分隔面板-可将区域水平或垂直分隔，并可自由拖动以调整各个区域的大小", "splitter.html"));
        componentInfos.add(new ComponentInfo("SplitterPanel", "分隔面板子元素-可将区域水平或垂直分隔，并可自由拖动以调整各个区域的大小", "splitter.html"));

        componentInfos.add(new ComponentInfo("ConfigProvider", "全局配置-被用来提供全局的配置选项，让你的配置能够在全局都能够被访问到", "config-provider.html"));


        componentInfos.add(new ComponentInfo("Autocomplete", "自动补全输入框-根据输入内容提供对应的输入建议", "autocomplete.html"));

        componentInfos.add(new ComponentInfo("Cascader", "级联选择器-当一个数据集合有清晰的层级结构时，可通过级联选择器逐级查看并选择", "cascader.html"));
        ElementPlusSpider elementPlusSpider = new ElementPlusSpider();

//        for (ComponentInfo componentInfo : componentInfos) {
//            elementPlusSpider.getComponent(componentInfo);
//        }
        elementPlusSpider.getComponent(componentInfos.get(0));
    }
}
