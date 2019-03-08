/**
 * 用于给json数据进行封装,ret是返回结果是否成功,data是json数据本体,若ret是false则msg是返回错误内容的信息
 */
package com.ssy.common;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;


@Getter
@Setter
public class JsonData {

    /*返回结果是否失败*/
    private boolean ret;

    /*异常信息*/
    private String msg;

    /*正常返回时给前台的数据*/
    private Object data;

    public JsonData(boolean ret){
        this.ret=ret;
    }

    /*成功时调用的静态方法*/
    public static JsonData success(Object object,String msg){
        JsonData jsonData = new JsonData(true);
        jsonData.data=object;
        jsonData.msg=msg;
        return  jsonData;
    }
    public static JsonData success(Object object){
        JsonData jsonData = new JsonData(true);
        jsonData.data=object;
        return  jsonData;
    }
    public static JsonData success(){
        JsonData jsonData = new JsonData(true);
        return  jsonData;
    }

    /*失败调用的静态方法*/
    public static JsonData fail(String msg){
        JsonData jsonData = new JsonData(false);
        jsonData.msg=msg;
        return jsonData;
    }

    public Map<String,Object> toMap(){
        HashMap<String,Object> result=new HashMap<>();
        result.put("ret",ret);
        result.put("msg",msg);
        result.put("data",data);
        return result;
    }
}
