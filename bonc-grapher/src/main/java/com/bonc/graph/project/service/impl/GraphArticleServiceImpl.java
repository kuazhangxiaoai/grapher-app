package com.bonc.graph.project.service.impl;

import com.bonc.common.config.RuoYiConfig;
import com.bonc.common.utils.DateUtils;
import com.bonc.graph.project.domain.Article;
import com.bonc.graph.project.dto.ArticleDto;
import com.bonc.graph.project.mapper.GraphArticleMapper;
import com.bonc.graph.project.service.GraphArticleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class GraphArticleServiceImpl implements GraphArticleService {

    @Autowired
    private GraphArticleMapper graphArticleMapper;

    @Override
    public List<Map<String, Object>> selectArticle(String condition,String topicId) {
        return graphArticleMapper.selectArticle(condition,topicId);
    }
    /*新增图谱*/
    @Override
    public String addArticle(ArticleDto articleDto, String userName) {
        Article article = new Article();
        // 图谱id
        String articleId = UUID.randomUUID().toString();
        article.setArticleId(articleId);

        // 图谱名字
        String articleName = articleDto.getArticleName();
        article.setArticleName(articleName);

        // 创建方式
        String createMethod = articleDto.getCreateMethod();
        createMethod = (createMethod == null) ? "" : createMethod.trim();
        if("自文本创建".equals(createMethod)){
            article.setCreateMethod("0");
        }else if("自数据库创建".equals(createMethod)){
            article.setCreateMethod("1");
        }else if("任意创建".equals(createMethod)){
            article.setCreateMethod("2");
        }

        // 处理文件上传
        MultipartFile file = articleDto.getMultipartFile();
        if (file != null && !file.isEmpty()) { // 非空校验
            // 获取文件保存基础路径
            String fileSavePath = RuoYiConfig.getUploadPath();
            // 获取原始文件名
            String originalFilename = file.getOriginalFilename();
            originalFilename = (originalFilename == null) ? "" : originalFilename;

            // 获取文件后缀名
            String suffix = getFileSuffix(originalFilename);

            // 生成新的唯一文件名
            String newFileName = UUID.randomUUID().toString() + suffix;
            // 拼接绝对路径
            String filePath = fileSavePath + File.separator + newFileName;

            // 将文件写入到文件夹中
            File destFile = new File(filePath);
            try {
                if (!destFile.getParentFile().exists()) {
                    destFile.getParentFile().mkdirs();
                }
                file.transferTo(destFile);
            } catch (IOException e) {
                throw new RuntimeException("文件上传失败：" + e.getMessage());
            }

            article.setFileName(originalFilename); // 原始文件名
            article.setFileUrl(filePath);          // 文件完整路径
            article.setFileType(suffix);           // 文件后缀
            article.setFileSize(String.valueOf(file.getSize())); // 实际文件大小（字节）
        }


        article.setTopicId(articleDto.getTopicId());
        article.setCreateBy(userName);

        // 将article保存到数据库
        graphArticleMapper.addArticle(article);
        return articleId;
    }

    /*更新图谱*/
    @Override
    public int updateArticle(Article article) {
        article.setUpdateTime(DateUtils.getNowDate());
        return graphArticleMapper.updateArticle(article);
    }

    /*删除图谱*/
    @Override
    public int deleteArticle(Article article) {
        article.setUpdateTime(DateUtils.getNowDate());
        article.setDelFlag("2");
        return graphArticleMapper.deleteArticle(article);
    }



    /**安全文件后缀名*/
    private String getFileSuffix(String originalFilename) {
        if (originalFilename == null || originalFilename.isEmpty()) {
            return "";
        }
        int lastDotIndex = originalFilename.lastIndexOf(".");
        if (lastDotIndex <= 0 || lastDotIndex == originalFilename.length() - 1) {
            return "";
        }
        return originalFilename.substring(lastDotIndex);
    }
}