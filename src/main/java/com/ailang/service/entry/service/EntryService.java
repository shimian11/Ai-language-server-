package com.ailang.service.entry.service;

import com.ailang.service.entry.pojo.vo.AdminOverviewVO;
import com.ailang.service.entry.pojo.dto.EntryListQuery;
import com.ailang.service.entry.pojo.dto.EntrySaveRequest;
import com.ailang.service.entry.pojo.vo.EntryVO;
import com.ailang.service.entry.pojo.dto.PageResult;
import com.ailang.service.entry.pojo.vo.EntryGroupVO;

import java.util.List;

/**
 * 案例 Service。
 */
public interface EntryService {

    /**
     * #7 分页列表：未登录强制 status=PUBLISHED（传了也无效）；登录后 status 空 = 全部。
     * 排序：COALESCE(published_at, created_at) 倒序；htmlSource 截断 2KB。
     */
    PageResult<EntryVO> page(EntryListQuery query, boolean authenticated);

    /**
     * #8 详情：非发布案例仅鉴权可见（否则按不存在处理）；浏览数 +1；htmlSource 全文。
     */
    EntryVO detail(Long id, boolean authenticated);

    /** #9 新建：title/categoryId/style 必填，images 与 htmlSource 至少一项；发布态写首次发布时间 */
    EntryVO create(EntrySaveRequest request);

    /** #10 更新：覆盖式更新（htmlSource 可清空），图片列表按 url diff；状态随入参 */
    EntryVO update(Long id, EntrySaveRequest request);

    /** #11 发布/下架：首次发布写 published_at（下架不清理） */
    void updateStatus(Long id, String status);

    /** #12 删除：图片记录随外键级联删除 */
    void remove(Long id);

    /** #13 相关案例：同大类已发布、排除自身、最近 2 条 */
    List<EntryVO> related(Long id);

    /** 后台分组视图：按 (设计大类, 案例标题) 聚合，返回每组统计与封面 */
    List<EntryGroupVO> groups(EntryListQuery query, boolean authenticated);

    /** #15 复制计数：copy_count 直接自增 */
    void incrementCopy(Long id);

    /** 管理端总览（Dashboard） */
    AdminOverviewVO overview();
}
