package com.bonc.graph.sequence.dto;

import lombok.Data;

@Data
public class NodeDeleteDTO {
    private Integer level;
    private String levelId;
    private String nodeHash;
}
