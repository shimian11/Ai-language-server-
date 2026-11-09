package com.ailang.service.article.mapper;

import com.ailang.service.article.pojo.entity.Article;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/** 文章 Mapper（MyBatis-Plus 通用 CRUD） */
@Mapper
public interface ArticleMapper extends BaseMapper<Article> {
}
