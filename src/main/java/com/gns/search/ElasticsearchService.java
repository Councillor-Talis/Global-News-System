package com.gns.search;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gns.entity.Article;
import com.gns.mapper.ArticleMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.HighlightQuery;
import org.springframework.data.elasticsearch.core.query.highlight.Highlight;
import org.springframework.data.elasticsearch.core.query.highlight.HighlightField;
import org.springframework.data.elasticsearch.core.query.highlight.HighlightFieldParameters;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ElasticsearchService {

    private final ElasticsearchOperations esOperations;
    private final ArticleRepository articleRepository;
    private final ArticleMapper articleMapper;

    /** 同步单篇文章到 ES */
    public void indexArticle(Article article) {
        if (article == null || article.getStatus() == 0) return;
        try {
            articleRepository.save(toDocument(article));
            log.debug("文章已索引: id={}", article.getId());
        } catch (Exception e) {
            log.error("ES索引失败: id={}, err={}", article.getId(), e.getMessage());
        }
    }

    /** 全量重建索引 */
    public int indexAllArticles() {
        List<Article> articles = articleMapper.selectList(
                new LambdaQueryWrapper<Article>().eq(Article::getStatus, 1)
        );
        if (articles.isEmpty()) return 0;

        List<ArticleDocument> docs = new ArrayList<>();
        for (Article article : articles) {
            docs.add(toDocument(article));
        }
        articleRepository.saveAll(docs);
        log.info("全量索引完成，共 {} 篇", docs.size());
        return docs.size();
    }

    /** 删除文章索引 */
    public void deleteArticle(Long id) {
        try {
            articleRepository.deleteById(id);
        } catch (Exception e) {
            log.warn("ES删除失败: id={}", id);
        }
    }

    /** 全文搜索（标题+摘要+正文，支持高亮） */
    public SearchResult search(String keyword, int page, int size) {
        try {
            // 构造高亮字段
            List<HighlightField> highlightFields = List.of(
                    new HighlightField("title",
                            HighlightFieldParameters.builder()
                                    .withPreTags("<em class='highlight'>")
                                    .withPostTags("</em>")
                                    .withNumberOfFragments(0)
                                    .build()),
                    new HighlightField("summary",
                            HighlightFieldParameters.builder()
                                    .withPreTags("<em class='highlight'>")
                                    .withPostTags("</em>")
                                    .withNumberOfFragments(2)
                                    .withFragmentSize(150)
                                    .build())
            );

            // 构造查询
            NativeQuery query = NativeQuery.builder()
                    .withQuery(q -> q
                            .multiMatch(m -> m
                                    .query(keyword)
                                    .fields("title^3", "summary^2", "content")
                                    .fuzziness("1")
                            )
                    )
                    .withHighlightQuery(
                            new HighlightQuery(new Highlight(highlightFields), ArticleDocument.class)
                    )
                    .withPageable(PageRequest.of(page - 1, size))
                    .build();

            SearchHits<ArticleDocument> hits = esOperations.search(query, ArticleDocument.class);

            long total = hits.getTotalHits();
            List<ArticleDocument> records = new ArrayList<>();

            for (SearchHit<ArticleDocument> hit : hits.getSearchHits()) {
                ArticleDocument doc = hit.getContent();

                // 应用高亮
                List<String> titleHighlight = hit.getHighlightField("title");
                if (!titleHighlight.isEmpty()) {
                    doc.setTitle(titleHighlight.get(0));
                }
                List<String> summaryHighlight = hit.getHighlightField("summary");
                if (!summaryHighlight.isEmpty()) {
                    doc.setSummary(summaryHighlight.get(0));
                }

                records.add(doc);
            }

            log.info("ES搜索: keyword={}, 命中 {} 条", keyword, total);
            return new SearchResult(total, page, size, records);

        } catch (Exception e) {
            log.error("ES搜索失败: keyword={}, err={}", keyword, e.getMessage());
            return new SearchResult(0, page, size, Collections.emptyList());
        }
    }

    /** Article → ArticleDocument */
    private ArticleDocument toDocument(Article article) {
        ArticleDocument doc = new ArticleDocument();
        BeanUtils.copyProperties(article, doc);
        if (article.getPubTime() != null) {
            doc.setPubTime(article.getPubTime().toString().replace("T", " "));
        }
        // content 太大时截断，避免ES索引过慢
        if (doc.getContent() != null && doc.getContent().length() > 10000) {
            doc.setContent(doc.getContent().substring(0, 10000));
        }
        return doc;
    }
}