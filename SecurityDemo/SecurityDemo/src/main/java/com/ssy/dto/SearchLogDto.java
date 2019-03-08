package com.ssy.dto;


import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

@Setter
@Getter
@ToString
public class SearchLogDto {
    //类型
    private Integer type;

    private String beforeSeq;

    private String afterSeq;

    private String operator;

    private Date fromTime; //yyyy-MM-dd HH:mm:ss

    private Date toTime;
}
