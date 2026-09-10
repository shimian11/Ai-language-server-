package com.ailang.service.entry.pojo.vo;

import lombok.Data;

/**
 * 图片展示项（与前端 EntryImage 对齐）。
 */
@Data
public class EntryImageVO {

    /** 主键 ID */
    private Long id;

    /** 图片对象 URL */
    private String url;

    /** 是否主图 */
    private Boolean isMain;
}