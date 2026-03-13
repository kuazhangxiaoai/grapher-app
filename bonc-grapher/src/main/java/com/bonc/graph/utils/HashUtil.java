package com.bonc.graph.utils;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.lang.hash.MurmurHash;
import cn.hutool.core.util.StrUtil;
import com.bonc.graph.sequence.dto.GraphSaveDTO;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Hash生成工具类
 */
public class HashUtil {

    /**
     * 生成节点Hash
     * 规则：node_name + node_color + 排序后的属性list（property_key、property_value）
     */
    public static String generateNodeHash(String nodeName, String nodeColor, List<GraphSaveDTO.PropertyDTO> properties) {
        // 1. 拼接基础信息
        StringBuilder sb = new StringBuilder();
        sb.append(StrUtil.emptyToDefault(nodeName, ""));
        sb.append(StrUtil.emptyToDefault(nodeColor, ""));

        // 2. 排序并拼接属性
        if (CollectionUtil.isNotEmpty(properties)) {
            List<GraphSaveDTO.PropertyDTO> sortedProperties = properties.stream()
                    .sorted((p1, p2) -> p1.getPropertyKey().compareTo(p2.getPropertyKey()))
                    .collect(Collectors.toList());
            for (GraphSaveDTO.PropertyDTO prop : sortedProperties) {
                sb.append(StrUtil.emptyToDefault(prop.getPropertyKey(), ""));
                sb.append(StrUtil.emptyToDefault(prop.getPropertyValue(), ""));
            }
        }

        // 3. 生成Hash（使用MurmurHash保证唯一性）
        int hash = MurmurHash.hash32(sb.toString());
        return String.valueOf(hash);
    }

    /**
     * 生成关系Hash
     * 规则：relation_name + relation_type + relation_trigger + start_node_hash + end_node_hash + 排序后的属性list
     */
    public static String generateRelationHash(String relationName, String relationType, String relationTrigger,
                                              String startNodeHash, String endNodeHash, List<GraphSaveDTO.PropertyDTO> properties) {
        // 1. 拼接基础信息
        StringBuilder sb = new StringBuilder();
        sb.append(StrUtil.emptyToDefault(relationName, ""));
        sb.append(StrUtil.emptyToDefault(relationType, ""));
        sb.append(StrUtil.emptyToDefault(relationTrigger, ""));
        sb.append(StrUtil.emptyToDefault(startNodeHash, ""));
        sb.append(StrUtil.emptyToDefault(endNodeHash, ""));

        // 2. 排序并拼接属性
        if (CollectionUtil.isNotEmpty(properties)) {
            List<GraphSaveDTO.PropertyDTO> sortedProperties = properties.stream()
                    .sorted((p1, p2) -> p1.getPropertyKey().compareTo(p2.getPropertyKey()))
                    .collect(Collectors.toList());
            for (GraphSaveDTO.PropertyDTO prop : sortedProperties) {
                sb.append(StrUtil.emptyToDefault(prop.getPropertyKey(), ""));
                sb.append(StrUtil.emptyToDefault(prop.getPropertyValue(), ""));
            }
        }

        // 3. 生成Hash
        long hash64 = MurmurHash.hash64(sb.toString());
        return String.valueOf(hash64);
    }
}
