package com.bonc.graph.sequence.service;

import com.bonc.graph.sequence.domain.GraphSequence;
import com.bonc.graph.sequence.domain.GraphSequencePosition;
import com.bonc.graph.sequence.mapper.GraphSequenceMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class GraphSequenceService {

    @Resource
    private GraphSequenceMapper graphSequenceMapper;
    @Resource
    private GraphSequencePositionService graphSequencePositionService;

    /**
     * 根据articleId查询段落列表（包含关联的位置信息）
     */
    public List<GraphSequence> getSequenceListByArticleId(String articleId) {
        // 1. 查询段落主表数据
        List<GraphSequence> sequenceList = graphSequenceMapper.selectByArticleId(articleId);
        // 2. 为每个段落组装位置信息列表
        if (!CollectionUtils.isEmpty(sequenceList)) {
            for (GraphSequence sequence : sequenceList) {
                List<GraphSequencePosition> positionList = graphSequencePositionService.getPositionListBySequenceId(sequence.getSequenceId());
                sequence.setSequencePositionList(positionList); // 直接设置到实体中
            }
        }
        return sequenceList;
    }

    /**
     * 新增段落（仅核心字段）
     */
    @Transactional(rollbackFor = Exception.class)
    public String createSequence(String articleId, String sequenceContent) {
        GraphSequence sequence = new GraphSequence();
        String sequenceId = UUID.randomUUID().toString().replace("-", "");
        sequence.setSequenceId(sequenceId);
        sequence.setArticleId(articleId);
        sequence.setSequenceContent(sequenceContent);
        LocalDateTime now = LocalDateTime.now();
        sequence.setCreateTime(now);
        sequence.setUpdateTime(now);
        graphSequenceMapper.insert(sequence);
        return sequenceId;
    }

    /**
     * 更新段落更新时间
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateSequenceUpdateTime(String sequenceId) {
        graphSequenceMapper.updateUpdateTime(sequenceId, LocalDateTime.now());
    }

    /**
     * 删除段落（先删子表，再删主表）
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteSequence(String sequenceId) {
        // 1. 校验段落存在
        GraphSequence sequence = graphSequenceMapper.selectBySequenceId(sequenceId);
        if (sequence == null) {
            throw new RuntimeException("段落不存在，sequenceId：" + sequenceId);
        }
        // 2. 删子表：位置信息
        graphSequencePositionService.deletePositionBySequenceId(sequenceId);
        // 3. 删主表
        graphSequenceMapper.deleteBySequenceId(sequenceId);
    }
}
