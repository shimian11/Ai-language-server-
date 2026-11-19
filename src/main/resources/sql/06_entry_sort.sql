-- ============================================================
-- 06 案例手动排序（幂等）
-- 后台拖拽排序需要持久化：sort 小者靠前
-- ============================================================

ALTER TABLE public.entry ADD COLUMN IF NOT EXISTS sort integer DEFAULT 0 NOT NULL;

COMMENT ON COLUMN public.entry.sort IS '手动排序值（小者靠前，后台拖拽维护）';

-- 首次初始化：按现有 id 顺序铺开，已经设置过的行不覆盖
UPDATE public.entry SET sort = id * 10 WHERE sort = 0;
