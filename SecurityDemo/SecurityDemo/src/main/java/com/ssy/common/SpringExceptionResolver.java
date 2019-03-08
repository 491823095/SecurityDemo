/**
 * Spring异常处理类
 */
package com.ssy.common;

import com.ssy.exception.ParamException;
import com.ssy.exception.PermissionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
public class SpringExceptionResolver implements HandlerExceptionResolver {
    @Override
    public ModelAndView resolveException(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) {

        /*获得请求的地址*/
        String url=httpServletRequest.getRequestURI().toString();
        ModelAndView modelAndView;
        /*全局默认异常信息*/
        String defaultMsg="System error(默认异常)";

        /*通过规范接口的后缀来判断是页面请求还是json请求*/
        /*从最后开始判断*/
        if (url.endsWith(".json")){
            /*判断异常是否为我们自定义的异常*/
            if (e instanceof PermissionException || e instanceof ParamException){
                /*把异常信息返回给jsonData*/
                JsonData jsonData = JsonData.fail(e.getMessage());
                /*这里的jasonView对应在spring-servlet内的bean,关联起来,解析json数据*/
                modelAndView=new ModelAndView("jasonView",jsonData.toMap());
                /*假如不是自己的异常*/
            }else {
                /*假如不是我们自定义的异常*/
                /*把全局默认异常给返回*/
                log.error("unknow json exception,url:"+url,e);
                JsonData jsonData = JsonData.fail(defaultMsg);
                modelAndView=new ModelAndView("jasonView",jsonData.toMap());
            }

            /*页面请求*/
        }else if(url.endsWith(".page")){
            log.error("unknow page exception,url:"+url,e);
            JsonData jsonData=JsonData.fail(defaultMsg);
            modelAndView=new ModelAndView("exception",jsonData.toMap());

            /*若不是页面请求也不是json请求时*/
        }else {
            log.error("unknow exception,url:"+url,e);
            JsonData jsonData=JsonData.fail(defaultMsg);
            modelAndView=new ModelAndView("jasonView",jsonData.toMap());
        }
        return modelAndView;
    }
}
