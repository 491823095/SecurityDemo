package com.ssy.controller;

import com.ssy.common.ApplicationContextHelper;
import com.ssy.common.JsonData;
import com.ssy.dao.SysAclModuleMapper;
import com.ssy.params.TestVo;
import com.ssy.pojo.SysAclModule;
import com.ssy.util.BeanValidator;
import com.ssy.util.JsonMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.MapUtils;
import org.apache.ibatis.exceptions.PersistenceException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

@Controller
@RequestMapping("/test")
@Slf4j
public class TestController {

    @RequestMapping("/hello.json")
    @ResponseBody
    public JsonData hello(){
        log.info("搞我？");
        throw new RuntimeException("test exception");
        //return JsonData.success("hello,permission");
    }

    @RequestMapping("/validate.json")
    @ResponseBody
    public JsonData validate(TestVo vo){

        SysAclModuleMapper moduleMapper=ApplicationContextHelper.popBean(SysAclModuleMapper.class);
        SysAclModule sysAclModule=moduleMapper.selectByPrimaryKey(1);
        log.info(JsonMapper.obj2String(sysAclModule));
        //log.info("validate");
        /*测试刚写的validate方法,验证*/
       /* try {
            Map<String,String > map=BeanValidator.validateObject(vo);
            *//*判断map不能为空*//*
            if (MapUtils.isNotEmpty(map)){
                for (Map.Entry<String,String> entry:map.entrySet() ){
                    *//*占位符的方式输入日志*//*
                    log.info("{}->{}",entry.getKey(),entry.getValue());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }*/
        BeanValidator.check(vo);
        return JsonData.success("test validate");
    }

}
