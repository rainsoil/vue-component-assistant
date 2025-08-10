package com.chu7.vuecomponentassistant.remote.utils;

import com.chu7.vuecomponentassistant.exceptions.VueKitException;
import com.intellij.openapi.diagnostic.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * HTTP客户端工具类
 */
public class HttpClient {
    private static final Logger LOG = Logger.getInstance(HttpClient.class);
    private static final int CONNECT_TIMEOUT = 10000; // 10秒
    private static final int READ_TIMEOUT = 30000; // 30秒
    private static final String USER_AGENT = "VueKit/1.0.0";

    /**
     * 异步下载JSON内容
     */
    public static CompletableFuture<String> downloadJsonAsync(String url) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return downloadJson(url);
            } catch (Exception e) {
                throw new RuntimeException("下载失败: " + e.getMessage(), e);
            }
        });
    }

    /**
     * 同步下载JSON内容
     */
    public static String downloadJson(String url) throws VueKitException {
        HttpURLConnection connection = null;
        BufferedReader reader = null;
        
        try {
            URL urlObj = new URL(url);
            connection = (HttpURLConnection) urlObj.openConnection();
            
            // 设置连接参数
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", USER_AGENT);
            connection.setRequestProperty("Accept", "application/json");
            connection.setConnectTimeout(CONNECT_TIMEOUT);
            connection.setReadTimeout(READ_TIMEOUT);
            
            // 检查响应码
            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new VueKitException("HTTP请求失败，响应码: " + responseCode);
            }
            
            // 读取响应内容
            reader = new BufferedReader(
                new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8)
            );
            
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            
            LOG.info("成功下载JSON内容，URL: " + url + ", 长度: " + response.length());
            return response.toString();
            
        } catch (IOException e) {
            LOG.error("下载JSON失败: " + url, e);
            throw new VueKitException("网络请求失败: " + e.getMessage());
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    LOG.warn("关闭读取器失败", e);
                }
            }
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    /**
     * 检查URL是否可访问
     */
    public static CompletableFuture<Boolean> checkUrlAccessibleAsync(String url) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return checkUrlAccessible(url);
            } catch (Exception e) {
                LOG.warn("检查URL可访问性失败: " + url, e);
                return false;
            }
        });
    }

    /**
     * 同步检查URL是否可访问
     */
    public static boolean checkUrlAccessible(String url) throws VueKitException {
        HttpURLConnection connection = null;
        
        try {
            URL urlObj = new URL(url);
            connection = (HttpURLConnection) urlObj.openConnection();
            
            connection.setRequestMethod("HEAD");
            connection.setRequestProperty("User-Agent", USER_AGENT);
            connection.setConnectTimeout(CONNECT_TIMEOUT);
            connection.setReadTimeout(READ_TIMEOUT);
            
            int responseCode = connection.getResponseCode();
            return responseCode == HttpURLConnection.HTTP_OK;
            
        } catch (IOException e) {
            LOG.warn("检查URL可访问性失败: " + url, e);
            return false;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    /**
     * 带重试的下载
     */
    public static CompletableFuture<String> downloadJsonWithRetry(String url, int maxRetries) {
        return CompletableFuture.supplyAsync(() -> {
            Exception lastException = null;
            
            for (int attempt = 1; attempt <= maxRetries; attempt++) {
                try {
                    LOG.info("尝试下载JSON，第 " + attempt + " 次，URL: " + url);
                    String result = downloadJson(url);
                    LOG.info("下载成功，URL: " + url);
                    return result;
                } catch (Exception e) {
                    lastException = e;
                    LOG.warn("下载失败，第 " + attempt + " 次尝试，URL: " + url, e);
                    
                    if (attempt < maxRetries) {
                        try {
                            // 指数退避
                            long delay = (long) Math.pow(2, attempt) * 1000;
                            TimeUnit.MILLISECONDS.sleep(delay);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            throw new RuntimeException("下载被中断", ie);
                        }
                    }
                }
            }
            
            throw new RuntimeException("下载失败，已重试 " + maxRetries + " 次", lastException);
        });
    }
    
    /**
     * 下载JSON样本（用于验证）
     */
    public static String downloadJsonSample(String url) throws VueKitException {
        HttpURLConnection connection = null;
        BufferedReader reader = null;
        
        try {
            URL urlObj = new URL(url);
            connection = (HttpURLConnection) urlObj.openConnection();
            
            // 设置连接参数
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", USER_AGENT);
            connection.setRequestProperty("Accept", "application/json");
            connection.setConnectTimeout(CONNECT_TIMEOUT);
            connection.setReadTimeout(5000); // 较短的超时时间用于验证
            
            // 检查响应码
            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new VueKitException("HTTP请求失败，响应码: " + responseCode);
            }
            
            // 读取前1KB内容用于验证
            reader = new BufferedReader(
                new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8)
            );
            
            char[] buffer = new char[1024];
            int bytesRead = reader.read(buffer);
            
            if (bytesRead > 0) {
                String sample = new String(buffer, 0, bytesRead);
                LOG.info("成功下载JSON样本，URL: " + url + ", 长度: " + sample.length());
                return sample;
            } else {
                throw new VueKitException("无法读取响应内容");
            }
            
        } catch (IOException e) {
            LOG.error("下载JSON样本失败: " + url, e);
            throw new VueKitException("网络请求失败: " + e.getMessage());
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    LOG.warn("关闭读取器失败", e);
                }
            }
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
} 