package com.xiaowc.search.esdao;

import com.xiaowc.search.model.dto.post.PostEsDTO;
import java.util.List;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * 帖子 ES 操作
 *
 * ES的增删改查：
 *  第一种方式：ElasticsearchRepository，默认提供了简单的增删改查，多用于可预期的、相对没那么复杂的查询、自定义查询
 *  第二种方式：Spring默认给我们提供的操作es的客户端对象ElasticsearchRestTemplate，也提供了增删改查，他的增删改查更
 *             灵活，适用于更复杂的操作
 */
public interface PostEsDao extends ElasticsearchRepository<PostEsDTO, Long> {

    /**
     * 第一种方式：
     *  自定义方法：通过userId查询
     *  需要严格按照es中方法的规则来，这样es才会自动生成一个对应的方法，不用对该方法有任何的实现也可以用
     * @param userId
     * @return
     */
    List<PostEsDTO> findByUserId(Long userId);

    /**
     * 第一种方式：
     *  自定义方法：通过title查询
     *  需要严格按照es中方法的规则来，这样es才会自动生成一个对应的方法，不用对该方法有任何的实现也可以用
     * @param title
     * @return
     */
    List<PostEsDTO> findByTitle(String title);
}