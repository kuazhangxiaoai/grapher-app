package com.bonc.graph.template.domain;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class GraphRelationTemplate {
    private Long relationTemplateId;
    private String topicId;
    private String relationTemplateName;
    private String relationTemplateType;
    private Long startNodeTemplateId;
    private Long endNodeTemplateId;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private String isLibraryFlag;
    private String isDeleteFlag;
    // 关联属性列表
    private List<GraphRelationTemplateProperty> properties;
}
