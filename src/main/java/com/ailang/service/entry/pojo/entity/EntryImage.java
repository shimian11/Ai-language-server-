package com.ailang.service.entry.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 案例效果图（一案例多图，其一为主图）。
 */
@Data
@TableName("entry_image")
public class EntryImage {

    /** 主键 ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属案例 ID */
    private Long entryId;

    /** 图片对象 URL */
    private String url;

    /** 是否主图 */
    private Boolean isMain;

    /** 排序权重（小者靠前） */
    private Integer sort;
}