package com.ailang.service.article.pojo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/** 新建公司别名入参（实习管理页「＋ 新建公司」） */
@Data
public class CompanyCreateRequest {

    /** 脱敏别名显示名（如「某独角兽」） */
    @NotBlank(message = "公司别名不能为空")
    private String name;

    /** 角色标注（如「前端 · 2026.06 — 09」） */
    private String meta;

    /** 是否里程碑节点 */
    private Boolean milestone;
}
