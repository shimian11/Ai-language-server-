package com.ailang.service.entry.pojo.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 案例。view_count/copy_count 由 SQL 自增维护（不经实体更新），
 * created_at/updated_at 由数据库默认值与触发器维护。
 */
@Data
@TableName("entry")
public class Entry {

    /** 主键 ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 案例标题 */
    private String title;

    /** 案例摘要 */
    private String summary;

    /** 所属大类 ID */
    private Long categoryId;

    /** 所属平台：web（网页端）/ app（应用端） */
    private String platform;

    /** 具体样式（来自 style 预置名单，存名称） */
    private String style;

    /** AI 生成提示词 */
    private String prompt;

    /** 覆盖式更新：允许显式置空（ALWAYS 策略使 null 也参与 UPDATE） */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String htmlSource;

    /** 逗号分隔标签串（对外 VO 转数组） */
    private String tags;

    /** DRAFT / PUBLISHED */
    private String status;

    /** 首次发布时间；未发布为 null */
    private LocalDateTime publishedAt;

    /** 浏览量（SQL 自增维护） */
    private Long viewCount;

    /** 复制数（SQL 自增维护） */
    private Long copyCount;

    /** 创建时间（数据库默认值维护） */
    private LocalDateTime createdAt;

    /** 更新时间（数据库 ON UPDATE 维护） */
    private LocalDateTime updatedAt;
}