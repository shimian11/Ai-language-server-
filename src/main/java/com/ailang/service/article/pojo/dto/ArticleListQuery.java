package com.ailang.service.article.pojo.dto;

import lombok.Data;

/**
 * 文章列表查询入参。
 * 隐私规则与案例一致：未登录强制 status=PUBLISHED。
 */
@Data
public class ArticleListQuery {

    /** 页码（默认 1） */
    private long page = 1;

    /** 每页条数（默认 20） */
    private long size = 20;

    /** 内容类型：note / internship / interview */
    private String type;

    /** 栏目 key（proj/ai/learn/exp/bigu/algo） */
    private String topic;

    /** 状态：DRAFT / PUBLISHED（登录后 status 空 = 全部） */
    private String status;

    /** 关键词（匹配 标题/摘要/标签） */
    private String keyword;

    /** 公司 key（实习记录按公司过滤） */
    private String companyKey;

    /** 排序方向：asc / desc（按展示日期；默认 desc） */
    private String sort;
}
