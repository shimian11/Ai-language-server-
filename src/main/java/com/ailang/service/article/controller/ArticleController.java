package com.ailang.service.article.controller;

import com.ailang.common.result.ApiResponse;
import com.ailang.service.article.pojo.dto.ArticleListQuery;
import com.ailang.service.article.pojo.dto.ArticleSaveRequest;
import com.ailang.service.article.pojo.dto.CompanyCreateRequest;
import com.ailang.service.article.pojo.vo.ArticleVO;
import com.ailang.service.article.pojo.vo.CompanyVO;
import com.ailang.service.article.service.ArticleService;
import com.ailang.service.entry.pojo.dto.PageResult;
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
 * 博客文章接口（心得/实习/面试，2026-11 改版新增）。
 * 公开 GET 的隐私规则（未登录只见已发布）在 Service 内处理；
 * 写操作由 SecurityConfig 统一拦截（anyRequest().authenticated()）。
 */
@RestController
@RequestMapping("/api/articles")
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleService articleService;

    /**
     * 分页列表。
     *
     * @param query 查询参数（page,size,type,topic,status,keyword,companyKey,sort）
     * @return 分页结果
     */
    @GetMapping
    public ApiResponse<PageResult<ArticleVO>> page(ArticleListQuery query) {
        return ApiResponse.ok(articleService.page(query, authenticated()));
    }

    /**
     * 公司列表（实习两级时间线第一级；含已发布/全部计数）。
     *
     * @return 公司列表（按 sort 升序）
     */
    @GetMapping("/companies")
    public ApiResponse<List<CompanyVO>> companies() {
        return ApiResponse.ok(articleService.companies());
    }

    /**
     * 新建公司别名（后台实习管理「＋ 新建公司」）。
     *
     * @param request 公司入参（名称必填）
     * @return 新建后的公司 VO
     */
    @PostMapping("/companies")
    public ApiResponse<CompanyVO> createCompany(@Valid @RequestBody CompanyCreateRequest request) {
        return ApiResponse.ok(articleService.createCompany(request));
    }

    /**
     * 文章详情。
     *
     * @param id 文章 ID
     * @return 文章详情；不存在或匿名访问未发布文章时返回错误
     */
    @GetMapping("/{id}")
    public ApiResponse<ArticleVO> detail(@PathVariable Long id) {
        return ApiResponse.ok(articleService.detail(id, authenticated()));
    }

    /**
     * 新建文章。
     *
     * @param request 文章入参
     * @return 新建后的文章 VO
     */
    @PostMapping
    public ApiResponse<ArticleVO> create(@Valid @RequestBody ArticleSaveRequest request) {
        return ApiResponse.ok(articleService.create(request));
    }

    /**
     * 覆盖式更新文章。
     *
     * @param id      文章 ID
     * @param request 文章入参
     * @return 更新后的文章 VO
     */
    @PutMapping("/{id}")
    public ApiResponse<ArticleVO> update(@PathVariable Long id, @Valid @RequestBody ArticleSaveRequest request) {
        return ApiResponse.ok(articleService.update(id, request));
    }

    /**
     * 发布 / 下架（首次发布写 published_at）。
     *
     * @param id      文章 ID
     * @param request 状态请求（status：DRAFT / PUBLISHED）
     */
    @PutMapping("/{id}/status")
    public ApiResponse<Void> updateStatus(@PathVariable Long id, @RequestBody ArticleSaveRequest request) {
        articleService.updateStatus(id, request.getStatus());
        return ApiResponse.ok();
    }

    /**
     * 删除文章。
     *
     * @param id 文章 ID
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> remove(@PathVariable Long id) {
        articleService.remove(id);
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
