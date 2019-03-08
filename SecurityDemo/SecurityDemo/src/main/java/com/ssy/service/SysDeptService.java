package com.ssy.service;

import com.google.common.base.Preconditions;
import com.ssy.common.RequestHolder;
import com.ssy.dao.SysDeptMapper;
import com.ssy.dao.SysUserMapper;
import com.ssy.exception.ParamException;
import com.ssy.params.DeptParam;
import com.ssy.pojo.SysDept;
import com.ssy.util.BeanValidator;
import com.ssy.util.IpUtil;
import com.ssy.util.LevelUtil;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Service
public class SysDeptService {

    @Resource
    private SysDeptMapper sysDeptMapper;

    @Resource
    private SysUserMapper sysUserMapper;

    @Resource
    private SysLogService sysLogService;

    public void save(DeptParam param){
        //先做验证,是否为空
        BeanValidator.check(param);
        /*先判断该信息是否符合要求,部门相同返回true,抛出异常*/
        if (checkExist(param.getParentId(),param.getName(),param.getId())){
            throw new ParamException("同一层级下存在相同名称的部门");
        }
        //在实体类上面添加builer可以使用下面的方法,即可参数传进去并实例化出来
        SysDept sysDept=SysDept.builder().name(param.getName()).parentId(param.getParentId())
                .seq(param.getSeq()).remark(param.getRemark()).build();

        //获取上级的level,先获取其parentId的等级，再获取当前的等级
        //后调用刚设置好的level工具类,把工具类生成的id设置为最终的levelId
        sysDept.setLevel(LevelUtil.calculateLevel(getLevel(param.getParentId()),param.getParentId()));

        /*待完成*/
        sysDept.setOperator(RequestHolder.getCurrentUser().getUsername());
        sysDept.setOperateIp(IpUtil.getRemoteIp(RequestHolder.getCurrentRequest()));
        sysDept.setOperatorTime(new Date());
        /*这个方法是会判断非空,为空的值不会插入进去*/
        sysDeptMapper.insertSelective(sysDept);
        //更新到日志内
        sysLogService.saveDeptLog(null,sysDept);
     }

     public void update(DeptParam param){
         //先做验证,是否为空
         BeanValidator.check(param);
         /*先判断该信息是否符合要求,部门相同返回true,抛出异常*/
         if (checkExist(param.getParentId(),param.getName(),param.getId())){
             throw new ParamException("同一层级下存在相同名称的部门");
         }
         SysDept before=sysDeptMapper.selectByPrimaryKey(param.getId());
         //判断是否为空
         Preconditions.checkNotNull(before,"待更新的部门不存在");

         SysDept after=SysDept.builder().id(param.getId()).name(param.getName()).parentId(param.getParentId())
                 .seq(param.getSeq()).remark(param.getRemark()).build();
         //设置level等级
         after.setLevel(LevelUtil.calculateLevel(getLevel(param.getParentId()),param.getParentId()));
         /*待完成*/
         after.setOperator(RequestHolder.getCurrentUser().getUsername());
         after.setOperateIp(IpUtil.getRemoteIp(RequestHolder.getCurrentRequest()));
         after.setOperatorTime(new Date());

         updateWithChild(before,after);
         //记录日志
         sysLogService.saveDeptLog(before,after);
     }

     //相当于自己设置一个级联,同时更新父子部门
     @Transactional
     protected void updateWithChild(SysDept before,SysDept after){

        //判断是否需要更新子部门
         String newLevelPrefix=after.getLevel();
         String oldLevelPrefix=before.getLevel();
         //判断是否一致，不一致则开始更新子部门
         if (!newLevelPrefix.equals(oldLevelPrefix)){
             List<SysDept> deptList=sysDeptMapper.getChildDeptListByLevel(before.getLevel());
             if (CollectionUtils.isNotEmpty(deptList)){
                 for (SysDept dept:deptList){
                     String level=dept.getLevel();
                     //判断before的level前缀是否与after的一致
                     if (level.indexOf(oldLevelPrefix)==0){
                        level=newLevelPrefix+level.substring(oldLevelPrefix.length());
                        dept.setLevel(level);
                     }
                 }
                 sysDeptMapper.batchUpdateLevel(deptList);
             }
         }
         sysDeptMapper.updateByPrimaryKey(after);
     }

    /*判断同一部门下是否有重复的部门名(父类ID、部门名称、部门ID)*/
    public boolean checkExist(Integer parentId,String deptName,Integer deptId){
        return sysDeptMapper.CountByNameAndParentId(parentId,deptName,deptId)>0;
    }

    /*获取上级部门id的方法*/
    private String getLevel(Integer deptId){
        SysDept dept=sysDeptMapper.selectByPrimaryKey(deptId);
        if (dept==null){
            return null;
        }
        return dept.getLevel();
    }

    public void delete(int deptId){
        //先判断删除的部门是否存在
        SysDept dept=sysDeptMapper.selectByPrimaryKey(deptId);
        Preconditions.checkNotNull(dept,"待删除的部门不存在,无法删除");
        //判断是否存在子部门
        if (sysDeptMapper.countByParentId(dept.getId())>0){
            throw  new ParamException("当前部门下面有子部门,无法删除");
        }
        //判断该部门下面是否存在用户
        if (sysUserMapper.countByDeptId(dept.getId())>0){
            throw new ParamException("当前部门下面有用户,无法删除");
        }
        //若都通过了则进行删除
        sysDeptMapper.deleteByPrimaryKey(deptId);
    }
}
