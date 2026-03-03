package com.bonc.graph.sequence.mapper;

import com.bonc.graph.sequence.domain.GraphNodeProperty;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface GraphNodePropertyMapper {
    /**
     * 批量插入节点属性
     */
    int batchInsert(@Param("properties") List<GraphNodeProperty> properties);

    /**
     * 根据nodeId列表删除节点属性
     */
    int deleteByNodeIds(@Param("nodeIds") List<Long> nodeIds);

    /**
     * 根据nodeId查询节点属性
     */
    List<GraphNodeProperty> selectByNodeId(@Param("nodeId") Long nodeId);

    /**
     * 根据sequenceId删除节点属性（先删节点再删属性时用）
     */
    int deleteBySequenceId(@Param("sequenceId") String sequenceId);
}
