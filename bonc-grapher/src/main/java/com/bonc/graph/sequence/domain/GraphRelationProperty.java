package com.bonc.graph.sequence.domain;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 关系属性表实体
 */
@Data
public class GraphRelationProperty {
    private Long relationPropertyId;
    private Long relationId;
    private String propertyKey;
    private String propertyValue;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
