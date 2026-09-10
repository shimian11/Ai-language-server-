package com.ailang.service.entry.controller;

import com.ailang.common.result.ApiResponse;
import com.ailang.service.entry.pojo.dto.CategorySaveRequest;
import com.ailang.service.entry.pojo.dto.CategorySortRequest;
import com.ailang.service.entry.pojo.vo.CategoryVO;
import com.ailang.service.entry.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 分类接口 #2-#6（鉴权拦截在 M2 的 SecurityConfig 统一接入）。
 */
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * #2 大类列表（含已发布案例数）。
     *
     * @return 分类列表（含各分类已发布案例数）
     */
    @GetMapping
    public ApiResponse<List<CategoryVO>> list() {
        return ApiResponse.ok(categoryService.listAll());
    }

    /**
     * #3 新增大类。
     *
     * @param request 新增请求（name 必填，不可重名）
     * @return 新建后的分类信息；重名时返回 code=400
     */
    @PostMapping
    public ApiResponse<CategoryVO> create(@Valid @RequestBody CategorySaveRequest request) {
        return ApiResponse.ok(categoryService.create(request.getName()));
    }

    /**
     * #4 重命名分类。
     *
     * @param id      分类 ID
     * @param request 重命名请求（newname 必填，不可与其他分类重名）
     */
    @PutMapping("/{id}")
    public ApiResponse<Void> rename(@PathVariable Long id, @Valid @RequestBody CategorySaveRequest request) {
        categoryService.rename(id, request.getName());
        return ApiResponse.ok();
    }

    /**
     * #5 批量排序：按数组顺序写 sort=1..n。
     *
     * @param request 排序请求（ids 顺序即排序顺序）
     */
    @PutMapping("/sort")
    public ApiResponse<Void> sort(@Valid @RequestBody CategorySortRequest request) {
        categoryService.reorder(request.getIds());
        return ApiResponse.ok();
    }

    /**
     * #6 删除分类。
     *
     * @param id 分类 ID
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> remove(@PathVariable Long id) {
        categoryService.remove(id);
        return ApiResponse.ok();
    }
}
