package com.bonc.graph.sequence.mapper;

import com.bonc.graph.sequence.domain.GraphSequence;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface GraphSequenceMapper {
    /**
     * 根据articleId查询段落列表
     */
    List<GraphSequence> selectByArticleId(@Param("articleId") String articleId);

    /**
     * 根据sequenceId查询段落
     */
    GraphSequence selectBySequenceId(@Param("sequenceId") String sequenceId);

    /**
     * 插入段落主表
     */
    int insert(GraphSequence sequence);

    /**
     * 更新段落更新时间
     */
    int updateUpdateTime(@Param("sequenceId") String sequenceId, @Param("updateTime") LocalDateTime updateTime);

    /**
     * 根据sequenceId删除段落
     */
    int deleteBySequenceId(@Param("sequenceId") String sequenceId);
}
