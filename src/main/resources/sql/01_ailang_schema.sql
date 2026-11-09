-- ailang 库全量表结构（category/entry/entry_image）
-- 导出时间: 2026-10-13，来源: 生产 wheretogo-postgres 容器（PG 16.14）
-- 恢复方式: psql -U <用户> -d <库名> -f 本文件（先 01 后 02）
--
-- PostgreSQL database dump
--

\restrict c0n32sLkxd4g0fXKDTYl0w5su7Oiy1bm0Aw3UtEu4YurtIApggdMYknNneedjSC

-- Dumped from database version 16.14 (Debian 16.14-1.pgdg12+1)
-- Dumped by pg_dump version 16.14 (Debian 16.14-1.pgdg12+1)

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: set_updated_at(); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.set_updated_at() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
  NEW.updated_at := CURRENT_TIMESTAMP;
  RETURN NEW;
END;
$$;


SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: category; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.category (
    id bigint NOT NULL,
    name character varying(50) NOT NULL,
    sort integer DEFAULT 0 NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


--
-- Name: TABLE category; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.category IS '设计大类';


--
-- Name: COLUMN category.name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.category.name IS '大类名称';


--
-- Name: COLUMN category.sort; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.category.sort IS '排序（小者靠前，首页胶囊墙前7）';


--
-- Name: category_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.category_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: category_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.category_id_seq OWNED BY public.category.id;


--
-- Name: entry; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.entry (
    id bigint NOT NULL,
    title character varying(120) NOT NULL,
    summary character varying(500) DEFAULT ''::character varying NOT NULL,
    category_id bigint NOT NULL,
    style character varying(50) NOT NULL,
    prompt text NOT NULL,
    html_source text,
    tags character varying(255) DEFAULT ''::character varying NOT NULL,
    status character varying(10) DEFAULT 'DRAFT'::character varying NOT NULL,
    published_at timestamp without time zone,
    view_count bigint DEFAULT 0 NOT NULL,
    copy_count bigint DEFAULT 0 NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    platform character varying(10) DEFAULT 'web'::character varying NOT NULL
);


--
-- Name: TABLE entry; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.entry IS '案例';


--
-- Name: COLUMN entry.prompt; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.entry.prompt IS '提示词（Markdown）';


--
-- Name: COLUMN entry.html_source; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.entry.html_source IS 'HTML 源码（可空，与图片至少一项）';


--
-- Name: entry_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.entry_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: entry_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.entry_id_seq OWNED BY public.entry.id;


--
-- Name: entry_image; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.entry_image (
    id bigint NOT NULL,
    entry_id bigint NOT NULL,
    url character varying(500) NOT NULL,
    is_main boolean DEFAULT false NOT NULL,
    sort integer DEFAULT 0 NOT NULL
);


--
-- Name: TABLE entry_image; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.entry_image IS '案例效果图';


--
-- Name: COLUMN entry_image.url; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.entry_image.url IS 'MinIO object URL';


--
-- Name: entry_image_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.entry_image_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: entry_image_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.entry_image_id_seq OWNED BY public.entry_image.id;


--
-- Name: category id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.category ALTER COLUMN id SET DEFAULT nextval('public.category_id_seq'::regclass);


--
-- Name: entry id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.entry ALTER COLUMN id SET DEFAULT nextval('public.entry_id_seq'::regclass);


--
-- Name: entry_image id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.entry_image ALTER COLUMN id SET DEFAULT nextval('public.entry_image_id_seq'::regclass);


--
-- Name: category category_name_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.category
    ADD CONSTRAINT category_name_key UNIQUE (name);


--
-- Name: category category_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.category
    ADD CONSTRAINT category_pkey PRIMARY KEY (id);


--
-- Name: entry_image entry_image_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.entry_image
    ADD CONSTRAINT entry_image_pkey PRIMARY KEY (id);


--
-- Name: entry entry_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.entry
    ADD CONSTRAINT entry_pkey PRIMARY KEY (id);


--
-- Name: idx_category; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_category ON public.entry USING btree (category_id);


--
-- Name: idx_category_platform; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_category_platform ON public.entry USING btree (category_id, platform);


--
-- Name: idx_entry_image_entry; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_entry_image_entry ON public.entry_image USING btree (entry_id);


--
-- Name: idx_status_date; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_status_date ON public.entry USING btree (status, published_at DESC);


--
-- Name: category trg_category_updated; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trg_category_updated BEFORE UPDATE ON public.category FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();


--
-- Name: entry trg_entry_updated; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trg_entry_updated BEFORE UPDATE ON public.entry FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();


--
-- Name: entry entry_category_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.entry
    ADD CONSTRAINT entry_category_id_fkey FOREIGN KEY (category_id) REFERENCES public.category(id);


--
-- Name: entry_image entry_image_entry_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.entry_image
    ADD CONSTRAINT entry_image_entry_id_fkey FOREIGN KEY (entry_id) REFERENCES public.entry(id) ON DELETE CASCADE;


--
-- PostgreSQL database dump complete
--

\unrestrict c0n32sLkxd4g0fXKDTYl0w5su7Oiy1bm0Aw3UtEu4YurtIApggdMYknNneedjSC

