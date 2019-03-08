package com.ssy.service;

import com.google.common.base.Preconditions;
import com.ssy.beans.LogType;
import com.ssy.beans.PageQuery;
import com.ssy.beans.PageResult;
import com.ssy.common.RequestHolder;
import com.ssy.dao.*;
import com.ssy.dto.SearchLogDto;
import com.ssy.exception.ParamException;
import com.ssy.params.SearchLogParam;
import com.ssy.pojo.*;
import com.ssy.util.BeanValidator;
import com.ssy.util.IpUtil;
import com.ssy.util.JsonMapper;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.type.TypeReference;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Service
public class SysLogService {
    @Resource
    private SysLogMapper sysLogMapper;

    @Resource
    private SysDeptMapper sysDeptMapper;

    @Resource
    private SysUserMapper sysUserMapper;

    @Resource
    private SysAclModuleMapper sysAclModuleMapper;

    @Resource
    private SysAclMapper sysAclMapper;

    @Resource
    private SysRoleMapper sysRoleMapper;

    @Resource
    private SysRoleAclService sysRoleAclService;

    @Resource
    private SysRoleUserService sysRoleUserService;

    @Resource
    private SysRoleUserMapper sysRoleUserMapper;


    public void recover(int id){
        SysLogWithBLOBs syslog=sysLogMapper.selectByPrimaryKey(id);
        Preconditions.checkNotNull(syslog,"待还原记录不存在");
        //判断是用户还是部门
        switch (syslog.getType()){
            case LogType.TYPE_DEPT:
                SysDept beforeDept=sysDeptMapper.selectByPrimaryKey(syslog.getTargetId());
                Preconditions.checkNotNull(beforeDept,"待还原的部门已经不存在");
                if (StringUtils.isBlank(syslog.getNewValue()) || StringUtils.isBlank(syslog.getOldValue())){
                    throw  new ParamException("新增和删除操作不做还原");
                }
                SysDept afterDept=JsonMapper.String2Obj(syslog.getOldValue(), new TypeReference<SysDept>() {
                });
                afterDept.setOperator(RequestHolder.getCurrentUser().getUsername());
                afterDept.setOperatorTime(new Date());
                afterDept.setOperateIp(IpUtil.getRemoteIp(RequestHolder.getCurrentRequest()));
                sysDeptMapper.updateByPrimaryKeySelective(afterDept);
                //记得别忘记更新日志
                saveDeptLog(beforeDept,afterDept);
                break;
            case LogType.TYPE_USER:
                SysUser beforeUser=sysUserMapper.selectByPrimaryKey(syslog.getTargetId());
                Preconditions.checkNotNull(beforeUser,"待还原的用户已经不存在");
                if (StringUtils.isBlank(syslog.getNewValue()) || StringUtils.isBlank(syslog.getOldValue())){
                    throw  new ParamException("新增和删除操作不做还原");
                }
                SysUser afterUser=JsonMapper.String2Obj(syslog.getOldValue(), new TypeReference<SysUser>() {
                });
                afterUser.setOperator(RequestHolder.getCurrentUser().getUsername());
                afterUser.setOperatorTime(new Date());
                afterUser.setOperateIp(IpUtil.getRemoteIp(RequestHolder.getCurrentRequest()));
                sysUserMapper.updateByPrimaryKeySelective(afterUser);
                //记得别忘记更新日志
                saveUserLog(beforeUser,afterUser);
                break;
            case LogType.TYPE_ACL_MODULE:
                SysAclModule beforeAclModule=sysAclModuleMapper.selectByPrimaryKey(syslog.getTargetId());
                Preconditions.checkNotNull(beforeAclModule,"待还原的权限模块已经不存在");
                if (StringUtils.isBlank(syslog.getNewValue()) || StringUtils.isBlank(syslog.getOldValue())){
                    throw  new ParamException("新增和删除操作不做还原");
                }
                SysAclModule afterAclModule=JsonMapper.String2Obj(syslog.getOldValue(), new TypeReference<SysAclModule>() {
                });
                afterAclModule.setOperator(RequestHolder.getCurrentUser().getUsername());
                afterAclModule.setOperatorTime(new Date());
                afterAclModule.setOperateIp(IpUtil.getRemoteIp(RequestHolder.getCurrentRequest()));
                sysAclModuleMapper.updateByPrimaryKeySelective(afterAclModule);
                //记得别忘记更新日志
                saveAclModuleLog(beforeAclModule,afterAclModule);
                break;
            case LogType.TYPE_ACL:
                SysAcl beforeAcl=sysAclMapper.selectByPrimaryKey(syslog.getTargetId());
                Preconditions.checkNotNull(beforeAcl,"待还原的权限点已经不存在");
                if (StringUtils.isBlank(syslog.getNewValue()) || StringUtils.isBlank(syslog.getOldValue())){
                    throw  new ParamException("新增和删除操作不做还原");
                }
                SysAcl afterAcl=JsonMapper.String2Obj(syslog.getOldValue(), new TypeReference<SysAcl>() {
                });
                afterAcl.setOperator(RequestHolder.getCurrentUser().getUsername());
                afterAcl.setOperateTime(new Date());
                afterAcl.setOperateIp(IpUtil.getRemoteIp(RequestHolder.getCurrentRequest()));
                sysAclMapper.updateByPrimaryKeySelective(afterAcl);
                //记得别忘记更新日志
                saveAclLog(beforeAcl,afterAcl);
                break;
            case LogType.TYPE_ROLE:
                SysRole beforeRole=sysRoleMapper.selectByPrimaryKey(syslog.getTargetId());
                Preconditions.checkNotNull(beforeRole,"待还原的角色已经不存在");
                if (StringUtils.isBlank(syslog.getNewValue()) || StringUtils.isBlank(syslog.getOldValue())){
                    throw  new ParamException("新增和删除操作不做还原");
                }
                SysRole afterRole=JsonMapper.String2Obj(syslog.getOldValue(), new TypeReference<SysRole>() {
                });
                afterRole.setOperator(RequestHolder.getCurrentUser().getUsername());
                afterRole.setOperateTime(new Date());
                afterRole.setOperateIp(IpUtil.getRemoteIp(RequestHolder.getCurrentRequest()));
                sysRoleMapper.updateByPrimaryKeySelective(afterRole);
                //记得别忘记更新日志
                saveRoleLog(beforeRole,afterRole);
                break;
            case LogType.TYPE_ROLE_ACL:
                SysRole aclRole=sysRoleMapper.selectByPrimaryKey(syslog.getTargetId());
                Preconditions.checkNotNull(aclRole,"角色权限已经不存在");
                sysRoleAclService.changeRoleAcls(syslog.getTargetId(),JsonMapper.String2Obj(syslog.getOldValue(), new TypeReference<List<Integer>>() {
                }));
                break;
            case LogType.TYPE_ROLE_USER:
                SysRole userRole=sysRoleMapper.selectByPrimaryKey(syslog.getTargetId());
                Preconditions.checkNotNull(userRole,"角色用户已经不存在");
                sysRoleUserService.changeRoleUsers(syslog.getTargetId(),JsonMapper.String2Obj(syslog.getOldValue(), new TypeReference<List<Integer>>() {
                }));
                break;
                default:;
        }
    }

    public PageResult<SysLogWithBLOBs> searchPage(SearchLogParam param, PageQuery query){
        BeanValidator.check(query);
        SearchLogDto dto=new SearchLogDto();
        dto.setType(param.getType());
        if (StringUtils.isNotBlank(param.getBeforeSeq())){
            dto.setBeforeSeq("%"+param.getBeforeSeq()+"%");
        }
        if (StringUtils.isNotBlank(param.getAfterSeq())){
            dto.setAfterSeq("%"+param.getAfterSeq()+"%");
        }
        if (StringUtils.isNotBlank(param.getOperator())){
            dto.setOperator("%"+param.getOperator()+"%");
        }
        try {
            SimpleDateFormat dateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            if (StringUtils.isNotBlank(param.getFromTime())){
                dto.setFromTime(dateFormat.parse(param.getFromTime()));
            }
            if (StringUtils.isNotBlank(param.getToTime())){
                dto.setToTime(dateFormat.parse(param.getToTime()));
            }

        } catch (ParseException e) {
            throw  new ParamException("传入的日期格式有问题,正确格式为:yyyy-MM-dd HH:mm:ss");
        }
        int count=sysLogMapper.countBySearchDto(dto);
        if (count>0){
            List<SysLogWithBLOBs> logList=sysLogMapper.getPageListBySearchDto(dto,query);
            return PageResult.<SysLogWithBLOBs>builder().total(count).data(logList).build();
        }
        return  PageResult.<SysLogWithBLOBs>builder().build();

    }

    public void saveDeptLog(SysDept before,SysDept after){
        SysLogWithBLOBs sysLog=new SysLogWithBLOBs();
        sysLog.setType(LogType.TYPE_DEPT);
        sysLog.setTargetId(after==null ?before.getId() : after.getId());
        sysLog.setOldValue(before==null ? "" : JsonMapper.obj2String(before));
        sysLog.setNewValue(after==null ? "" : JsonMapper.obj2String(after));
        sysLog.setOperator(RequestHolder.getCurrentUser().getUsername());
        sysLog.setOperateIp(IpUtil.getRemoteIp(RequestHolder.getCurrentRequest()));
        sysLog.setOperateTime(new Date());
        sysLog.setStatus(1);
        sysLogMapper.insertSelective(sysLog);
    }

    public void saveUserLog(SysUser before,SysUser after){
        SysLogWithBLOBs sysLog=new SysLogWithBLOBs();
        sysLog.setType(LogType.TYPE_USER);
        sysLog.setTargetId(after==null ?before.getId() : after.getId());
        sysLog.setOldValue(before==null ? "" : JsonMapper.obj2String(before));
        sysLog.setNewValue(after==null ? "" : JsonMapper.obj2String(after));
        sysLog.setOperator(RequestHolder.getCurrentUser().getUsername());
        sysLog.setOperateIp(IpUtil.getRemoteIp(RequestHolder.getCurrentRequest()));
        sysLog.setOperateTime(new Date());
        sysLog.setStatus(1);
        sysLogMapper.insertSelective(sysLog);
    }

    public void saveAclModuleLog(SysAclModule before,SysAclModule after){
        SysLogWithBLOBs sysLog=new SysLogWithBLOBs();
        sysLog.setType(LogType.TYPE_ACL_MODULE);
        sysLog.setTargetId(after==null ?before.getId() : after.getId());
        sysLog.setOldValue(before==null ? "" : JsonMapper.obj2String(before));
        sysLog.setNewValue(after==null ? "" : JsonMapper.obj2String(after));
        sysLog.setOperator(RequestHolder.getCurrentUser().getUsername());
        sysLog.setOperateIp(IpUtil.getRemoteIp(RequestHolder.getCurrentRequest()));
        sysLog.setOperateTime(new Date());
        sysLog.setStatus(1);
        sysLogMapper.insertSelective(sysLog);
    }

    public void saveAclLog(SysAcl before,SysAcl after){
        SysLogWithBLOBs sysLog=new SysLogWithBLOBs();
        sysLog.setType(LogType.TYPE_ACL);
        sysLog.setTargetId(after==null ?before.getId() : after.getId());
        sysLog.setOldValue(before==null ? "" : JsonMapper.obj2String(before));
        sysLog.setNewValue(after==null ? "" : JsonMapper.obj2String(after));
        sysLog.setOperator(RequestHolder.getCurrentUser().getUsername());
        sysLog.setOperateIp(IpUtil.getRemoteIp(RequestHolder.getCurrentRequest()));
        sysLog.setOperateTime(new Date());
        sysLog.setStatus(1);
        sysLogMapper.insertSelective(sysLog);
    }

    public void saveRoleLog(SysRole before,SysRole after){
        SysLogWithBLOBs sysLog=new SysLogWithBLOBs();
        sysLog.setType(LogType.TYPE_ROLE);
        sysLog.setTargetId(after==null ?before.getId() : after.getId());
        sysLog.setOldValue(before==null ? "" : JsonMapper.obj2String(before));
        sysLog.setNewValue(after==null ? "" : JsonMapper.obj2String(after));
        sysLog.setOperator(RequestHolder.getCurrentUser().getUsername());
        sysLog.setOperateIp(IpUtil.getRemoteIp(RequestHolder.getCurrentRequest()));
        sysLog.setOperateTime(new Date());
        sysLog.setStatus(1);
        sysLogMapper.insertSelective(sysLog);
    }




}
