package com.xiaowc.search.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiaowc.search.common.ErrorCode;
import com.xiaowc.search.exception.BusinessException;
import com.xiaowc.search.model.entity.Picture;
import com.xiaowc.search.service.PictureService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 图片服务实现类
 */
@Service
public class PictureServiceImpl implements PictureService {

    /**
     * 通过标题来查询图片，这里并不是从数据库中去查，而是从在线网站中去爬虫，我们这里相当于一个转发的作用
     *
     * 前端访问这个接口，然后这个接口再去通过Jsoup.connect(url).get()访问在线地址得到返回的数据，所以相当于我们写的这个
     *接口相当于转发的作用
     * 方式一：先请求完成，获得渲染后的数据之后，再从这个html文档中获取对应的标签，获取相关的数据
     *   利用jsoup工具包，可以方便解析这个html文档
     * @param searchText 标题
     * @param pageNum 当前在第几页
     * @param pageSize 每页的大小
     * @return
     */
    @Override
    public Page<Picture> searchPicture(String searchText, long pageNum, long pageSize) {
        long current = (pageNum - 1) * pageSize;
        String url = String.format("https://cn.bing.com/images/search?q=%s&first=%s", searchText, current); // 图片的下标，有点像偏移量
        Document doc = null;
        try {
            doc = Jsoup.connect(url).get(); // 连接某个地址，调用get()方法获取html文档数据
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "数据获取异常");
        }
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
            pictures.add(picture); // 将请求到的图片保存下来
            if (pictures.size() >= pageSize) { // 限制爬虫，防止前端每页超过20条数据的请求
                break;
            }
        }
        Page<Picture> picturePage = new Page<>(pageNum, pageSize);
        picturePage.setRecords(pictures); // 当前获取到的数据在第几页
        return picturePage;
    }
}
