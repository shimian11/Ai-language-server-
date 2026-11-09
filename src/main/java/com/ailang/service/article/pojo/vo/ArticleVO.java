package com.ailang.service.article.pojo.vo;

import lombok.Data;

import java.util.List;

/**
 * 文章 VO（对齐前端 types.Article）。
 * 日期输出为字符串（yyyy-MM 或 yyyy-MM-dd），保证前端按字符串排序稳定。
 */
@Data
public class ArticleVO {

    /** 主键 ID */
    private Long id;

    /** 内容类型：note / internship / interview */
    private String type;

    /** 栏目 key */
    private String topic;

    /** 栏目显示名（实习 = 公司别名） */
    private String sub;

    /** 标题 */
    private String title;

    /** 摘要 */
    private String summary;

    /** 正文（Markdown/内联 HTML） */
    private String body;

    /** 封面图 URL（可空串） */
    private String cover;

    /** 标签数组 */
    private List<String> tags;

    /** 所属公司 key（实习记录使用，其余 null） */
    private String company;

    /** 所属公司显示名 */
    private String companyName;

    /** 展示日期 */
    private String date;

    /** 结果标注 */
    private String result;

    /** 结果徽标样式：ok / warn / fail */
    private String resultClass;

    /** 阅读时长标注 */
    private String read;

    /** 状态：DRAFT / PUBLISHED */
    private String status;

    /** 首次发布日期（yyyy-MM-dd；未发布为 null） */
    private String publishedAt;

    /** 创建日期（yyyy-MM-dd） */
    private String createdAt;
}
