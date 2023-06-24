package com.xiaowc.search.manager;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiaowc.search.common.ErrorCode;
import com.xiaowc.search.datasource.*;
import com.xiaowc.search.exception.BusinessException;
import com.xiaowc.search.exception.ThrowUtils;
import com.xiaowc.search.model.dto.post.PostQueryRequest;
import com.xiaowc.search.model.dto.search.SearchRequest;
import com.xiaowc.search.model.dto.user.UserQueryRequest;
import com.xiaowc.search.model.entity.Picture;
import com.xiaowc.search.model.enums.SearchTypeEnum;
import com.xiaowc.search.model.vo.PostVO;
import com.xiaowc.search.model.vo.SearchVO;
import com.xiaowc.search.model.vo.UserVO;
import com.xiaowc.search.service.PictureService;
import com.xiaowc.search.service.PostService;
import com.xiaowc.search.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 搜索门面
 */
@Component
@Slf4j
public class SearchFacade {

    @Resource
    private PostService postService;

    @Resource
    private UserService userService;

    @Resource
    private PictureService pictureService;

    @Resource
    private PostDataSource postDataSource;

    @Resource
    private UserDataSource userDataSource;

    @Resource
    private PictureDataSource pictureDataSource;

    @Resource
    private DataSourceRegistry dataSourceRegistry;

    /**
     * 查询所有的接口（user、passage、post、video）
     *   前端只传一个请求就可以查询所有的接口
     * 这里用到了门面模式和适配器模式
     *
     * @param searchRequest
     * @param request
     * @return 非controller中不用在封装一个BaseResponse了，我们在SearchController中去封装BaseResponse就可以了
     */
    public SearchVO searchAll(@RequestBody SearchRequest searchRequest, HttpServletRequest request) {
        String type = searchRequest.getType();
        SearchTypeEnum searchTypeEnum = SearchTypeEnum.getEnumByValue(type); // 搜索的类型（post/user/picture/video）
        ThrowUtils.throwIf(StringUtils.isBlank(type), ErrorCode.PARAMS_ERROR);
        String searchText = searchRequest.getSearchText(); // 得到要查询的关键字
        long current = searchRequest.getCurrent();
        long pageSize = searchRequest.getPageSize();
        // 如果没有传搜索的类型，那么就异步搜索出所有数据
        // 这里用到了门面模式和适配器模式
        if (searchTypeEnum == null) {
//            // 搜索出用户数据
//            CompletableFuture<Page<UserVO>> userTask = CompletableFuture.supplyAsync(() -> {
//                UserQueryRequest userQueryRequest = new UserQueryRequest();
//                userQueryRequest.setUserName(searchText);
//                Page<UserVO> userVOPage = userDataSource.doSearch(searchText, current, pageSize);
//                return userVOPage;
//            });
//            //搜索出帖子数据
//            CompletableFuture<Page<PostVO>> postTask = CompletableFuture.supplyAsync(() -> {
//                PostQueryRequest postQueryRequest = new PostQueryRequest();
//                postQueryRequest.setSearchText(searchText);
//                Page<PostVO> postVOPage = postDataSource.doSearch(searchText, current, pageSize);
//                return postVOPage;
//            });
//            //搜索出图片数据
//            CompletableFuture<Page<Picture>> pictureTask = CompletableFuture.supplyAsync(() -> {
//                Page<Picture> picturePage = pictureDataSource.doSearch(searchText, 1, 10);
//                return picturePage;
//            });
//            // allOf()方法用来并行运行多个CompletableFuture，等到所有的CompletableFuture都运行完之后再返回
//            // join()方法是阻塞调用他们的线程（通常为主线程）来获取CompletableFuture异步之后的返回值
//            CompletableFuture.allOf(userTask, postTask, pictureTask).join(); // 这里相当于阻塞，只有上面三个全部执行完之后才会往下执行

            // 不是异步搜索
            // 搜索出用户数据
            UserQueryRequest userQueryRequest = new UserQueryRequest();
            userQueryRequest.setUserName(searchText);
            Page<UserVO> userVOPage = userDataSource.doSearch(searchText, current, pageSize);
            // 搜索出帖子数据
            PostQueryRequest postQueryRequest = new PostQueryRequest();
            postQueryRequest.setSearchText(searchText);
            Page<PostVO> postVOPage = postDataSource.doSearch(searchText, current, pageSize);
            // 搜索出图片数据
            Page<Picture> picturePage = pictureDataSource.doSearch(searchText, 1, 10);

            try {
//                // 得到异步搜索出来的数据，也就是拿到异步搜索的返回值
//                Page<UserVO> userVOPage = userTask.get();
//                Page<PostVO> postVOPage = postTask.get();
//                Page<Picture> picturePage = pictureTask.get();

                // 将这些数据设置给searchVO并返回给前端去展示
                SearchVO searchVO = new SearchVO();
                searchVO.setUserList(userVOPage.getRecords());
                searchVO.setPostList(postVOPage.getRecords());
                searchVO.setPictureList(picturePage.getRecords());
                return searchVO; // 返回给前端
            } catch (Exception e) {
                log.error("查询异常", e);
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "查询异常");
            }
        } else { // 单一搜索，如果传了类型，那么就根据数据源搜索出该类型的数据，而不会搜索出所有数据
            SearchVO searchVO = new SearchVO();
            // 这样改代码会很麻烦，比如我们要新增加一个数据源，那么我们就要在做如下改变：
            //  1.新增一个SerchTypeEnum枚举值Video
            //  2.新增一个VideoService
            //  3.新增一个case VIDEO
            // 看起来没有问题，这是因为我们的代码比较规范，如果业务很复杂的话，就不一定这么规范了
//            switch (searchTypeEnum) {
//                case POST:
//                    PostQueryRequest postQueryRequest = new PostQueryRequest();
//                    postQueryRequest.setSearchText(searchText);
//                    Page<PostVO> postVOPage = postService.listPostVOByPage(postQueryRequest, request);
//                    searchVO.setPostList(postVOPage.getRecords());
//                    break;
//                case USER:
//                    UserQueryRequest userQueryRequest = new UserQueryRequest();
//                    userQueryRequest.setUserName(searchText);
//                    Page<UserVO> userVOPage = userService.listUserVOByPage(userQueryRequest);
//                    searchVO.setUserList(userVOPage.getRecords());
//                    break;
//                case PICTURE:
//                    Page<Picture> picturePage = pictureService.searchPicture(searchText, 1, 10);
//                    searchVO.setPictureList(picturePage.getRecords());
//                    break;
//                default:
//            }
            // 改进：这里用到了注册器模式，也是一种单例模式，对上面进行优化
            DataSource<?> dataSource = dataSourceRegistry.getDataSourceByType(type); // 单一搜索，根据type获取数据源
            Page<?> page = dataSource.doSearch(searchText, current, pageSize); // 执行数据源中的搜索方法
            searchVO.setDataList(page.getRecords());
            return searchVO; // 返回给前端
        }
    }
}
