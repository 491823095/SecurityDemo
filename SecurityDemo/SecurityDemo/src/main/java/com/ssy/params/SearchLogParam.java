package com.ssy.params;

import com.sun.org.apache.xpath.internal.operations.Or;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class SearchLogParam {

    //类型
    private Integer type;

    private String beforeSeq;

    private String afterSeq;

    private String operator;

    private String fromTime; //yyyy-MM-dd HH:mm:ss

    private String toTime;


}
