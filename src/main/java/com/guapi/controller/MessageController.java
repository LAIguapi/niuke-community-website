package com.guapi.controller;

import com.alibaba.fastjson.JSONObject;
import com.guapi.entity.Message;
import com.guapi.entity.Page;
import com.guapi.entity.User;
import com.guapi.service.MessageService;
import com.guapi.service.UserService;
import com.guapi.util.CommunityConstant;
import com.guapi.util.CommunityUtil;
import com.guapi.util.HostHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.HtmlUtils;

import java.util.*;

@Controller
public class MessageController implements CommunityConstant {
    @Autowired
    private MessageService messageService;
    private static final Logger logger = LoggerFactory.getLogger(MessageController.class);

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;


    @RequestMapping(value = "/letter/list", method = RequestMethod.GET)
    public String getLettersList(Model model, Page page) {
        User user = hostHolder.getUser();
        //设置分页信息
        page.setLimit(5);
        page.setPath("/letter/list");
        page.setRows(messageService.findConversationCount(user.getId()));
        List<Message> conversationList = messageService.findConversations(user.getId(), page.getOffset(), page.getLimit());
        ArrayList<Map<String, Object>> conversations = new ArrayList<>();
        if (conversationList != null) {
            for (Message message : conversationList) {
                HashMap<String, Object> map = new HashMap<>();
                map.put("conversation", message);
                map.put("letterCount", messageService.findLetterCount(message.getConversationId()));
                map.put("unreadCount", messageService.findLetterUnreadCount(user.getId(), message.getConversationId()));
                int targetId = user.getId() == message.getFromId() ? message.getToId() : message.getFromId();
                map.put("target", userService.findUserById(targetId));
                //此处如果没找到呢？

                conversations.add(map);
            }
        }
        model.addAttribute("conversations", conversations);
        //查询未读消息数量
        int letterUnreadCount = messageService.findLetterUnreadCount(user.getId(), null);
        model.addAttribute("letterUnreadCount", letterUnreadCount);

        int noticeUnreadCount = messageService.findUnreadNoticeCount(user.getId(), null);
        model.addAttribute("noticeUnreadCount", noticeUnreadCount);
        return "site/letter";


    }

    @RequestMapping(path = "/letter/detail/{conversationId}", method = RequestMethod.GET)
    public String getLetterDetail(@PathVariable("conversationId") String conversationId, Page page, Model model) {
        //设置分页信息
        page.setLimit(5);
        page.setPath("/letter/detail/" + conversationId);
        page.setRows(messageService.findLetterCount(conversationId));

        //私信列表
        List<Message> letterList = messageService.findLetters(conversationId, page.getOffset(), page.getLimit());
        List<Map<String, Object>> letters = new ArrayList<>();
        if (letterList != null) {

            for (Message message : letterList) {
                HashMap<String, Object> map = new HashMap<>();
                map.put("letter", message);
                map.put("fromUser", userService.findUserById(message.getFromId()));
                letters.add(map);
            }
        }
        model.addAttribute("letters", letters);

        //设置已读
        List<Integer> ids = getLetterIds(letterList);
        if (!ids.isEmpty()) {
            messageService.readMessage(ids);
        }

        //私信目标
        model.addAttribute("target", getLetterTarget(conversationId));

        return "site/letter-detail";
    }

    private List<Integer> getLetterIds(List<Message> letterList) {
        ArrayList<Integer> ids = new ArrayList<>();

        if (letterList != null) {
            for (Message message : letterList) {
                if (hostHolder.getUser().getId() == message.getToId() && message.getStatus() == 0) {
                    ids.add(message.getId());
                }
            }
        }
        return ids;
    }

    private User getLetterTarget(String conversationId) {
        String[] ids = conversationId.split("_");
        int id0 = Integer.parseInt(ids[0]);
        int id1 = Integer.parseInt(ids[1]);
        if (hostHolder.getUser().getId() == id0) {
            return userService.findUserById(id1);
        } else {
            return userService.findUserById(id0);
        }
    }

    @RequestMapping(path = "/letter/send", method = RequestMethod.POST)
    @ResponseBody
    public String sendLetter(String toName, String content) {
//        Integer.valueOf("asd");
        User target = userService.findUserByName(toName);
        if (target == null) {
            return CommunityUtil.getJSONString(1, "目标用户不存在");
        }
        Message message = new Message();
        message.setFromId(hostHolder.getUser().getId());
        message.setToId(target.getId());
        if (message.getFromId() < message.getToId()) {
            message.setConversationId(message.getFromId() + "_" + message.getToId());
        } else {
            message.setConversationId(message.getToId() + "_" + message.getFromId());
        }
        message.setContent(content);
        message.setStatus(0);
        message.setCreateTime(new Date());
        messageService.addMessage(message);
        //若有报错，统一处理
        return CommunityUtil.getJSONString(0);
    }

    @RequestMapping(path = "/notice/list", method = RequestMethod.GET)
    public String getNoticeList(Model model) {
        User user = hostHolder.getUser();
        int userId = user.getId();
        //查询评论类通知
        //如果，没有最近消息，那么message==null，无法进入判断，导致所有数据无法加入，导致messageVO内无数据
        //且关于此项的消息提醒为空，commentNotice是一个空值，那么在前端尝试获取其中的消息时，就会发生模板报错

        /*
            解决方案：把map对象放入if中，当message不为空才对其赋值，而且在前端处判断VO是否为空，若为空，则不显示，也就不需要进行下一步判断
            th:if="${followNotice!=null}"

         */
        Message message = messageService.findLatestNotice(userId, TOPIC_COMMENT);
        //此处获取到message==null
        //logger.warn("message:"+message+" + "+this.getClass().getName());

        if (message != null) {
            Map<String, Object> messageVO = new HashMap<>();
            messageVO.put("message", message);
            String content = HtmlUtils.htmlUnescape(message.getContent());
            Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);
            messageVO.put("user", userService.findUserById((Integer) data.get("userId")));
            messageVO.put("entityType", data.get("entityType"));
            messageVO.put("entityId", data.get("entityId"));
            messageVO.put("postId", data.get("postId"));

            int count = messageService.findNoticeCount(userId, TOPIC_COMMENT);
            messageVO.put("count", count);

            int unread = messageService.findUnreadNoticeCount(userId, TOPIC_COMMENT);
            messageVO.put("unread", unread);
            model.addAttribute("commentNotice", messageVO);
        }
        //查询点赞类通知
        message = messageService.findLatestNotice(userId, TOPIC_LIKE);
        //messageVO=new HashMap<>();
        if (message != null) {
            Map<String, Object> messageVO = new HashMap<>();
            messageVO.put("message", message);
            String content = HtmlUtils.htmlUnescape(message.getContent());
            Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);
            messageVO.put("user", userService.findUserById((Integer) data.get("userId")));
            messageVO.put("entityType", data.get("entityType"));
            messageVO.put("entityId", data.get("entityId"));
            messageVO.put("postId", data.get("postId"));

            int count = messageService.findNoticeCount(userId, TOPIC_LIKE);
            messageVO.put("count", count);

            int unread = messageService.findUnreadNoticeCount(userId, TOPIC_LIKE);
            messageVO.put("unread", unread);
            model.addAttribute("likeNotice", messageVO);
        }

        //查询关注类通知

        message = messageService.findLatestNotice(userId, TOPIC_FOLLOW);
        //messageVO=new HashMap<>();
        if (message != null) {
            Map<String, Object> messageVO = new HashMap<>();
            messageVO.put("message", message);
            String content = HtmlUtils.htmlUnescape(message.getContent());
            Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);
            messageVO.put("user", userService.findUserById((Integer) data.get("userId")));
            messageVO.put("entityType", data.get("entityType"));
            messageVO.put("entityId", data.get("entityId"));
//            messageVO.put("postId",data.get("postId"));

            int count = messageService.findNoticeCount(userId, TOPIC_FOLLOW);
            messageVO.put("count", count);

            int unread = messageService.findUnreadNoticeCount(userId, TOPIC_FOLLOW);
            messageVO.put("unread", unread);
            model.addAttribute("followNotice", messageVO);
        }

        //查询未读消息数量
        int letterUnreadCount = messageService.findLetterUnreadCount(userId, null);
        model.addAttribute("letterUnreadCount", letterUnreadCount);
        int noticeUnreadCount = messageService.findUnreadNoticeCount(userId, null);
        model.addAttribute("noticeUnreadCount", noticeUnreadCount);

        return "site/notice";
    }

    @RequestMapping(path = "/notice/detail/{topic}", method = RequestMethod.GET)
    public String getNoticeDetail(@PathVariable("topic") String topic, Page page, Model model) {
        User user = hostHolder.getUser();
        int userId = user.getId();

        page.setLimit(5);
        page.setPath("/notice/detail/" + topic);
        //logger.error(page.getPath());
        page.setRows(messageService.findNoticeCount(userId, topic));

        List<Message> noticeList = messageService.findNotices(userId, topic, page.getOffset(), page.getLimit());
        List<Map<String, Object>> noticeVOList = new ArrayList<>();
        if (noticeList != null) {
            for (Message notice : noticeList) {
                Map<String, Object> map = new HashMap<>();

                map.put("notice", notice);

                String content = HtmlUtils.htmlUnescape(notice.getContent());
                Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);
                map.put("user", userService.findUserById((Integer) data.get("userId")));
                map.put("entityType", data.get("entityType"));
                map.put("entityId", data.get("entityId"));
                map.put("postId", data.get("postId"));

                //通知作者
                map.put("fromUser",userService.findUserById(notice.getFromId()));

                noticeVOList.add(map);
            }
        }
        model.addAttribute("notices",noticeVOList);

        //设置已读
        List<Integer> ids=getLetterIds(noticeList);
        if (!ids.isEmpty()){
            messageService.readMessage(ids);
        }
        return "/site/notice-detail";
    }


}
