package com.bonc.graph.template.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 关系模版保存 DTO
 */
@Data
public class RelationTemplateSaveDTO {
    private Long relationTemplateId;
    @NotBlank(message = "专题ID不能为空")
    private String topicId;
    @NotBlank(message = "关系模版名称不能为空")
    private String relationTemplateName;
    private String relationTemplateType;
    private Long startNodeTemplateId;
    private Long endNodeTemplateId;
    @NotNull(message = "是否加入组件库标识不能为空")
    private String isLibraryFlag;
    // 属性列表
    private List<RelationTemplatePropertySaveDTO> properties;
}
