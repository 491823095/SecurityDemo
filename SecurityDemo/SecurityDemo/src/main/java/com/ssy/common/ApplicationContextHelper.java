/**
 * 调用SpringBean的方法,在Spring-servlet内配置优先加载到Spring
 */
package com.ssy.common;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component("applicationContextHelper")
public class ApplicationContextHelper implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext Context) throws BeansException {
        applicationContext=Context;
    }

    public static <T> T popBean(Class<T> clazz){
        if (applicationContext==null){
            return  null;
        }
        return applicationContext.getBean(clazz);
    }

    public static <T> T popBean(String name,Class<T> clazz){
        if (applicationContext==null){
            return null;
        }
        return applicationContext.getBean(name,clazz);
    }
}
