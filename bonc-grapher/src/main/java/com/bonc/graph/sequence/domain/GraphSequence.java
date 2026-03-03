package com.bonc.graph.sequence.domain;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 段落表实体
 */
@Data
public class GraphSequence {
    private String sequenceId;
    private String sequenceContent;
    private Integer sequenceX0;
    private Integer sequenceY0;
    private Integer sequenceX1;
    private Integer sequenceY1;
    private Integer sequencePage;
    private String articleId;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
