package com.guapi.service;

import com.guapi.dao.CommentMapper;
import com.guapi.entity.Comment;
import com.guapi.entity.DiscussPost;
import com.guapi.util.CommunityConstant;
import com.guapi.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import java.util.ArrayList;
import java.util.List;

@Service
public class CommentService implements CommunityConstant {
    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private DiscussPostService discussPostService;
    @Autowired
    private SensitiveFilter sensitiveFilter;

    public List<Comment> fineCommentsByEntity(int entityType, int entityId, int offset, int limit) {
        return commentMapper.selectCommentByEntity(entityType, entityId, offset, limit);
    }

    public int findCommentCount(int entityType, int entityId) {
        return commentMapper.selectCountByEntity(entityType, entityId);
    }


    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED)
    public int addComment(Comment comment) {
        if (comment == null) {
            throw new IllegalArgumentException("参数不能为空");
        }
        //添加评论
        comment.setContent(HtmlUtils.htmlEscape(comment.getContent()));
        comment.setContent(sensitiveFilter.filter(comment.getContent()));
        int rows = commentMapper.insertComment(comment);

        //更新帖子评论数量
        if (comment.getEntityType() == ENTITY_TYPE_POST) {
            int count = commentMapper.selectCountByEntity(comment.getEntityType(), comment.getEntityId());

            discussPostService.updateCommentCount(comment.getEntityId(), count);
        }
        return rows;

    }


    public Comment findCommentById(int id) {
        return commentMapper.selectCommentById(id);
    }

    public List<Comment> findCommentByUser(int userId, int offset, int limit) {
        //此处要进行判断，判断评论回复的帖子是否存在
        //但是此处删除完成后，在前端会发现数据不是指定数据。。。暂时想不到什么好的处理方法
        List<Comment> list = commentMapper.selectCommentsByUser(userId, offset, limit);
        if (list == null) {
            throw new IllegalArgumentException("回复列表为空");
        }
        List<Comment> comments = new ArrayList<>();
        for (Comment comment : list) {
            DiscussPost post = discussPostService.findDiscussPostById(comment.getEntityId());
            //如果能查找到数据或者帖子不是被删除的状态
            if (post != null && post.getStatus() != 2) {
                comments.add(comment);
            }
        }
        return comments;
    }

    public int findUserCommentRows(int userId) {
        return commentMapper.selectUserCommentRows(userId);
    }

}
