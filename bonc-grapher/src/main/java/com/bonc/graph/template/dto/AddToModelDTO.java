package com.bonc.graph.template.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 添加到模型 DTO
 */
@Data
public class AddToModelDTO {
    @NotBlank(message = "专题ID不能为空")
    private String topicId;
    @NotNull(message = "模版ID不能为空")
    private Long templateId;
    @NotBlank(message = "模版类型不能为空（node/relation）")
    private String templateType; // node/relation
}
