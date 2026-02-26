package com.bonc.graph.template.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 节点属性模版保存DTO
 */
@Data
public class NodeTemplatePropertySaveDTO {
    private Long nodeTemplatePropertyId;
    @NotBlank(message = "属性key不能为空")
    private String propertyKey;
    @NotBlank(message = "属性类型不能为空")
    private String propertyType;
}
