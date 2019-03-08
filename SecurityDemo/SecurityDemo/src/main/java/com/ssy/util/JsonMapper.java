/**
 *
 */
package com.ssy.util;

import lombok.extern.slf4j.Slf4j;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.ser.impl.SimpleFilterProvider;
import org.codehaus.jackson.type.TypeReference;

import java.io.IOException;

@Slf4j
public class JsonMapper {

    private static ObjectMapper objectMapper=new ObjectMapper();

    static {
        //config
        objectMapper.disable(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES);

        objectMapper.configure(SerializationConfig.Feature.FAIL_ON_EMPTY_BEANS,false);

        objectMapper.setFilters(new SimpleFilterProvider().setFailOnUnknownId(false));
        /*排除转换json格式时为空的字段*/
        objectMapper.setSerializationInclusion(JsonSerialize.Inclusion.NON_EMPTY);
    }

    /*object对象转String方法*/
    public static <T> String obj2String(T src){
        /*判断传来的值是否为空*/
        if (src==null){
            return null;
        }
        /*不为空则判断是否为String,为了防止出现转换异常,需要捕抓异常并返回null*/
        try {
            return src instanceof String ? (String) src : objectMapper.writeValueAsString(src);
        } catch (IOException e) {
            /**/
            log.warn("日志:parse object to String exception error:{}",e);
            return null;
        }
    }

    /*String方法转object对象*/
    /*参数用的是jackson里的类型*/
    public static <T> T String2Obj(String src, TypeReference<T> tTypeReference){
        /*判断传过来的字符串和类型不为空*/
        if (src==null || tTypeReference==null){
            return null;
        }
        try {
            return (T) (tTypeReference.getType().equals(String.class) ? src : objectMapper.readValue( src,tTypeReference));
        } catch (Exception e) {
            log.warn("parse String to Object exception,String :{},TypeReference<T>:{},error:{}",src,tTypeReference.getType(),e);
            return null;
        }
    }

}
