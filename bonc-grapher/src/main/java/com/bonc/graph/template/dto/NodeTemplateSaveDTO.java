package com.bonc.graph.template.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 节点模版保存DTO
 */
@Data
public class NodeTemplateSaveDTO {
    private Long nodeTemplateId;
    @NotBlank(message = "专题ID不能为空")
    private String topicId;
    @NotBlank(message = "节点模版名称不能为空")
    private String nodeTemplateName;
    private String nodeTemplateDescription;
    @NotNull(message = "是否加入组件库标识不能为空")
    private String isLibraryFlag;
    @NotBlank(message = "节点模版颜色不能为空")
    private String nodeTemplateColor;
    // 属性列表
    private List<NodeTemplatePropertySaveDTO> properties;
}
