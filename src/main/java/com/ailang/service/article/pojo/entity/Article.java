package com.ailang.service.article.pojo.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 博客文章（2026-11 改版）。
 * type 区分三类（note/internship/interview）；company_id 仅实习记录使用；
 * body 存 Markdown（可含内联 HTML）；
 * created_at 由数据库默认值维护、updated_at 由触发器维护（不经实体更新）。
 * 可空文本字段统一 ALWAYS 策略：覆盖式更新时 null 也参与 UPDATE（支持清空）。
 */
@Data
@TableName("article")
public class Article {

    /** 主键 ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 内容类型：note（心得）/ internship（实习记录）/ interview（面试内容） */
    private String type;

    /** 栏目 key：proj/ai/learn/exp/bigu/algo；实习记录为空串 */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String topic;

    /** 栏目显示名（实习记录 = 公司别名） */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String sub;

    /** 标题 */
    private String title;

    /** 摘要（列表展示） */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String summary;

    /** 正文 Markdown（可含内联 HTML） */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String body;

    /** 封面图 URL（MinIO objectUrl，可空串）；列表/收藏卡展示用 */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String cover;

    /** 逗号分隔标签串（对外 VO 转数组） */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String tags;

    /** 所属公司 ID（实习记录使用；其余为 null） */
    private Long companyId;

    /** 展示日期：实习 = 时间点（如 2026-08），其余 = 发布日期（如 2026-10-18） */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String displayDate;

    /** 结果标注（面经：通过/未通过/待定；其余复用为「高频/手写」等） */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String result;

    /** 结果徽标样式：ok / warn / fail */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String resultClass;

    /** 阅读时长标注（如「6 分钟」「12 问」） */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String readLabel;

    /** DRAFT / PUBLISHED */
    private String status;

    /** 首次发布时间；未发布为 null */
    private LocalDateTime publishedAt;

    /** 创建时间（数据库默认值维护） */
    private LocalDateTime createdAt;

    /** 更新时间（数据库触发器维护） */
    private LocalDateTime updatedAt;
}
