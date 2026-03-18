package com.bonc.graph.sequence.dto;

import lombok.Data;

import java.util.List;

/**
 * 图谱查询返回结果
 */
@Data
public class GraphResponseDTO {
    // 节点列表
    private List<NodeResponseDTO> nodes;
    // 关系列表
    private List<RelationResponseDTO> relations;

    /**
     * 节点返回DTO
     */
    @Data
    public static class NodeResponseDTO {
        private String nodeHash;
        private Long nodeTemplateId;//节点模版ID
        private String nodeTemplateName;//节点模版名称
        private String nodeName;
        private String nodeDescription;
        private String nodeColor;
        private List<PropertyDTO> properties;
    }

    /**
     * 关系返回DTO
     */
    @Data
    public static class RelationResponseDTO {
        private String relationHash;
        private Long relationTemplateId;//关系模版ID
        private String relationTemplateName;//关系模版名称
        private String relationName;
        private String relationType;
        private String relationTrigger;
        private String startNodeHash;
        private String endNodeHash;
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
