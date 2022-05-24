package com.guapi.controller;

import com.guapi.entity.DiscussPost;
import com.guapi.entity.Page;
import com.guapi.entity.SearchResult;
import com.guapi.service.ElasticSearchService;
import com.guapi.service.LikeService;
import com.guapi.service.UserService;
import com.guapi.util.CommunityConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



@Controller
public class SearchController implements CommunityConstant {
    private static final Logger logger = LoggerFactory.getLogger(SearchController.class);

    @Autowired
    private ElasticSearchService elasticSearchService;

    @Autowired
    private UserService userService;

    @Autowired
    private LikeService likeService;

    //search?keyword=xxx
    @RequestMapping(path = "/search", method = RequestMethod.GET)
    public String search(String keyword, Page page, Model model) {

        //搜索帖子
        try {
            SearchResult searchResult = elasticSearchService.searchDiscussPost(keyword, page.getCurrent() - 1, page.getLimit());
            ArrayList<Map<String, Object>> discussPosts = new ArrayList<>();
            List<DiscussPost> list = searchResult.getList();
            if (list != null) {
                for (DiscussPost post : list) {

                    HashMap<String, Object> map = new HashMap<>();
                    //获取帖子，帖子作者
                    map.put("post", post);
                    map.put("user", userService.findUserById(post.getUserId()));
                    map.put("likeCount",likeService.findEntityLikeCount(ENTITY_TYPE_POST, post.getId()));
                    discussPosts.add(map);
                }
            }
            model.addAttribute("discussPosts",discussPosts);
            model.addAttribute("keyword",keyword);
            //设置分页信息
            page.setPath("/search?keyword="+keyword);
            page.setRows(searchResult.getTotal()==0?0: (int) searchResult.getTotal());
        } catch (IOException e) {
            logger.error("此处是"+this.getClass().getName()+",出现了意料之外的错误");
            e.printStackTrace();
        }
        return "site/search";
    }
}
