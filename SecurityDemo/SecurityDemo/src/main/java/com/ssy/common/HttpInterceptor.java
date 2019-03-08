/**
 *
 */
package com.ssy.common;

import com.ssy.util.JsonMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@Slf4j
public class HttpInterceptor extends HandlerInterceptorAdapter {

    /*类似于aop的前置增强*/
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String url=request.getRequestURI().toString();
        Map parameterMap=request.getParameterMap();
        log.info("request 前置增强 : url:{},params:{}",url,JsonMapper.obj2String(parameterMap));
        return true;
    }

    /*后置增强*/
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        String url=request.getRequestURI().toString();
        Map parameterMap=request.getParameterMap();
        log.info("request 后置增强  url:{},params:{}",url,JsonMapper.obj2String(parameterMap));
        //super.postHandle(request, response, handler, modelAndView);
        removeThreadLocalInfo();
    }

    /*最终增强*/
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        String url=request.getRequestURI().toString();
        Map parameterMap=request.getParameterMap();
        log.info("request 最终增强  url:{},params:{}",url,JsonMapper.obj2String(parameterMap));
        //super.afterCompletion(request, response, handler, ex);
        removeThreadLocalInfo();
    }

    //用于清除多余进程
    public void removeThreadLocalInfo(){
        RequestHolder.remove();
    }
}
