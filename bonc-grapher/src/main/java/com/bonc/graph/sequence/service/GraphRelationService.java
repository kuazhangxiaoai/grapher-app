package com.bonc.graph.sequence.service;

import com.bonc.graph.sequence.domain.GraphRelation;
import com.bonc.graph.sequence.domain.GraphRelationProperty;
import com.bonc.graph.sequence.dto.GraphSaveDTO;
import com.bonc.graph.sequence.mapper.GraphRelationMapper;
import com.bonc.graph.sequence.mapper.GraphRelationPropertyMapper;
import com.bonc.graph.utils.HashUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class GraphRelationService {

    @Resource
    private GraphRelationMapper graphRelationMapper;
    @Resource
    private GraphRelationPropertyMapper graphRelationPropertyMapper;

    /**
     * 批量保存关系及属性
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveRelations(List<GraphSaveDTO.RelationDTO> relationDTOList, String sequenceId, String articleId,
                              String topicId, String fieldId) {
        if (relationDTOList == null || relationDTOList.isEmpty()) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        List<GraphRelation> relationList = new ArrayList<>();
        List<GraphRelationProperty> propertyList = new ArrayList<>();

        // 1. 构建关系
        for (GraphSaveDTO.RelationDTO relationDTO : relationDTOList) {
            // 生成关系Hash
            String relationHash = HashUtil.generateRelationHash(
                    relationDTO.getRelationName(),
                    relationDTO.getRelationType(),
                    relationDTO.getRelationTrigger(),
                    relationDTO.getStartNodeHash(),
                    relationDTO.getEndNodeHash(),
                    relationDTO.getProperties()
            );

            GraphRelation relation = new GraphRelation();
            relation.setRelationHash(relationHash);
            relation.setRelationName(relationDTO.getRelationName());
            relation.setRelationType(relationDTO.getRelationType());
            relation.setRelationTrigger(relationDTO.getRelationTrigger());
            relation.setStartNodeHash(relationDTO.getStartNodeHash());
            relation.setEndNodeHash(relationDTO.getEndNodeHash());
            relation.setCreateTime(now);
            relation.setUpdateTime(now);
            relation.setFieldId(fieldId);
            relation.setTopicId(topicId);
            relation.setArticleId(articleId);
            relation.setSequenceId(sequenceId);
            relationList.add(relation);
        }

        // 2. 批量插入关系
        graphRelationMapper.batchInsert(relationList);

        // 3. 重新查询关系获取relationId
        List<GraphRelation> savedRelations = graphRelationMapper.selectBySequenceId(sequenceId);

        // 4. 构建并插入关系属性
        for (int i = 0; i < savedRelations.size(); i++) {
            GraphRelation relation = savedRelations.get(i);
            GraphSaveDTO.RelationDTO relationDTO = relationDTOList.get(i);
            List<GraphSaveDTO.PropertyDTO> properties = relationDTO.getProperties();

            if (properties != null && !properties.isEmpty()) {
                for (GraphSaveDTO.PropertyDTO propDTO : properties) {
                    GraphRelationProperty property = new GraphRelationProperty();
                    property.setRelationId(relation.getRelationId());
                    property.setPropertyKey(propDTO.getPropertyKey());
                    property.setPropertyValue(propDTO.getPropertyValue());
                    property.setCreateTime(now);
                    property.setUpdateTime(now);
                    propertyList.add(property);
                }
            }
        }

        if (!propertyList.isEmpty()) {
            graphRelationPropertyMapper.batchInsert(propertyList);
        }
    }

    /**
     * 根据sequenceId删除关系及属性
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteRelationsBySequenceId(String sequenceId) {
        // 1. 删除关系属性
        graphRelationPropertyMapper.deleteBySequenceId(sequenceId);
        // 2. 删除关系
        graphRelationMapper.deleteBySequenceId(sequenceId);
    }

    /**
     * 根据sequenceId查询关系（含属性）
     */
    public List<GraphRelation> getRelationsBySequenceId(String sequenceId) {
        List<GraphRelation> relations = graphRelationMapper.selectBySequenceId(sequenceId);
        // 填充属性
        for (GraphRelation relation : relations) {
            List<GraphRelationProperty> properties = graphRelationPropertyMapper.selectByRelationId(relation.getRelationId());
            relation.setProperties(properties);
        }
        return relations;
    }

    /**
     * 根据articleId查询关系（去重，含属性）
     */
    public List<GraphRelation> getDistinctRelationsByArticleId(String articleId) {
        List<GraphRelation> relations = graphRelationMapper.selectDistinctByArticleId(articleId);
        // 填充属性
        for (GraphRelation relation : relations) {
            List<GraphRelationProperty> properties = graphRelationPropertyMapper.selectByRelationId(relation.getRelationId());
            relation.setProperties(properties);
        }
        return relations;
    }
}
