package com.bonc.graph.sequence.dto;

import lombok.Data;

@Data
public class SearchNodeOrRelationNameDTO {
    private String articleId;
    private Long nodeTemplateId;
    private Long  relationTemplateId;
    private String nodeName; //搜索的名称
    private String relationName; //搜索的关系名称
}
