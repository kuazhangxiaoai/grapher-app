package com.bonc.graph.template.mapper;

import com.bonc.graph.template.domain.GraphNodeTemplate;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface GraphNodeTemplateMapper {
    // 按topicId查询节点模版（未删除）
    List<GraphNodeTemplate> selectByTopicId(@Param("topicId") String topicId);
    // 按ID查询节点模版（未删除）
    GraphNodeTemplate selectById(@Param("id") Long id);
    // 按名称+专题ID查询（未删除）
    int countByNameAndTopicId(@Param("name") String name, @Param("topicId") String topicId);
    // 插入节点模版
    int insert(GraphNodeTemplate template);
    // 更新删除标识
    int updateDeleteFlag(@Param("id") Long id, @Param("isDeleteFlag") String isDeleteFlag);
    // 模糊查询组件库节点（isLibraryFlag=1，未删除）
    List<GraphNodeTemplate> selectLibraryByLikeName(@Param("name") String name);
}
