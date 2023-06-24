package com.xiaowc.search.job.cycle;

import com.xiaowc.search.esdao.PostEsDao;
import com.xiaowc.search.mapper.PostMapper;
import com.xiaowc.search.model.dto.post.PostEsDTO;
import com.xiaowc.search.model.entity.Post;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * 增量同步帖子数据到 es
 */
// todo 取消注释开启任务
//@Component
@Slf4j
public class IncSyncPostToEs {

    @Resource
    private PostMapper postMapper;

    @Resource
    private PostEsDao postEsDao;

    /**
     * 定时任务：每分钟执行一次，将数据库中变化的数据同步到es中去
     *
     * 优点：简单易懂、占用时间少、不用引入第三方中间件
     * 缺点：有时间差
     * 应用场景：数据短时间内不同步影响不大、或者数据几乎不发生修改
     */
    @Scheduled(fixedRate = 60 * 1000)
    public void run() {
        // 查询近 5 分钟内的数据
        Date fiveMinutesAgoDate = new Date(new Date().getTime() - 5 * 60 * 1000L);
        List<Post> postList = postMapper.listPostWithDelete(fiveMinutesAgoDate); // 在数据库中以updateTime为条件查询
        if (CollectionUtils.isEmpty(postList)) {
            log.info("no inc post");
            return;
        }
        List<PostEsDTO> postEsDTOList = postList.stream()
                .map(PostEsDTO::objToDto)
                .collect(Collectors.toList()); // 将帖子转成包装类(就是对象es中的字段)
        final int pageSize = 500;
        int total = postEsDTOList.size();
        log.info("IncSyncPostToEs start, total {}", total);
        for (int i = 0; i < total; i += pageSize) {
            int end = Math.min(i + pageSize, total);
            log.info("sync from {} to {}", i, end);
            postEsDao.saveAll(postEsDTOList.subList(i, end)); // 将从数据库中查询出来的数据同步到es中去
        }
        log.info("IncSyncPostToEs end, total {}", total);
    }
}
