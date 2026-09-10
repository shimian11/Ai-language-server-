package com.ailang.service.entry.pojo.dto;

import lombok.Data;

/**
 * 分页结果（与前端 PageResult 对齐）。
 */
@Data
public class PageResult<T> {

    /** 当前页数据列表 */
    private java.util.List<T> list;

    /** 总记录数 */
    private long total;

    /** 当前页 */
    private long page;

    /** 每页条数 */
    private long size;

    public static <T> PageResult<T> of(java.util.List<T> list, long total, long page, long size) {
        PageResult<T> result = new PageResult<>();
        result.setList(list);
        result.setTotal(total);
        result.setPage(page);
        result.setSize(size);
        return result;
    }
}