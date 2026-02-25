package com.bonc.graph.project.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class ArticleDto {
    public String articleName; // 图谱名字
    public String createMethod;// 创建方式
    public String fileSize;// 文件大小
    public String  topicId; //专题id
    public MultipartFile multipartFile; //文件
}
