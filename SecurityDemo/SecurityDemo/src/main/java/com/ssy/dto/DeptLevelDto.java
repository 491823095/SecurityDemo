package com.ssy.dto;

import com.google.common.collect.Lists;
import com.ssy.pojo.SysDept;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.beans.BeanUtils;

import java.util.List;

@Getter
@Setter
@ToString
public class DeptLevelDto extends SysDept {

    //自己包含自己能组成树形结构
    private List<DeptLevelDto> deptList=Lists.newArrayList();

    //适配方法:传入dept实体类时转换成这个结构
    public static DeptLevelDto adapt(SysDept dept){
        DeptLevelDto levelDto=new DeptLevelDto();
        //把传入的dept对象通过方法转换成deptLevelDto对象
        BeanUtils.copyProperties(dept,levelDto);
        return levelDto;
    }
}
