package com.ssy.filter;

import com.ssy.common.RequestHolder;
import com.ssy.pojo.SysUser;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 判断用户是否登陆
 */
@Slf4j
public class LoginFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest req=(HttpServletRequest)servletRequest;
        HttpServletResponse res=(HttpServletResponse)servletResponse;
        //获得请求的url
        String servletPath=req.getServletPath();

        SysUser sysUser=(SysUser)req.getSession().getAttribute("user");
        //若用户未空则返回登陆页面
        if (sysUser==null){
            String path="/signin.jsp";
            res.sendRedirect(path);
            return;
        }
        //存进去,后面取值都是在RequestHolder内取
        RequestHolder.add(sysUser);
        RequestHolder.add(req);
        filterChain.doFilter(servletRequest,servletResponse);
        return;
    }

    @Override
    public void destroy() {

    }
}
