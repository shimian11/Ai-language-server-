package com.ailang.service.entry.pojo.vo;

import lombok.Data;

/**
 * 后台案例管理「分组视图」用：按 (设计大类, 案例标题) 聚合一组，供首屏统计与进入二级列表。
 */
@Data
public class EntryGroupVO {

    /** 所属设计大类 ID */
    private Long categoryId;

    /** 所属设计大类名称 */
    private String categoryName;

    /** 案例标题（该组共享） */
    private String title;

    /** 该组下案例数（按样式条数统计） */
    private long count;

    /** 封面：该组代表案例的主图，可空 */
    private String cover;
}