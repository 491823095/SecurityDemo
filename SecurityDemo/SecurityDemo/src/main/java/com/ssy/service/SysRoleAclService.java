package com.ssy.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.ssy.beans.LogType;
import com.ssy.common.RequestHolder;
import com.ssy.dao.SysLogMapper;
import com.ssy.dao.SysRoleAclMapper;
import com.ssy.pojo.SysLogWithBLOBs;
import com.ssy.pojo.SysRoleAcl;
import com.ssy.util.IpUtil;
import com.ssy.util.JsonMapper;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Service
public class SysRoleAclService {

    @Resource
    private SysRoleAclMapper sysRoleAclMapper;

    @Resource
    private SysLogMapper sysLogMapper;

    public void changeRoleAcls(Integer roleId, List<Integer> aclIdList){
        List<Integer> originAclIdList=sysRoleAclMapper.getAclIdListByRoleIdList(Lists.newArrayList(roleId));
        //判断传进来的集合与查出来的集合是否相等，先判断长度
        if (originAclIdList.size()==aclIdList.size()){
            Set<Integer> originAclIdSet=Sets.newHashSet(originAclIdList);
            Set<Integer> aclIdSet=Sets.newHashSet(aclIdList);
            //若长度相同，那么移除不同，还有剩余
            originAclIdSet.removeAll(aclIdSet);
            if (CollectionUtils.isEmpty(originAclIdSet)){
                return ;
            }
        }
        //若上面判断都通过则更新
        updateRoleAcls(roleId,aclIdList);

        saveRoleAclLog(roleId,originAclIdList,aclIdList);
    }

    @Transactional
    public void updateRoleAcls(int roleId,List<Integer> aclIdList){
        //先删除原来的值
        sysRoleAclMapper.deleteByRoleId(roleId);
        //判断传过来的集合是否为空
        if (CollectionUtils.isEmpty(aclIdList)){
            return ;
        }
        //填充值进去新增的roleAclList
        List<SysRoleAcl> roleAclList=Lists.newArrayList();
        for (Integer aclId:aclIdList){
            SysRoleAcl roleAcl=SysRoleAcl.builder().roleId(roleId).aclId(aclId).
                    operateIp(IpUtil.getRemoteIp(RequestHolder.getCurrentRequest())).
                    operateTime(new Date()).operator(RequestHolder.getCurrentUser().getUsername()).build();
            roleAclList.add(roleAcl);
        }
        //批量更新
        sysRoleAclMapper.batchInsert(roleAclList);
    }

    private void saveRoleAclLog(int roleId, List<Integer>before,List<Integer> after){
        SysLogWithBLOBs sysLog=new SysLogWithBLOBs();
        sysLog.setType(LogType.TYPE_ROLE_ACL);
        sysLog.setTargetId(roleId);
        sysLog.setOldValue(before==null ? "" : JsonMapper.obj2String(before));
        sysLog.setNewValue(after==null ? "" : JsonMapper.obj2String(after));
        sysLog.setOperator(RequestHolder.getCurrentUser().getUsername());
        sysLog.setOperateIp(IpUtil.getRemoteIp(RequestHolder.getCurrentRequest()));
        sysLog.setOperateTime(new Date());
        sysLog.setStatus(1);
        sysLogMapper.insertSelective(sysLog);
    }
}
