package com.bonc.graph.sequence.service;

import com.bonc.graph.sequence.domain.GraphSequencePosition;
import com.bonc.graph.sequence.mapper.GraphSequencePositionMapper;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 段落位置信息Service
 */
@Service
public class GraphSequencePositionService {

    @Resource
    private GraphSequencePositionMapper graphSequencePositionMapper;

    /**
     * 批量插入位置信息
     */
    @Transactional(rollbackFor = Exception.class)
    public void batchCreateSequencePosition(String sequenceId, List<GraphSequencePosition> positionList) {
        if (CollectionUtils.isEmpty(positionList)) {
            throw new RuntimeException("段落位置信息不能为空");
        }
        LocalDateTime now = LocalDateTime.now();
        for (GraphSequencePosition position : positionList) {
            position.setPositionId(UUID.randomUUID().toString().replace("-", ""));
            position.setSequenceId(sequenceId);
            position.setCreateTime(now);
            position.setUpdateTime(now);
        }
        graphSequencePositionMapper.batchInsert(positionList);
    }

    /**
     * 根据sequenceId查询位置信息列表
     */
    public List<GraphSequencePosition> getPositionListBySequenceId(String sequenceId) {
        return graphSequencePositionMapper.selectBySequenceId(sequenceId);
    }

    /**
     * 根据sequenceId删除位置信息
     */
    @Transactional(rollbackFor = Exception.class)
    public void deletePositionBySequenceId(String sequenceId) {
        graphSequencePositionMapper.deleteBySequenceId(sequenceId);
    }
}
