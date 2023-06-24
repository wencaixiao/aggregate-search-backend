package com.xiaowc.search.esdao;

import com.xiaowc.search.model.dto.post.PostEsDTO;
import com.xiaowc.search.model.dto.post.PostQueryRequest;
import com.xiaowc.search.model.entity.Post;
import com.xiaowc.search.service.PostService;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import javax.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

/**
 * 帖子 ES 操作测试
 *  ES的增删改查：
 *   第一种方式：ElasticsearchRepository，默认提供了简单的增删改查，多用于可预期的、相对没那么复杂的查询、自定义查询
 *              返回结果简单直接
 *   第二种方式：Spring默认给我们提供的操作es的客户端对象ElasticsearchRestTemplate，也提供了增删改查，他的增删改查更
 *              灵活，适用于更复杂的操作，返回结果更完整，但需要自己解析
 */
@SpringBootTest
public class PostEsDaoTest {

    /**
     * ES的增删改查：
     *  第一种方式：ElasticsearchRepository，默认提供了简单的增删改查，多用于可预期的、相对没那么复杂的查询、自定义查询
     *  postEsDao继承了ElasticsearchRepository
     */
    @Resource
    private PostEsDao postEsDao;

    @Resource
    private PostService postService;

    @Test
    void test() {
        PostQueryRequest postQueryRequest = new PostQueryRequest();
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Post> page =
                postService.searchFromEs(postQueryRequest);
        System.out.println(page);
    }

    /**
     * 自带的查询
     *  第一种方式：ElasticsearchRepository，默认提供了简单的增删改查
     */
    @Test
    void testSelect() {
        System.out.println(postEsDao.count());
        Page<PostEsDTO> PostPage = postEsDao.findAll(  // 分页查询，并且进行排序
                PageRequest.of(0, 5, Sort.by("createTime")));
        List<PostEsDTO> postList = PostPage.getContent();
        Optional<PostEsDTO> byId = postEsDao.findById(1L);
        System.out.println(byId);
        System.out.println(postList);
    }

    /**
     * 自带的增
     *  第一种方式：ElasticsearchRepository，默认提供了简单的增删改查
     */
    @Test
    void testAdd() {
        PostEsDTO postEsDTO = new PostEsDTO();
        postEsDTO.setId(1L);
        postEsDTO.setTitle("xiaowc是小黑子");
        postEsDTO.setContent("xiaowc的GitHub：https://github.com/wencaixiao");
        postEsDTO.setTags(Arrays.asList("java", "python"));
        postEsDTO.setUserId(1L);
        postEsDTO.setCreateTime(new Date());
        postEsDTO.setUpdateTime(new Date());
        postEsDTO.setIsDelete(0);
        postEsDao.save(postEsDTO);
        System.out.println(postEsDTO.getId());
    }

    /**
     * 自带的根据id进行查询
     *  第一种方式：ElasticsearchRepository，默认提供了简单的增删改查
     */
    @Test
    void testFindById() {
        Optional<PostEsDTO> postEsDTO = postEsDao.findById(1L);
        System.out.println(postEsDTO);
    }

    /**
     * 自带的查询数量
     *  ElasticsearchRepository，默认提供了简单的增删改查
     */
    @Test
    void testCount() {
        System.out.println(postEsDao.count());
    }

    /**
     * 自定义查询：根据userId来查询
     *  ElasticsearchRepository，默认提供了简单的增删改查
     */
    @Test
    void testFindByCategory() {
        List<PostEsDTO> postEsDaoTestList = postEsDao.findByUserId(1L);
        System.out.println(postEsDaoTestList);
    }

    /**
     * 自定义查询：根据title来查询
     *  ElasticsearchRepository，默认提供了简单的增删改查
     */
    @Test
    void testFindByTitle() {
        List<PostEsDTO> postEsDTOS = postEsDao.findByTitle("小黑");
        System.out.println(postEsDTOS);
    }
}
