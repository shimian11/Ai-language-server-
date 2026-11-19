package com.ailang.service.entry.pojo.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 案例手动排序入参：按数组顺序重写 sort。
 */
@Data
public class EntrySortRequest {

    /** 按目标展示顺序排列的案例 ID */
    @NotEmpty(message = "排序列表不能为空")
    private List<Long> ids;
}
