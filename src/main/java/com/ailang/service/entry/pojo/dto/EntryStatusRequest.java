package com.ailang.service.entry.pojo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 发布/下架入参。
 */
@Data
public class EntryStatusRequest {

    /** 目标状态（DRAFT / PUBLISHED） */
    @NotBlank(message = "状态不能为空")
    private String status;
}