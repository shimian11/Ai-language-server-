package com.ailang.service.entry.controller;

import com.ailang.common.result.ApiResponse;
import com.ailang.service.entry.pojo.vo.AdminOverviewVO;
import com.ailang.service.entry.service.EntryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理端接口（需鉴权，由 SecurityConfig 兜底拦截）。
 */
@RestController
@RequiredArgsConstructor
public class AdminController {

    private final EntryService entryService;

    /**
     * Dashboard 总览：统计卡 + 最近 5 条已发布案例。
     *
     * @return 总览数据
     */
    @GetMapping("/api/admin/overview")
    public ApiResponse<AdminOverviewVO> overview() {
        return ApiResponse.ok(entryService.overview());
    }
}
