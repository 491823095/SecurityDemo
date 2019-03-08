package com.ssy.dto;

import com.google.common.collect.Lists;
import com.ssy.pojo.SysAclModule;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.beans.BeanUtils;

import java.util.List;

@Getter
@Setter
@ToString
public class AclModuleLevelDto extends SysAclModule {

    private List<AclModuleLevelDto> aclModuleList=Lists.newArrayList();

    private List<AclDto> aclList=Lists.newArrayList();

    public static AclModuleLevelDto adapt(SysAclModule sysAclModule){
        AclModuleLevelDto aclModuleLevelDto=new AclModuleLevelDto();
        BeanUtils.copyProperties(sysAclModule,aclModuleLevelDto);
        return aclModuleLevelDto;
    }
}
