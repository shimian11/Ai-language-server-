package com.ailang.service.article.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 公司别名（实习两级时间线第一级）。
 * key 为稳定标识（前台分组用），name 为脱敏显示名；
 * created_at/updated_at 由数据库默认值与触发器维护。
 */
@Data
@TableName("company")
public class Company {

    /** 主键 ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 稳定标识（唯一；前台时间线分组用） */
    private String key;

    /** 脱敏别名显示名（如「某大厂」） */
    private String name;

    /** 角色标注（如「全栈 · 2026.03 — 05」） */
    private String meta;

    /** 是否里程碑节点（如「阶段总结」，前台金色） */
    private Boolean milestone;

    /** 排序（小者靠前） */
    private Integer sort;

    /** 创建时间（数据库默认值维护） */
    private LocalDateTime createdAt;

    /** 更新时间（数据库触发器维护） */
    private LocalDateTime updatedAt;
}
