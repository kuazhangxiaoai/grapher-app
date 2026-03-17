package com.bonc.graph.sequence.mapper;

import com.bonc.graph.sequence.domain.GraphNode;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface GraphNodeMapper {
    /**
     * 批量插入节点
     */
    int batchInsert(@Param("nodes") List<GraphNode> nodes);

    /**
     * 根据sequenceId删除节点
     */
    int deleteBySequenceId(@Param("sequenceId") String sequenceId);

    /**
     * 根据sequenceId查询节点列表
     */
    List<GraphNode> selectBySequenceId(@Param("sequenceId") String sequenceId);

    /**
     * 根据articleId查询节点列表（去重）
     */
    List<GraphNode> selectDistinctByArticleId(@Param("articleId") String articleId);

    /**
     * 根据topicId查询节点列表（去重）
     */
    List<GraphNode> selectDistinctByTopicId(@Param("topicId") String topicId);

    /**
     * 根据fieldId查询节点列表（去重）
     */
    List<GraphNode> selectDistinctByFieldId(@Param("fieldId") String fieldId);

    /**
     * 根据articleId和名称查询节点名称
     */
    List<String> getNodeNamesByArticleId(@Param("articleId")String articleId, @Param("nodeName")String nodeName);


}
