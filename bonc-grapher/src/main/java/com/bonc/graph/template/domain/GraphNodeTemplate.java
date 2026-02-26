package com.bonc.graph.template.domain;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class GraphNodeTemplate {
    private Long nodeTemplateId;
    private String topicId;
    private String nodeTemplateName;
    private String nodeTemplateDescription;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private String isLibraryFlag;
    private String isDeleteFlag;
    private String nodeTemplateColor;
    // 关联属性列表
    private List<GraphNodeTemplateProperty> properties;
}
