package com.ssy.service;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.ssy.common.RequestHolder;
import com.ssy.dao.SysRoleAclMapper;
import com.ssy.dao.SysRoleMapper;
import com.ssy.dao.SysRoleUserMapper;
import com.ssy.dao.SysUserMapper;
import com.ssy.exception.ParamException;
import com.ssy.params.RoleParam;
import com.ssy.pojo.SysRole;
import com.ssy.pojo.SysUser;
import com.ssy.util.BeanValidator;
import com.ssy.util.IpUtil;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SysRoleService {

    @Resource
    private SysRoleMapper sysRoleMapper;

    @Resource
    private SysRoleUserMapper sysRoleUserMapper;

    @Resource
    private SysRoleAclMapper sysRoleAclMapper;

    @Resource
    private SysUserMapper sysUserMapper;

    @Resource
    private SysLogService sysLogService;

    public void save(RoleParam roleParam){
        BeanValidator.check(roleParam);
        if (checkExist(roleParam.getName(),roleParam.getId())){
            throw new ParamException("角色名称已经存在");
        }
        SysRole role=SysRole.builder().name(roleParam.getName()).status(roleParam.getStatus()).
                type(roleParam.getType()).remark(roleParam.getRemark()).build();
        role.setOperateIp(IpUtil.getRemoteIp(RequestHolder.getCurrentRequest()));
        role.setOperator(RequestHolder.getCurrentUser().getUsername());
        role.setOperateTime(new Date());
        sysRoleMapper.insertSelective(role);

        sysLogService.saveRoleLog(null,role);
    }

    public void update(RoleParam roleParam){
        BeanValidator.check(roleParam);
        if (checkExist(roleParam.getName(),roleParam.getId())){
            throw new ParamException("角色名称已经存在");
        }
        SysRole before=sysRoleMapper.selectByPrimaryKey(roleParam.getId());
        Preconditions.checkNotNull(before,"待更新的角色不存在");
        SysRole after=SysRole.builder().id(roleParam.getId()).name(roleParam.getName()).status(roleParam.getStatus()).
                type(roleParam.getType()).remark(roleParam.getRemark()).build();
        after.setOperateIp(IpUtil.getRemoteIp(RequestHolder.getCurrentRequest()));
        after.setOperator(RequestHolder.getCurrentUser().getUsername());
        after.setOperateTime(new Date());
        sysRoleMapper.updateByPrimaryKeySelective(after);

        sysLogService.saveRoleLog(before,after);
    }

    public List<SysRole> getAll(){
        return sysRoleMapper.getAll();
    }

    private boolean checkExist(String name,Integer id){
        return sysRoleMapper.countByName(name,id)>0;
    }

    public List<SysRole> getRoleListByUserId(int userId){
        List<Integer> roleIdList=sysRoleUserMapper.getRoleIdListByUserId(userId);
        if (CollectionUtils.isEmpty(roleIdList)){
            return Lists.newArrayList();
        }
        return sysRoleMapper.getByIdList(roleIdList);
    }

    public List<SysRole> getRoleListByAclId(int aclId){
        List<Integer> roleIdList=sysRoleAclMapper.getRoleIdListByAclId(aclId);
        if (CollectionUtils.isEmpty(roleIdList)){
            return Lists.newArrayList();
        }
        return sysRoleMapper.getByIdList(roleIdList);
    }

    public List<SysUser> getUserListByRoleList(List<SysRole> roleList){
        if (CollectionUtils.isEmpty(roleList)){
            return Lists.newArrayList();
        }
        List<Integer> roleIdList=roleList.stream().map(role -> role.getId()).collect(Collectors.toList());
        //通过上面的角色id集合找用户
        List<Integer> userIdList=sysRoleUserMapper.getUserIdListByRoleIdList(roleIdList);
        if (CollectionUtils.isEmpty(userIdList)){
            return Lists.newArrayList();
        }
        return sysUserMapper.getByIdList(userIdList);
    }
}
