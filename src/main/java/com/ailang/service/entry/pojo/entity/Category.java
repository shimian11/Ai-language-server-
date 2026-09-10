package com.ailang.service.entry.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 设计大类（受管分类，仅一级）。
 * created_at/updated_at 由数据库默认值与 ON UPDATE 维护，不参与插入更新。
 */
@Data
@TableName("category")
public class Category {

    /** 主键 ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 大类名称（唯一） */
    private String name;

    /** 排序权重（小者靠前，首页胶囊墙前 7） */
    private Integer sort;

    /** 创建时间（数据库默认值维护） */
    private LocalDateTime createdAt;

    /** 更新时间（数据库 ON UPDATE 维护） */
    private LocalDateTime updatedAt;
}