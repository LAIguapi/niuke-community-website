package com.guapi.controller.interceptor;

import com.guapi.entity.LoginTicket;
import com.guapi.entity.User;
import com.guapi.service.UserService;
import com.guapi.util.CookieUtil;
import com.guapi.util.HostHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

@Component
public class LoginTicketInterceptor implements HandlerInterceptor {


    private static final Logger logger = LoggerFactory.getLogger(LoginTicketInterceptor.class);
    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
//        System.out.println("preHandle执行------");
        //获取cookie
        String ticket = CookieUtil.getValue(request, "ticket");
        if (ticket != null) {
//            logger.info(ticket.toString());
            LoginTicket loginTicket = userService.findLoginTicket(ticket);
            //检查凭证是否过期
            if (loginTicket != null && loginTicket.getStatus() == 0 && loginTicket.getExpired().after(new Date())) {
                User user = userService.findUserById(loginTicket.getUserId());
                hostHolder.setUser(user);
//                logger.info("preHandle user----->"+user);
                //构造用户认证的结果，存入SecurityContext，用于授权
                Authentication authentication = new UsernamePasswordAuthenticationToken(
                        //用户，用户的认证，用户权限
                        user, user.getPassword(), userService.getAuthorities(user.getId())
                );
                //存入权限
                SecurityContextHolder.setContext(new SecurityContextImpl(authentication));
            }
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        User user = hostHolder.getUser();
        if (user != null && modelAndView != null) {
            modelAndView.addObject("loginUser",user);
//            logger.info("postHandle user----->"+user);


        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
//        logger.info("afterCompletion------->clear");
        hostHolder.clear();
        //补充清理验证结果
    }
}
