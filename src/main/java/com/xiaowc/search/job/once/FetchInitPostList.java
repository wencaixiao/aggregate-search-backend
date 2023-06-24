package com.xiaowc.search.job.once;

import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.xiaowc.search.model.entity.Post;
import com.xiaowc.search.service.PostService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 爬虫：获取初始帖子列表，直接利用Hutool工具包向第三方网站发送http请求获取资源
 */
// 添加@Component注释后，每次启动 springboot 项目时会执行一次 run 方法，不加@Component就不起作用
//@Component
@Slf4j
public class FetchInitPostList implements CommandLineRunner {

    @Resource
    private PostService postService;

    /**
     * 爬虫：获取网站中的数据，并将其保存到数据库中
     * 利用hutool工具包向网站发送http请求，从而得到数据
     */
    @Override
    public void run(String... args) {
        // 1. 获取数据
        String json = "{\"current\":1,\"pageSize\":8,\"sortField\":\"createTime\",\"sortOrder\":\"descend\",\"category\":\"文章\",\"reviewStatus\":1}";
        String url = "https://www.code-nav.cn/api/post/search/page/vo";
        String result = HttpRequest  // 利用hutool工具包发送请求得到返回的数据
                .post(url)
                .body(json)
                .execute()
                .body();
//        System.out.println(result);
        // 2. 返回的是json数据，需要将json转对象保存到数据库中去
        Map<String, Object> map = JSONUtil.toBean(result, Map.class);
        JSONObject data = (JSONObject) map.get("data");
        JSONArray records = (JSONArray) data.get("records");
        List<Post> postList = new ArrayList<>(); //帖子列表
        // 将爬出来的数据放入到post中去，保存到数据库中去，可以对爬出来的数据进行查看，再一步一步调试取出
        for (Object record : records) {
            JSONObject tempRecord = (JSONObject) record;
            Post post = new Post(); //新建一个帖子
            post.setTitle(tempRecord.getStr("title"));
            post.setContent(tempRecord.getStr("content"));
            JSONArray tags = (JSONArray) tempRecord.get("tags");
            List<String> tagList = tags.toList(String.class);
            post.setTags(JSONUtil.toJsonStr(tagList));
            post.setUserId(1L);
            postList.add(post);
        }
//        System.out.println(postList);
        // 3. 数据入库
        boolean b = postService.saveBatch(postList); // 插入到数据库中去
        if (b) {
            log.info("获取初始化帖子列表成功，条数 = {}", postList.size());
        } else {
            log.error("获取初始化帖子列表失败");
        }
    }
}
