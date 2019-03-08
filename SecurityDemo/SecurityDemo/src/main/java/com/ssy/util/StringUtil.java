package com.ssy.util;

import com.google.common.base.Splitter;

import java.util.List;
import java.util.stream.Collectors;

public class StringUtil {

    public static List<Integer> splitToListInt(String str){
        //以逗号为分割，去除空格和空值，转换成List
        List<String> strList=Splitter.on(",").trimResults().omitEmptyStrings().splitToList(str);
        return strList.stream().map(strItem -> Integer.parseInt(strItem)).collect(Collectors.toList());

    }
}
