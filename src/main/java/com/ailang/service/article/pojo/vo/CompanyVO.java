package com.ailang.service.article.pojo.vo;

import lombok.Data;

/** 公司 VO（前台时间线 / 后台公司分组通用） */
@Data
public class CompanyVO {

    /** 稳定标识 */
    private String key;

    /** 脱敏别名显示名 */
    private String name;

    /** 角色标注 */
    private String meta;

    /** 是否里程碑节点 */
    private Boolean milestone;

    /** 已发布记录数（前台时间线计数） */
    private Long countPublished;

    /** 全部记录数（后台管理计数） */
    private Long countAll;
}
