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
import javax.validation.ValidationException;
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

        // 1. 校验节点模板名称唯一性
        checkNodeTemplateNameUnique(dto);

        List<GraphNodeTemplate> templates = new ArrayList<>();
        GraphNodeTemplate mainTemplate = buildNodeTemplate(dto, dto.getTopicId(), "0");
        templates.add(mainTemplate);

        if ("1".equals(dto.getIsLibraryFlag())) {
            // 组件库模板：topicId为空，isLibraryFlag=1
            GraphNodeTemplate libraryTemplate = buildNodeTemplate(dto, null, "1");
            templates.add(libraryTemplate);
        }

        // 2. 新增/修改逻辑处理
        for (GraphNodeTemplate template : templates) {
            if (dto.getNodeTemplateId() == null) {
                // 新增：直接插入
                nodeTemplateMapper.insert(template);
            } else {
                // 修改：先更新模板基础信息，再处理属性
                template.setNodeTemplateId(dto.getNodeTemplateId());
                nodeTemplateMapper.updateById(template);
                // 逻辑删除原有属性（保证属性全量更新）
                nodePropertyMapper.updateDeleteFlagByNodeTemplateId(dto.getNodeTemplateId(), "1");
            }

            // 3. 处理属性（新增/修改都需要重新插入属性）
            if (!CollectionUtils.isEmpty(dto.getProperties())) {
                // 确定属性关联的模板ID（新增用插入后的ID，修改用传入的ID）
                Long bindTemplateId = dto.getNodeTemplateId() == null ? template.getNodeTemplateId() : dto.getNodeTemplateId();
                List<GraphNodeTemplateProperty> properties = buildNodeProperties(dto.getProperties(), bindTemplateId);
                nodePropertyMapper.batchInsert(properties);
            }
        }
    }

    // 3. 本体设计-关系模版保存接口
    @Transactional(rollbackFor = Exception.class)
    public void saveRelationTemplate(RelationTemplateSaveDTO dto) {
        // 1. 校验关系模板名称唯一性（适配DTO字段）
        checkRelationTemplateNameUnique(dto);

        List<GraphRelationTemplate> templates = new ArrayList<>();
        GraphRelationTemplate mainTemplate = buildRelationTemplate(dto, dto.getTopicId(), "0");
        templates.add(mainTemplate);

        if ("1".equals(dto.getIsLibraryFlag())) {
            // 组件库模板：topicId为空，isLibraryFlag=1
            GraphRelationTemplate libraryTemplate = buildRelationTemplate(dto, null, "1");
            templates.add(libraryTemplate);
        }

        // 2. 新增/修改逻辑处理
        for (GraphRelationTemplate template : templates) {
            if (dto.getRelationTemplateId() == null) {
                // 新增：直接插入
                relationTemplateMapper.insert(template);
            } else {
                // 修改：先更新模板基础信息，再处理属性
                template.setRelationTemplateId(dto.getRelationTemplateId());
                relationTemplateMapper.updateById(template);
                // 逻辑删除原有属性
                relationPropertyMapper.updateDeleteFlagByRelationTemplateId(dto.getRelationTemplateId(), "1");
            }

            // 3. 处理属性（新增/修改都需要重新插入属性）
            if (!CollectionUtils.isEmpty(dto.getProperties())) {
                Long bindTemplateId = dto.getRelationTemplateId() == null ? template.getRelationTemplateId() : dto.getRelationTemplateId();
                List<GraphRelationTemplateProperty> properties = buildRelationProperties(dto.getProperties(), bindTemplateId);
                relationPropertyMapper.batchInsert(properties);
            }
        }
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

    // 2-6. 图谱构建-节点模版查询接口
    public List<GraphNodeTemplate> queryNodeTemplate(String topicId) {
        // 查询节点模版
        List<GraphNodeTemplate> nodeTemplates = nodeTemplateMapper.selectByTopicId(topicId);
        return nodeTemplates;
    }

    // 2-7. 图谱构建-节点模版属性查询接口
    public List<GraphNodeTemplateProperty> queryNodeTemplateProperties(Long nodeTemplateId) {
        // 查询节点模版属性
        List<GraphNodeTemplateProperty> properties = nodePropertyMapper.selectByNodeTemplateId(nodeTemplateId);
        return properties;
    }

    // 2-8. 图谱构建-根据topicId查询关系列表接口
    public List<GraphRelationTemplate> queryRelationTemplate(String topicId) {
        // 查询关系模版
        List<GraphRelationTemplate> relationTemplates = relationTemplateMapper.selectByTopicId(topicId);
        return relationTemplates;
    }

    // 2-9. 图谱构建-根据relationTemplateId查询关系属性列表接口
    public List<GraphRelationTemplateProperty> queryRelationTemplateProperties(Long relationTemplateId) {
        // 查询节点模版属性
        List<GraphRelationTemplateProperty> properties = relationPropertyMapper.selectByRelationTemplateId(relationTemplateId);
        return properties;
    }

    /**
     * 校验节点模板名称唯一性
     * 规则：
     * 1. 组件库模板（isLibraryFlag=1）：需同时校验「组件库内」和「同专题普通模板」名称，两者都不能重复；
     * 2. 普通模板（isLibraryFlag≠1）：仅校验「同专题普通模板」名称，不能重复；
     * 3. 修改时排除自身ID，避免误判。
     */
    private void checkNodeTemplateNameUnique(NodeTemplateSaveDTO dto) {
        String nodeTemplateName = dto.getNodeTemplateName();
        String topicId = dto.getTopicId();
        Long nodeTemplateId = dto.getNodeTemplateId();


        // 1. 校验普通模板（同专题）名称是否重复（所有场景都需要先校验普通模板）
        int topicTemplateCount = nodeTemplateMapper.countTopicTemplateByName(
                nodeTemplateName,
                topicId,
                nodeTemplateId
        );
        if (topicTemplateCount > 0) {
            throw new ValidationException("同专题下已存在名称为【" + nodeTemplateName + "】的节点模板，请勿重复创建");
        }

        // 2. 组件库模板额外校验组件库内名称是否重复
        if ("1".equals(dto.getIsLibraryFlag())) {
            int libraryTemplateCount = nodeTemplateMapper.countLibraryTemplateByName(
                    nodeTemplateName,
                    nodeTemplateId
            );
            if (libraryTemplateCount > 0) {
                throw new ValidationException("组件库中已存在名称为【" + nodeTemplateName + "】的节点模板，请勿重复创建");
            }
        }
    }

    /**
     * 校验关系模板名称唯一性
     * 规则：
     * 1. 组件库模板（isLibraryFlag=1）：需同时校验「组件库内」和「同专题普通模板」名称，两者都不能重复；
     * 2. 普通模板（isLibraryFlag≠1）：仅校验「同专题普通模板」名称，不能重复；
     * 3. 修改时排除自身ID，避免误判。
     */
    private void checkRelationTemplateNameUnique(RelationTemplateSaveDTO dto) {
        String relationTemplateName = dto.getRelationTemplateName();
        Long relationTemplateId = dto.getRelationTemplateId();
        String topicId = dto.getTopicId();

        // 1. 校验普通模板（同专题）名称是否重复
        int topicTemplateCount = relationTemplateMapper.countTopicTemplateByName(
                relationTemplateName,
                topicId,
                relationTemplateId
        );
        if (topicTemplateCount > 0) {
            throw new ValidationException("同专题下已存在名称为【" + relationTemplateName + "】的关系模板，请勿重复创建");
        }

        // 2. 组件库模板额外校验组件库内名称是否重复
        if ("1".equals(dto.getIsLibraryFlag())) {
            int libraryTemplateCount = relationTemplateMapper.countLibraryTemplateByName(
                    relationTemplateName,
                    relationTemplateId
            );
            if (libraryTemplateCount > 0) {
                throw new ValidationException("组件库中已存在名称为【" + relationTemplateName + "】的关系模板，请勿重复创建");
            }
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
