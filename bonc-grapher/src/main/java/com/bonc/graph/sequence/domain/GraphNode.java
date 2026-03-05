package com.bonc.graph.sequence.domain;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 节点表实体
 */
@Data
public class GraphNode {
    private Long nodeId;
    private String nodeHash;
    private String nodeTemplateName;//节点类型
    private String nodeName;
    private String nodeDescription;
    private String nodeColor;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private String fieldId;
    private String topicId;
    private String articleId;
    private String sequenceId;
    // 节点属性（关联查询用）
    private List<GraphNodeProperty> properties;
}
