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

-- 案例（一个案例 = 一条记录；同一大类下可录入多条案例）
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

-- 说明：初始示例数据已按需求移除（改为由管理员在后台自行录入）。
--       如需默认分类/示例案例，可在下方以幂等 INSERT ... WHERE NOT EXISTS 补充。
