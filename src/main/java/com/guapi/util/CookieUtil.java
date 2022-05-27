package com.guapi.util;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

public class CookieUtil {
    public static String getValue(HttpServletRequest request, String name){
        if (request == null || name == null) {
            throw new IllegalArgumentException("参数为空");
        }
        Cookie[] cookies = request.getCookies();
        if (cookies!=null){
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(name)) {
                    //System.out.println("成功取得"+name+"的cookie值");
                    return cookie.getValue();
                }
            }
        }
        //System.out.println("此处返回了一个空值"+CookieUtil.class);
        return null;
    }
}
