package com.xiaowc.search.datasource;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiaowc.search.common.ErrorCode;
import com.xiaowc.search.exception.BusinessException;
import com.xiaowc.search.model.entity.Picture;
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
public class PictureDataSource implements DataSource<Picture> {

    /**
     * 方式一：先请求完成，获得渲染后的数据之后，再从这个html文档中获取对应的标签，获取相关的数据
     *   利用jsoup工具包，可以方便解析这个html文档
     * @param searchText
     * @param pageNum
     * @param pageSize
     * @return
     */
    @Override
    public Page<Picture> doSearch(String searchText, long pageNum, long pageSize) {
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
            pictures.add(picture);
            if (pictures.size() >= pageSize) {
                break;
            }
        }
        Page<Picture> picturePage = new Page<>(pageNum, pageSize);
        picturePage.setRecords(pictures);
        return picturePage;
    }
}
