package com.bonc.common.utils.file;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 文件下载工具类
 */
public class FileDownloadUtil {

    /**
     * 下载文件到响应流（适用于中小型文件）
     * @param filePath 文件完整路径
     * @param response HttpServletResponse
     * @throws IOException 文件操作异常
     */
    public static void downloadFile(String filePath, HttpServletResponse response) throws IOException {
        File file = new File(filePath);
        if (!file.exists()) {
            throw new FileNotFoundException("文件不存在: " + filePath);
        }

        // 设置响应头
        response.setContentType("application/octet-stream");
        response.setContentLengthLong(file.length());
        
        // 处理文件名编码
        String encodedFileName = URLEncoder.encode(file.getName(), "UTF-8").replace("+", "%20");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, 
                         "attachment; filename=\"" + encodedFileName + "\"");

        // 使用缓冲流提高性能
        try (InputStream in = new BufferedInputStream(new FileInputStream(file));
             OutputStream out = new BufferedOutputStream(response.getOutputStream())) {
            
            byte[] buffer = new byte[8192]; // 8KB缓冲区
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
            out.flush();
        }
    }

    /**
     * 下载文件并返回ResponseEntity（推荐Spring Boot使用）
     * @param filePath 文件完整路径
     * @return ResponseEntity<Resource>
     * @throws IOException 文件操作异常
     */
    public static ResponseEntity<Resource> downloadFileAsResponse(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        Resource resource = new UrlResource(path.toUri());

        if (!resource.exists() || !resource.isReadable()) {
            throw new FileNotFoundException("文件不存在或不可读: " + filePath);
        }

        String encodedFileName = URLEncoder.encode(path.getFileName().toString(), "UTF-8")
                                        .replace("+", "%20");

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, 
                       "attachment; filename=\"" + encodedFileName + "\"")
                .body(resource);
    }

    /**
     * 大文件下载（使用StreamingResponseBody避免内存溢出）
     * @param filePath 文件完整路径
     * @return ResponseEntity<StreamingResponseBody>
     * @throws IOException 文件操作异常
     */
    public static ResponseEntity<StreamingResponseBody> downloadLargeFile(String filePath) throws IOException {
        File file = new File(filePath);
        if (!file.exists()) {
            throw new FileNotFoundException("文件不存在: " + filePath);
        }

        String encodedFileName = URLEncoder.encode(file.getName(), "UTF-8")
                                        .replace("+", "%20");

        StreamingResponseBody responseBody = outputStream -> {
            try (InputStream in = new BufferedInputStream(new FileInputStream(file))) {
                byte[] buffer = new byte[16384]; // 16KB缓冲区
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                outputStream.flush();
            }
        };

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, 
                       "attachment; filename=\"" + encodedFileName + "\"")
                .body(responseBody);
    }
}