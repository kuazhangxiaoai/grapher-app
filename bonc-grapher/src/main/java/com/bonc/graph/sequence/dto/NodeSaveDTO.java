package com.bonc.graph.sequence.dto;

import com.bonc.graph.sequence.domain.GraphSequencePosition;
import lombok.Data;

import java.util.List;

@Data
public class NodeSaveDTO {
    //层级：1-全部 2-领域下 3-专题下 4-文章下 5-段落下
    private Integer level;
    //层级关联ID：level=2填fieldId的值，level=3填topicId的值，level=4填articleId的值，level=5填sequenceId的值
    private String levelId;
    private GraphNodeDTO node;
    private List<GraphNodePropertyDTO> properties;

    private String articleId;
    private String sequenceId; // 为空则新增段落，不为空为修改
    //段落内容
    private String sequenceContent;
    // 段落位置信息列表
    private List<GraphSequencePosition> sequencePositionList;

    @Data
    public static class GraphNodeDTO {
        private Long nodeId;
        private String nodeHash;
        private String nodeName;
        private String nodeDescription;
        private String nodeColor;
        private String nodeTemplateName;
        private Long nodeTemplateId;
    }

    @Data
    public static class GraphNodePropertyDTO {
        private String propertyKey;
        private String propertyValue;
    }
}
