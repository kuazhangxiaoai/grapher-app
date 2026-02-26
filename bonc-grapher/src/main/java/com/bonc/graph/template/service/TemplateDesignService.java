package com.bonc.graph.template.service;

import com.bonc.graph.template.dto.AddToModelDTO;
import com.bonc.graph.template.dto.NodeTemplateSaveDTO;
import com.bonc.graph.template.dto.RelationTemplateSaveDTO;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

public interface TemplateDesignService {

    Map<String, Object> queryTemplateByTopicId(@Param("topicId") String topicId);

    void saveNodeTemplate(NodeTemplateSaveDTO dto);

    void saveRelationTemplate(RelationTemplateSaveDTO dto);

    void deleteNodeTemplate(Long nodeTemplateId);

    void deleteRelationTemplate(Long relationTemplateId);

    Map<String, Object> queryLibraryTemplate(String templateName);

    void addToModel(AddToModelDTO dto);
}
