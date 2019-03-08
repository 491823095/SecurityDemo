package com.ssy.dao;

import com.ssy.pojo.SysAclModule;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SysAclModuleMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(SysAclModule record);

    int insertSelective(SysAclModule record);

    SysAclModule selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(SysAclModule record);

    int updateByPrimaryKey(SysAclModule record);

    //判断一个部门内是否有重复的部门名
    int countByNameAndParentId(@Param("parentId")Integer parentId, @Param("name")String name, @Param("id")Integer id);

    //类似同步更新(级联)子类
    void batchUpdateLevel(@Param("aclModuleList") List<SysAclModule> aclModuleList);

    //查询level(模糊查询)
    List<SysAclModule> getChildAclModuleListByLevel(@Param("level")String level);

    List<SysAclModule> getAllAclModule();

    int countByParentId(@Param("aclModuleId")int aclModuleId);
}