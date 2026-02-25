package com.bonc.graph.project.domain;

import lombok.Data;

import java.util.Date;

@Data
public class Article {
    public String articleId; //图谱ID
    public String articleName; // 图谱名字
    public String createMethod;// 创建方式
    public String fileName;// 文件名字
    public String fileType; // 文件类型
    public String fileSize;// 文件大小
    public String fileUrl;  // 文件地址
    public String topicId; // 专题ID
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
