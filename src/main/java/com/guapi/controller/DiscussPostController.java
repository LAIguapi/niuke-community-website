package com.guapi.controller;

import com.guapi.entity.*;
import com.guapi.event.EventProducer;
import com.guapi.service.CommentService;
import com.guapi.service.DiscussPostService;
import com.guapi.service.LikeService;
import com.guapi.service.UserService;
import com.guapi.util.CommunityConstant;
import com.guapi.util.CommunityUtil;
import com.guapi.util.HostHolder;
import com.guapi.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

@Controller
@RequestMapping("/discuss")
public class DiscussPostController implements CommunityConstant {
    @Autowired
    private HostHolder hostHolder;
    @Autowired
    private UserService userService;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private LikeService likeService;

    @Autowired
    private EventProducer eventProducer;

    @Autowired
    private RedisTemplate redisTemplate;

    @RequestMapping(path = "/add", method = RequestMethod.POST)
    @ResponseBody
    public String addDiscussPost(String title, String content) {
        User user = hostHolder.getUser();
        if (user == null) {
            return CommunityUtil.getJSONString(403, "用户未登陆");
        }

        DiscussPost discussPost = new DiscussPost();
        discussPost.setUserId(user.getId());
        discussPost.setTitle(title);
        discussPost.setContent(content);
        discussPost.setCreateTime(new Date());
        discussPostService.addDiscussPost(discussPost);

        //计算帖子分数
        String redisKey = RedisUtil.getPostScoreKey();
        redisTemplate.opsForSet().add(redisKey,discussPost.getId());


        //触发发帖事件
        Event event = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(user.getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(discussPost.getId());
        eventProducer.fireEvent(event);

        //报错情况统一处理
        return CommunityUtil.getJSONString(0, "发布成功");
    }

    @RequestMapping(path = "/detail/{discussPostId}", method = RequestMethod.GET)
    public String getDiscussPost(@PathVariable("discussPostId") int id, Model model, Page page) {
        //帖子
        DiscussPost post = discussPostService.findDiscussPostById(id);
        model.addAttribute("post", post);
        User user = userService.findUserById(post.getUserId());
        model.addAttribute("user", user);

        //点赞数量
        long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, id);
        model.addAttribute("likeCount", likeCount);
        //点赞状态
        int likeStatus = hostHolder.getUser() == null ? 0 : likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_POST, id);
        model.addAttribute("likeStatus", likeStatus);

        //分页信息
        page.setLimit(5);
        page.setPath("/discuss/detail/" + id);
        page.setRows(post.getCommentCount());

        //回复帖子的评论列表
        List<Comment> commentList = commentService.fineCommentsByEntity(ENTITY_TYPE_POST, post.getId(), page.getOffset(), page.getLimit());

        //回复评论的评论
        //评论VO列表
        List<Map<String, Object>> commentViewObjectList = new ArrayList<>();
        if (commentList != null) {
            for (Comment comment : commentList) {
                //一个评论的VO
                HashMap<String, Object> commentViewObject = new HashMap<>();
                //向VO里添加了评论
                commentViewObject.put("comment", comment);
                //添加了该条评论的作者
                commentViewObject.put("user", userService.findUserById(comment.getUserId()));


                //点赞数量
                likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, comment.getId());
                commentViewObject.put("likeCount", likeCount);
                //点赞状态
                likeStatus = hostHolder.getUser() == null ? 0 : likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_COMMENT, comment.getId());
                commentViewObject.put("likeStatus", likeStatus);

                //评论的回复列表
                List<Comment> replyCommentList = commentService.fineCommentsByEntity(ENTITY_TYPE_COMMENT, comment.getId(), 0, Integer.MAX_VALUE);
                //回复评论的VO列表
                ArrayList<Map<String, Object>> replyViewObjectList = new ArrayList<>();
                if (replyCommentList != null) {
                    for (Comment reply : replyCommentList) {
                        HashMap<String, Object> replyViewObject = new HashMap<>();
                        //回复
                        replyViewObject.put("reply", reply);
                        //作者
                        replyViewObject.put("user", userService.findUserById(reply.getUserId()));
                        //回复目标
                        User target = reply.getTargetId() == 0 ? null : userService.findUserById(reply.getTargetId());
                        replyViewObject.put("target", target);
                        //点赞数量
                        likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, reply.getId());
                        replyViewObject.put("likeCount", likeCount);
                        //点赞状态
                        likeStatus = hostHolder.getUser() == null ? 0 : likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_COMMENT, reply.getId());
                        replyViewObject.put("likeStatus", likeStatus);

                        replyViewObjectList.add(replyViewObject);
                    }
                }

                commentViewObject.put("replys", replyViewObjectList);
                //回复数量
                int replyCount = commentService.findCommentCount(ENTITY_TYPE_COMMENT, comment.getId());
                commentViewObject.put("replyCount", replyCount);

                commentViewObjectList.add(commentViewObject);

            }
        }

        model.addAttribute("comments", commentViewObjectList);

        return "site/discuss-detail";
    }

    //置顶功能
    @RequestMapping(path = "/top", method = RequestMethod.POST)
    @ResponseBody
    public String setType(int id) {
        discussPostService.updateType(id, 1);

        Event event = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(id);
        eventProducer.fireEvent(event);

        return CommunityUtil.getJSONString(0);
    }

    //加精功能
    @RequestMapping(path = "/wonderful", method = RequestMethod.POST)
    @ResponseBody
    public String setWonderful(int id) {
        discussPostService.updateStatus(id, 1);

        //帖子数据发生变化，需要重新加载
        Event event = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(id);
        eventProducer.fireEvent(event);

        String redisKey = RedisUtil.getPostScoreKey();
        redisTemplate.opsForSet().add(redisKey,id);

        return CommunityUtil.getJSONString(0);
    }

    //删除功能
    @RequestMapping(path = "/delete", method = RequestMethod.POST)
    @ResponseBody
    public String setDelete(int id) {
        discussPostService.updateStatus(id, 2);

        //从事件中删除
        Event event = new Event()
                .setTopic(TOPIC_DELETE)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(id);
        eventProducer.fireEvent(event);
        return CommunityUtil.getJSONString(0);
    }
}
