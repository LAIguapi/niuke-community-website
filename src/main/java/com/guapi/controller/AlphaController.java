package com.guapi.controller;

import com.guapi.service.AlphaService;
import com.guapi.util.CommunityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

@Controller
@RequestMapping("/alpha")
@RestController
public class AlphaController {
    @Autowired
    private AlphaService alphaService;
    @RequestMapping("/hello")
    @ResponseBody

    public String hello(){
        return "Hello,Springboot";
    }
    @ResponseBody
    @RequestMapping("/data")
    public String getData(){
        return alphaService.find();
    }

    //MVC演示
    @RequestMapping("/http")
    public void http(HttpServletRequest request, HttpServletResponse response){
        //可以用response对象向浏览器输出对象，所以是void,不需要返回
        System.out.println(request.getMethod());
        System.out.println(request.getServletPath());
        Enumeration<String> enumeration = request.getHeaderNames();
        while (enumeration.hasMoreElements()) {
            String name = enumeration.nextElement();
            String values = request.getHeader(name);
            System.out.println(name+"  : "+values);
        }
        System.out.println(request.getParameter("code"));

        //返回响应数据
        response.setContentType("text/html;charset=utf-8");
        try(PrintWriter writer = response.getWriter()) {
            writer.write("<h1>tnnd<h1>");
            writer.write("<h1>tnnd<h1>");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Get请求
    //查询所有学生：/students?current=&limit=20
    @RequestMapping(path = "/students",method = RequestMethod.GET)
    @ResponseBody
    public String getStudents(
            @RequestParam(name = "current",required = false,defaultValue = "1") int current,
            @RequestParam(name = "limit",required = false,defaultValue = "10")int limit){

        System.out.println(current);
        System.out.println(limit);
        return "some students";
    }

    //根据编号查询 /students/123
    @RequestMapping(path = "/student/{id}",method = RequestMethod.GET)
    @ResponseBody
    public String getStudent(@PathVariable("id") int id){
        System.out.println(id);
        return "a student";

    }

    @RequestMapping(path = "/student",method = RequestMethod.POST)
    @ResponseBody
    public String saveStudent(String name ,int age){
        System.out.println(name);
        System.out.println(age);
        return "保存成功";
    }
    //响应HTML
    @RequestMapping(path = "/teacher",method = RequestMethod.GET)
    public ModelAndView getTeacher(){
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("name","张三");
        modelAndView.addObject("age",30);
        modelAndView.setViewName("/demo/view");
        return modelAndView;
    }
    //推荐用更简洁的这种方式↓
    @RequestMapping(path = "/school",method = RequestMethod.GET)
    public String getSchool(Model model){
        model.addAttribute("name","北京大学");
        model.addAttribute("age",20);
        return "/demo/view";
    }
    //响应JSON数据，异步请求
    //java对象--json字符串--js对象
    @RequestMapping(path = "/emp",method = RequestMethod.GET)
    @ResponseBody
    //不加此注解，浏览器会以为返回的是HTML
    public Map<String ,Object> getEmp(){
        HashMap<String, Object> map = new HashMap<>();
        map.put("name","张三");
        map.put("age",20);
        map.put("salary",8000);
        return map;
    }

    @RequestMapping(path = "/emps",method = RequestMethod.GET)
    @ResponseBody
    //不加此注解，浏览器会以为返回的是HTML
    public List<Map<String ,Object>> getEmps(){
        ArrayList<Map<String, Object>> list = new ArrayList<>();
        HashMap<String, Object> map = new HashMap<>();
        map.put("name","张三");
        map.put("age",20);
        map.put("salary",8000);

        HashMap<String, Object> map1 = new HashMap<>();
        map1.put("name","王八");
        map1.put("age",50);
        map1.put("salary",8500);

        HashMap<String, Object> map2 = new HashMap<>();
        map2.put("name","沙口");
        map2.put("age",2000);
        map2.put("salary",100);
        list.add(map);
        list.add(map1);
        list.add(map2);

        return list;
    }


    //Cookie
    @RequestMapping(path = "/cookie/set",method = RequestMethod.GET)
    @ResponseBody
    public String setCookie(HttpServletResponse response){
        //创建cookie
        Cookie cookie = new Cookie("code", CommunityUtil.generateUUID());
        //设置cookie生效的范围
        cookie.setPath("/guapi");
        //将cookie存放的生存周期
        cookie.setMaxAge(60*10);
        response.addCookie(cookie);

        return "ser cookie";
    }


    @RequestMapping(value = "/cookie/get",method = RequestMethod.GET)
    @ResponseBody
    public String getCookie(@PathVariable("code")String  code){
        System.out.println(code);
        return "get cookie";
    }


    @RequestMapping(value = "/session/set",method = RequestMethod.GET)
    @ResponseBody
    public String setSession(HttpSession session){
        session.setAttribute("id",1);
        session.setAttribute("name","test");
        return "set session";
    }
    @RequestMapping(value = "/session/get",method = RequestMethod.GET)
    @ResponseBody
    public String getSession(HttpSession session) {
        System.out.println(session.getAttribute("id"));
        System.out.println(session.getAttribute("name"));
        return "get session";
    }
}
