package com.ailang.service.entry.mapper;

import com.ailang.service.entry.pojo.entity.Category;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * 分类 Mapper（保留仅用于回溯历史 category_id → 名称；分类管理接口已下线）。
 */
@Mapper
public interface CategoryMapper extends BaseMapper<Category> {
}
