package com.guapi.quartz;

import com.guapi.entity.DiscussPost;
import com.guapi.service.DiscussPostService;
import com.guapi.service.ElasticSearchService;
import com.guapi.service.LikeService;
import com.guapi.util.CommunityConstant;
import com.guapi.util.RedisUtil;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PostScoreRefreshJob implements Job, CommunityConstant {

    private static final Logger logger = LoggerFactory.getLogger(PostScoreRefreshJob.class);

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private LikeService likeService;

    @Autowired
    private ElasticSearchService elasticSearchService;

    private static final Date epoch;

    static {
        try {
            epoch = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2014-08-01 00:00:00");
        } catch (ParseException e) {
            throw new RuntimeException("初始化牛客时间失败", e);
        }
    }

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        String redisKey = RedisUtil.getPostScoreKey();
        ;
        BoundSetOperations operations = redisTemplate.boundSetOps(redisKey);

        if (operations.size() == 0) {
            logger.info("[任务取消] 没有需要刷新的帖子");
            return;
        }
        logger.info("[任务开始] 正在刷新帖子分数:" + operations.size());
        while (operations.size() > 0) {
            this.refresh((Integer) operations.pop());
        }
        logger.info("[任务结束] 帖子分数刷新完毕" );

    }

    private void refresh(int postId) {
        DiscussPost discussPost = discussPostService.findDiscussPostById(postId);
        if (discussPost == null) {
            logger.error("该id对应的帖子不存在" + postId);
            return;
        }
        //是否加精
        boolean wonderful = discussPost.getStatus() == 1;
        //评论数量
        int commentCount = discussPost.getCommentCount();
        //点赞数量
        long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, postId);

        //计算权重
        double weigh = (wonderful ? 75 : 0) + commentCount * 10 + likeCount * 2;
        //计算总分=权重+时间间隔(天)
        double score = Math.log10(Math.max(weigh, 1)) +
                (discussPost.getCreateTime().getTime() - epoch.getTime()) / (1000 * 3600 * 24);

        //更新帖子分数
        discussPostService.updateScore(postId,score);

        //同步搜索数据
        discussPost.setScore(score);
        elasticSearchService.saveDiscussPost(discussPost);

    }
}
