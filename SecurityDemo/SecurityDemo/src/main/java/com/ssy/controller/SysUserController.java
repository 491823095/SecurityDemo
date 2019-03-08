package com.ssy.controller;

/**
 * 作为系统后台使用的controller
 */

import com.google.common.collect.Maps;
import com.ssy.beans.PageQuery;
import com.ssy.beans.PageResult;
import com.ssy.common.JsonData;
import com.ssy.params.UserParam;
import com.ssy.pojo.SysUser;
import com.ssy.service.SysRoleService;
import com.ssy.service.SysTreeService;
import com.ssy.service.SysUserService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import java.util.Map;

@Controller
@RequestMapping("/sys/user")
public class SysUserController {

    @Resource
    private SysUserService sysUserService;

    @Resource
    private SysTreeService sysTreeService;

    @Resource
    private SysRoleService sysRoleService;

    @RequestMapping("/noAuth.page")
    public ModelAndView moAuth(){
        return new ModelAndView("noAuth");
    }

    /*增*/
    @RequestMapping("/save.json")
    @ResponseBody
    public JsonData saveUser(UserParam param){
        sysUserService.save(param);
        return JsonData.success();
    }

    /*改*/
    @RequestMapping("/update.json")
    @ResponseBody
    public JsonData updateUser(UserParam param){
        sysUserService.update(param);
        return JsonData.success();
    }

    @RequestMapping("/page.json")
    @ResponseBody
    public JsonData page(@RequestParam("deptId")int deptId, PageQuery pageQuery){
        PageResult<SysUser> result=sysUserService.getPageByDeptId(deptId,pageQuery);
        return JsonData.success(result);
    }

    @RequestMapping("/acls.json")
    @ResponseBody
    public JsonData acls(@RequestParam("userId") int userId){

        Map<String,Object> map=Maps.newHashMap();
        map.put("acls",sysTreeService.userAclTree(userId));
        map.put("roles",sysRoleService.getRoleListByUserId(userId));
        return JsonData.success(map);
    }

}
