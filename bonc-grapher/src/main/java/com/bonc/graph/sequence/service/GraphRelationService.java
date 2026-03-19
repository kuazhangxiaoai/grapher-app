package com.bonc.graph.sequence.service;

import com.bonc.graph.sequence.domain.GraphRelation;
import com.bonc.graph.sequence.domain.GraphRelationProperty;
import com.bonc.graph.sequence.dto.GraphSaveDTO;
import com.bonc.graph.sequence.mapper.GraphRelationMapper;
import com.bonc.graph.sequence.mapper.GraphRelationPropertyMapper;
import com.bonc.graph.template.domain.GraphNodeTemplate;
import com.bonc.graph.template.domain.GraphRelationTemplate;
import com.bonc.graph.template.mapper.GraphRelationTemplateMapper;
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
    @Resource
    private GraphRelationTemplateMapper relationTemplateMapper;

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
            relation.setRelationTemplateId(relationDTO.getRelationTemplateId());
            relation.setRelationTemplateName(relationDTO.getRelationTemplateName());
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
        // 1. 查询原始节点列表
        List<GraphRelation> relations = graphRelationMapper.selectBySequenceId(sequenceId);
        // 用于存储最终返回的有效节点
        List<GraphRelation> validRelations = new ArrayList<>();

        // 2. 第一步：过滤+更新模板信息（先处理无效节点，避免后续无效查询）
        for (GraphRelation relation : relations) {
            Long relationNodeTemplateId = relation.getRelationTemplateId();
            // 过滤条件：nodeTemplateId为空 或 模板查询不到
            if (relationNodeTemplateId == null) {
                continue;
            }
            GraphRelationTemplate relationTemplate = relationTemplateMapper.selectById(relationNodeTemplateId);
            if (relationTemplate == null) {
                continue;
            }

            // 模板存在，更新内存中节点的名称和颜色
            relation.setRelationTemplateName(relationTemplate.getRelationTemplateName());
            relation.setRelationType(relationTemplate.getRelationTemplateType());
            // 仅将有效节点加入待填充属性的列表
            validRelations.add(relation);
        }

        // 3. 第二步：仅为有效节点填充属性（减少无效的数据库查询）
        for (GraphRelation validRelation : validRelations) {
            List<GraphRelationProperty> properties = graphRelationPropertyMapper.selectByRelationId(validRelation.getRelationId());
            validRelation.setProperties(properties);
        }

        // 4. 返回最终处理后的有效节点列表
        return validRelations;
    }

    /**
     * 根据articleId查询关系（去重，含属性）
     */
    public List<GraphRelation> getDistinctRelationsByArticleId(String articleId) {
        // 1. 查询原始节点列表
        List<GraphRelation> relations = graphRelationMapper.selectDistinctByArticleId(articleId);
        // 用于存储最终返回的有效节点
        List<GraphRelation> validRelations = new ArrayList<>();

        // 2. 第一步：过滤+更新模板信息（先处理无效节点，避免后续无效查询）
        for (GraphRelation relation : relations) {
            Long relationNodeTemplateId = relation.getRelationTemplateId();
            // 过滤条件：nodeTemplateId为空 或 模板查询不到
            if (relationNodeTemplateId == null) {
                continue;
            }
            GraphRelationTemplate relationTemplate = relationTemplateMapper.selectById(relationNodeTemplateId);
            if (relationTemplate == null) {
                continue;
            }

            // 模板存在，更新内存中节点的名称和颜色
            relation.setRelationTemplateName(relationTemplate.getRelationTemplateName());
            relation.setRelationType(relationTemplate.getRelationTemplateType());
            // 仅将有效节点加入待填充属性的列表
            validRelations.add(relation);
        }

        // 3. 第二步：仅为有效节点填充属性（减少无效的数据库查询）
        for (GraphRelation validRelation : validRelations) {
            List<GraphRelationProperty> properties = graphRelationPropertyMapper.selectByRelationId(validRelation.getRelationId());
            validRelation.setProperties(properties);
        }

        // 4. 返回最终处理后的有效节点列表
        return validRelations;
    }

    /**
     * 根据topicId查询关系（去重，含属性）
     */
    public List<GraphRelation> getDistinctRelationsByTopicId(String topicId) {
        // 1. 查询原始节点列表
        List<GraphRelation> relations = graphRelationMapper.selectDistinctByTopicId(topicId);
        // 用于存储最终返回的有效节点
        List<GraphRelation> validRelations = new ArrayList<>();

        // 2. 第一步：过滤+更新模板信息（先处理无效节点，避免后续无效查询）
        for (GraphRelation relation : relations) {
            Long relationNodeTemplateId = relation.getRelationTemplateId();
            // 过滤条件：nodeTemplateId为空 或 模板查询不到
            if (relationNodeTemplateId == null) {
                continue;
            }
            GraphRelationTemplate relationTemplate = relationTemplateMapper.selectById(relationNodeTemplateId);
            if (relationTemplate == null) {
                continue;
            }

            // 模板存在，更新内存中节点的名称和颜色
            relation.setRelationTemplateName(relationTemplate.getRelationTemplateName());
            relation.setRelationType(relationTemplate.getRelationTemplateType());
            // 仅将有效节点加入待填充属性的列表
            validRelations.add(relation);
        }

        // 3. 第二步：仅为有效节点填充属性（减少无效的数据库查询）
        for (GraphRelation validRelation : validRelations) {
            List<GraphRelationProperty> properties = graphRelationPropertyMapper.selectByRelationId(validRelation.getRelationId());
            validRelation.setProperties(properties);
        }

        // 4. 返回最终处理后的有效节点列表
        return validRelations;
    }

    /**
     * 根据fieldId查询关系（去重，含属性）
     */
    public List<GraphRelation> getDistinctRelationsByFieldId(String fieldId) {
        // 1. 查询原始节点列表
        List<GraphRelation> relations = graphRelationMapper.selectDistinctByFieldId(fieldId);
        // 用于存储最终返回的有效节点
        List<GraphRelation> validRelations = new ArrayList<>();

        // 2. 第一步：过滤+更新模板信息（先处理无效节点，避免后续无效查询）
        for (GraphRelation relation : relations) {
            Long relationNodeTemplateId = relation.getRelationTemplateId();
            // 过滤条件：nodeTemplateId为空 或 模板查询不到
            if (relationNodeTemplateId == null) {
                continue;
            }
            GraphRelationTemplate relationTemplate = relationTemplateMapper.selectById(relationNodeTemplateId);
            if (relationTemplate == null) {
                continue;
            }

            // 模板存在，更新内存中节点的名称和颜色
            relation.setRelationTemplateName(relationTemplate.getRelationTemplateName());
            relation.setRelationType(relationTemplate.getRelationTemplateType());
            // 仅将有效节点加入待填充属性的列表
            validRelations.add(relation);
        }

        // 3. 第二步：仅为有效节点填充属性（减少无效的数据库查询）
        for (GraphRelation validRelation : validRelations) {
            List<GraphRelationProperty> properties = graphRelationPropertyMapper.selectByRelationId(validRelation.getRelationId());
            validRelation.setProperties(properties);
        }

        // 4. 返回最终处理后的有效节点列表
        return validRelations;
    }

    /**
     * 根据articleId查询所有关系名称
     */
    public Object getRelationNamesByArticleId(String articleId, String relationName,Long relationTemplateId) {
        return graphRelationMapper.getRelationNamesByArticleId(articleId,relationName,relationTemplateId);
    }
}
