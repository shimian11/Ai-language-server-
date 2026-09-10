package com.ailang.service.entry.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ailang.service.entry.pojo.entity.Entry;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * 案例 Mapper。
 */
@Mapper
public interface EntryMapper extends BaseMapper<Entry> {

    /** 累计浏览数（管理端总览用） */
    @Select("SELECT COALESCE(SUM(view_count), 0) FROM entry")
    long sumViewCount();
}
