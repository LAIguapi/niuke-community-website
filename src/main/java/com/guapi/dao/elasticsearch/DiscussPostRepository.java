package com.guapi.dao.elasticsearch;

import com.guapi.entity.DiscussPost;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

//ElasticsearchRepository<DiscussPost,Integer>声明处理的实体类以及其主键的类型
@Repository
public interface DiscussPostRepository extends ElasticsearchRepository<DiscussPost,Integer> {
    //让Spring自动实现
}
