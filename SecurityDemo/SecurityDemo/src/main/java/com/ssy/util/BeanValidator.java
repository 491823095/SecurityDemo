package com.ssy.util;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.ssy.exception.ParamException;
import org.apache.commons.collections.MapUtils;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.*;

public class BeanValidator  {
    /*校验工厂*/
    private static ValidatorFactory validatorFactory=Validation.buildDefaultValidatorFactory();

    /*map的校验方法*/
    /*Map的key是错误字段,value是错误信息*/
    public static <T> Map<String,String> validate(T t, Class... gourps){
        Validator validator=validatorFactory.getValidator();

        /*获取校验结果*/
        Set validateResult=validator.validate(t,gourps);
        /*判断获得的是否有值*/
        if (validateResult.isEmpty()){
            /*为空则返回一个空的map,这种创建的map不能再往里面添加元素*/
            return Collections.emptyMap();
        }else {
            LinkedHashMap errors=Maps.newLinkedHashMap();
            /*开始用迭代器获取内容*/
            Iterator iterator=validateResult.iterator();
            while (iterator.hasNext()){
                ConstraintViolation violation=(ConstraintViolation)iterator.next();
                /*把错误信息存放进map里面*/
                errors.put(violation.getPropertyPath().toString(),violation.getMessage());
            }
            return errors;
        }
    }

    /*list的校验方法*/
    public static Map<String,String> validateList(Collection<?> connection){
        /*先判断connection是否为空,若为空自动抛出异常*/
        Preconditions.checkNotNull(connection);

        Iterator iterator=connection.iterator();
        Map error;
        do {
            /*假如没有数据就返回一个空的map*/
            if (!iterator.hasNext()){
                return Collections.emptyMap();
            }
            /*获取值存入error*/
            Object object=iterator.next();
            error=validate(object,new  Class[0]);

        }while (error.isEmpty());
        return error;
    }

    /*整合上面两个方法写一个通用的验证方法*/
    public static Map<String,String> validateObject(Object first,Object... objects){
        /*先判断objects不为空,并且传入的是多个值时*/
        if (objects!=null&&objects.length>0){
            /*把所有内容都变成一个数组传进去变成一个list,再调用上面验证list的方法*/
           return validateList(Lists.asList(first,objects));
        }else {
            /*若传进来的只有一个值则调用验证一个值的方法*/
           return validate(first,new Class[0]);
        }
    }

    /*判断是否有异常*/
    public static void check(Object param) throws ParamException{
        Map<String,String> map=BeanValidator.validateObject(param);
        /*判断是否为空的方法,通过工具类的方法*/
        if (MapUtils.isNotEmpty(map)){
            throw new ParamException(map.toString());
        }
    }
}
