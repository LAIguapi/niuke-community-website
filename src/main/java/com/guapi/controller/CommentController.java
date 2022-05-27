package com.guapi.controller;

import com.guapi.entity.Comment;
import com.guapi.entity.DiscussPost;
import com.guapi.entity.Event;
import com.guapi.event.EventProducer;
import com.guapi.service.CommentService;
import com.guapi.service.DiscussPostService;
import com.guapi.util.CommunityConstant;
import com.guapi.util.HostHolder;
import com.guapi.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Date;

@Controller
@RequestMapping("/comment")
public class CommentController implements CommunityConstant {
    @Autowired
    private CommentService commentService;

    @Autowired
    private EventProducer eventProducer;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private DiscussPostService discussPostService;

    @RequestMapping(value = "/add/{discussPostId}", method = RequestMethod.POST)
    public String addComment(@PathVariable("discussPostId") int discussPostId, Comment comment) {
        //有可能没登录，user为空，报异常
        comment.setUserId(hostHolder.getUser().getId());
        comment.setStatus(0);
        comment.setCreateTime(new Date());
        commentService.addComment(comment);

        //触发评论事件
        Event event = new Event()
                .setTopic(TOPIC_COMMENT)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(comment.getEntityType())
                .setEntityId(comment.getEntityId())
                .setData("postId",discussPostId);
        //评论目标可能是帖子，也可能是评论

        System.out.println(comment.getEntityId());

        if (comment.getEntityType()==ENTITY_TYPE_POST){
            DiscussPost target = discussPostService.findDiscussPostById(comment.getEntityId());
            event.setEntityUserId(target.getUserId());
        }else if (comment.getEntityType()==ENTITY_TYPE_COMMENT){
            Comment target = commentService.findCommentById(comment.getEntityId());

            System.out.println(comment.getContent());
            event.setEntityUserId(target.getUserId());
        }
        eventProducer.fireEvent(event);

        if (comment.getEntityType()==ENTITY_TYPE_POST){
            event = new Event()
                    .setTopic(TOPIC_PUBLISH)
                    .setUserId(comment.getUserId())
                    .setEntityType(ENTITY_TYPE_POST)
                    .setEntityId(discussPostId);
            eventProducer.fireEvent(event);

            //计算帖子分数
            String redisKey = RedisUtil.getPostScoreKey();
            redisTemplate.opsForSet().add(redisKey,discussPostId);
        }

        return "redirect:/discuss/detail/" + discussPostId;
    }
}
