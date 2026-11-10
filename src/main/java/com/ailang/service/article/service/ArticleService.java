package com.ailang.service.article.service;

import com.ailang.service.article.pojo.dto.ArticleListQuery;
import com.ailang.service.article.pojo.dto.ArticleSaveRequest;
import com.ailang.service.article.pojo.dto.CompanyCreateRequest;
import com.ailang.service.article.pojo.dto.CompanyUpdateRequest;
import com.ailang.service.article.pojo.vo.ArticleVO;
import com.ailang.service.article.pojo.vo.CompanyVO;

import java.util.List;

/**
 * 文章 Service：心得/实习/面试三类内容的增删改查。
 * 隐私规则与案例模块一致：未登录只见已发布。
 */
public interface ArticleService {

    /** 分页列表（隐私规则：未登录强制只看已发布） */
    com.ailang.service.entry.pojo.dto.PageResult<ArticleVO> page(ArticleListQuery query, boolean authenticated);

    /** 文章详情（未登录访问非已发布文章时抛业务异常） */
    ArticleVO detail(Long id, boolean authenticated);

    /** 新建文章 */
    ArticleVO create(ArticleSaveRequest request);

    /** 覆盖式更新文章 */
    ArticleVO update(Long id, ArticleSaveRequest request);

    /** 发布 / 下架（首次发布写 published_at） */
    void updateStatus(Long id, String status);

    /** 删除文章 */
    void remove(Long id);

    /** 公司列表（含已发布/全部计数） */
    List<CompanyVO> companies();

    /** 新建公司别名（key 由名称生成） */
    CompanyVO createCompany(CompanyCreateRequest request);

    /** 编辑公司节点（key 保持不变，仅改名称/标注/里程碑） */
    CompanyVO updateCompany(String key, CompanyUpdateRequest request);

    /** 删除公司节点（仅当名下无任何记录时允许，避免文章失去归属） */
    void removeCompany(String key);
}
