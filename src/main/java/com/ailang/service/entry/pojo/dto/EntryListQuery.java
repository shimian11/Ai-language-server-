package com.ailang.service.entry.pojo.dto;

import lombok.Data;

/**
 * 列表查询参数（page 从 1 起）。
 */
@Data
public class EntryListQuery {

    /** 页码（从 1 起） */
    private Integer page = 1;

    /** 每页条数 */
    private Integer size = 10;

    /** 分类筛选 ID（可空） */
    private Long categoryId;

    /** 关键字搜索（标题/摘要，可空） */
    private String keyword;

    /** DRAFT / PUBLISHED；空 = 全部（仅鉴权后生效，未登录强制 PUBLISHED） */
    private String status;

    /** 平台：web / app；可空 */
    private String platform;
}