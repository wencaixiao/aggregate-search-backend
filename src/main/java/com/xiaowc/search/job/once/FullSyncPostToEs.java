package com.xiaowc.search.job.once;

import com.xiaowc.search.esdao.PostEsDao;
import com.xiaowc.search.model.dto.post.PostEsDTO;
import com.xiaowc.search.model.entity.Post;
import com.xiaowc.search.service.PostService;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * 全量同步帖子到 es
 */
// 添加@Component注释后，每次启动 springboot 项目时会执行一次 run 方法，不加@Component就不起作用
@Component
@Slf4j
public class FullSyncPostToEs implements CommandLineRunner {

    @Resource
    private PostService postService;

    @Resource
    private PostEsDao postEsDao;

    /**
     * 全量同步：将数据库中的所有数据查询出来，同步到es中去
     *   如果发现有一样的id，则es不会插入相同的数据，只是去更新这个数据
     * @param args
     */
    @Override
    public void run(String... args) {
        List<Post> postList = postService.list(); // 将数据库中全部的数据查询出来，然后同步到es中去
        if (CollectionUtils.isEmpty(postList)) {
            return;
        }
        // 将帖子转成包装类(就是对象es中的字段)
        List<PostEsDTO> postEsDTOList = postList.stream().map(PostEsDTO::objToDto).collect(Collectors.toList());
        final int pageSize = 500; // 每次同步的数量
        int total = postEsDTOList.size(); // 需要往es中同步数据的总数
        log.info("FullSyncPostToEs start, total {}", total); // 开始同步日志
        for (int i = 0; i < total; i += pageSize) {
            int end = Math.min(i + pageSize, total);
            log.info("sync from {} to {}", i, end);
            // postEsDTOList.subList(i, end)表示将list中索引为[i,end)中的数据取出
            postEsDao.saveAll(postEsDTOList.subList(i, end)); // 将从数据库中查询出来的数据同步到es中去
        }
        log.info("FullSyncPostToEs end, total {}", total); // 同步完成日志
    }
}
