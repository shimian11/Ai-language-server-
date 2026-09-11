-- ============================================================
-- AI 网页设计提示词库 · 初始化脚本（PostgreSQL）
-- 由 compose 的 db-init 服务在 ailang 库中执行（幂等，可重复运行）
-- ============================================================

-- 设计大类（受管分类，仅一级）
CREATE TABLE IF NOT EXISTS category (
  id          BIGSERIAL PRIMARY KEY,
  name        VARCHAR(50)  NOT NULL UNIQUE,
  sort        INT          NOT NULL DEFAULT 0,
  created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 案例
CREATE TABLE IF NOT EXISTS entry (
  id           BIGSERIAL PRIMARY KEY,
  title        VARCHAR(120) NOT NULL,
  summary      VARCHAR(500) NOT NULL DEFAULT '',
  category_id  BIGINT       NOT NULL REFERENCES category(id),
  platform     VARCHAR(10)  NOT NULL DEFAULT 'web',
  style        VARCHAR(50)  NOT NULL,
  prompt       TEXT         NOT NULL,
  html_source  TEXT         NULL,
  tags         VARCHAR(255) NOT NULL DEFAULT '',
  status       VARCHAR(10)  NOT NULL DEFAULT 'DRAFT',
  published_at TIMESTAMP    NULL,
  view_count   BIGINT       NOT NULL DEFAULT 0,
  copy_count   BIGINT       NOT NULL DEFAULT 0,
  created_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 效果图（一案例多图，其一为主图）
CREATE TABLE IF NOT EXISTS entry_image (
  id        BIGSERIAL PRIMARY KEY,
  entry_id  BIGINT       NOT NULL REFERENCES entry(id) ON DELETE CASCADE,
  url       VARCHAR(500) NOT NULL,
  is_main   BOOLEAN      NOT NULL DEFAULT FALSE,
  sort      INT          NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_status_date ON entry (status, published_at DESC);
CREATE INDEX IF NOT EXISTS idx_category ON entry (category_id);
CREATE INDEX IF NOT EXISTS idx_category_platform ON entry (category_id, platform);
CREATE INDEX IF NOT EXISTS idx_entry_image_entry ON entry_image (entry_id);

-- updated_at 自动更新（PG 无 ON UPDATE CURRENT_TIMESTAMP，以触发器实现）
CREATE OR REPLACE FUNCTION set_updated_at() RETURNS trigger AS $$
BEGIN
  NEW.updated_at := CURRENT_TIMESTAMP;
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_category_updated ON category;
CREATE TRIGGER trg_category_updated BEFORE UPDATE ON category
  FOR EACH ROW EXECUTE FUNCTION set_updated_at();

DROP TRIGGER IF EXISTS trg_entry_updated ON entry;
CREATE TRIGGER trg_entry_updated BEFORE UPDATE ON entry
  FOR EACH ROW EXECUTE FUNCTION set_updated_at();

COMMENT ON TABLE category IS '设计大类';
COMMENT ON COLUMN category.name IS '大类名称';
COMMENT ON COLUMN category.sort IS '排序（小者靠前，首页胶囊墙前7）';
COMMENT ON TABLE entry IS '案例';
COMMENT ON COLUMN entry.prompt IS '提示词（Markdown）';
COMMENT ON COLUMN entry.html_source IS 'HTML 源码（可空，与图片至少一项）';
COMMENT ON TABLE entry_image IS '案例效果图';
COMMENT ON COLUMN entry_image.url IS 'MinIO object URL';

-- ============================================================
-- 初始数据（幂等）
-- ============================================================

INSERT INTO category (name, sort)
SELECT v.name, v.sort FROM (VALUES
  ('大屏设计', 1),
  ('轮播', 2),
  ('落地页', 3),
  ('导航菜单', 4)
) AS v(name, sort)
WHERE NOT EXISTS (SELECT 1 FROM category WHERE name = v.name);

INSERT INTO entry (title, summary, category_id, platform, style, prompt, tags, status, published_at)
SELECT '指挥中心主视觉大屏：环形数据围绕核心指标',
       '中央放核心 KPI 大数字，四周环绕趋势图与环形占比，一眼锁定重点。',
       (SELECT id FROM category WHERE name = '大屏设计'),
       'web',
       '中心聚焦型 · HUD',
       '设计一张指挥中心数据大屏，采用中心聚焦型布局：画面正中是一个超大号的核心 KPI 数字（今日综合达成率），四周以三圈同心圆环环绕分布六个卫星指标卡（接入设备、待处理告警、平均响应、今日工单、用户满意度、在线率）。整体深蓝底色，青色与亮蓝作为高亮色，指标卡带细边框与微光效果，风格参考科幻指挥舱 HUD 界面，构图对称、重心稳定，16:9 横版。',
       '数据可视化,深色,HUD', 'PUBLISHED', NOW()
WHERE NOT EXISTS (SELECT 1 FROM entry WHERE title = '指挥中心主视觉大屏：环形数据围绕核心指标');

INSERT INTO entry (title, summary, category_id, platform, style, prompt, tags, status, published_at)
SELECT 'SaaS 产品落地页：左文右图经典分栏',
       '左侧大标题 + 行动按钮 + 信任背书，右侧产品截图带浮动阴影。',
       (SELECT id FROM category WHERE name = '落地页'),
       'web',
       '左文右图分栏',
       '设计一个 SaaS 产品落地页首屏，左右分栏布局：左侧为大标题、副标题、主行动按钮与客户信任背书，右侧为带浮动阴影的产品截图。整体浅色极简风格，留白充足，分栏比例约 5:7。',
       'SaaS,首屏', 'PUBLISHED', NOW()
WHERE NOT EXISTS (SELECT 1 FROM entry WHERE title = 'SaaS 产品落地页：左文右图经典分栏');
