package com.ssy.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.ssy.beans.LogType;
import com.ssy.common.RequestHolder;
import com.ssy.dao.SysLogMapper;
import com.ssy.dao.SysRoleUserMapper;
import com.ssy.dao.SysUserMapper;
import com.ssy.pojo.SysLogWithBLOBs;
import com.ssy.pojo.SysRoleUser;
import com.ssy.pojo.SysUser;
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
public class SysRoleUserService {

    @Resource
    private SysRoleUserMapper sysRoleUserMapper;

    @Resource
    private SysUserMapper sysUserMapper;

    @Resource
    private SysLogMapper sysLogMapper;

    //取出该角色下所有的用户列表
    public List<SysUser> getListByRoleId(int roleId){
        List<Integer> userIdList=sysRoleUserMapper.getUserIdListByRoleId(roleId);
        if (CollectionUtils.isEmpty(userIdList)){
            return Lists.newArrayList();
        }
        return sysUserMapper.getByIdList(userIdList);
    }

    public void changeRoleUsers(int roleId, List<Integer> userIdList) {
        List<Integer> originUserIdList = sysRoleUserMapper.getUserIdListByRoleId(roleId);
        if (originUserIdList.size() == userIdList.size()) {
            Set<Integer> originUserIdSet = Sets.newHashSet(originUserIdList);
            Set<Integer> userIdSet = Sets.newHashSet(userIdList);
            originUserIdSet.removeAll(userIdSet);
            if (CollectionUtils.isEmpty(originUserIdSet)) {
                return;
            }
        }
        updateRoleUsers(roleId, userIdList);
        saveRoleUserLog(roleId,originUserIdList,userIdList);
    }

    @Transactional
    protected void updateRoleUsers(int roleId,List<Integer> userIdList){
        sysRoleUserMapper.deleteByRoleId(roleId);
        if (CollectionUtils.isEmpty(userIdList)){
            return ;
        }
        List<SysRoleUser> roleUserList=Lists.newArrayList();
        for (Integer userId: userIdList){
            SysRoleUser roleUser=SysRoleUser.builder().roleId(roleId).userId(userId).
                    operateIp(IpUtil.getRemoteIp(RequestHolder.getCurrentRequest())).
                    operator(RequestHolder.getCurrentUser().getUsername()).
                    operateTime(new Date()).build();
            roleUserList.add(roleUser);
        }
        sysRoleUserMapper.batchInsert(roleUserList);
    }

    private void saveRoleUserLog(int roleId,List<Integer> before,List<Integer> after){
        SysLogWithBLOBs sysLog=new SysLogWithBLOBs();
        sysLog.setType(LogType.TYPE_ROLE_USER);
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
