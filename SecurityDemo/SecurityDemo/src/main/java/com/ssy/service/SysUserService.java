package com.ssy.service;

/**
 * 用户service层
 */

import com.google.common.base.Preconditions;
import com.ssy.beans.PageQuery;
import com.ssy.beans.PageResult;
import com.ssy.common.RequestHolder;
import com.ssy.dao.SysUserMapper;
import com.ssy.exception.ParamException;
import com.ssy.params.UserParam;
import com.ssy.pojo.SysUser;
import com.ssy.util.BeanValidator;
import com.ssy.util.IpUtil;
import com.ssy.util.MD5Util;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@Service
public class SysUserService {

    @Resource
    private SysUserMapper sysUserMapper;

    @Resource
    private SysLogService sysLogService;

    //增
    public void save(UserParam userParam){
        BeanValidator.check(userParam);
        if (checkTelephoneExist(userParam.getTelephone(),userParam.getId())){
            throw new ParamException("电话已被占用");
        }
        if (checkEmailExist(userParam.getMail(),userParam.getId())){
            throw new ParamException("邮箱已被占用");
        }
        //暂定密码
        String password="123456";
        String encryptedPassword=MD5Util.encrypt(password);

        SysUser sysUser=SysUser.builder().username(userParam.getUsername()).telephone(userParam.getTelephone()).mail(userParam.getMail())
                .password(encryptedPassword).deptId(userParam.getDeptId()).status(userParam.getStatus()).remark(userParam.getRemark()).build();
        sysUser.setOperator(RequestHolder.getCurrentUser().getUsername());
        sysUser.setOperateIp(IpUtil.getRemoteIp(RequestHolder.getCurrentRequest()));
        sysUser.setOperatorTime(new Date());
        //TODO：sendEmail

        //新增
        sysUserMapper.insertSelective(sysUser);
        sysLogService.saveUserLog(null,sysUser);
    }

    //管理员更新其他账号信息
    public void update(UserParam userParam){
        BeanValidator.check(userParam);
        if (checkTelephoneExist(userParam.getTelephone(),userParam.getId())){
            throw new ParamException("电话已被占用");
        }
        if (checkEmailExist(userParam.getMail(),userParam.getId())){
            throw new ParamException("邮箱已被占用");
        }

        //未更新前要先校验信息
        SysUser before=sysUserMapper.selectByPrimaryKey(userParam.getId());
        //非空判断
        Preconditions.checkNotNull(before,"待更新的用户不存在");
        //更新后的用户
        SysUser after=SysUser.builder().id(userParam.getId()).username(userParam.getUsername()).telephone(userParam.getTelephone()).mail(userParam.getMail())
                .password(before.getPassword()).deptId(userParam.getDeptId()).status(userParam.getStatus()).remark(userParam.getRemark()).build();
        after.setOperator(RequestHolder.getCurrentUser().getUsername());
        after.setOperateIp(IpUtil.getRemoteIp(RequestHolder.getCurrentRequest()));
        after.setOperatorTime(new Date());
        sysUserMapper.updateByPrimaryKeySelective(after);

        sysLogService.saveUserLog(before,after);
    }

    //检查邮箱是否被使用
    public boolean checkEmailExist(String mail,Integer userId){
        return sysUserMapper.countByMail(mail,userId)>0;
    }

    //检查电话是否被使用
    public boolean checkTelephoneExist(String phone,Integer userId){
        return sysUserMapper.countByTelephone(phone,userId)>0;
    }

    //查
    public SysUser findByKeyword(String keyword){
        return sysUserMapper.findByKeyword(keyword);
    }

    public PageResult<SysUser> getPageByDeptId(int deptId, PageQuery pageQuery){
        BeanValidator.check(pageQuery);
        int count=sysUserMapper.countByDeptId(deptId);
        if (count>0){
            //把获取到的信息转换成指定格式
            List<SysUser> list=sysUserMapper.getPageByDeptId(deptId,pageQuery);
            return PageResult.<SysUser>builder().total(count).data(list).build();
        }
        return PageResult.<SysUser>builder().build();
    }

    public List<SysUser> getAll(){
        return sysUserMapper.getAll();
    }
}
