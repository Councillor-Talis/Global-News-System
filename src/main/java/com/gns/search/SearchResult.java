package com.gns.search;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

/** ES 搜索结果封装 */
@Data
@AllArgsConstructor
public class SearchResult {
    private long total;
    private int page;
    private int size;
    private List<ArticleDocument> records;
}