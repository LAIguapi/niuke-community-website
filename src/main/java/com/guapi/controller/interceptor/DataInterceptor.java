package com.guapi.controller.interceptor;

import com.guapi.entity.User;
import com.guapi.service.DataService;
import com.guapi.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class DataInterceptor implements HandlerInterceptor {
    @Autowired
    private DataService dataService;

    @Autowired
    private HostHolder hostHolder;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //统计uv
        String ip = request.getRemoteHost();
        dataService.recordUV(ip);
        //统计DAU
        User user = hostHolder.getUser();
        if (user!=null){
           dataService.recordDAU(user.getId());
        }
        return true;


    }
}
