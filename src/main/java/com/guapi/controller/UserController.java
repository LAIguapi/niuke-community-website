package com.guapi.controller;

import com.guapi.annotation.LoginRequired;
import com.guapi.entity.Comment;
import com.guapi.entity.DiscussPost;
import com.guapi.entity.Page;
import com.guapi.entity.User;
import com.guapi.service.*;
import com.guapi.util.CommunityConstant;
import com.guapi.util.CommunityUtil;
import com.guapi.util.HostHolder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/user")
public class UserController implements CommunityConstant {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Value("${community.path.upload}")
    private String uploadPath;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private UserService userService;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private FollowService followService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private LikeService likeService;

    @RequestMapping(path = "/setting", method = RequestMethod.GET)
    @LoginRequired
    public String getSettingPage() {
        return "/site/setting";
    }

    //?????????????????????????????????
    @RequestMapping(path = "/upload", method = RequestMethod.POST)
    @LoginRequired
    public String uploadHeader(MultipartFile headerImage, Model model) {

        if (headerImage == null) {
            model.addAttribute("error", "??????????????????????????????");
            return "/site/setting";
        }
//        logger.info("????????????????????????");
        String filename = headerImage.getOriginalFilename();
        //?????????????????????
        String suffix = filename.substring(filename.lastIndexOf("."));
        if (StringUtils.isBlank(suffix)) {
            model.addAttribute("error", "?????????????????????");
            return "/site/setting";
        }
        //?????????????????????
        filename = CommunityUtil.generateUUID() + suffix;
        //????????????
        File dest = new File(uploadPath + "/" + filename);
        try {
            headerImage.transferTo(dest);
        } catch (IOException e) {
            logger.error("??????????????????" + e.getCause());
            throw new RuntimeException("??????????????????????????????????????????!!" + e);
        }

        //?????????????????????????????????web?????????
        //http://localhost:80/guapi/user/header/xx.png
        User user = hostHolder.getUser();
        String headerUrl = domain + contextPath + "/user/header/" + filename;
        userService.updateHeader(user.getId(), headerUrl);
//        logger.info("????????????");
        return "redirect:/index";
    }

    @RequestMapping(path = "/header/{filename}", method = RequestMethod.GET)
    public void getHeader(@PathVariable("filename") String filename, HttpServletResponse response) {
        //??????????????????????????????
        //D:/CodeConllection/PraticeNiuKe/upload/
        filename = uploadPath + "/" + filename;
//        logger.info("filename---------"+filename);
        //??????????????????
        String suffix = filename.substring(filename.lastIndexOf("."));
//        logger.info("suffix-----------"+suffix);
        //????????????
        response.setContentType("image/" + suffix);
        try (
                FileInputStream fis = new FileInputStream(filename);
                ServletOutputStream os = response.getOutputStream();
        ) {
            byte[] buffer = new byte[1024];
            int b = 0;
            while ((b = fis.read(buffer)) != -1) {
                os.write(buffer, 0, b);
            }
        } catch (IOException e) {
            logger.error("??????????????????" + e.getCause());
        }
    }

    //????????????
    @RequestMapping(value = "/profile/{userId}", method = RequestMethod.GET)
    public String getProfilePage(@PathVariable("userId") int userId, Model model) {
        User user = userService.findUserById(userId);
        if (user == null) {
            throw new RuntimeException("??????????????????");
        }
        //??????
        model.addAttribute("user", user);
        //????????????
        int likeCount = likeService.findUserLikeCount(userId);
        model.addAttribute("likeCount", likeCount);
        //????????????
        long followeeCount = followService.findFolloweeCount(userId, ENTITY_TYPE_USER);
        model.addAttribute("followeeCount", followeeCount);
        //????????????

        long followerCount = followService.findFollowerCount(ENTITY_TYPE_USER, userId);
        model.addAttribute("followerCount", followerCount);
        //???????????????

        boolean hasFollowed = false;
//        System.out.println("hostHolder.getUser().id================>"+hostHolder.getUser().getId());
        if (hostHolder.getUser() != null) {
            hasFollowed = followService.hasFollowed(hostHolder.getUser().getId(), ENTITY_TYPE_USER, userId);
        }
//        System.out.println("hasFollowed===============>"+hasFollowed);
        model.addAttribute("hasFollowed", hasFollowed);
        return "/site/profile";
    }


    //????????????????????????
    @RequestMapping(path = "/myPost/{userId}", method = RequestMethod.GET)
    public String getMyPostPage(@PathVariable("userId") int userId, Page page, Model model) {
        int orderMode = 0;
        User user = userService.findUserById(userId);
        if (user == null) {
            throw new RuntimeException("??????????????????");
        }
        model.addAttribute("user", user);
        //???????????????????????????

        page.setLimit(5);
        page.setRows(discussPostService.findDiscussPostRows(userId));
        //?????????????????????????????????????????????????????????????????????

        page.setPath("/user/myPost/" + userId);
        List<DiscussPost> list = discussPostService.findDiscussPosts(userId, page.getOffset(), page.getLimit(), orderMode);

        List<Map<String, Object>> discussPosts = new ArrayList<>();
        if (list != null) {
            for (DiscussPost post : list) {
                HashMap<String, Object> map = new HashMap<>();
                map.put("post", post);
                //??????????????????
                long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, post.getId());
                map.put("likeCount", likeCount);
                discussPosts.add(map);
            }
        }

        model.addAttribute("posts", discussPosts);
        //model.addAttribute("orderMode",orderMode);
        return "/site/my-post";
    }

    //?????????????????????
    @RequestMapping(path = "/myReply/{userId}", method = RequestMethod.GET)
    public String getMyReplyPage(@PathVariable("userId") int userId, Page page, Model model) {
        User user = userService.findUserById(userId);
        if (user == null) {
            throw new RuntimeException("??????????????????");
        }
        model.addAttribute("user", user);
        page.setLimit(5);

        page.setRows(commentService.findUserCommentRows(userId));//????????????
        page.setPath("/user/myReply/" + userId);


        //??????????????????
        List<Comment> list = commentService.findCommentByUser(userId, page.getOffset(), page.getLimit());
        //while (list.size()!=page.getLimit()){
        //    list=commentService.findCommentByUser(userId,page.getOffset(),page.getLimit()+1);
        //}

        List<Map<String, Object>> commentList = new ArrayList<>();

        for (Comment comment : list) {
            //???????????????comment
            HashMap<String, Object> map = new HashMap<>();
            //??????????????????
            if (comment.getEntityType() == ENTITY_TYPE_POST) {
                DiscussPost target = discussPostService.findDiscussPostById(comment.getEntityId());
                //?????????????????????????????????????????????????????????????????????target??????null
                map.put("target", target);
                map.put("comment", comment);
                commentList.add(map);
            }else if(comment.getEntityType()==ENTITY_TYPE_COMMENT){
                Comment commentById = commentService.findCommentById(comment.getEntityId());
                //??????????????????????????????????????????????????????????????????????????????????????????
                DiscussPost target = discussPostService.findDiscussPostById(commentById.getEntityId());
                map.put("target",target);
                map.put("comment",comment);
                commentList.add(map);
            }

        }




        model.addAttribute("commentList", commentList);

        return "/site/my-reply";

    }

}
