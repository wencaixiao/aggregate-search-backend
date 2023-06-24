package com.xiaowc.search.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiaowc.search.model.entity.Picture;

/**
 * 图片服务
 */
public interface PictureService {

    Page<Picture> searchPicture(String searchText, long pageNum, long pageSize);
}
