package com.bonc.graph.template.service.impl;

import com.bonc.graph.template.domain.GraphNodeTemplate;
import com.bonc.graph.template.domain.GraphNodeTemplateProperty;
import com.bonc.graph.template.domain.GraphRelationTemplate;
import com.bonc.graph.template.domain.GraphRelationTemplateProperty;
import com.bonc.graph.template.dto.*;
import com.bonc.graph.template.mapper.GraphNodeTemplateMapper;
import com.bonc.graph.template.mapper.GraphNodeTemplatePropertyMapper;
import com.bonc.graph.template.mapper.GraphRelationTemplateMapper;
import com.bonc.graph.template.mapper.GraphRelationTemplatePropertyMapper;
import com.bonc.graph.template.service.TemplateDesignService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TemplateDesignServiceImpl implements TemplateDesignService {
    @Resource
    private GraphNodeTemplateMapper nodeTemplateMapper;
    @Resource
    private GraphNodeTemplatePropertyMapper nodePropertyMapper;
    @Resource
    private GraphRelationTemplateMapper relationTemplateMapper;
    @Resource
    private GraphRelationTemplatePropertyMapper relationPropertyMapper;


    // 1. 本体设计-节点/关系模版查询接口
    public Map<String, Object> queryTemplateByTopicId(String topicId) {
        Map<String, Object> result = new HashMap<>();
        // 查询节点模版+属性
        List<GraphNodeTemplate> nodeTemplates = nodeTemplateMapper.selectByTopicId(topicId);
        if (!CollectionUtils.isEmpty(nodeTemplates)) {
            nodeTemplates.forEach(node -> {
                List<GraphNodeTemplateProperty> properties = nodePropertyMapper.selectByNodeTemplateId(node.getNodeTemplateId());
                node.setProperties(properties);
            });
        }
        // 查询关系模版+属性

        List<GraphRelationTemplate> relationTemplates = relationTemplateMapper.selectByTopicId(topicId);
        if (!CollectionUtils.isEmpty(relationTemplates)) {
            relationTemplates.forEach(relation -> {
                List<GraphRelationTemplateProperty> properties = relationPropertyMapper.selectByRelationTemplateId(relation.getRelationTemplateId());
                relation.setProperties(properties);
            });
        }
        result.put("nodeTemplates", nodeTemplates);
        result.put("relationTemplates", relationTemplates);
        return result;
    }

    // 2. 本体设计-节点模版保存接口
    @Transactional(rollbackFor = Exception.class)
    public void saveNodeTemplate(NodeTemplateSaveDTO dto) {

        List<GraphNodeTemplate> templates = new ArrayList<>();
        GraphNodeTemplate mainTemplate = buildNodeTemplate(dto, dto.getTopicId(),"0");
        templates.add(mainTemplate);

        if ("1".equals(dto.getIsLibraryFlag())) {
            // 处理组件库标识=1的情况：插入组件库数据，topicId为空，isLibraryFlag=1
            GraphNodeTemplate libraryTemplate = buildNodeTemplate(dto, null, "1");
            templates.add(libraryTemplate);
        }

        // 插入节点模版
        templates.forEach(template -> {
            nodeTemplateMapper.insert(template);
            // 插入属性
            if (!CollectionUtils.isEmpty(dto.getProperties())) {
                List<GraphNodeTemplateProperty> properties = buildNodeProperties(dto.getProperties(), template.getNodeTemplateId());
                nodePropertyMapper.batchInsert(properties);
            }
        });
    }

    // 3. 本体设计-关系模版保存接口
    @Transactional(rollbackFor = Exception.class)
    public void saveRelationTemplate(RelationTemplateSaveDTO dto) {
        List<GraphRelationTemplate> templates = new ArrayList<>();
        GraphRelationTemplate mainTemplate = buildRelationTemplate(dto, dto.getTopicId(), "0");
        templates.add(mainTemplate);

        if ("1".equals(dto.getIsLibraryFlag())) {
            GraphRelationTemplate libraryTemplate = buildRelationTemplate(dto, null, "1");
            templates.add(libraryTemplate);
        }

        // 插入关系模版
        templates.forEach(template -> {
            relationTemplateMapper.insert(template);
            // 插入属性
            if (!CollectionUtils.isEmpty(dto.getProperties())) {
                List<GraphRelationTemplateProperty> properties = buildRelationProperties(dto.getProperties(), template.getRelationTemplateId());
                relationPropertyMapper.batchInsert(properties);
            }
        });
    }

    // 4. 本体设计-节点模版删除接口（逻辑删除）
    @Transactional(rollbackFor = Exception.class)
    public void deleteNodeTemplate(Long nodeTemplateId) {
        // 更新节点模版删除标识
        nodeTemplateMapper.updateDeleteFlag(nodeTemplateId, "1");
        // 更新属性删除标识
        nodePropertyMapper.updateDeleteFlagByNodeTemplateId(nodeTemplateId, "1");
    }

    // 5. 本体设计-关系模版删除接口（逻辑删除）
    @Transactional(rollbackFor = Exception.class)
    public void deleteRelationTemplate(Long relationTemplateId) {
        relationTemplateMapper.updateDeleteFlag(relationTemplateId, "1");
        relationPropertyMapper.updateDeleteFlagByRelationTemplateId(relationTemplateId, "1");
    }

    // 6. 本体设计-组件库查询接口
    public Map<String, Object> queryLibraryTemplate(String templateName) {
        Map<String, Object> result = new HashMap<>();
        // 查询组件库节点
        List<GraphNodeTemplate> nodeTemplates = nodeTemplateMapper.selectLibraryByLikeName(templateName);
        if (!CollectionUtils.isEmpty(nodeTemplates)) {
            nodeTemplates.forEach(node -> {
                List<GraphNodeTemplateProperty> properties = nodePropertyMapper.selectByNodeTemplateId(node.getNodeTemplateId());
                node.setProperties(properties);
            });
        }
        // 查询组件库关系
        List<GraphRelationTemplate> relationTemplates = relationTemplateMapper.selectLibraryByLikeName(templateName);
        if (!CollectionUtils.isEmpty(relationTemplates)) {
            relationTemplates.forEach(relation -> {
                List<GraphRelationTemplateProperty> properties = relationPropertyMapper.selectByRelationTemplateId(relation.getRelationTemplateId());
                relation.setProperties(properties);
            });
        }
        result.put("nodeTemplates", nodeTemplates);
        result.put("relationTemplates", relationTemplates);
        return result;
    }

    // 7. 本体设计-添加到模型接口
    @Transactional(rollbackFor = Exception.class)
    public void addToModel(AddToModelDTO dto) {
        if ("node".equals(dto.getTemplateType())) {
            // 节点类型：查询组件库节点，插入新数据到指定专题
            GraphNodeTemplate libraryNode = nodeTemplateMapper.selectById(dto.getTemplateId());
            if (libraryNode == null) {
                throw new RuntimeException("节点模版不存在");
            }
            // 校验名称是否重复
            int count = nodeTemplateMapper.countByNameAndTopicId(libraryNode.getNodeTemplateName(), dto.getTopicId());
            if (count > 0) {
                throw new RuntimeException("节点模版名称已存在");
            }
            // 构建新节点（专题ID=目标专题，组件库标识=0）
            GraphNodeTemplate newNode = new GraphNodeTemplate();
            BeanUtils.copyProperties(libraryNode, newNode);
            newNode.setNodeTemplateId(null);
            newNode.setTopicId(dto.getTopicId());
            newNode.setIsLibraryFlag("0");
            newNode.setCreateTime(LocalDateTime.now());
            newNode.setUpdateTime(LocalDateTime.now());
            newNode.setIsDeleteFlag("0");
            nodeTemplateMapper.insert(newNode);

            // 复制属性
            List<GraphNodeTemplateProperty> libraryProperties = nodePropertyMapper.selectByNodeTemplateId(libraryNode.getNodeTemplateId());
            if (!CollectionUtils.isEmpty(libraryProperties)) {
                List<GraphNodeTemplateProperty> newProperties = new ArrayList<>();
                libraryProperties.forEach(prop -> {
                    GraphNodeTemplateProperty newProp = new GraphNodeTemplateProperty();
                    BeanUtils.copyProperties(prop, newProp);
                    newProp.setNodeTemplatePropertyId(null);
                    newProp.setNodeTemplateId(newNode.getNodeTemplateId());
                    newProp.setCreateTime(LocalDateTime.now());
                    newProp.setUpdateTime(LocalDateTime.now());
                    newProp.setIsDeleteFlag("0");
                    newProperties.add(newProp);
                });
                nodePropertyMapper.batchInsert(newProperties);
            }
        } else if ("relation".equals(dto.getTemplateType())) {
            // 关系类型：逻辑同节点
            GraphRelationTemplate libraryRelation = relationTemplateMapper.selectById(dto.getTemplateId());
            if (libraryRelation == null) {
                throw new RuntimeException("关系模版不存在");
            }
            // 校验名称重复
            int count = relationTemplateMapper.countByNameAndTopicId(libraryRelation.getRelationTemplateName(), dto.getTopicId());
            if (count > 0) {
                throw new RuntimeException("关系模版名称已存在");
            }
            // 构建新关系
            GraphRelationTemplate newRelation = new GraphRelationTemplate();
            BeanUtils.copyProperties(libraryRelation, newRelation);
            newRelation.setRelationTemplateId(null);
            newRelation.setTopicId(dto.getTopicId());
            newRelation.setIsLibraryFlag("0");
            newRelation.setCreateTime(LocalDateTime.now());
            newRelation.setUpdateTime(LocalDateTime.now());
            newRelation.setIsDeleteFlag("0");
            relationTemplateMapper.insert(newRelation);

            // 复制属性
            List<GraphRelationTemplateProperty> libraryProperties = relationPropertyMapper.selectByRelationTemplateId(libraryRelation.getRelationTemplateId());
            if (!CollectionUtils.isEmpty(libraryProperties)) {
                List<GraphRelationTemplateProperty> newProperties = new ArrayList<>();
                libraryProperties.forEach(prop -> {
                    GraphRelationTemplateProperty newProp = new GraphRelationTemplateProperty();
                    BeanUtils.copyProperties(prop, newProp);
                    newProp.setRelationTemplatePropertyId(null);
                    newProp.setRelationTemplateId(newRelation.getRelationTemplateId());
                    newProp.setCreateTime(LocalDateTime.now());
                    newProp.setUpdateTime(LocalDateTime.now());
                    newProp.setIsDeleteFlag("0");
                    newProperties.add(newProp);
                });
                relationPropertyMapper.batchInsert(newProperties);
            }
        } else {
            throw new RuntimeException("模版类型错误，仅支持node/relation");
        }
    }

    // ========== 私有构建方法 ==========
    private GraphNodeTemplate buildNodeTemplate(NodeTemplateSaveDTO dto, String topicId, String isLibraryFlag) {
        GraphNodeTemplate template = new GraphNodeTemplate();
        template.setTopicId(topicId);
        template.setNodeTemplateName(dto.getNodeTemplateName());
        template.setNodeTemplateDescription(dto.getNodeTemplateDescription());
        template.setIsLibraryFlag(isLibraryFlag);
        template.setIsDeleteFlag("0");
        template.setCreateTime(LocalDateTime.now());
        template.setUpdateTime(LocalDateTime.now());
        template.setNodeTemplateColor(dto.getNodeTemplateColor());
        return template;
    }

    private List<GraphNodeTemplateProperty> buildNodeProperties(List<NodeTemplatePropertySaveDTO> dtoList, Long nodeTemplateId) {
        List<GraphNodeTemplateProperty> list = new ArrayList<>();
        dtoList.forEach(dto -> {
            GraphNodeTemplateProperty prop = new GraphNodeTemplateProperty();
            prop.setNodeTemplateId(nodeTemplateId);
            prop.setPropertyKey(dto.getPropertyKey());
            prop.setPropertyType(dto.getPropertyType());
            prop.setIsDeleteFlag("0");
            prop.setCreateTime(LocalDateTime.now());
            prop.setUpdateTime(LocalDateTime.now());
            list.add(prop);
        });
        return list;
    }

    private GraphRelationTemplate buildRelationTemplate(RelationTemplateSaveDTO dto, String topicId, String isLibraryFlag) {
        GraphRelationTemplate template = new GraphRelationTemplate();
        template.setTopicId(topicId);
        template.setRelationTemplateName(dto.getRelationTemplateName());
        template.setRelationTemplateType(dto.getRelationTemplateType());
        template.setStartNodeTemplateId(dto.getStartNodeTemplateId());
        template.setEndNodeTemplateId(dto.getEndNodeTemplateId());
        template.setIsLibraryFlag(isLibraryFlag);
        template.setIsDeleteFlag("0");
        template.setCreateTime(LocalDateTime.now());
        template.setUpdateTime(LocalDateTime.now());
        return template;
    }

    private List<GraphRelationTemplateProperty> buildRelationProperties(List<RelationTemplatePropertySaveDTO> dtoList, Long relationTemplateId) {
        List<GraphRelationTemplateProperty> list = new ArrayList<>();
        dtoList.forEach(dto -> {
            GraphRelationTemplateProperty prop = new GraphRelationTemplateProperty();
            prop.setRelationTemplateId(relationTemplateId);
            prop.setPropertyKey(dto.getPropertyKey());
            prop.setPropertyType(dto.getPropertyType());
            prop.setIsDeleteFlag("0");
            prop.setCreateTime(LocalDateTime.now());
            prop.setUpdateTime(LocalDateTime.now());
            list.add(prop);
        });
        return list;
    }
}
