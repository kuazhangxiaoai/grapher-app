package com.bonc.graph.sequence.service;

import com.bonc.graph.sequence.domain.GraphSequence;
import com.bonc.graph.sequence.mapper.GraphSequenceMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class GraphSequenceService {

    @Resource
    private GraphSequenceMapper graphSequenceMapper;

    /**
     * 根据articleId查询段落列表
     */
    public List<GraphSequence> getSequenceListByArticleId(String articleId) {
        return graphSequenceMapper.selectByArticleId(articleId);
    }

    /**
     * 新增段落（接收前端传递的段落字段）
     * @param articleId 文章ID
     * @param sequenceContent 段落内容
     * @param sequenceX0 x0坐标
     * @param sequenceY0 y0坐标
     * @param sequenceX1 x1坐标
     * @param sequenceY1 y1坐标
     * @param sequencePage 页码
     * @return 生成的sequenceId
     */
    public String createSequence(String articleId, String sequenceContent,
                                 Integer sequenceX0, Integer sequenceY0,
                                 Integer sequenceX1, Integer sequenceY1,
                                 Integer sequencePage) {
        GraphSequence sequence = new GraphSequence();
        // 生成唯一sequenceId
        String sequenceId = UUID.randomUUID().toString().replace("-", "");
        sequence.setSequenceId(sequenceId);
        sequence.setArticleId(articleId);
        // 填充前端传递的段落字段
        sequence.setSequenceContent(sequenceContent);
        sequence.setSequenceX0(sequenceX0);
        sequence.setSequenceY0(sequenceY0);
        sequence.setSequenceX1(sequenceX1);
        sequence.setSequenceY1(sequenceY1);
        sequence.setSequencePage(sequencePage);
        // 设置时间
        LocalDateTime now = LocalDateTime.now();
        sequence.setCreateTime(now);
        sequence.setUpdateTime(now);
        // 插入数据库
        graphSequenceMapper.insert(sequence);
        return sequenceId;
    }

    /**
     * 更新段落更新时间
     */
    public void updateSequenceUpdateTime(String sequenceId) {
        graphSequenceMapper.updateUpdateTime(sequenceId, LocalDateTime.now());
    }
}
