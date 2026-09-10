package com.ailang.service.entry.pojo.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 案例 VO（列表与详情共用；列表场景 htmlSource 由 Service 截断 2KB）。
 */
@Data
public class EntryVO {

    /** 案例 ID */
    private Long id;

    /** 标题 */
    private String title;

    /** 简介 */
    private String summary;

    /** 所属分类 ID */
    private Long categoryId;

    /** 所属分类名称 */
    private String categoryName;

    /** 所属平台：web / app */
    private String platform;

    /** 具体样式（自由文本） */
    private String style;

    /** 提示词（Markdown） */
    private String prompt;

    /** HTML 源码（列表场景由 Service 截断 2KB） */
    private String htmlSource;

    /** 效果图列表 */
    private List<EntryImageVO> images;

    /** 标签数组 */
    private List<String> tags;

    /** DRAFT / PUBLISHED */
    private String status;

    /** 首次发布时间；未发布为 null */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime publishedAt;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime createdAt;

    /** 浏览量 */
    private Long viewCount;

    /** 复制数 */
    private Long copyCount;
}