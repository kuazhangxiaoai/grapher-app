package com.bonc.graph.sequence.domain;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 关系表实体
 */
@Data
public class GraphRelation {
    private Long relationId;
    private String relationHash;
    private String relationName;
    private String relationType;
    private String relationTrigger;
    private String startNodeHash;
    private String endNodeHash;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private String fieldId;
    private String topicId;
    private String articleId;
    private String sequenceId;
    // 关系属性（关联查询用）
    private List<GraphRelationProperty> properties;
}
