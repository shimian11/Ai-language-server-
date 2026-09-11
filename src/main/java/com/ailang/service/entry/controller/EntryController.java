package com.ailang.service.entry.controller;

import com.ailang.common.result.ApiResponse;
import com.ailang.service.entry.pojo.dto.EntryListQuery;
import com.ailang.service.entry.pojo.dto.EntrySaveRequest;
import com.ailang.service.entry.pojo.dto.EntryStatusRequest;
import com.ailang.service.entry.pojo.dto.PageResult;
import com.ailang.service.entry.pojo.vo.EntryVO;
import com.ailang.service.entry.service.EntryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
 * 案例接口 #7-#13、#15。写操作由 SecurityConfig 统一拦截；
 * 公开 GET 的隐私规则（未登录只见已发布）在 Service 内处理。
 */
@RestController
@RequestMapping("/api/entries")
@RequiredArgsConstructor
public class EntryController {

    private final EntryService entryService;

    /**
     * #7 分页列表。
     *
     * @param query 查询参数（page,size,categoryId,keyword,status）
     * @return 分页结果
     */
    @GetMapping
    public ApiResponse<PageResult<EntryVO>> page(EntryListQuery query) {
        return ApiResponse.ok(entryService.page(query, authenticated()));
    }

    /**
     * #8 案例详情（浏览数 +1）。
     *
     * @param id 案例 ID
     * @return 案例详情；不存在或未登录访问非已发布案例时返回错误
     */
    @GetMapping("/{id}")
    public ApiResponse<EntryVO> detail(@PathVariable Long id) {
        return ApiResponse.ok(entryService.detail(id, authenticated()));
    }

    /**
     * #9 新建案例。
     *
     * @param request 新建入参（含基本信息与图片/标签）
     * @return 新建后的案例 VO
     */
    @PostMapping
    public ApiResponse<EntryVO> create(@Valid @RequestBody EntrySaveRequest request) {
        return ApiResponse.ok(entryService.create(request));
    }

    /**
     * #10 更新案例（覆盖式；图片按 url diff）。
     *
     * @param id      案例 ID
     * @param request 更新入参
     * @return 更新后的案例 VO
     */
    @PutMapping("/{id}")
    public ApiResponse<EntryVO> update(@PathVariable Long id, @Valid @RequestBody EntrySaveRequest request) {
        return ApiResponse.ok(entryService.update(id, request));
    }

    /**
     * #11 发布/下架案例。
     *
     * @param id      案例 ID
     * @param request 状态请求（status：DRAFT / PUBLISHED）
     */
    @PutMapping("/{id}/status")
    public ApiResponse<Void> updateStatus(@PathVariable Long id, @Valid @RequestBody EntryStatusRequest request) {
        entryService.updateStatus(id, request.getStatus());
        return ApiResponse.ok();
    }

    /**
     * #12 删除案例（图片记录级联删除）。
     *
     * @param id 案例 ID
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> remove(@PathVariable Long id) {
        entryService.remove(id);
        return ApiResponse.ok();
    }

    /**
     * #13 相关案例（同大类已发布最近 2 条）。
     *
     * @param id 案例 ID
     * @return 相关案例列表
     */
    @GetMapping("/{id}/related")
    public ApiResponse<List<EntryVO>> related(@PathVariable Long id) {
        return ApiResponse.ok(entryService.related(id));
    }

    /**
     * #15 复制计数 +1。
     *
     * @param id 案例 ID
     */
    @PostMapping("/{id}/copy")
    public ApiResponse<Void> copy(@PathVariable Long id) {
        entryService.incrementCopy(id);
        return ApiResponse.ok();
    }

    /**
     * 前台接口判断是否带有效 token（决定隐私规则与管理端全量查询）。
     *
     * @return 是否已认证（非匿名）
     */
    private boolean authenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);
    }
}
