package com.ssy.filter;

import com.google.common.base.Splitter;
import com.google.common.collect.Sets;
import com.ssy.common.ApplicationContextHelper;
import com.ssy.common.JsonData;
import com.ssy.common.RequestHolder;
import com.ssy.pojo.SysUser;
import com.ssy.service.SysCoreService;
import com.ssy.util.JsonMapper;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

@Slf4j
public class AclControlFilter implements Filter {

    private static Set<String> exclusionUrlsSet=Sets.newConcurrentHashSet();

    //用户无权限访问页面
    private final  static String noAuthUrl="/sys/user/noAuth.page";

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        //读取配置中的白名单字段并存在全局Set里面
        String exclusionUrls=filterConfig.getInitParameter("exclusionUrls");
        List<String> exclusionUrlList=Splitter.on(",").trimResults().omitEmptyStrings().splitToList(exclusionUrls);
        exclusionUrlsSet=Sets.newConcurrentHashSet(exclusionUrlList);
        exclusionUrlsSet.add(noAuthUrl);
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request=(HttpServletRequest)servletRequest;
        HttpServletResponse response=(HttpServletResponse)servletResponse;
        String servletPath=request.getServletPath();
        Map requestMap=request.getParameterMap();

        if (exclusionUrlsSet.contains(servletPath)){
            filterChain.doFilter(servletRequest,servletResponse);
            return ;
        }

        //判断权限
        SysUser sysUser=RequestHolder.getCurrentUser();
        if (sysUser==null){
            log.info("someone visit {} ,but no login,parameter:{}",servletPath,JsonMapper.obj2String(requestMap));
            noAuth(request,response);
            return ;
        }

        //在Spring容器内获取syscoreservice
        SysCoreService sysCoreService=ApplicationContextHelper.popBean(SysCoreService.class);
        if (!sysCoreService.hasUrlAcl(servletPath)){
            log.info("{} visit {},but no login,parameter:{}",JsonMapper.obj2String(sysUser),servletPath,JsonMapper.obj2String(requestMap));
            noAuth(request,response);
            return;
        }

        filterChain.doFilter(servletRequest,servletResponse);
        return;
    }

    private void noAuth(HttpServletRequest request,HttpServletResponse response){
        String servletPath=request.getServletPath();
        //判断请求是json还是页面
        if (servletPath.endsWith(".json")){
            JsonData jsonData=JsonData.fail("没有访问权限,如需要访问,请联系管理员");
            try {
                response.setHeader("Content-Type","application/json");
                response.getWriter().print(JsonMapper.obj2String(jsonData));
                return;
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else{
            clientRedirect(noAuthUrl,response);
            return;
        }
    }

    private void clientRedirect(String url,HttpServletResponse response){
        response.setHeader("Content-Type","text/html");
        try {
            response.getWriter().print("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n"
                    + "<html xmlns=\"http://www.w3.org/1999/xhtml\">\n" + "<head>\n" + "<meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\"/>\n"
                    + "<title>跳转中...</title>\n" + "</head>\n" + "<body>\n" + "跳转中，请稍候...\n" + "<script type=\"text/javascript\">//<![CDATA[\n"
                    + "window.location.href='" + url + "?ret='+encodeURIComponent(window.location.href);\n" + "//]]></script>\n" + "</body>\n" + "</html>\n");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void destroy() {

    }
}
