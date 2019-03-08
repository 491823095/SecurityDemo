package com.ssy.service;

import com.google.common.base.Preconditions;
import com.ssy.common.RequestHolder;
import com.ssy.dao.SysAclMapper;
import com.ssy.dao.SysAclModuleMapper;
import com.ssy.exception.ParamException;
import com.ssy.params.AclModelParam;
import com.ssy.pojo.SysAclModule;
import com.ssy.util.BeanValidator;
import com.ssy.util.IpUtil;
import com.ssy.util.LevelUtil;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@Service
public class SysAclModuleService {
    @Resource
    private SysAclModuleMapper sysAclModuleMapper;

    @Resource
    private SysAclMapper sysAclMapper;

    @Resource
    private SysLogService sysLogService;

    public void save(AclModelParam param){
        BeanValidator.check(param);
        /*先判断该信息是否符合要求,部门相同返回true,抛出异常*/
        if (checkExist(param.getParentId(),param.getName(),param.getId())){
            throw new ParamException("同一层级下存在相同名称的权限模块");
        }
        SysAclModule aclModule=SysAclModule.builder().name(param.getName()).
                parentId(param.getParentId()).seq(param.getSeq()).status(param.getStatus()).remark(param.getRemark()).build();
        //设置level等级
        aclModule.setLevel(LevelUtil.calculateLevel(getLevel(param.getParentId()),param.getParentId()));
        aclModule.setOperator(RequestHolder.getCurrentUser().getUsername());
        aclModule.setOperateIp(IpUtil.getRemoteIp(RequestHolder.getCurrentRequest()));
        aclModule.setOperatorTime(new Date());
        sysAclModuleMapper.insertSelective(aclModule);

        sysLogService.saveAclModuleLog(null,aclModule);
    }

    public void update(AclModelParam param){
        BeanValidator.check(param);
        /*先判断该信息是否符合要求,部门相同返回true,抛出异常*/
        if (checkExist(param.getParentId(),param.getName(),param.getId())){
            throw new ParamException("同一层级下存在相同名称的权限模块");
        }
        SysAclModule before=sysAclModuleMapper.selectByPrimaryKey(param.getId());
        Preconditions.checkNotNull(before,"待更新的权限模块不存在");
        SysAclModule after=SysAclModule.builder().name(param.getName()).parentId(param.getParentId()).
                seq(param.getSeq()).status(param.getStatus()).remark(param.getRemark()).id(param.getId()).build();
        //设置level等级
        after.setLevel(LevelUtil.calculateLevel(getLevel(param.getParentId()),param.getParentId()));
        after.setOperator(RequestHolder.getCurrentUser().getUsername());
        after.setOperateIp(IpUtil.getRemoteIp(RequestHolder.getCurrentRequest()));
        after.setOperatorTime(new Date());
        updateWithChild(before,after);

        sysLogService.saveAclModuleLog(before,after);
    }

    @Transactional
    protected void updateWithChild(SysAclModule before,SysAclModule after){
        //判断是否需要更新子部门
        String newLevelPrefix=after.getLevel();
        String oldLevelPrefix=before.getLevel();
        //判断是否一致，不一致则开始更新子部门
        if (!newLevelPrefix.equals(oldLevelPrefix)){
            List<SysAclModule> aclModuleList=sysAclModuleMapper.getChildAclModuleListByLevel(before.getLevel());
            if (CollectionUtils.isNotEmpty(aclModuleList)){
                for (SysAclModule aclModule:aclModuleList){
                    String level=aclModule.getLevel();
                    //判断before的level前缀是否与after的一致
                    if (level.indexOf(oldLevelPrefix)==0){
                        level=newLevelPrefix+level.substring(oldLevelPrefix.length());
                        aclModule.setLevel(level);
                    }
                }
                sysAclModuleMapper.batchUpdateLevel(aclModuleList);
            }
        }
        sysAclModuleMapper.updateByPrimaryKeySelective(after);
    }

    private boolean checkExist(Integer parentId,String AclModuleName,Integer deptId){
        return sysAclModuleMapper.countByNameAndParentId(parentId,AclModuleName,deptId)>0;
    }

    /*获取上级部门id的方法*/
    private String getLevel(Integer AclModuleId){
        SysAclModule aclModule=sysAclModuleMapper.selectByPrimaryKey(AclModuleId);
        if (aclModule==null){
            return null;
        }
        return aclModule.getLevel();
    }

    public void delete(int aclModuleId){
        //先判断module是否存在
        SysAclModule aclModule=sysAclModuleMapper.selectByPrimaryKey(aclModuleId);
        Preconditions.checkNotNull(aclModule,"待删除的权限模块不存在,无法删除");
        //判断是否存在子模块
        if (sysAclModuleMapper.countByParentId(aclModule.getId())>0){
            throw new ParamException("当前模块下面有子模块,无法删除");
        }
        //判断是否存在权限点
        if (sysAclMapper.countByAclModuleId(aclModule.getId())>0){
            throw  new ParamException("当前模块下面有用户,无法删除");
        }
        //若上面都通过则进行删除操作
        sysAclMapper.deleteByPrimaryKey(aclModuleId);
    }
}
