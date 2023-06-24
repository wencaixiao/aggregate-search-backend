package com.xiaowc.search.controller;

import com.xiaowc.search.common.BaseResponse;
import com.xiaowc.search.common.ResultUtils;
import com.xiaowc.search.manager.SearchFacade;
import com.xiaowc.search.model.dto.search.SearchRequest;
import com.xiaowc.search.model.vo.SearchVO;
import com.xiaowc.search.service.PictureService;
import com.xiaowc.search.service.PostService;
import com.xiaowc.search.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 图片接口
 */
@RestController
@RequestMapping("/search")
@Slf4j
public class SearchController {

    @Resource
    private UserService userService;

    @Resource
    private PostService postService;

    @Resource
    private PictureService pictureService;

    @Resource
    private SearchFacade searchFacade;

    /**
     * 查询所有的接口（user、passage、post）
     *   前端只传一个请求就可以查询所有的接口
     *
     * @param searchRequest 查询请求
     * @param request
     * @return
     */
    @PostMapping("/all")
    public BaseResponse<SearchVO> searchAll(@RequestBody SearchRequest searchRequest, HttpServletRequest request) {
        return ResultUtils.success(searchFacade.searchAll(searchRequest, request));
    }

}
