package com.ailang.service.entry.pojo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 案例新建/更新入参（与前端 EntryEditor payload 对齐）。
 */
@Data
public class EntrySaveRequest {

    /** 案例标题 */
    @NotBlank(message = "标题不能为空")
    private String title;

    /** 案例摘要 */
    private String summary;

    /** 所属大类 ID */
    @NotNull(message = "所属分类不能为空")
    private Long categoryId;

    /** 设计风格 */
    @NotBlank(message = "具体样式不能为空")
    private String style;

    /** 所属平台：web / app；可空，默认 web */
    private String platform;

    /** AI 生成提示词 */
    private String prompt;

    /** 可空；html源码 */
    private String htmlSource;

    /** 图片列表 */
    private List<EntryImageRequest> images;

    /** 标签数组 */
    private List<String> tags;

    /** DRAFT / PUBLISHED；新建/更新时的目标状态 */
    private String status;
}