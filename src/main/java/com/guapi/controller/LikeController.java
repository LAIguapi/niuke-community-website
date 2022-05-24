package com.guapi.controller;

import com.guapi.entity.Event;
import com.guapi.entity.User;
import com.guapi.event.EventProducer;
import com.guapi.service.LikeService;
import com.guapi.util.CommunityConstant;
import com.guapi.util.CommunityUtil;
import com.guapi.util.HostHolder;
import com.guapi.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Controller
public class LikeController implements CommunityConstant {
    @Autowired
    private EventProducer eventProducer;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private LikeService likeService;

    @Autowired
    private HostHolder hostHolder;

    @RequestMapping(path = "/like", method = RequestMethod.POST)
    @ResponseBody
    public String like(int entityType, int entityId,int entityUserId,int postId) {
        User user = hostHolder.getUser();

        //点赞
        likeService.like(user.getId(), entityType, entityId,entityUserId);
        //数量
        long likeCount = likeService.findEntityLikeCount(entityType, entityId);
        int likeStatus = likeService.findEntityLikeStatus(user.getId(), entityType, entityId);
//        System.out.println("likeStatus========================》"+likeStatus);
        Map<String, Object> map = new HashMap<>();
        map.put("likeCount", likeCount);
        map.put("likeStatus", likeStatus);

        //点赞事件
        if (likeStatus == 1) {
            Event event = new Event()
                    .setTopic(TOPIC_LIKE)
                    .setUserId(hostHolder.getUser().getId())
                    .setEntityType(entityType)
                    .setEntityId(entityId)
                    .setEntityUserId(entityUserId)
                    .setData("postId",postId);

            eventProducer.fireEvent(event);
        }

        if (entityType==ENTITY_TYPE_POST){
            //计算帖子分数
            String redisKey = RedisUtil.getPostScoreKey();
            redisTemplate.opsForSet().add(redisKey,postId);
        }


        return CommunityUtil.getJSONString(0, null, map);
    }


}
