package com.ailang.service.article.service.impl;

import com.ailang.common.exception.BizException;
import com.ailang.service.article.pojo.dto.ArticleListQuery;
import com.ailang.service.article.pojo.dto.ArticleSaveRequest;
import com.ailang.service.article.pojo.dto.CompanyCreateRequest;
import com.ailang.service.article.pojo.entity.Article;
import com.ailang.service.article.pojo.entity.Company;
import com.ailang.service.article.mapper.ArticleMapper;
import com.ailang.service.article.mapper.CompanyMapper;
import com.ailang.service.article.pojo.vo.ArticleVO;
import com.ailang.service.article.pojo.vo.CompanyVO;
import com.ailang.service.article.service.ArticleService;
import com.ailang.service.entry.pojo.dto.PageResult;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
/**
 * 文章 Service 实现。
 * - 隐私规则：未登录强制只看已发布（与案例模块一致）
 * - 排序：按展示日期 display_date（空值排最后），方向由查询参数决定
 * - 公司：保存时按 key 解析 company_id；公司列表带已发布/全部两个计数
 */
@Service
@RequiredArgsConstructor
public class ArticleServiceImpl implements ArticleService {

    private static final String PUBLISHED = "PUBLISHED";
    private static final String DRAFT = "DRAFT";
    /** 日期输出格式（yyyy-MM-dd；实习的 yyyy-MM 原样保留） */
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final ArticleMapper articleMapper;
    private final CompanyMapper companyMapper;

    /** 分页查询文章：类型/栏目/状态/公司/关键词过滤 + 展示日期排序 */
    @Override
    public PageResult<ArticleVO> page(ArticleListQuery query, boolean authenticated) {
        QueryWrapper<Article> wrapper = new QueryWrapper<>();
        // 隐私规则：未登录强制只看已发布；登录后 status 空 = 全部
        String status = authenticated ? query.getStatus() : PUBLISHED;
        if (status != null && !status.isBlank()) {
            wrapper.eq("status", status);
        }
        if (query.getType() != null && !query.getType().isBlank()) {
            wrapper.eq("type", query.getType());
        }
        if (query.getTopic() != null && !query.getTopic().isBlank()) {
            wrapper.eq("topic", query.getTopic());
        }
        if (query.getCompanyKey() != null && !query.getCompanyKey().isBlank()) {
            // 按公司 key 解析 id 后过滤（company key 唯一）
            Company company = companyMapper.selectOne(
                    new QueryWrapper<Company>().eq("key", query.getCompanyKey()));
            wrapper.eq("company_id", company == null ? -1L : company.getId());
        }
        if (query.getKeyword() != null && !query.getKeyword().isBlank()) {
            String keyword = query.getKeyword().trim();
            wrapper.and(w -> w.like("title", keyword)
                    .or().like("summary", keyword)
                    .or().like("tags", keyword));
        }
        // 排序：展示日期倒序（asc 传参翻转），同日内按 id 稳定排序
        if ("asc".equalsIgnoreCase(query.getSort())) {
            wrapper.orderByAsc("display_date", "id");
        } else {
            wrapper.orderByDesc("display_date", "id");
        }
        // 空日期排最后：这里依赖 PG 的 NULLS LAST 语义由 MP 拼接，简单处理为分页后内存兜底排序
        Page<Article> result = articleMapper.selectPage(new Page<>(query.getPage(), query.getSize()), wrapper);
        Map<Long, Company> companyMap = companyMap();
        List<ArticleVO> vos = result.getRecords().stream()
                .map(a -> toVO(a, companyMap))
                .toList();
        // 空日期排最后：按「是否为空 + 日期 + id」二级排序（同页内稳定；日期非空的行已由 SQL 排好）
        if ("asc".equalsIgnoreCase(query.getSort())) {
            vos = vos.stream().sorted(Comparator
                    .comparing((ArticleVO v) -> v.getDate() == null || v.getDate().isBlank())
                    .thenComparing(ArticleVO::getDate, Comparator.nullsLast(Comparator.naturalOrder()))
                    .thenComparing(ArticleVO::getId)).toList();
        } else {
            vos = vos.stream().sorted(Comparator
                    .comparing((ArticleVO v) -> !(v.getDate() == null || v.getDate().isBlank()))
                    .thenComparing(ArticleVO::getDate, Comparator.nullsLast(Comparator.reverseOrder()))
                    .thenComparing(ArticleVO::getId, Comparator.reverseOrder())).toList();
        }
        return PageResult.of(vos, result.getTotal(), result.getCurrent(), result.getSize());
    }

    /** 文章详情：不存在或匿名访问未发布文章时抛业务异常 */
    @Override
    public ArticleVO detail(Long id, boolean authenticated) {
        Article article = articleMapper.selectById(id);
        if (article == null || (!authenticated && !PUBLISHED.equals(article.getStatus()))) {
            throw new BizException("文章不存在或尚未发布");
        }
        return toVO(article, companyMap());
    }

    /** 新建文章：校验公司、组装标签，首次发布写 published_at */
    @Override
    @Transactional
    public ArticleVO create(ArticleSaveRequest request) {
        Article article = new Article();
        applyFields(article, request);
        article.setStatus(request.getStatus() == null || request.getStatus().isBlank() ? DRAFT : request.getStatus());
        if (PUBLISHED.equals(article.getStatus())) {
            article.setPublishedAt(LocalDateTime.now());
        }
        articleMapper.insert(article);
        return toVO(article, companyMap());
    }

    /** 覆盖式更新文章（不改状态与发布时间；状态走独立接口） */
    @Override
    @Transactional
    public ArticleVO update(Long id, ArticleSaveRequest request) {
        Article article = articleMapper.selectById(id);
        if (article == null) {
            throw new BizException("文章不存在");
        }
        applyFields(article, request);
        articleMapper.updateById(article);
        return toVO(article, companyMap());
    }

    /** 发布 / 下架：首次发布写 published_at，下架仅改状态 */
    @Override
    @Transactional
    public void updateStatus(Long id, String status) {
        Article article = articleMapper.selectById(id);
        if (article == null) {
            throw new BizException("文章不存在");
        }
        article.setStatus(status);
        if (PUBLISHED.equals(status) && article.getPublishedAt() == null) {
            article.setPublishedAt(LocalDateTime.now());
        }
        articleMapper.updateById(article);
    }

    /** 删除文章 */
    @Override
    @Transactional
    public void remove(Long id) {
        articleMapper.deleteById(id);
    }

    /** 公司列表：按 sort 排序，附已发布/全部两个计数 */
    @Override
    public List<CompanyVO> companies() {
        List<Company> companies = companyMapper.selectList(
                new QueryWrapper<Company>().orderByAsc("sort", "id"));
        // 一次查询取所有文章，内存中按公司聚合计数（个人站量级足够）
        List<Article> articles = articleMapper.selectList(new QueryWrapper<>());
        return companies.stream().map(c -> {
            CompanyVO vo = new CompanyVO();
            vo.setKey(c.getKey());
            vo.setName(c.getName());
            vo.setMeta(c.getMeta());
            vo.setMilestone(Boolean.TRUE.equals(c.getMilestone()));
            vo.setCountPublished(articles.stream()
                    .filter(a -> c.getId().equals(a.getCompanyId()) && PUBLISHED.equals(a.getStatus()))
                    .count());
            vo.setCountAll(articles.stream()
                    .filter(a -> c.getId().equals(a.getCompanyId()))
                    .count());
            return vo;
        }).toList();
    }

    /** 新建公司别名：key 由名称生成（名称本身即稳定别名），重复名称抛业务异常 */
    @Override
    @Transactional
    public CompanyVO createCompany(CompanyCreateRequest request) {
        Company exists = companyMapper.selectOne(
                new QueryWrapper<Company>().eq("name", request.getName().trim()));
        if (exists != null) {
            throw new BizException("公司别名「" + request.getName() + "」已存在");
        }
        Company company = new Company();
        company.setKey(request.getName().trim());
        company.setName(request.getName().trim());
        company.setMeta(request.getMeta() == null ? "" : request.getMeta());
        company.setMilestone(Boolean.TRUE.equals(request.getMilestone()));
        // 排序追加到末尾
        Integer maxSort = companyMapper.selectList(new QueryWrapper<>()).stream()
                .map(Company::getSort)
                .max(Comparator.naturalOrder())
                .orElse(0);
        company.setSort(maxSort + 1);
        companyMapper.insert(company);
        CompanyVO vo = new CompanyVO();
        vo.setKey(company.getKey());
        vo.setName(company.getName());
        vo.setMeta(company.getMeta());
        vo.setMilestone(company.getMilestone());
        vo.setCountAll(0L);
        vo.setCountPublished(0L);
        return vo;
    }

    /* ==================== 私有工具 ==================== */

    /** 把请求字段覆盖到实体（tags 数组 → 逗号分隔；公司 key → id） */
    private void applyFields(Article article, ArticleSaveRequest request) {
        article.setType(request.getType());
        article.setTopic(request.getTopic() == null ? "" : request.getTopic());
        article.setSub(request.getSub() == null ? "" : request.getSub());
        article.setTitle(request.getTitle());
        article.setSummary(request.getSummary() == null ? "" : request.getSummary());
        article.setBody(request.getBody() == null ? "" : request.getBody());
        article.setCover(request.getCover() == null ? "" : request.getCover());
        article.setTags(request.getTags() == null ? "" : String.join(",", request.getTags()));
        article.setDisplayDate(request.getDisplayDate() == null ? "" : request.getDisplayDate());
        article.setResult(request.getResult() == null ? "" : request.getResult());
        article.setResultClass(request.getResultClass() == null ? "" : request.getResultClass());
        article.setReadLabel(request.getReadLabel() == null ? "" : request.getReadLabel());
        // 公司 key → id（找不到则置空，不阻断保存）
        if (request.getCompanyKey() != null && !request.getCompanyKey().isBlank()) {
            Company company = companyMapper.selectOne(
                    new QueryWrapper<Company>().eq("key", request.getCompanyKey()));
            article.setCompanyId(company == null ? null : company.getId());
        } else {
            article.setCompanyId(null);
        }
    }

    /** 公司缓存：id → Company（避免循环内查库） */
    private Map<Long, Company> companyMap() {
        return companyMapper.selectList(new QueryWrapper<>()).stream()
                .collect(Collectors.toMap(Company::getId, c -> c));
    }

    /** 实体 → VO：日期格式化、标签拆分、公司信息补全 */
    private ArticleVO toVO(Article article, Map<Long, Company> companyMap) {
        ArticleVO vo = new ArticleVO();
        vo.setId(article.getId());
        vo.setType(article.getType());
        vo.setTopic(article.getTopic());
        vo.setSub(article.getSub());
        vo.setTitle(article.getTitle());
        vo.setSummary(article.getSummary());
        vo.setBody(article.getBody());
        vo.setCover(article.getCover());
        vo.setTags(article.getTags() == null || article.getTags().isBlank()
                ? List.of()
                : List.of(article.getTags().split(",")));
        // 公司信息：key 给前端做分组，name 给展示
        Company company = article.getCompanyId() == null ? null : companyMap.get(article.getCompanyId());
        vo.setCompany(company == null ? null : company.getKey());
        vo.setCompanyName(company == null ? null : company.getName());
        vo.setDate(article.getDisplayDate());
        vo.setResult(article.getResult());
        vo.setResultClass(article.getResultClass());
        vo.setRead(article.getReadLabel());
        vo.setStatus(article.getStatus());
        vo.setPublishedAt(article.getPublishedAt() == null ? null : DATE_FMT.format(article.getPublishedAt()));
        vo.setCreatedAt(article.getCreatedAt() == null ? null : DATE_FMT.format(article.getCreatedAt()));
        return vo;
    }
}
