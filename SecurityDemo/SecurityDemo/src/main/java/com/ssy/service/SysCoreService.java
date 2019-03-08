package com.ssy.service;

import com.google.common.collect.Lists;
import com.ssy.beans.CacheKeyConstants;
import com.ssy.common.RequestHolder;
import com.ssy.dao.SysAclMapper;
import com.ssy.dao.SysRoleAclMapper;
import com.ssy.dao.SysRoleUserMapper;
import com.ssy.pojo.SysAcl;
import com.ssy.util.JsonMapper;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.type.TypeReference;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SysCoreService {

    @Resource
    private SysAclMapper sysAclMapper;

    @Resource
    private SysRoleUserMapper sysRoleUserMapper;

    @Resource
    private SysRoleAclMapper sysRoleAclMapper;

    @Resource
    private SysCacheService sysCacheService;

    public List<SysAcl> getCurrentUserAclList(){
        //当前用户id,在session内拿(RequestHolder)
        int userId=RequestHolder.getCurrentUser().getId();
        return getUserAclList(userId);
    }

    //当前指定角色的权限列表
    public List<SysAcl> getRoleAclList(int roleId){
        List<Integer> aclIdList=sysRoleAclMapper.getAclIdListByRoleIdList(Lists.<Integer>newArrayList(roleId));
        if (CollectionUtils.isEmpty(aclIdList)){
            return Lists.newArrayList();
        }
        return sysAclMapper.getByIdList(aclIdList);
    }

    //获取当前用户权限列表
    public List<SysAcl> getUserAclList(int userId){
        //判断是否超级管理员
        if (isSuperAdmin()){
            return sysAclMapper.getAll();
        }
        //判断已有的权限
        List<Integer> userRoleIdList=sysRoleUserMapper.getRoleIdListByUserId(userId);
        if (CollectionUtils.isEmpty(userRoleIdList)){
            return Lists.newArrayList();
        }
        List<Integer> userAclIdList=sysRoleAclMapper.getAclIdListByRoleIdList(userRoleIdList);

        if (CollectionUtils.isEmpty(userAclIdList)){
            return Lists.newArrayList();
        }
        return sysAclMapper.getByIdList(userAclIdList);
    }

    //判断是否超级管理员
    public boolean isSuperAdmin(){
        return true;
    }

    public boolean hasUrlAcl(String url){
        if (isSuperAdmin()){
            return true;
        }
        List<SysAcl> aclList=sysAclMapper.getByUrl(url);
        if (CollectionUtils.isEmpty(aclList)){
            return true;
        }

        List<SysAcl> userAclIdList=getCurrentUserAclListFroCache();
        Set<Integer> userAclIdSet =userAclIdList.stream().map(acl -> acl.getId()).collect(Collectors.toSet());

        boolean hasValidAcl=false;

        //规则:只要有一个权限点有权限,那么我们就认为有访问权限
        for (SysAcl acl:aclList){
            //判断用户是否有权限点访问
            if (acl==null || acl.getStatus()!=1){
                continue;
            }
            hasValidAcl=true;
            //判断acluser里面的权限是否包含对应id
            if (userAclIdSet.contains(acl.getId())){
                return true;
            }
        }
        if (!hasValidAcl){
            return true;
        }
        return false;
    }

    public List<SysAcl> getCurrentUserAclListFroCache(){
        int userId=RequestHolder.getCurrentUser().getId();
        String cacheValue=sysCacheService.getFromCache(CacheKeyConstants.USER_ACLS,String.valueOf(userId));
        if (StringUtils.isBlank(cacheValue)){
            List<SysAcl> aclList=getCurrentUserAclList();
            if (CollectionUtils.isNotEmpty(aclList)){
                sysCacheService.saveCache(JsonMapper.obj2String(aclList),600,CacheKeyConstants.USER_ACLS,String.valueOf(userId));
            }
            return  aclList;
        }
        return JsonMapper.String2Obj(cacheValue, new TypeReference<List<SysAcl>>() {
        });
    }
}
