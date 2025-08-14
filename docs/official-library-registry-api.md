# 官方组件库市场 API 接口文档

## 概述

官方组件库市场 API 提供了获取可用组件库列表的接口，支持 VueKit 插件的远程组件库管理功能。

## 接口信息

- **URL**: `https://gitee.com/rainsoil/vuekit-repo/raw/master/libraries.json`
- **方法**: GET
- **格式**: JSON
- **编码**: UTF-8

## 请求参数

无参数，直接 GET 请求即可。

## 响应格式

返回一个 JSON 数组，包含所有可用的官方组件库信息。

## 数据结构

### 组件库对象字段说明

| 字段名 | 类型 | 必需 | 说明 |
|--------|------|------|------|
| `id` | string | ✅ | 组件库唯一标识符 |
| `name` | string | ✅ | 组件库名称（技术名称） |
| `displayName` | string | ✅ | 组件库显示名称 |
| `description` | string | ✅ | 组件库描述信息 |
| `version` | string | ✅ | 组件库版本号 |
| `framework` | string | ✅ | 支持的框架（vue2/vue3） |
| `category` | string | ✅ | 组件库分类 |
| `author` | string | ✅ | 作者/团队名称 |
| `homepage` | string | ✅ | 官方主页URL |
| `downloadUrl` | string | ✅ | 组件库数据下载URL |
| `downloadCount` | number | ❌ | 下载次数统计 |
| `rating` | number | ❌ | 评分（0-5） |
| `tags` | string[] | ❌ | 标签数组 |
| `lastUpdated` | string | ❌ | 最后更新时间（ISO 8601格式） |
| `license` | string | ❌ | 许可证类型 |
| `repository` | string | ❌ | 代码仓库URL |
| `documentation` | string | ❌ | 文档URL |
| `features` | string[] | ❌ | 特性列表 |
| `requirements` | object | ❌ | 系统要求 |

### requirements 对象字段

| 字段名 | 类型 | 说明 |
|--------|------|------|
| `vue` | string | Vue 版本要求 |
| `node` | string | Node.js 版本要求 |

## 示例响应

```json
[
  {
    "id": "element-plus",
    "name": "element-plus",
    "displayName": "Element Plus",
    "description": "Vue 3 组件库，基于 Element UI 设计系统，提供丰富的组件和功能",
    "version": "2.5.0",
    "framework": "vue3",
    "category": "UI Framework",
    "author": "Element Plus Team",
    "homepage": "https://element-plus.org",
    "downloadUrl": "https://cdn.vuekit.dev/libraries/element-plus.json",
    "downloadCount": 50000,
    "rating": 4.9,
    "tags": ["vue3", "ui", "components", "element", "design-system"],
    "lastUpdated": "2024-12-01T10:00:00Z",
    "license": "MIT",
    "repository": "https://github.com/element-plus/element-plus",
    "documentation": "https://element-plus.org/zh-CN/",
    "features": [
      "60+ 组件",
      "TypeScript 支持",
      "主题定制",
      "国际化",
      "响应式设计"
    ],
    "requirements": {
      "vue": ">=3.3.0",
      "node": ">=16.0.0"
    }
  }
]
```

## 支持的组件库分类

- **UI Framework**: UI 组件框架
- **Full Stack Framework**: 全栈开发框架
- **Utility Library**: 工具库
- **Animation Library**: 动画库
- **Chart Library**: 图表库
- **Form Library**: 表单库

## 支持的框架类型

- **vue2**: Vue 2.x
- **vue3**: Vue 3.x
- **react**: React
- **angular**: Angular

## 错误处理

### HTTP 状态码

- `200`: 成功返回数据
- `404`: 接口不存在
- `500`: 服务器内部错误

### 错误响应格式

```json
{
  "error": "错误信息",
  "code": "错误代码",
  "timestamp": "2024-12-01T10:00:00Z"
}
```

## 缓存策略

- 客户端应该实现适当的缓存机制
- 建议缓存时间：1小时
- 支持 ETag 和 Last-Modified 头

## 使用示例

### JavaScript 示例

```javascript
async function fetchOfficialLibraries() {
  try {
    const response = await fetch('https://gitee.com/rainsoil/vuekit-repo/raw/master/libraries.json');
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }
    const libraries = await response.json();
    return libraries;
  } catch (error) {
    console.error('获取官方组件库列表失败:', error);
    return [];
  }
}

// 使用示例
fetchOfficialLibraries().then(libraries => {
  console.log('获取到', libraries.length, '个组件库');
  libraries.forEach(lib => {
    console.log(`${lib.displayName} - ${lib.description}`);
  });
});
```

### Java 示例

```java
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.util.List;

public class OfficialLibraryFetcher {
    private static final String API_URL = "https://gitee.com/rainsoil/vuekit-repo/raw/master/libraries.json";
    private static final Gson gson = new Gson();
    
    public static List<OfficialLibrary> fetchLibraries() {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .build();
                
            HttpResponse<String> response = client.send(request, 
                HttpResponse.BodyHandlers.ofString());
                
            if (response.statusCode() == 200) {
                Type listType = new TypeToken<List<OfficialLibrary>>(){}.getType();
                return gson.fromJson(response.body(), listType);
            } else {
                throw new RuntimeException("HTTP error: " + response.statusCode());
            }
        } catch (Exception e) {
            throw new RuntimeException("获取官方组件库失败", e);
        }
    }
}
```

## 更新频率

- 组件库信息每周更新一次
- 版本信息实时更新
- 下载统计每日更新

## 注意事项

1. 请合理使用 API，避免频繁请求
2. 建议实现本地缓存机制
3. 网络异常时应该使用本地默认数据
4. 支持 CORS 跨域请求
5. 所有 URL 都应该是 HTTPS

## 联系信息

如有问题或建议，请联系：
- 邮箱: support@vuekit.dev
- GitHub: https://github.com/vuekit/vuekit
- 文档: https://docs.vuekit.dev 