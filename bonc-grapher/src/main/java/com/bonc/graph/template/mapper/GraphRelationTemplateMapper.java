package com.bonc.graph.template.mapper;

import com.bonc.graph.template.domain.GraphRelationTemplate;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface GraphRelationTemplateMapper {
    // 按topicId查询关系模版（未删除）
    List<GraphRelationTemplate> selectByTopicId(@Param("topicId") String topicId);
    // 按ID查询关系模版（未删除）
    GraphRelationTemplate selectById(@Param("id") Long id);
    // 按名称+专题ID查询（未删除）
    int countByNameAndTopicId(@Param("name") String name, @Param("topicId") String topicId);
    // 插入关系模版
    int insert(GraphRelationTemplate template);
    // 更新删除标识
    int updateDeleteFlag(@Param("id") Long id, @Param("isDeleteFlag") String isDeleteFlag);
    // 模糊查询组件库关系（isLibraryFlag=1，未删除）
    List<GraphRelationTemplate> selectLibraryByLikeName(@Param("name") String name);
}
