package com.bonc.graph.sequence.domain;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 段落表实体
 */
@Data
public class GraphSequence {
    private String sequenceId;
    private String sequenceContent;
    private String articleId;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private List<GraphSequencePosition> sequencePositionList;
}
