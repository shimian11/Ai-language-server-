-- ============================================================
-- 03 博客文章与公司别名（2026-11 改版新增；幂等，可重复执行）
-- article：心得/实习/面试三类内容（type 区分）
-- company：实习公司别名（脱敏），支撑前台两级时间线
-- ============================================================

-- updated_at 自动维护函数（与 01 保持一致；此处兜底重建，保证 03 可单独执行）
CREATE OR REPLACE FUNCTION set_updated_at() RETURNS trigger AS $$
BEGIN
  NEW.updated_at = NOW();
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- 公司别名表：key 为稳定标识（前台时间线分组用），name 为脱敏显示名
CREATE TABLE IF NOT EXISTS company (
  id         BIGSERIAL PRIMARY KEY,
  key        VARCHAR(64)  NOT NULL UNIQUE,
  name       VARCHAR(128) NOT NULL,
  meta       VARCHAR(128) DEFAULT '',
  milestone  BOOLEAN      DEFAULT FALSE,
  sort       INT          DEFAULT 0,
  created_at TIMESTAMP    DEFAULT NOW(),
  updated_at TIMESTAMP    DEFAULT NOW()
);

-- 文章表：type 区分三类；topic 为栏目；company_id 仅实习记录使用
CREATE TABLE IF NOT EXISTS article (
  id            BIGSERIAL PRIMARY KEY,
  type          VARCHAR(16)  NOT NULL CHECK (type IN ('note','internship','interview')),
  topic         VARCHAR(16)  DEFAULT '',
  sub           VARCHAR(64)  DEFAULT '',
  title         VARCHAR(200) NOT NULL,
  summary       VARCHAR(500) DEFAULT '',
  body          TEXT         DEFAULT '',
  tags          VARCHAR(200) DEFAULT '',
  company_id    BIGINT       REFERENCES company(id) ON DELETE SET NULL,
  display_date  VARCHAR(16)  DEFAULT '',
  result        VARCHAR(16)  DEFAULT '',
  result_class  VARCHAR(8)   DEFAULT '',
  read_label    VARCHAR(16)  DEFAULT '',
  status        VARCHAR(16)  NOT NULL DEFAULT 'DRAFT' CHECK (status IN ('DRAFT','PUBLISHED')),
  published_at  TIMESTAMP,
  created_at    TIMESTAMP    DEFAULT NOW(),
  updated_at    TIMESTAMP    DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_article_type    ON article(type);
CREATE INDEX IF NOT EXISTS idx_article_topic   ON article(topic);
CREATE INDEX IF NOT EXISTS idx_article_company ON article(company_id);
CREATE INDEX IF NOT EXISTS idx_article_status  ON article(status);

-- updated_at 触发器（先删后建，保证幂等）
DROP TRIGGER IF EXISTS trg_article_updated_at ON article;
CREATE TRIGGER trg_article_updated_at BEFORE UPDATE ON article
  FOR EACH ROW EXECUTE FUNCTION set_updated_at();
DROP TRIGGER IF EXISTS trg_company_updated_at ON company;
CREATE TRIGGER trg_company_updated_at BEFORE UPDATE ON company
  FOR EACH ROW EXECUTE FUNCTION set_updated_at();
