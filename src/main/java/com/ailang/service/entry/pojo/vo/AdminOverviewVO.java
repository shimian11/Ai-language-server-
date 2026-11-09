package com.ailang.service.entry.pojo.vo;

import lombok.Data;

import java.util.List;

/**
 * 管理端总览（GET /api/admin/overview，Dashboard 用）。
 */
@Data
public class AdminOverviewVO {

    /** 已发布案例数（提示词） */
    private long published;

    /** 草稿案例数（提示词） */
    private long drafts;

    /** 文章总数（心得 + 实习 + 面试） */
    private long articleTotal;

    /** 文章草稿数 */
    private long articleDrafts;

    /** 总浏览量（提示词） */
    private long totalViews;

    /** 最近发布的案例 */
    private List<EntryVO> recent;

    /** 最近发布的文章（心得/实习/面试混排，最多 5 条） */
    private List<ArticleBrief> recentArticles;

    /**
     * 文章简要信息（仪表盘最近文章列表用，避免整篇 body 传输）。
     */
    @Data
    public static class ArticleBrief {

        /** 文章 ID */
        private Long id;

        /** 内容类型：note / internship / interview */
        private String type;

        /** 栏目/公司显示名 */
        private String sub;

        /** 标题 */
        private String title;

        /** 状态：DRAFT / PUBLISHED */
        private String status;

        /** 展示日期 */
        private String date;
    }
}