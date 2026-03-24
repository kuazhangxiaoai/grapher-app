package com.bonc.graph.sequence.dto;

import lombok.Data;

import java.util.List;

@Data
public class RelationSaveDTO {
    private Integer level;
    private String levelId;
    private GraphRelationDTO relation;
    private List<GraphRelationPropertyDTO> properties;

    @Data
    public static class GraphRelationDTO {
        private Long relationId;
        private String relationHash;
        private String relationName;
        private String relationType;
        private String relationTrigger;
        private String startNodeHash;
        private String endNodeHash;
        private String relationTemplateName;
        private Long relationTemplateId;
    }








    @Data
    public static class GraphRelationPropertyDTO {
        private String propertyKey;
        private String propertyValue;
    }
}
