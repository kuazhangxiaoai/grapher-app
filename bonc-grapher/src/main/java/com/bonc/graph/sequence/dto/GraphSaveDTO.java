package com.bonc.graph.sequence.dto;

import lombok.Data;

import java.util.List;

/**
 * 图谱保存提交入参
 */
@Data
public class GraphSaveDTO {
    private String articleId;
    private String sequenceId; // 为空则新增，不为空则修改

    //段落相关字段（前端上送）
    private String sequenceContent;
    private Integer sequenceX0;
    private Integer sequenceY0;
    private Integer sequenceX1;
    private Integer sequenceY1;
    private Integer sequencePage;

    // 节点列表
    private List<NodeDTO> graphNode;
    // 关系列表
    private List<RelationDTO> graphRelation;

    /**
     * 节点DTO
     */
    @Data
    public static class NodeDTO {
        private String nodeHash;
        private String nodeName;
        private String nodeDescription;
        private String nodeColor;
        // 节点属性
        private List<PropertyDTO> properties;
    }

    /**
     * 关系DTO
     */
    @Data
    public static class RelationDTO {
        private String relationName;
        private String relationType;
        private String relationTrigger;
        private String startNodeHash;
        private String endNodeHash;
        // 关系属性
        private List<PropertyDTO> properties;
    }

    /**
     * 属性DTO
     */
    @Data
    public static class PropertyDTO {
        private String propertyKey;
        private String propertyValue;
    }
}
