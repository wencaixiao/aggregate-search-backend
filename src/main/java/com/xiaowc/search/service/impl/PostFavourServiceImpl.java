package com.xiaowc.search.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaowc.search.common.ErrorCode;
import com.xiaowc.search.exception.BusinessException;
import com.xiaowc.search.mapper.PostFavourMapper;
import com.xiaowc.search.model.entity.Post;
import com.xiaowc.search.model.entity.PostFavour;
import com.xiaowc.search.model.entity.User;
import com.xiaowc.search.service.PostFavourService;
import com.xiaowc.search.service.PostService;
import javax.annotation.Resource;
import org.springframework.aop.framework.AopContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 帖子收藏服务实现
 */
@Service
public class PostFavourServiceImpl extends ServiceImpl<PostFavourMapper, PostFavour>
        implements PostFavourService {

    @Resource
    private PostService postService;

    /**
     * 帖子收藏
     *
     * @param postId
     * @param loginUser
     * @return
     */
    @Override
    public int doPostFavour(long postId, User loginUser) {
        // 判断是否存在
        Post post = postService.getById(postId);
        if (post == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        // 是否已帖子收藏
        long userId = loginUser.getId();
        // 每个用户串行帖子收藏
        // 锁必须要包裹住事务方法
        PostFavourService postFavourService = (PostFavourService) AopContext.currentProxy();
        // String类重写了equals和hashcode方法，因此不同线程传进来的userAccount值相同，但是不属于同一个对象
        // 如果常量池中存在当前字符串, 就会直接返回当前字符串，如果常量池中没有此字符串，会将此字符串放入常量池中后, 再返回
        // 所以每次返回的是同一个对象
        synchronized (String.valueOf(userId).intern()) {
            return postFavourService.doPostFavourInner(userId, postId);
        }
    }

    /**
     * 分页获取用户收藏的帖子列表
     *  select postId from post_favour where userId = #{favourUserId}表示从post_favour表中查询自己收藏的postId
     *  再利用这个postId去从post表中查询完整的帖子post，相当于联表查询
     *  ${ew.customSqlSegment}对应@Param(Constants.WRAPPER) Wrapper queryWrapper这个查询条件
     *      select p.*
     *      from post p
     *      join (select postId from post_favour where userId = #{favourUserId}) pf
     *      on p.id = pf.postId ${ew.customSqlSegment}
     *
     * @param page
     * @param queryWrapper
     * @param favourUserId
     * @return
     */
    @Override
    public Page<Post> listFavourPostByPage(IPage<Post> page, Wrapper<Post> queryWrapper, long favourUserId) {
        if (favourUserId <= 0) {
            return new Page<>();
        }
        return baseMapper.listFavourPostByPage(page, queryWrapper, favourUserId); // 自己写的SQL
    }

    /**
     * 封装了事务的方法
     *
     * @param userId
     * @param postId
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)  // post和post_favour要么同时更新成功，要么同时更新失败
    public int doPostFavourInner(long userId, long postId) {
        PostFavour postFavour = new PostFavour();
        postFavour.setUserId(userId);
        postFavour.setPostId(postId);
        QueryWrapper<PostFavour> postFavourQueryWrapper = new QueryWrapper<>(postFavour);
        PostFavour oldPostFavour = this.getOne(postFavourQueryWrapper);
        boolean result;
        // 更新post_favour的同时，必须同时更新post表，关联查询，必须保证原子性，因此本方法要添加事务
        // 已收藏
        if (oldPostFavour != null) { // 数据库中有数据，说明之前已经收藏过了，此时取消收藏
            result = this.remove(postFavourQueryWrapper);
            if (result) {
                // 帖子收藏数 - 1
                result = postService.update()
                        .eq("id", postId)
                        .gt("favourNum", 0)
                        .setSql("favourNum = favourNum - 1")
                        .update();
                return result ? -1 : 0;
            } else {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR);
            }
        } else { // 数据库中没有数据，说明之前还没有收藏，此时应该收藏成功，将数据插入
            // 未帖子收藏
            result = this.save(postFavour);
            if (result) {
                // 帖子收藏数 + 1
                result = postService.update()
                        .eq("id", postId)
                        .setSql("favourNum = favourNum + 1")
                        .update();
                return result ? 1 : 0;
            } else {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR);
            }
        }
    }

}




