package com.ssy.controller;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.ssy.common.JsonData;
import com.ssy.params.RoleParam;
import com.ssy.pojo.SysUser;
import com.ssy.service.*;
import com.ssy.util.StringUtil;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/sys/role")
public class SysRoleController {

    @Resource
    private SysRoleService sysRoleService;

    @Resource
    private SysTreeService sysTreeService;

    @Resource
    private SysRoleAclService sysRoleAclService;

    @Resource
    private SysRoleUserService sysRoleUserService;

    @Resource
    private SysUserService sysUserService;

    @RequestMapping("/role.page")
    public ModelAndView page(){
        return new ModelAndView("role");
    }

    @RequestMapping("/save.json")
    @ResponseBody
    public JsonData saveRole(RoleParam param){
        sysRoleService.save(param);
        return JsonData.success();
    }

    @RequestMapping("/update.json")
    @ResponseBody
    public JsonData updateRole(RoleParam param){
        sysRoleService.update(param);
        return JsonData.success();
    }

    @RequestMapping("/list.json")
    @ResponseBody
    public JsonData list(){
        return JsonData.success(sysRoleService.getAll());
    }

    @RequestMapping("/roleTree.json")
    @ResponseBody
    public JsonData roleTree(@RequestParam("roleId")int roleId){
        return JsonData.success(sysTreeService.roleTree(roleId));
    }

    @RequestMapping("/changeAcls.json")
    @ResponseBody
    public JsonData changeAcls(@RequestParam("roleId")int roleId,@RequestParam(value = "aclIds",required = false,defaultValue = "")String aclIds){
        List<Integer> aclIdList=StringUtil.splitToListInt(aclIds);
        sysRoleAclService.changeRoleAcls(roleId,aclIdList);
        return JsonData.success();
    }

    @RequestMapping("/changeUsers.json")
    @ResponseBody
    public JsonData changeUsers(@RequestParam("roleId")int roleId,@RequestParam(value = "userIds",required = false,defaultValue = "")String userIds){
        List<Integer> userIdList=StringUtil.splitToListInt(userIds);
        sysRoleUserService.changeRoleUsers(roleId,userIdList);
        return JsonData.success();
    }

    @RequestMapping("/users.json")
    @ResponseBody
    public JsonData users(@RequestParam("roleId")int roleId){
        //已选用户列表
        List<SysUser> selectedUserList=sysRoleUserService.getListByRoleId(roleId);
        //所有用户列表
        List<SysUser> allUserList=sysUserService.getAll();
        //用所有用户减去已选用户等于未选用户
        List<SysUser> unselectedUserList=Lists.newArrayList();

        //jdk1.8的加速方法
        Set<Integer> selectedUserIdSet = selectedUserList.stream().map(sysUser -> sysUser.getId()).collect(Collectors.toSet());
        for (SysUser sysUser:allUserList){
            //先判断用户状态    被选中的集合中不包含该用户
            if (sysUser.getStatus()==1 && !selectedUserIdSet.contains(sysUser.getId())){
                unselectedUserList.add(sysUser);
            }
        }
        //所有信息都放在一个map里面返回出去
        Map<String,List<SysUser>> map=Maps.newHashMap();
        map.put("selected",selectedUserList);
        map.put("unselected",unselectedUserList);
        return JsonData.success(map);
    }
}
