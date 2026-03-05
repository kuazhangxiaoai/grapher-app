package com.bonc.graph.sequence.domain;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 段落位置表实体
 */
@Data
public class GraphSequencePosition {
    private String positionId;       // 位置ID
    private String sequenceId;       // 关联的段落ID
    private Integer sequenceX0;      // 坐标x0
    private Integer sequenceY0;      // 坐标y0
    private Integer sequenceX1;      // 坐标x1
    private Integer sequenceY1;      // 坐标y1
    private Integer sequencePage;    // 页码
    private LocalDateTime createTime;// 创建时间
    private LocalDateTime updateTime;// 更新时间
}
