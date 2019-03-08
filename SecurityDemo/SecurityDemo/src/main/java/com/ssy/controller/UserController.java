package com.ssy.controller;

import com.ssy.pojo.SysUser;
import com.ssy.service.SysUserService;
import com.ssy.util.MD5Util;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

/**
 * 用户操作的controller
 */
@SessionAttributes(value = {"user"},types = {SysUser.class})
@Controller
public class UserController {

    @Resource
    private SysUserService sysUserService;

    @RequestMapping("/login.page")
    public ModelAndView login(@RequestParam("username")String username,
                              @RequestParam("password")String password,
                              @RequestParam("ret")String ret,
                              Map<String,Object> map){
        SysUser sysUser=sysUserService.findByKeyword(username);

        //登陆错误的提示
        String errorMsg="";
        //判断非空和密码是否一致和状态是否被冻结
        if (StringUtils.isBlank(username)){
            errorMsg="用户名不可为空";
        }else if (StringUtils.isBlank(password)){
            errorMsg="密码不可为空";
        }else if (sysUser==null) {
            errorMsg="查询不到指定的用户";
        }else if(!sysUser.getPassword().equals(MD5Util.encrypt(password))){
            errorMsg="用户名或密码错误";
        }else if(sysUser.getStatus()!=1){
            errorMsg="用户已被冻结,请联系管理员";
        }else{
            //登陆成功了
            map.put("user",sysUser);
            if (StringUtils.isNotBlank(ret)){
                return new ModelAndView("redirect:"+ret);
            }else{
                System.out.println("登陆成功");
                return new ModelAndView("redirect:/admin/index.page");
            }
        } //if
        //返回错误信息
        map.put("error",errorMsg);
        map.put("username",username);
        if (StringUtils.isNotBlank(ret)){
            map.put("ret",ret);
        }
        String path="signin.jsp";
        return new ModelAndView("forward:/"+path);
    } //login

    @RequestMapping("/logout.page")
    public void logout(HttpServletRequest request, HttpServletResponse response){
        //清空session
        request.getSession().invalidate();
        String path="signin.jsp";
        try {
            //注销后返回首页
            response.sendRedirect(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
