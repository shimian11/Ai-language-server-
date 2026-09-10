package com.ailang.service.entry.service;

import com.ailang.service.entry.pojo.vo.CategoryVO;

import java.util.List;

/**
 * 分类 Service。
 */
public interface CategoryService {

    /** 全部分类（含已发布案例数，sort 升序） */
    List<CategoryVO> listAll();

    /** 新增分类（重名校验），返回新分类 */
    CategoryVO create(String name);

    /** 重命名（存在性与重名校验，排除自身） */
    void rename(Long id, String name);

    /** 批量排序：ids 顺序即 sort（1..n） */
    void reorder(List<Long> ids);

    /** 删除（有关联案例时拒绝） */
    void remove(Long id);
}
