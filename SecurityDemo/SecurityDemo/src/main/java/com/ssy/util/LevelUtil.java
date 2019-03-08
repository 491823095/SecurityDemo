package com.ssy.util;

import org.apache.commons.lang3.StringUtils;

public class LevelUtil {

    public final static String SEPARATOR=".";

    public final static String ROOT="0";

    /*层次是：0
    *        0.1
    *        0.11
    *        0.111
    *        这就是上面设置常量的原因*/
    public static String calculateLevel(String parentLevel,int parentId){
        /*判断是否为首次传值*/
        if (!StringUtils.isNotBlank(parentLevel)){
            /*若是第一次则直接返回0,0代表着第一层*/
            return LevelUtil.ROOT;
        }else {
            /*这里是将数字组合起来变成如: 0.1之类的字符串*/
            String str=StringUtils.join(parentLevel,SEPARATOR,parentId);
            return str;
        }
    }
}
