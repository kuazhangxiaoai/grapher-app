package com.bonc.common.utils.http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class PptHttpUtils {
    private static final Logger log = LoggerFactory.getLogger(HttpUtils.class);

    // 默认连接超时时间(毫秒)
    private static final int DEFAULT_CONNECT_TIMEOUT = 60000;
    // 默认读取超时时间(毫秒)
    private static final int DEFAULT_READ_TIMEOUT = 60000;

    /**
     * 发送GET请求
     * @param url 请求地址
     * @param params 请求参数
     * @param headers 请求头
     * @return 响应内容
     */
    public static String sendGet(String url, Map<String, String> params, Map<String, String> headers) {
        return sendGet(url, params, headers, DEFAULT_CONNECT_TIMEOUT, DEFAULT_READ_TIMEOUT);
    }

    /**
     * 发送GET请求(带超时设置)
     * @param url 请求地址
     * @param params 请求参数
     * @param headers 请求头
     * @param connectTimeout 连接超时(毫秒)
     * @param readTimeout 读取超时(毫秒)
     * @return 响应内容
     */
    public static String sendGet(String url, Map<String, String> params, Map<String, String> headers,
                                 int connectTimeout, int readTimeout) {
        StringBuilder result = new StringBuilder();
        BufferedReader in = null;
        HttpURLConnection conn = null;

        try {
            // 构建带参数的URL
            String fullUrl = buildUrlWithParams(url, params);
            URL realUrl = new URL(fullUrl);

            // 打开连接
            conn = (HttpURLConnection) realUrl.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(connectTimeout);
            conn.setReadTimeout(readTimeout);

            // 设置请求头
            setHeaders(conn, headers);

            // 建立连接
            conn.connect();

            // 检查响应状态
            int responseCode = conn.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new IOException("HTTP响应错误: " + responseCode + " - " + conn.getResponseMessage());
            }

            // 读取响应
            in = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
            String line;
            while ((line = in.readLine()) != null) {
                result.append(line);
            }

        } catch (Exception e) {
            log.error("发送GET请求出错: {}", e.getMessage(), e);
            // 尝试读取错误流
            if (conn != null) {
                try (InputStream errorStream = conn.getErrorStream()) {
                    if (errorStream != null) {
                        String errorResponse = readStream(errorStream);
                        log.error("错误响应内容: {}", errorResponse);
                    }
                } catch (IOException ex) {
                    log.error("读取错误流失败: {}", ex.getMessage());
                }
            }
            throw new RuntimeException("发送GET请求失败: " + e.getMessage(), e);
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
                if (conn != null) {
                    conn.disconnect();
                }
            } catch (IOException e) {
                log.error("关闭连接时出错: {}", e.getMessage());
            }
        }
        return result.toString();
    }

    /**
     * 发送POST请求
     * @param url 请求地址
     * @param params 表单参数
     * @param headers 请求头
     * @return 响应内容
     */
    public static String sendPost(String url, Map<String, String> params, Map<String, String> headers) throws UnsupportedEncodingException {
        return sendPost(url, params, headers, DEFAULT_CONNECT_TIMEOUT, DEFAULT_READ_TIMEOUT);
    }

    /**
     * 发送POST请求(带超时设置)
     * @param url 请求地址
     * @param params 表单参数
     * @param headers 请求头
     * @param connectTimeout 连接超时(毫秒)
     * @param readTimeout 读取超时(毫秒)
     * @return 响应内容
     */
    public static String sendPost(String url, Map<String, String> params, Map<String, String> headers,
                                  int connectTimeout, int readTimeout) throws UnsupportedEncodingException {
        return sendPost(url, mapToFormData(params), headers, "application/x-www-form-urlencoded",
                connectTimeout, readTimeout);
    }

    /**
     * 发送POST请求(自定义请求体)
     * @param url 请求地址
     * @param body 请求体内容
     * @param headers 请求头
     * @param contentType 内容类型
     * @return 响应内容
     */
    public static String sendPost(String url, String body, Map<String, String> headers, String contentType) {
        return sendPost(url, body, headers, contentType, DEFAULT_CONNECT_TIMEOUT, DEFAULT_READ_TIMEOUT);
    }

    /**
     * 发送POST请求(自定义请求体，带超时设置)
     * @param url 请求地址
     * @param body 请求体内容
     * @param headers 请求头
     * @param contentType 内容类型
     * @param connectTimeout 连接超时(毫秒)
     * @param readTimeout 读取超时(毫秒)
     * @return 响应内容
     */
    public static String sendPost(String url, String body, Map<String, String> headers, String contentType,
                                  int connectTimeout, int readTimeout) {
        OutputStreamWriter out = null;
        BufferedReader in = null;
        HttpURLConnection conn = null;
        StringBuilder result = new StringBuilder();

        try {
            URL realUrl = new URL(url);
            conn = (HttpURLConnection) realUrl.openConnection();

            // 设置连接属性
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setConnectTimeout(connectTimeout);
            conn.setReadTimeout(readTimeout);

            // 设置请求头
            setHeaders(conn, headers);
            conn.setRequestProperty("Content-Type", contentType);

            // 发送请求体
            out = new OutputStreamWriter(conn.getOutputStream(), StandardCharsets.UTF_8);
            out.write(body);
            out.flush();

            // 检查响应状态
            int responseCode = conn.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new IOException("HTTP响应错误: " + responseCode + " - " + conn.getResponseMessage());
            }

            // 读取响应
            in = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
            String line;
            while ((line = in.readLine()) != null) {
                result.append(line);
            }

        } catch (Exception e) {
            log.error("发送POST请求出错: {}", e.getMessage(), e);
            // 尝试读取错误流
            if (conn != null) {
                try (InputStream errorStream = conn.getErrorStream()) {
                    if (errorStream != null) {
                        String errorResponse = readStream(errorStream);
                        log.error("错误响应内容: {}", errorResponse);
                    }
                } catch (IOException ex) {
                    log.error("读取错误流失败: {}", ex.getMessage());
                }
            }
            throw new RuntimeException("发送POST请求失败: " + e.getMessage(), e);
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
                if (conn != null) {
                    conn.disconnect();
                }
            } catch (IOException e) {
                log.error("关闭连接时出错: {}", e.getMessage());
            }
        }
        return result.toString();
    }

    // ============== 辅助方法 ==============

    /**
     * 构建带参数的URL
     */
    private static String buildUrlWithParams(String url, Map<String, String> params) throws UnsupportedEncodingException {
        if (params == null || params.isEmpty()) {
            return url;
        }

        StringBuilder sb = new StringBuilder(url);
        if (!url.contains("?")) {
            sb.append("?");
        } else if (!url.endsWith("&")) {
            sb.append("&");
        }

        boolean first = true;
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (!first) {
                sb.append("&");
            }
            first = false;
            sb.append(URLEncoder.encode(entry.getKey(), String.valueOf(StandardCharsets.UTF_8)))
                    .append("=")
                    .append(URLEncoder.encode(entry.getValue(), String.valueOf(StandardCharsets.UTF_8)));
        }

        return sb.toString();
    }

    /**
     * 将Map转换为表单数据
     */
    private static String mapToFormData(Map<String, String> params) throws UnsupportedEncodingException {
        if (params == null || params.isEmpty()) {
            return "";
        }
        return buildUrlWithParams("", params).substring(1); // 去掉开头的"?"
    }

    /**
     * 设置请求头
     */
    private static void setHeaders(HttpURLConnection conn, Map<String, String> headers) {
        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                conn.setRequestProperty(entry.getKey(), entry.getValue());
            }
        }
        // 设置默认头
        conn.setRequestProperty("Accept-Charset", "UTF-8");
        conn.setRequestProperty("User-Agent", "Mozilla/5.0");
        conn.setRequestProperty("Connection", "Keep-Alive");
    }

    /**
     * 读取输入流
     */
    private static String readStream(InputStream inputStream) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            return response.toString();
        }
    }


    public static void main(String[] args) {
//        Map<String, String> formParams = new HashMap<>();
//        formParams.put("fileName", "abc");
//        formParams.put("createdBy", "zhangsan");
//
//        Map<String, String> headers = new HashMap<>();
//        headers.put("X-Requested-With", "XMLHttpRequest");
//
//        try {
//            String response = PptHttpUtils.sendPost("http://10.11.52.173:9000/build", formParams, headers);
//            System.out.println("登录结果: " + response);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }



        String jsonBody = "{\"fileName\":\"abc\",\"createdBy\":\"zhangsan\"}";

        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");

        try {
            String response = PptHttpUtils.sendPost("http://10.11.52.173:9000/build", jsonBody, headers, "application/json");
            System.out.println("创建结果: " + response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
