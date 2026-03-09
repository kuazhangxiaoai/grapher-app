package com.bonc.graph.template.service;

import com.bonc.graph.template.domain.GraphNodeTemplate;
import com.bonc.graph.template.domain.GraphNodeTemplateProperty;
import com.bonc.graph.template.domain.GraphRelationTemplate;
import com.bonc.graph.template.domain.GraphRelationTemplateProperty;
import com.bonc.graph.template.dto.AddToModelDTO;
import com.bonc.graph.template.dto.NodeTemplateSaveDTO;
import com.bonc.graph.template.dto.RelationTemplateSaveDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface TemplateDesignService {

    Map<String, Object> queryTemplateByTopicId(@Param("topicId") String topicId);

    void saveNodeTemplate(NodeTemplateSaveDTO dto);

    void saveRelationTemplate(RelationTemplateSaveDTO dto);

    void deleteNodeTemplate(Long nodeTemplateId);

    void deleteRelationTemplate(Long relationTemplateId);

    Map<String, Object> queryLibraryTemplate(String templateName);

    void addToModel(AddToModelDTO dto);

    List<GraphNodeTemplate> queryNodeTemplate(@Param("topicId") String topicId);

    List<GraphNodeTemplateProperty> queryNodeTemplateProperties(@Param("nodeTemplateId") Long nodeTemplateId);

    List<GraphRelationTemplate> queryRelationTemplate(@Param("topicId") String topicId);

    List<GraphRelationTemplateProperty> queryRelationTemplateProperties(@Param("relationTemplateId") Long relationTemplateId);
}
