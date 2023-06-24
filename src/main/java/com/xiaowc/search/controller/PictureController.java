package com.xiaowc.search.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiaowc.search.common.BaseResponse;
import com.xiaowc.search.common.ErrorCode;
import com.xiaowc.search.common.ResultUtils;
import com.xiaowc.search.exception.ThrowUtils;
import com.xiaowc.search.model.dto.picture.PictureQueryRequest;
import com.xiaowc.search.model.entity.Picture;
import com.xiaowc.search.service.PictureService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 图片接口
 */
@RestController
@RequestMapping("/picture")
@Slf4j
public class PictureController {

    @Resource
    private PictureService pictureService;

    /**
     * 通过标题来查询图片，这里并不是从数据库中去查，而是从在线网站中去爬虫，我们这里相当于一个转发的作用
     * 分页获取图片列表（封装类）
     *  通过标题来查询图片
     *  前端访问这个接口，然后这个接口再去通过Jsoup.connect(url).get()访问在线地址得到返回的数据返回给前端，
     * 所以相当于我们写的这个接口相当于转发的作用
     * @param pictureQueryRequest 图片查询请求类
     * @param request
     * @return
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<Picture>> listPictureByPage(@RequestBody PictureQueryRequest pictureQueryRequest,
                                                        HttpServletRequest request) {
        long current = pictureQueryRequest.getCurrent(); // 当前页号
        long size = pictureQueryRequest.getPageSize(); // 页面大小
        // 限制爬虫，防止前端每页超过20条数据的请求
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        String searchText = pictureQueryRequest.getSearchText();
        Page<Picture> picturePage = pictureService.searchPicture(searchText, current, size); //通过标题来查询图片，包含了分页信息
        return ResultUtils.success(picturePage);
    }


}
