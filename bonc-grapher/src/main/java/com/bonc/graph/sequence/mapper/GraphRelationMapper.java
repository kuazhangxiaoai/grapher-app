package com.bonc.graph.sequence.mapper;

import com.bonc.graph.sequence.domain.GraphRelation;
import com.bonc.graph.sequence.dto.SearchNodeOrRelationNameDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface GraphRelationMapper {
    /**
     * 批量插入关系
     */
    int batchInsert(@Param("relations") List<GraphRelation> relations);

    /**
     * 根据sequenceId删除关系
     */
    int deleteBySequenceId(@Param("sequenceId") String sequenceId);

    /**
     * 根据relationTemplateId删除关系
     */
    int deleteByRelationTemplateId(@Param("relationTemplateId") Long relationTemplateId);

    /**
     * 根据sequenceId查询关系列表
     */
    List<GraphRelation> selectBySequenceId(@Param("sequenceId") String sequenceId);

    /**
     * 根据articleId查询关系列表（去重）
     */
    List<GraphRelation> selectDistinctByArticleId(@Param("articleId") String articleId);

    /**
     * 根据topicId查询关系列表（去重）
     */
    List<GraphRelation> selectDistinctByTopicId(@Param("topicId") String topicId);

    /**
     * 根据fieldId查询关系列表（去重）
     */
    List<GraphRelation> selectDistinctByFieldId(@Param("fieldId") String fieldId);

    /**
     * 根据articleId和名称查询关系名称
     */
    List<String> getRelationNamesByArticleId(@Param("dto") SearchNodeOrRelationNameDTO searchNodeOrRelationNameDTO);

    List<GraphRelation> selectByLevelAndHash(@Param("level") Integer level,
                                             @Param("levelId") String levelId,
                                             @Param("relationHash") String relationHash);

    List<GraphRelation> selectByNodeHash(@Param("nodeHash") String nodeHash);

    int insert(GraphRelation graphRelation);

    int batchUpdate(@Param("relations") List<GraphRelation> relations);

    int batchDelete(@Param("relationIds") List<Long> relationIds);

}
