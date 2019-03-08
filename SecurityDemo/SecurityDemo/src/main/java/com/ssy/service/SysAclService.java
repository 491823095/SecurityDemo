package com.ssy.service;

import com.google.common.base.Preconditions;
import com.ssy.beans.PageQuery;
import com.ssy.beans.PageResult;
import com.ssy.common.RequestHolder;
import com.ssy.dao.SysAclMapper;
import com.ssy.exception.ParamException;
import com.ssy.params.AclParam;
import com.ssy.pojo.SysAcl;
import com.ssy.util.BeanValidator;
import com.ssy.util.IpUtil;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Service
public class SysAclService {
    @Resource
    private SysAclMapper sysAclMapper;

    @Resource
    private SysLogService sysLogService;

    public void save(AclParam aclParam){
        BeanValidator.check(aclParam);
        if (checkExist(aclParam.getAclModuleId(),aclParam.getName(),aclParam.getId())){
            throw new ParamException("当前权限模块下存在相同名称的权限点");
        }
        SysAcl sysAcl=SysAcl.builder().name(aclParam.getName()).aclModuleId(aclParam.getAclModuleId()).
                url(aclParam.getUrl()).type(aclParam.getType()).status(aclParam.getStatus()).seq(aclParam.getSeq()).
                remark(aclParam.getRemark()).build();
        sysAcl.setCode(generateCode());
        sysAcl.setOperateIp(IpUtil.getRemoteIp(RequestHolder.getCurrentRequest()));
        sysAcl.setOperateTime(new Date());
        sysAcl.setOperator(RequestHolder.getCurrentUser().getUsername());
        sysAclMapper.insertSelective(sysAcl);

        sysLogService.saveAclLog(null,sysAcl);
    }

    public void update(AclParam aclParam){
        BeanValidator.check(aclParam);
        if (checkExist(aclParam.getAclModuleId(),aclParam.getName(),aclParam.getId())){
            throw new ParamException("当前权限模块下存在相同名称的权限点");
        }
        SysAcl before=sysAclMapper.selectByPrimaryKey(aclParam.getId());
        Preconditions.checkNotNull(before,"待更新权限点不存在");

        SysAcl after=SysAcl.builder().id(aclParam.getId()).name(aclParam.getName()).aclModuleId(aclParam.getAclModuleId()).
                url(aclParam.getUrl()).type(aclParam.getType()).status(aclParam.getStatus()).seq(aclParam.getSeq()).
                remark(aclParam.getRemark()).build();
        after.setCode(generateCode());
        after.setOperateIp(IpUtil.getRemoteIp(RequestHolder.getCurrentRequest()));
        after.setOperateTime(new Date());
        after.setOperator(RequestHolder.getCurrentUser().getUsername());
        sysAclMapper.updateByPrimaryKeySelective(after);

        sysLogService.saveAclLog(before,after);
    }

    public boolean checkExist(int aclModuleId,String name,Integer id){
        return sysAclMapper.countByNameAndAclModuleId(aclModuleId,name,id)>0;
    }

    //唯一code码
    public String generateCode(){
        SimpleDateFormat dateFormat=new SimpleDateFormat("yyyyMMddHHmmss");
        return dateFormat.format(new Date())+"_"+(int)(Math.random()*100);
    }

    //
    public PageResult<SysAcl> getPageByAclModuleId(int aclModuleId,PageQuery page){
        BeanValidator.check(page);
        int count=sysAclMapper.countByAclModuleId(aclModuleId);
        if (count>0){
            List<SysAcl> aclList=sysAclMapper.getPageByAclModuleId(aclModuleId,page);
            return PageResult.<SysAcl>builder().data(aclList).total(count).build();
        }
        return PageResult.<SysAcl>builder().build();
    }
}
