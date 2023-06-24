package com.xiaowc.search.datasource;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiaowc.search.model.dto.post.PostQueryRequest;
import com.xiaowc.search.model.entity.Post;
import com.xiaowc.search.model.vo.PostVO;
import com.xiaowc.search.service.PostService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 帖子服务实现
 */
@Service
@Slf4j
public class PostDataSource implements DataSource<PostVO> {

    @Resource
    private PostService postService;

    /**
     * 适配器模式，将searchText和postQueryRequest进行关联
     *   将searchText/pageNum/pageSize设置到postQueryRequest中去，再调用postService中的查询方法（传入postQueryRequest参数）去查询
     * @param searchText
     * @param pageNum
     * @param pageSize
     * @return
     */
    @Override
    public Page<PostVO> doSearch(String searchText, long pageNum, long pageSize) {
        PostQueryRequest postQueryRequest = new PostQueryRequest();
        postQueryRequest.setSearchText(searchText);
        postQueryRequest.setCurrent(pageNum);
        postQueryRequest.setPageSize(pageSize);
        ServletRequestAttributes servletRequestAttributes =  (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = servletRequestAttributes.getRequest();
        Page<Post> postPage = postService.searchFromEs(postQueryRequest); // 从es中去查询
        return postService.getPostVOPage(postPage, request);
    }
}




