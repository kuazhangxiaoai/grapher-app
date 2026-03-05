package com.bonc.graph.sequence.dto;

import lombok.Data;

import java.util.List;

/**
 * 段落列表返回DTO
 * 包含段落基础信息和位置信息
 */
@Data
public class GraphSequenceDTO {
    // 段落ID
    private String sequenceId;
    // 段落内容
    private String sequenceContent;
    // 文章ID
    private String articleId;
    // 段落位置信息列表
    private List<SequencePosition> sequencePositionList;

    /**
     * 段落位置信息内部类
     * 仅保留前端需要的核心坐标字段
     */
    @Data
    public static class SequencePosition {
//        // 位置ID
//        private String positionId;
//        // 关联的段落ID
//        private String sequenceId;
        // 坐标x0
        private Integer sequenceX0;
        // 坐标y0
        private Integer sequenceY0;
        // 坐标x1
        private Integer sequenceX1;
        // 坐标y1
        private Integer sequenceY1;
        // 页码
        private Integer sequencePage;
    }
}
