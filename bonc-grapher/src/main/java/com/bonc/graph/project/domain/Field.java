package com.bonc.graph.project.domain;

import lombok.Data;

import java.util.Date;


/**
 *  领域类
 */
@Data
public class Field {
    public String fieldId; //领域id
    public String fieldName; // 领域名字
    /** 创建人 */
    private String createBy;
    /** 创建时间 */
    private Date createTime;
    /** 更新人 */
    private String updateBy;
    /** 更新时间 */
    private Date updateTime;
    /** 删除标志（0代表存在 2代表删除） */
    private String delFlag;

}
