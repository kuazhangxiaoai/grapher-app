package com.bonc.graph.project.domain;

import lombok.Data;

import java.util.Date;

@Data
public class Topic {
    public String topicId; //专题id
    public String topicName; // 专题名字
    public String fieldId; // 领域ID
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
