package com.xiaowc.search.model.vo;

import com.xiaowc.search.model.entity.Picture;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 聚合搜索，返回给前端的内容
 *
 *  把要返回给前端的user/post/picture数据放到一个对象中去，这样前端就可以从一个对象中拿到不同的列表
 */
@Data
public class SearchVO implements Serializable {

    /**
     * 搜索出来的用户
     */
    private List<UserVO> userList;

    /**
     * 搜索出来的帖子列表
     */
    private List<PostVO> postList;

    /**
     * 搜索出来的图片列表
     */
    private List<Picture> pictureList;

    /**
     * 通用的数据源，要取单一数据的时候，无论是用户、图片还是帖子，都从这个变量中去取
     */
    private List<?> dataList;

    private static final long serialVersionUID = 1L;

}
