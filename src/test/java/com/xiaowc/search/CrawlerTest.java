package com.xiaowc.search;

import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.xiaowc.search.model.entity.Picture;
import com.xiaowc.search.model.entity.Post;
import com.xiaowc.search.service.PostService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SpringBootTest
public class CrawlerTest {

    @Resource
    private PostService postService;

    /**
     * 方式一：先请求完成，获得渲染后的数据之后，再从这个html文档中获取对应的标签，获取相关的数据
     *   利用jsoup工具包，可以方便解析这个html文档
     * @throws IOException
     */
    @Test
    void testFetchPicture() throws IOException {
        int current = 1;
        String url = "https://cn.bing.com/images/search?q=小黑子&first=" + current; // 图片的下标，有点像偏移量
        Document doc = Jsoup.connect(url).get(); // 连接某个地址，调用get()方法获取html文档数据
        Elements elements = doc.select(".iuscp.isv"); // 用css选择器将图片地址拿到
        List<Picture> pictures = new ArrayList<>();
        for (Element element : elements) {
            // 取图片地址（murl）
            String m = element.select(".iusc").get(0).attr("m"); // 取出来是一个JSON字符串
            Map<String, Object> map = JSONUtil.toBean(m, Map.class); // 将JSON字符串解析成对象
            String murl = (String) map.get("murl");
//            System.out.println(murl);
            // 取标题
            String title = element.select(".inflnk").get(0).attr("aria-label");
//            System.out.println(title);
            Picture picture = new Picture();
            picture.setTitle(title); // 图片的标题
            picture.setUrl(murl); // 图片的地址
            pictures.add(picture);
        }
        System.out.println(pictures);
    }

    /**
     * 方式二：利用hutool工具包向网站发送http请求，从而得到数据
     * 爬虫：获取网站中的数据，并将其保存到数据库中
     */
    @Test
    void testFetchPassage() {
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
        Assertions.assertTrue(b); // 断言
    }
}
