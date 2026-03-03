package com.bonc.graph.sequence.mapper;

import com.bonc.graph.sequence.domain.GraphRelationProperty;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface GraphRelationPropertyMapper {
    /**
     * 批量插入关系属性
     */
    int batchInsert(@Param("properties") List<GraphRelationProperty> properties);

    /**
     * 根据relationId列表删除关系属性
     */
    int deleteByRelationIds(@Param("relationIds") List<Long> relationIds);

    /**
     * 根据relationId查询关系属性
     */
    List<GraphRelationProperty> selectByRelationId(@Param("relationId") Long relationId);

    /**
     * 根据sequenceId删除关系属性
     */
    int deleteBySequenceId(@Param("sequenceId") String sequenceId);
}
