package com.ailang.service.entry.service.impl;

import com.ailang.common.exception.BizException;
import com.ailang.service.entry.pojo.vo.CategoryVO;
import com.ailang.service.entry.pojo.entity.Category;
import com.ailang.service.entry.mapper.CategoryMapper;
import com.ailang.service.entry.service.CategoryService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryMapper categoryMapper;

    /** 查询全部分类（含已发布案例数） */
    @Override
    public List<CategoryVO> listAll() {
        return categoryMapper.selectAllWithEntryCount();
    }

    /** 新增分类：校验名称可用后创建，并排到末尾 */
    @Override
    public CategoryVO create(String name) {
        assertNameAvailable(name, null);

        Category category = new Category();
        category.setName(name);
        // 新分类排到末尾
        Integer maxSort = categoryMapper.selectList(new LambdaQueryWrapper<Category>()
                        .select(Category::getSort)
                        .orderByDesc(Category::getSort)
                        .last("LIMIT 1"))
                .stream().findFirst().map(Category::getSort).orElse(0);
        category.setSort(maxSort + 1);
        categoryMapper.insert(category);

        return toVO(category, 0L);
    }

    /** 重命名分类：先确认存在且新名称不与其他分类重命 */
    @Override
    public void rename(Long id, String name) {
        Category category = requireExisting(id);
        assertNameAvailable(name, id);
        category.setName(name);
        categoryMapper.updateById(category);
    }

    /** 批量排序：校验 id 全部存在且无重复后，按数组顺序写 sort=1..n */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reorder(List<Long> ids) {
        // 校验：id 不重复且全部存在
        Set<Long> existingIds = categoryMapper.selectList(new LambdaQueryWrapper<Category>()
                        .select(Category::getId)
                        .in(Category::getId, ids))
                .stream().map(Category::getId).collect(Collectors.toSet());
        if (existingIds.size() != ids.size() || ids.stream().distinct().count() != ids.size()) {
            throw new BizException("排序列表包含不存在或重复的分类");
        }
        for (int index = 0; index < ids.size(); index++) {
            Category category = new Category();
            category.setId(ids.get(index));
            category.setSort(index + 1);
            categoryMapper.updateById(category);
        }
    }

    /** 删除分类：存在关联案例时拒绝删除 */
    @Override
    public void remove(Long id) {
        requireExisting(id);
        long count = categoryMapper.countEntriesOfCategory(id);
        if (count > 0) {
            throw new BizException("该分类下还有 " + count + " 个案例，无法删除");
        }
        categoryMapper.deleteById(id);
    }

    private Category requireExisting(Long id) {
        Category category = categoryMapper.selectById(id);
        if (category == null) {
            throw new BizException("分类不存在");
        }
        return category;
    }

    private void assertNameAvailable(String name, Long excludeId) {
        Long duplicateId = categoryMapper.selectList(new LambdaQueryWrapper<Category>()
                        .select(Category::getId)
                        .eq(Category::getName, name))
                .stream().findFirst().map(Category::getId).orElse(null);
        if (duplicateId != null && !duplicateId.equals(excludeId)) {
            throw new BizException("分类名称「" + name + "」已存在");
        }
    }

    private CategoryVO toVO(Category category, Long entryCount) {
        CategoryVO vo = new CategoryVO();
        vo.setId(category.getId());
        vo.setName(category.getName());
        vo.setSort(category.getSort());
        vo.setEntryCount(entryCount);
        return vo;
    }
}
