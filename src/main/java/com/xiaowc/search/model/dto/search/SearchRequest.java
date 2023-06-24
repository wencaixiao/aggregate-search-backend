package com.xiaowc.search.model.dto.search;

import com.xiaowc.search.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 查询请求
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SearchRequest extends PageRequest implements Serializable {

    /**
     * 搜索关键词
     */
    private String searchText;

    /**
     * 搜索类型（post/user/picture/video）
     */
    private String type;

    private static final long serialVersionUID = 1L;
}