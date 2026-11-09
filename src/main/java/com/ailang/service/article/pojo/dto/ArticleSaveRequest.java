package com.ailang.service.article.pojo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.util.List;

/**
 * 文章新建/更新入参（覆盖式更新）。
 * type 决定字段语义：心得=intopic；实习=companyKey+displayDate；面试=topic+result。
 */
@Data
public class ArticleSaveRequest {

    /** 内容类型：note / internship / interview */
    @NotBlank(message = "内容类型不能为空")
    @Pattern(regexp = "note|internship|interview", message = "内容类型不合法")
    private String type;

    /** 栏目 key（心得/面试必填，实习为空） */
    private String topic;

    /** 栏目显示名（实习 = 公司别名显示名） */
    private String sub;

    /** 标题 */
    @NotBlank(message = "标题不能为空")
    private String title;

    /** 摘要 */
    private String summary;

    /** 正文 Markdown */
    private String body;

    /** 标签（数组，入库转逗号分隔） */
    private List<String> tags;

    /** 所属公司 key（实习记录使用；为空表示不挂公司） */
    private String companyKey;

    /** 展示日期（实习=时间点 2026-08；其余=2026-10-18） */
    private String displayDate;

    /** 结果标注（面经：通过/未通过/待定；其余：高频/手写等） */
    private String result;

    /** 结果徽标样式：ok / warn / fail */
    private String resultClass;

    /** 阅读时长标注 */
    private String readLabel;

    /** 状态：DRAFT / PUBLISHED（缺省 DRAFT） */
    @Pattern(regexp = "DRAFT|PUBLISHED", message = "状态不合法")
    private String status;
}
