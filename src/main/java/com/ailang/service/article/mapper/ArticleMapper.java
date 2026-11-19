package com.ailang.service.article.mapper;

import com.ailang.service.article.pojo.entity.Article;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/** 文章 Mapper（MyBatis-Plus 通用 CRUD） */
@Mapper
public interface ArticleMapper extends BaseMapper<Article> {

    /** 当前最大排序值（新建文章默认追加到末尾） */
    @Select("SELECT COALESCE(MAX(sort), 0) FROM article")
    int maxSort();
}
