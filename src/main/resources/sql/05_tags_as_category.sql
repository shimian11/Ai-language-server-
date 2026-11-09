-- ============================================================
-- 05 标签即分类迁移（2026-11 改版 §6.3① 方案 A；幂等，可重复执行）
-- 1) article 表补 cover 封面列（文章编辑器「封面图（可选）」）
-- 2) entry.category_id 的值并入 entry.tags（每个大类名作为一个标签追加）
-- 3) 校验与索引用于 tags LIKE 过滤
-- 说明：category 表与 entry.category_id 列**保留不删**（历史数据可回溯，
--       应用层已不再读写 category），后续确认无回滚需求时可人工 DROP。
-- ============================================================

-- ---- 1. article 封面列 ----
ALTER TABLE article ADD COLUMN IF NOT EXISTS cover VARCHAR(500) DEFAULT '';

-- ---- 1.5 entry.category_id 放开非空约束（标签即分类后新数据不再挂大类；历史值保留可回溯）----
ALTER TABLE entry ALTER COLUMN category_id DROP NOT NULL;

-- ---- 2. 大类名并入标签（仅追加缺失的；tags 空则直接写入）----
-- 逐类处理：把 category 名作为标签追加到该类全部 entry（避免一条 SQL 里跨表自连接的复杂性）
DO $$
DECLARE
  c RECORD;
  updated INT;
BEGIN
  FOR c IN SELECT id, name FROM category LOOP
    UPDATE entry
       SET tags = CASE
                    WHEN tags IS NULL OR btrim(tags) = '' THEN c.name
                    WHEN position(c.name IN tags) > 0 THEN tags          -- 已含该标签，跳过
                    ELSE tags || ',' || c.name
                  END
     WHERE category_id = c.id
       AND (tags IS NULL OR position(c.name IN tags) = 0);
    GET DIAGNOSTICS updated = ROW_COUNT;
    RAISE NOTICE 'category % (%) 并入标签：更新 % 行', c.id, c.name, updated;
  END LOOP;
END $$;

-- ---- 3. 标签过滤辅助索引（LIKE '%x%' 无法走普通索引，此索引仅辅助前缀/统计场景；保留以备 GIN 升级）----
CREATE INDEX IF NOT EXISTS idx_entry_tags ON entry (tags);

-- ---- 完成提示 ----
DO $$ BEGIN RAISE NOTICE '05 迁移完成：article.cover 已就绪；category 名已并入 entry.tags'; END $$;
