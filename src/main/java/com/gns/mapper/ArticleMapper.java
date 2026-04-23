package com.gns.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gns.entity.Article;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import java.util.List;

@Mapper
public interface ArticleMapper extends BaseMapper<Article> {

    @Update("UPDATE t_article SET view_count = view_count + 1 WHERE id = #{id}")
    void incrementViewCount(Long id);

    @Select("SELECT COUNT(*) FROM t_article WHERE DATE(created_at) = CURDATE()")
    Long countToday();

    @Select("SELECT COALESCE(SUM(view_count), 0) FROM t_article WHERE status = 1")
    Long totalViewCount();

    // 热点文章：浏览量前5
    @Select("SELECT * FROM t_article WHERE status = 1 ORDER BY view_count DESC LIMIT 5")
    List<Article> selectTop5ByViewCount();
}