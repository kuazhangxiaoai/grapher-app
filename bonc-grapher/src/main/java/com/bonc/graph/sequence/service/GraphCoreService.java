package com.bonc.graph.sequence.service;

import com.bonc.graph.project.domain.Article;
import com.bonc.graph.project.domain.Topic;
import com.bonc.graph.project.mapper.GraphArticleMapper;
import com.bonc.graph.project.mapper.GraphTopicMapper;
import com.bonc.graph.sequence.domain.GraphNode;
import com.bonc.graph.sequence.domain.GraphRelation;
import com.bonc.graph.sequence.dto.GraphResponseDTO;
import com.bonc.graph.sequence.dto.GraphSaveDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GraphCoreService {

    @Resource
    private GraphSequenceService graphSequenceService;
    @Resource
    private GraphNodeService graphNodeService;
    @Resource
    private GraphRelationService graphRelationService;
    @Resource
    private GraphArticleMapper graphArticleMapper;
    @Resource
    private GraphTopicMapper graphTopicMapper;
    /**
     * 图谱保存提交
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveGraph(GraphSaveDTO saveDTO) {
        String articleId = saveDTO.getArticleId();
        String sequenceId = saveDTO.getSequenceId();

        // 1. 查询文章获取topicId
        Article article = graphArticleMapper.selectByArticleId(articleId);
        if (article == null) {
            throw new RuntimeException("文章不存在，articleId：" + articleId);
        }
        String topicId = article.getTopicId();

        // 2. 查询专题获取fieldId
        Topic topic = graphTopicMapper.selectByTopicId(topicId);
        if (topic == null) {
            throw new RuntimeException("专题不存在，topicId：" + topicId);
        }
        String fieldId = topic.getFieldId();


        // 1. 新增：生成sequenceId并插入段落表
        if (sequenceId == null || sequenceId.isEmpty()) {
            sequenceId = graphSequenceService.createSequence(
                    articleId,
                    saveDTO.getSequenceContent(),
                    saveDTO.getSequenceX0(),
                    saveDTO.getSequenceY0(),
                    saveDTO.getSequenceX1(),
                    saveDTO.getSequenceY1(),
                    saveDTO.getSequencePage()
            );
        } else {
            // 2. 修改：删除原有节点、关系及属性
            graphNodeService.deleteNodesBySequenceId(sequenceId);
            graphRelationService.deleteRelationsBySequenceId(sequenceId);
            // 更新段落更新时间
            graphSequenceService.updateSequenceUpdateTime(sequenceId);
        }

        // 3. 保存节点及属性
        graphNodeService.saveNodes(saveDTO.getGraphNode(), sequenceId, articleId, topicId, fieldId);

        // 4. 保存关系及属性
        graphRelationService.saveRelations(saveDTO.getGraphRelation(), sequenceId, articleId, topicId, fieldId);
    }

    /**
     * 根据sequenceId查询图谱
     */
    public GraphResponseDTO getGraphBySequenceId(String sequenceId) {
        // 1. 查询节点
        List<GraphNode> nodes = graphNodeService.getNodesBySequenceId(sequenceId);
        // 2. 查询关系
        List<GraphRelation> relations = graphRelationService.getRelationsBySequenceId(sequenceId);
        // 3. 转换为返回DTO
        return convertToGraphResponseDTO(nodes, relations);
    }

    /**
     * 根据articleId查询图谱（去重）
     */
    public GraphResponseDTO getGraphByArticleId(String articleId) {
        // 1. 查询节点（去重）
        List<GraphNode> nodes = graphNodeService.getDistinctNodesByArticleId(articleId);
        // 2. 查询关系（去重）
        List<GraphRelation> relations = graphRelationService.getDistinctRelationsByArticleId(articleId);
        // 3. 转换为返回DTO
        return convertToGraphResponseDTO(nodes, relations);
    }

    /**
     * 转换为返回DTO
     */
    private GraphResponseDTO convertToGraphResponseDTO(List<GraphNode> nodes, List<GraphRelation> relations) {
        GraphResponseDTO responseDTO = new GraphResponseDTO();

        // 转换节点
        List<GraphResponseDTO.NodeResponseDTO> nodeDTOList = nodes.stream().map(node -> {
            GraphResponseDTO.NodeResponseDTO nodeDTO = new GraphResponseDTO.NodeResponseDTO();
            nodeDTO.setNodeHash(node.getNodeHash());
            nodeDTO.setNodeName(node.getNodeName());
            nodeDTO.setNodeDescription(node.getNodeDescription());
            nodeDTO.setNodeColor(node.getNodeColor());
            // 转换节点属性
            List<GraphResponseDTO.PropertyDTO> propDTOList = node.getProperties().stream().map(prop -> {
                GraphResponseDTO.PropertyDTO propDTO = new GraphResponseDTO.PropertyDTO();
                propDTO.setPropertyKey(prop.getPropertyKey());
                propDTO.setPropertyValue(prop.getPropertyValue());
                return propDTO;
            }).collect(Collectors.toList());
            nodeDTO.setProperties(propDTOList);
            return nodeDTO;
        }).collect(Collectors.toList());

        // 转换关系
        List<GraphResponseDTO.RelationResponseDTO> relationDTOList = relations.stream().map(relation -> {
            GraphResponseDTO.RelationResponseDTO relationDTO = new GraphResponseDTO.RelationResponseDTO();
            relationDTO.setRelationHash(relation.getRelationHash());
            relationDTO.setRelationName(relation.getRelationName());
            relationDTO.setRelationType(relation.getRelationType());
            relationDTO.setRelationTrigger(relation.getRelationTrigger());
            relationDTO.setStartNodeHash(relation.getStartNodeHash());
            relationDTO.setEndNodeHash(relation.getEndNodeHash());
            // 转换关系属性
            List<GraphResponseDTO.PropertyDTO> propDTOList = relation.getProperties().stream().map(prop -> {
                GraphResponseDTO.PropertyDTO propDTO = new GraphResponseDTO.PropertyDTO();
                propDTO.setPropertyKey(prop.getPropertyKey());
                propDTO.setPropertyValue(prop.getPropertyValue());
                return propDTO;
            }).collect(Collectors.toList());
            relationDTO.setProperties(propDTOList);
            return relationDTO;
        }).collect(Collectors.toList());

        responseDTO.setNodes(nodeDTOList);
        responseDTO.setRelations(relationDTOList);
        return responseDTO;
    }
}
