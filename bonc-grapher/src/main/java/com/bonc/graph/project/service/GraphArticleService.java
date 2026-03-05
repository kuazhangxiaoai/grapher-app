package com.bonc.graph.project.service;

import com.bonc.graph.project.domain.Article;
import com.bonc.graph.project.dto.ArticleDto;

import java.util.List;
import java.util.Map;

public interface GraphArticleService {
    /*新增图谱*/
    String addArticle(ArticleDto articleDto, String userName);

    /*更新图谱*/
    int updateArticle(Article article);

    /* 删除图谱 */
    int deleteArticle(Article article);

    /* 根据条件新增图谱 */
    List<Map<String,Object>> selectArticle(String condition,String topicId);

    /* 查找文件的url*/
    String getFileUrl(String articleId);
}
