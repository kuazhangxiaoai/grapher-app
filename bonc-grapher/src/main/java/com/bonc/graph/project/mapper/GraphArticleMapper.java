package com.bonc.graph.project.mapper;

import com.bonc.graph.project.domain.Article;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Mapper
@Repository
public interface GraphArticleMapper {
     /*新增图谱*/
     void addArticle(Article article);

     /*更新图谱 */
     int updateArticle(Article article);

     /* 删除图谱 */
     int deleteArticle(Article article);

     /*根据条件查询图谱*/
     List<Map<String, Object>> selectArticle(@Param("condition") String condition, @Param("topicId") String topicId);

     /*获取文件地址*/
     String getFileUrl(String articleId);

     /*通过主题id查找图谱*/
     List<Article> selectArticlesByTopicId(String oldTopicId);

     /*批量插入图谱*/
     void batchInsertArticles(List<Article> newArticles);
}
