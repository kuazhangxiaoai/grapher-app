package com.bonc.graph.sequence.mapper;

import com.bonc.graph.sequence.domain.GraphSequencePosition;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 段落位置信息Mapper
 */
@Mapper
public interface GraphSequencePositionMapper {

    /**
     * 插入单条位置信息
     */
    int insert(GraphSequencePosition position);

    /**
     * 批量插入位置信息
     */
    int batchInsert(@Param("list") List<GraphSequencePosition> positionList);

    /**
     * 根据sequenceId查询位置信息列表
     */
    List<GraphSequencePosition> selectBySequenceId(@Param("sequenceId") String sequenceId);

    /**
     * 根据sequenceId删除位置信息
     */
    int deleteBySequenceId(@Param("sequenceId") String sequenceId);
}
