package com.bonc.graph.template.domain;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class GraphRelationTemplateProperty {
    private Long relationTemplatePropertyId;
    private Long relationTemplateId;
    private String propertyKey;
    private String propertyType;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private String isDeleteFlag;
}
