package com.ailang.service.entry.pojo.vo;

import lombok.Data;

/**
 * 分类列表项（含已发布案例数）。
 */
@Data
public class CategoryVO {

    /** 主键 ID */
    private Long id;

    /** 大类名称 */
    private String name;

    /** 排序权重（小者靠前） */
    private Integer sort;

    /** 已发布案例数 */
    private Long entryCount;
}