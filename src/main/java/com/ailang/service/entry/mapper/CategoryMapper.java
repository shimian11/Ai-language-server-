package com.ailang.service.entry.mapper;

import com.ailang.service.entry.pojo.vo.CategoryVO;
import com.ailang.service.entry.pojo.entity.Category;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 分类 Mapper。entry 表的关联统计走原生 SQL（M3 前不依赖 Entry 实体）。
 */
@Mapper
public interface CategoryMapper extends BaseMapper<Category> {

    /**
     * 全部分类 + 已发布案例数，按 sort 升序。
     */
    @Select("""
            SELECT c.id, c.name, c.sort, COUNT(e.id) AS entry_count
            FROM category c
            LEFT JOIN entry e ON e.category_id = c.id AND e.status = 'PUBLISHED'
            GROUP BY c.id, c.name, c.sort
            ORDER BY c.sort ASC, c.id ASC
            """)
    List<CategoryVO> selectAllWithEntryCount();

    /**
     * 统计分类下的案例数（任意状态，删除保护用）。
     */
    @Select("SELECT COUNT(*) FROM entry WHERE category_id = #{categoryId}")
    long countEntriesOfCategory(Long categoryId);
}
