package com.bonc.graph.sequence.service;

import com.bonc.graph.sequence.domain.GraphSequence;
import com.bonc.graph.sequence.domain.GraphSequencePosition;
import com.bonc.graph.sequence.dto.GraphSequenceDTO;
import com.bonc.graph.sequence.mapper.GraphSequenceMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class GraphSequenceService {

    @Resource
    private GraphSequenceMapper graphSequenceMapper;
    @Resource
    private GraphSequencePositionService graphSequencePositionService;

    /**
     * 根据articleId查询段落列表
     */
    public List<GraphSequenceDTO> getSequenceListByArticleId(String articleId) {
        // 1. 查询段落主表数据（PostgreSQL适配）
        List<GraphSequence> sequenceList = graphSequenceMapper.selectByArticleId(articleId);

        // 2. 空值处理
        if (CollectionUtils.isEmpty(sequenceList)) {
            return Collections.emptyList();
        }

        // 3. 转换为DTO列表
        return sequenceList.stream().map(sequence -> {
            GraphSequenceDTO dto = new GraphSequenceDTO();
            BeanUtils.copyProperties(sequence, dto);

            // 4. 处理位置信息列表转换
            List<GraphSequencePosition> positionList = graphSequencePositionService.getPositionListBySequenceId(sequence.getSequenceId());
            if (!CollectionUtils.isEmpty(positionList)) {
                List<GraphSequenceDTO.SequencePosition> positionDTOList = positionList.stream()
                        .map(position -> {
                            GraphSequenceDTO.SequencePosition positionDTO = new GraphSequenceDTO.SequencePosition();
                            BeanUtils.copyProperties(position, positionDTO);
                            return positionDTO;
                        })
                        .collect(Collectors.toList());
                dto.setSequencePositionList(positionDTOList);
            } else {
                dto.setSequencePositionList(new ArrayList<>());
            }

            return dto;
        }).collect(Collectors.toList());
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
