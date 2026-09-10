package com.ailang.service.entry.pojo.vo;

import lombok.Data;

import java.util.List;

/**
 * 管理端总览（GET /api/admin/overview，Dashboard 用）。
 */
@Data
public class AdminOverviewVO {

    /** 已发布案例数 */
    private long published;

    /** 草稿案例数 */
    private long drafts;

    /** 分类总数 */
    private long categories;

    /** 总浏览量 */
    private long totalViews;

    /** 最近发布的案例 */
    private List<EntryVO> recent;
}