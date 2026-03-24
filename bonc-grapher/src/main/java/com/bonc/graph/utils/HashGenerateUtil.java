package com.bonc.graph.utils;

import cn.hutool.core.lang.hash.MurmurHash;
import com.bonc.common.utils.StringUtils;
import com.bonc.graph.sequence.dto.NodeSaveDTO;
import com.bonc.graph.sequence.dto.RelationSaveDTO;
import org.apache.commons.collections4.CollectionUtils;


import java.util.List;
import java.util.stream.Collectors;

public class HashGenerateUtil {

    /**
     * 生成节点Hash：nodeTemplateId + nodeName + 排序后的属性list
     */
    public static String generateNodeHash(Long nodeTemplateId, String nodeName,
                                          List<NodeSaveDTO.GraphNodePropertyDTO> properties) {
        StringBuilder sb = new StringBuilder();
        sb.append(nodeTemplateId == null ? "" : nodeTemplateId);
        sb.append(StringUtils.defaultString(nodeName));

        if (CollectionUtils.isNotEmpty(properties)) {
            List<NodeSaveDTO.GraphNodePropertyDTO> sortedProps = properties.stream()
                    .sorted((p1, p2) -> p1.getPropertyKey().compareTo(p2.getPropertyKey()))
                    .collect(Collectors.toList());
            for (NodeSaveDTO.GraphNodePropertyDTO prop : sortedProps) {
                sb.append(StringUtils.defaultString(prop.getPropertyKey()));
                sb.append(StringUtils.defaultString(prop.getPropertyValue()));
            }
        }

        long hash64 = MurmurHash.hash64(sb.toString());
        return String.valueOf(hash64);
    }

    /**
     * 生成关系Hash：relationTemplateId + relationName + startNodeHash + endNodeHash + 排序后的属性list
     */
    public static String generateRelationHash(Long relationTemplateId, String relationName,
                                              String startNodeHash, String endNodeHash,
                                              List<RelationSaveDTO.GraphRelationPropertyDTO> properties) {
        StringBuilder sb = new StringBuilder();
        sb.append(relationTemplateId == null ? "" : relationTemplateId);
        sb.append(StringUtils.defaultString(relationName));
        sb.append(StringUtils.defaultString(startNodeHash));
        sb.append(StringUtils.defaultString(endNodeHash));

        if (CollectionUtils.isNotEmpty(properties)) {
            List<RelationSaveDTO.GraphRelationPropertyDTO> sortedProps = properties.stream()
                    .sorted((p1, p2) -> p1.getPropertyKey().compareTo(p2.getPropertyKey()))
                    .collect(Collectors.toList());
            for (RelationSaveDTO.GraphRelationPropertyDTO prop : sortedProps) {
                sb.append(StringUtils.defaultString(prop.getPropertyKey()));
                sb.append(StringUtils.defaultString(prop.getPropertyValue()));
            }
        }

        long hash64 = MurmurHash.hash64(sb.toString());
        return String.valueOf(hash64);
    }
}
