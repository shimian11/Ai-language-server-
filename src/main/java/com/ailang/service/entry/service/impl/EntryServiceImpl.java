package com.ailang.service.entry.service.impl;

import com.ailang.common.exception.BizException;
import com.ailang.service.entry.pojo.vo.AdminOverviewVO;
import com.ailang.service.entry.pojo.dto.EntryImageRequest;
import com.ailang.service.entry.pojo.vo.EntryImageVO;
import com.ailang.service.entry.pojo.dto.EntryListQuery;
import com.ailang.service.entry.pojo.dto.EntrySaveRequest;
import com.ailang.service.entry.pojo.vo.EntryVO;
import com.ailang.service.entry.pojo.vo.EntryGroupVO;
import com.ailang.service.entry.pojo.dto.PageResult;
import com.ailang.service.entry.pojo.entity.Category;
import com.ailang.service.entry.pojo.entity.Entry;
import com.ailang.service.entry.pojo.entity.EntryImage;
import com.ailang.service.entry.mapper.CategoryMapper;
import com.ailang.service.entry.mapper.EntryImageMapper;
import com.ailang.service.entry.mapper.EntryMapper;
import com.ailang.service.entry.service.EntryService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 案例 Service 实现。
 * 列表排序与关键词语义对齐前端 mock：COALESCE(published_at, created_at) 倒序，
 * 关键词匹配 标题/简介/提示词/标签。
 */
@Service
@RequiredArgsConstructor
public class EntryServiceImpl implements EntryService {

    private static final String PUBLISHED = "PUBLISHED";
    private static final String DRAFT = "DRAFT";
    /** 列表场景 htmlSource 截断长度（2KB） */
    private static final int HTML_TRUNCATE = 2048;

    private final EntryMapper entryMapper;
    private final EntryImageMapper entryImageMapper;
    private final CategoryMapper categoryMapper;
    private final com.ailang.service.article.mapper.ArticleMapper articleMapper;

    /** 分页查询案例：按状态/分类/关键词过滤，非管理员强制只看已发布 */
    @Override
    public PageResult<EntryVO> page(EntryListQuery query, boolean authenticated) {
        QueryWrapper<Entry> wrapper = new QueryWrapper<>();
        // 隐私规则：未登录只可见已发布；登录后 status 空 = 全部
        String status = authenticated ? query.getStatus() : PUBLISHED;
        if (status != null && !status.isBlank()) {
            wrapper.eq("status", status);
        }
        if (query.getCategoryId() != null) {
            wrapper.eq("category_id", query.getCategoryId());
        }
        if (query.getTitle() != null && !query.getTitle().isBlank()) {
            wrapper.eq("title", query.getTitle());
        }
        if (query.getKeyword() != null && !query.getKeyword().isBlank()) {
            String keyword = query.getKeyword().trim();
            wrapper.and(w -> w.like("title", keyword)
                    .or().like("summary", keyword)
                    .or().like("prompt", keyword)
                    .or().like("tags", keyword));
        }
        if (query.getPlatform() != null && !query.getPlatform().isBlank()) {
            // 语义：选 Web → web + 通用；选 App → app + 通用；选通用 → 仅通用
            String p = query.getPlatform();
            wrapper.and(w -> w.eq("platform", p).or().eq("platform", "general"));
        }
        // 标签筛选：精确匹配逗号分隔标签串中的一个（前后逗号锚定，避免子串误命中）
        if (query.getTag() != null && !query.getTag().isBlank()) {
            String tag = query.getTag().trim();
            wrapper.and(w -> w.eq("tags", tag)
                    .or().likeRight("tags", tag + ",")
                    .or().likeLeft("tags", "," + tag)
                    .or().apply("tags LIKE {0}", "%," + tag + ",%"));
        }
        wrapper.orderByDesc("COALESCE(published_at, created_at)");

        int page = query.getPage() == null || query.getPage() < 1 ? 1 : query.getPage();
        int size = query.getSize() == null || query.getSize() < 1 ? 10 : query.getSize();
        Page<Entry> result = entryMapper.selectPage(new Page<>(page, size), wrapper);

        // htmlSource 返回全文：卡片/列表需要完整的 HTML 才能实时渲染预览（截断会产生残缺 HTML 导致 iframe 白屏）
        List<EntryVO> vos = toVOs(result.getRecords(), false);
        return PageResult.of(vos, result.getTotal(), result.getCurrent(), result.getSize());
    }

    /** 查询案例详情：非已发布案例仅鉴权可见；浏览数 +1 */
    @Override
    public EntryVO detail(Long id, boolean authenticated) {
        Entry entry = entryMapper.selectById(id);
        if (entry == null || (!PUBLISHED.equals(entry.getStatus()) && !authenticated)) {
            throw new BizException("案例不存在");
        }
        entryMapper.update(null, new LambdaUpdateWrapper<Entry>()
                .eq(Entry::getId, id)
                .setSql("view_count = view_count + 1"));
        entry.setViewCount(entry.getViewCount() == null ? 1 : entry.getViewCount() + 1);
        return toVOs(List.of(entry), false).get(0);
    }

    /** 新建案例：校验后写入主记录与图片列表；发布态写首次发布时间 */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public EntryVO create(EntrySaveRequest request) {
        assertSaveValid(request);

        Entry entry = new Entry();
        applyFields(entry, request);
        if (PUBLISHED.equals(request.getStatus())) {
            entry.setPublishedAt(LocalDateTime.now());
        }
        entryMapper.insert(entry);
        insertImages(entry.getId(), request.getImages());

        return toVOs(List.of(entryMapper.selectById(entry.getId())), false).get(0);
    }

    /** 更新案例：覆盖式更新主记录 + 图片按 url diff；首次发布写发布时间 */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public EntryVO update(Long id, EntrySaveRequest request) {
        Entry existing = requireExisting(id);
        assertSaveValid(request);

        Entry entry = new Entry();
        entry.setId(id);
        applyFields(entry, request);
        // 首次发布写 published_at；下架不清理原发布时间
        if (PUBLISHED.equals(request.getStatus()) && existing.getPublishedAt() == null) {
            entry.setPublishedAt(LocalDateTime.now());
        }
        entryMapper.updateById(entry);
        diffImages(id, request.getImages());

        return toVOs(List.of(entryMapper.selectById(id)), false).get(0);
    }

    /** 更新案例状态（发布/下架）：首次发布写 published_at，下架保留原发布时间 */
    @Override
    public void updateStatus(Long id, String status) {
        assertStatusValid(status);
        Entry existing = requireExisting(id);

        LambdaUpdateWrapper<Entry> wrapper = new LambdaUpdateWrapper<Entry>()
                .eq(Entry::getId, id)
                .set(Entry::getStatus, status);
        if (PUBLISHED.equals(status) && existing.getPublishedAt() == null) {
            wrapper.set(Entry::getPublishedAt, LocalDateTime.now());
        }
        entryMapper.update(null, wrapper);
    }

    /** 删除案例（entry_image 由外键级联删除） */
    @Override
    public void remove(Long id) {
        requireExisting(id);
        // entry_image 外键 ON DELETE CASCADE 级联删除
        entryMapper.deleteById(id);
    }

    /** 相关案例：共享任一标签的已发布案例优先，其次全部已发布；环形取 5 条 */
    @Override
    public List<EntryVO> related(Long id) {
        Entry self = entryMapper.selectById(id);
        if (self == null) {
            return List.of();
        }
        // 全部已发布（含自身），id 升序
        List<Entry> all = entryMapper.selectList(new QueryWrapper<Entry>()
                .eq("status", PUBLISHED)
                .orderByAsc("id"));
        List<Entry> others = all.stream()
                .filter(e -> !e.getId().equals(id))
                .toList();
        if (others.isEmpty()) {
            return List.of();
        }
        // 有共享标签的排前面（同分类次之），保持 id 升序稳定
        Set<String> selfTags = splitTags(self.getTags());
        List<Entry> ranked = others.stream()
                .sorted(Comparator
                        .comparing((Entry e) -> {
                            long shared = splitTags(e.getTags()).stream().filter(selfTags::contains).count();
                            return shared > 0 ? 0L : 1L;
                        })
                        .thenComparing(e -> e.getCategoryId() != null
                                && e.getCategoryId().equals(self.getCategoryId()) ? 0L : 1L)  // 自身 categoryId 也可能为 null，equals 已由左判空保护
                        .thenComparing(Entry::getId))
                .toList();
        final int WINDOW = 5;
        // 环形：从排序后首位起取 5 条（个人站量级不足 5 时全量返回）
        List<Entry> picked = new ArrayList<>();
        for (int k = 0; k < Math.min(WINDOW, ranked.size()); k++) {
            picked.add(ranked.get(k));
        }
        return toVOs(picked, true);
    }

    /** 标签串拆分为去重集合（空串安全） */
    private Set<String> splitTags(String tags) {
        if (tags == null || tags.isBlank()) {
            return Set.of();
        }
        return Arrays.stream(tags.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());
    }

    /** 后台分组视图：按 (设计大类, 案例标题) 聚合，返回每组统计与封面 */
    @Override
    public List<EntryGroupVO> groups(EntryListQuery query, boolean authenticated) {
        QueryWrapper<Entry> w = new QueryWrapper<Entry>()
                .select("COALESCE(category_id, 0) AS cat_key", "title", "MIN(id) AS min_id", "COUNT(*) AS cnt")
                .groupBy("cat_key", "title")
                .orderByAsc("MIN(id)");
        String status = authenticated ? query.getStatus() : PUBLISHED;
        if (status != null && !status.isBlank()) {
            w.eq("status", status);
        }
        if (query.getKeyword() != null && !query.getKeyword().isBlank()) {
            w.like("title", query.getKeyword().trim());
        }
        if (query.getPlatform() != null && !query.getPlatform().isBlank()) {
            String p = query.getPlatform();
            w.and(x -> x.eq("platform", p).or().eq("platform", "general"));
        }
        List<Map<String, Object>> rows = entryMapper.selectMaps(w);
        if (rows.isEmpty()) {
            return List.of();
        }
        Set<Long> categoryIds = new HashSet<>();
        Set<Long> minIds = new HashSet<>();
        for (Map<String, Object> r : rows) {
            categoryIds.add(((Number) r.get("cat_key")).longValue());
            minIds.add(((Number) r.get("min_id")).longValue());
        }
        Map<Long, String> catNames = new HashMap<>();
        // 过滤掉 0（未挂大类的行）后查名称；历史数据迁移期间仍按大类名展示
        Set<Long> realIds = new HashSet<>(categoryIds);
        realIds.remove(0L);
        if (!realIds.isEmpty()) {
            for (Category c : categoryMapper.selectList(
                    new LambdaQueryWrapper<Category>().in(Category::getId, realIds))) {
                catNames.put(c.getId(), c.getName());
            }
        }
        Map<Long, String> covers = new HashMap<>();
        for (EntryImage im : entryImageMapper.selectList(new QueryWrapper<EntryImage>()
                .in("entry_id", minIds)
                .orderByDesc("is_main")
                .orderByAsc("sort"))) {
            covers.putIfAbsent(im.getEntryId(), im.getUrl());
        }
        List<EntryGroupVO> result = new ArrayList<>();
        for (Map<String, Object> r : rows) {
            Long catKey = ((Number) r.get("cat_key")).longValue();
            long minId = ((Number) r.get("min_id")).longValue();
            EntryGroupVO vo = new EntryGroupVO();
            vo.setCategoryId(catKey);
            vo.setCategoryName(catNames.getOrDefault(catKey, ""));
            vo.setTitle((String) r.get("title"));
            vo.setCount(((Number) r.get("cnt")).longValue());
            vo.setCover(covers.get(minId));
            result.add(vo);
        }
        return result;
    }

    /** 复制计数 +1：copy_count 直接自增 */
    @Override
    public void incrementCopy(Long id) {
        requireExisting(id);
        entryMapper.update(null, new LambdaUpdateWrapper<Entry>()
                .eq(Entry::getId, id)
                .setSql("copy_count = copy_count + 1"));
    }

    /** 管理端总览：提示词统计 + 文章统计 + 双最近列表 */
    @Override
    public AdminOverviewVO overview() {
        AdminOverviewVO vo = new AdminOverviewVO();
        // 提示词统计
        vo.setPublished(entryMapper.selectCount(new LambdaQueryWrapper<Entry>()
                .eq(Entry::getStatus, PUBLISHED)));
        vo.setDrafts(entryMapper.selectCount(new LambdaQueryWrapper<Entry>()
                .eq(Entry::getStatus, DRAFT)));
        vo.setTotalViews(entryMapper.sumViewCount());
        vo.setRecent(toVOs(entryMapper.selectList(new QueryWrapper<Entry>()
                        .eq("status", PUBLISHED)
                        .orderByDesc("COALESCE(published_at, created_at)")
                        .last("LIMIT 5")),
                true));
        // 文章统计（article 表直接计数，body 不查——仪表盘只需简要字段）
        vo.setArticleTotal(articleMapper.selectCount(null));
        vo.setArticleDrafts(articleMapper.selectCount(
                new LambdaQueryWrapper<com.ailang.service.article.pojo.entity.Article>()
                        .eq(com.ailang.service.article.pojo.entity.Article::getStatus, DRAFT)));
        vo.setRecentArticles(articleMapper.selectList(new QueryWrapper<com.ailang.service.article.pojo.entity.Article>()
                        .select("id", "type", "sub", "title", "status", "display_date")
                        .orderByDesc("COALESCE(NULLIF(display_date,''), to_char(created_at,'YYYY-MM-DD'))")
                        .last("LIMIT 5"))
                .stream()
                .map(a -> {
                    AdminOverviewVO.ArticleBrief brief = new AdminOverviewVO.ArticleBrief();
                    brief.setId(a.getId());
                    brief.setType(a.getType());
                    brief.setSub(a.getSub());
                    brief.setTitle(a.getTitle());
                    brief.setStatus(a.getStatus());
                    brief.setDate(a.getDisplayDate());
                    return brief;
                })
                .toList());
        return vo;
    }

    // ==================== 私有方法 ====================

    private Entry requireExisting(Long id) {
        Entry entry = entryMapper.selectById(id);
        if (entry == null) {
            throw new BizException("案例不存在");
        }
        return entry;
    }

    /** 保存前校验：标签至少 1 个 + 效果图与 HTML 源码至少一项 + 状态合法（大类不再必填） */
    private void assertSaveValid(EntrySaveRequest request) {
        assertStatusValid(request.getStatus());
        // 标签即分类（2026-11 方案 A）：标签必填 ≥1，替代原 category 必填
        if (request.getTags() == null || request.getTags().stream().noneMatch(t -> t != null && !t.isBlank())) {
            throw new BizException("标签至少填 1 个");
        }
        boolean hasImages = request.getImages() != null && !request.getImages().isEmpty();
        boolean hasHtml = request.getHtmlSource() != null && !request.getHtmlSource().isBlank();
        if (!hasImages && !hasHtml) {
            throw new BizException("效果图与 HTML 源码至少填一项");
        }
    }

    private void assertStatusValid(String status) {
        if (!PUBLISHED.equals(status) && !DRAFT.equals(status)) {
            throw new BizException("状态值不合法");
        }
    }

    private void applyFields(Entry entry, EntrySaveRequest request) {
        entry.setTitle(request.getTitle());
        entry.setSummary(request.getSummary() == null ? "" : request.getSummary());
        // 标签即分类：categoryId 不再必填，可空（历史数据保留原值）
        entry.setCategoryId(request.getCategoryId());
        entry.setPlatform(request.getPlatform() == null || request.getPlatform().isBlank() ? "web" : request.getPlatform());
        entry.setStyle(request.getStyle());
        entry.setPrompt(request.getPrompt() == null ? "" : request.getPrompt());
        entry.setHtmlSource(request.getHtmlSource());
        entry.setTags(request.getTags() == null ? "" : String.join(",", request.getTags()));
        entry.setStatus(request.getStatus());
    }

    private void insertImages(Long entryId, List<EntryImageRequest> images) {
        if (images == null || images.isEmpty()) {
            return;
        }
        for (int index = 0; index < images.size(); index++) {
            EntryImageRequest request = images.get(index);
            EntryImage image = new EntryImage();
            image.setEntryId(entryId);
            image.setUrl(request.getUrl());
            image.setIsMain(Boolean.TRUE.equals(request.getIsMain()));
            image.setSort(index + 1);
            entryImageMapper.insert(image);
        }
    }

    /** 图片列表 diff：按 url 匹配——保留更新、新增插入、多余删除；顺序即 sort */
    private void diffImages(Long entryId, List<EntryImageRequest> images) {
        List<EntryImage> existing = entryImageMapper.selectList(
                new LambdaQueryWrapper<EntryImage>().eq(EntryImage::getEntryId, entryId));
        Map<String, EntryImage> existingByUrl = existing.stream()
                .collect(Collectors.toMap(EntryImage::getUrl, image -> image, (a, b) -> a));

        Set<String> keepUrls = new HashSet<>();
        List<EntryImageRequest> requests = images == null ? List.of() : images;
        for (int index = 0; index < requests.size(); index++) {
            EntryImageRequest request = requests.get(index);
            keepUrls.add(request.getUrl());
            boolean main = Boolean.TRUE.equals(request.getIsMain());
            int sort = index + 1;

            EntryImage matched = existingByUrl.get(request.getUrl());
            if (matched == null) {
                EntryImage image = new EntryImage();
                image.setEntryId(entryId);
                image.setUrl(request.getUrl());
                image.setIsMain(main);
                image.setSort(sort);
                entryImageMapper.insert(image);
            } else if (!matched.getSort().equals(sort) || !matched.getIsMain().equals(main)) {
                EntryImage update = new EntryImage();
                update.setId(matched.getId());
                update.setEntryId(entryId);
                update.setUrl(request.getUrl());
                update.setIsMain(main);
                update.setSort(sort);
                entryImageMapper.updateById(update);
            }
        }
        for (EntryImage image : existing) {
            if (!keepUrls.contains(image.getUrl())) {
                entryImageMapper.deleteById(image.getId());
            }
        }
    }

    /** 批量转 VO：聚合分类名与图片列表 */
    private List<EntryVO> toVOs(List<Entry> entries, boolean truncateHtml) {
        if (entries.isEmpty()) {
            return List.of();
        }
        Map<Long, String> categoryNames = loadCategoryNames(entries);
        Map<Long, List<EntryImage>> imagesByEntry = loadImages(entries);

        return entries.stream()
                .map(entry -> toVO(entry,
                        entry.getCategoryId() == null ? null : categoryNames.get(entry.getCategoryId()),
                        imagesByEntry.getOrDefault(entry.getId(), List.of()), truncateHtml))
                .collect(Collectors.toList());
    }

    private Map<Long, String> loadCategoryNames(List<Entry> entries) {
        Set<Long> categoryIds = entries.stream()
                .map(Entry::getCategoryId)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toSet());
        if (categoryIds.isEmpty()) {
            return Map.of();
        }
        return categoryMapper.selectList(new LambdaQueryWrapper<Category>()
                        .in(Category::getId, categoryIds))
                .stream()
                .collect(Collectors.toMap(Category::getId, Category::getName));
    }

    private Map<Long, List<EntryImage>> loadImages(List<Entry> entries) {
        List<Long> entryIds = entries.stream().map(Entry::getId).collect(Collectors.toList());
        if (entryIds.isEmpty()) {
            return Map.of();
        }
        return entryImageMapper.selectList(new LambdaQueryWrapper<EntryImage>()
                        .in(EntryImage::getEntryId, entryIds)
                        .orderByAsc(EntryImage::getSort))
                .stream()
                .collect(Collectors.groupingBy(EntryImage::getEntryId));
    }

    private EntryVO toVO(Entry entry, String categoryName, List<EntryImage> images, boolean truncateHtml) {
        EntryVO vo = new EntryVO();
        vo.setId(entry.getId());
        vo.setTitle(entry.getTitle());
        vo.setSummary(entry.getSummary());
        vo.setCategoryId(entry.getCategoryId());
        vo.setCategoryName(categoryName);
        vo.setPlatform(entry.getPlatform());
        vo.setStyle(entry.getStyle());
        vo.setPrompt(entry.getPrompt());
        vo.setHtmlSource(truncateHtml && entry.getHtmlSource() != null && entry.getHtmlSource().length() > HTML_TRUNCATE
                ? entry.getHtmlSource().substring(0, HTML_TRUNCATE)
                : entry.getHtmlSource());
        vo.setImages(images.stream().map(image -> {
            EntryImageVO imageVO = new EntryImageVO();
            imageVO.setId(image.getId());
            imageVO.setUrl(image.getUrl());
            imageVO.setIsMain(image.getIsMain());
            return imageVO;
        }).collect(Collectors.toList()));
        vo.setTags(entry.getTags() == null || entry.getTags().isBlank()
                ? List.of()
                : Arrays.stream(entry.getTags().split(","))
                .map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toList()));
        vo.setStatus(entry.getStatus());
        vo.setPublishedAt(entry.getPublishedAt());
        vo.setCreatedAt(entry.getCreatedAt());
        vo.setViewCount(entry.getViewCount());
        vo.setCopyCount(entry.getCopyCount());
        return vo;
    }
}
