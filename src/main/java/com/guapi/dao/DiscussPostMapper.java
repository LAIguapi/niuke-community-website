package com.guapi.dao;

import com.guapi.entity.DiscussPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DiscussPostMapper {
    /**
     *
     * @param userId--用户id
     * @param offset--起始行号
     * @param limit--每页最多显示多少数据
     * @return
     */
    List<DiscussPost> selectDiscussPosts(int userId,int offset,int limit,int orderMode);
    //@Param注解用于给参数取别名
    //如果只有一个参数，并且在<if>中使用，必须要使用别名
    int selectDiscussPostRows(@Param("userId") int userId);

    int insertDiscussPost(DiscussPost discussPost);

    DiscussPost selectDiscussPostById(int id);

    int updateCommentCount(int id,int commentCount);

    int updateType(int id, int type);

    int updateStatus(int id, int status);

    int updateScore(int id,double score);
}
