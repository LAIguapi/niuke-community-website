package com.guapi.dao;

import com.guapi.entity.Message;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MessageMapper {
    //查询当前用户所拥有的对话列表，针对每个会话返回最新私信
    List<Message> findConversations(int userId, int offset, int limit);
    //查询用户的会话数量
    int findConversationCount(int userId);
    //查询某个对话所包含的私信列表
    List<Message> findLetters(String conversationId, int offset, int limit);
    //查询某个会话包含的私信数量
    int findLetterCount(String conversationId);

    //查询未读
    int findLetterUnreadCount(int userId, String conversationId);

    //新增一个消息
    int insertMessage(Message message);

    //更改消息状态
    int updateStatus(List<Integer> ids,int status);


    //查询某个主题下的最新通知
    Message selectLatestNotice(int userId,String topic);
    //查询某个主题所包含的通知数量
    int selectNoticeCount(int userId,String topic);

    //查询未读通知的数量
    int selectUnreadNoticeCount(int userId,String topic);

    //查询某个主题所包含的通知列表
    List<Message> selectNotices(int userId,String topic,int offset,int limit);
}
