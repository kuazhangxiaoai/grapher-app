package com.bonc.graph.project.mapper;

import com.bonc.graph.project.domain.Article;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface GraphArticleMapper {
     /*新增图谱*/
     void addArticle(Article article);

     /*更新图谱 */
     int updateArticle(Article article);

     /* 删除图谱 */
     int deleteArticle(Article article);

     List<Map<String, Object>> selectArticle(@Param("condition") String condition, @Param("topicId") String topicId);
}
