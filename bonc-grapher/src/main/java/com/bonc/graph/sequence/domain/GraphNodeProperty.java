package com.bonc.graph.sequence.domain;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 节点属性表实体
 */
@Data
public class GraphNodeProperty {
    private Long nodePropertyId;
    private Long nodeId;
    private String propertyKey;
    private String propertyValue;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
