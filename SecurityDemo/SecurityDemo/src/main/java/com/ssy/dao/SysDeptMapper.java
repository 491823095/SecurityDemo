package com.ssy.dao;

import com.ssy.pojo.SysDept;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SysDeptMapper {
    int deleteByPrimaryKey(@Param("id") Integer id);

    int insert(SysDept record);

    int insertSelective(SysDept record);

    SysDept selectByPrimaryKey(@Param("id")Integer id);

    int updateByPrimaryKeySelective(SysDept record);

    int updateByPrimaryKey(SysDept record);

    //获取部门列表
    List<SysDept> getAllDept();

    //查询level(模糊查询)
    List<SysDept> getChildDeptListByLevel(@Param("level")String level);

    //类似同步更新(级联)子类
    void batchUpdateLevel(@Param("sysDeptList") List<SysDept> sysDeptList);

    //判断一个部门内是否有重复的部门名
    int CountByNameAndParentId(@Param("parentId")Integer parentId,@Param("name")String name,@Param("id")Integer id);

    //todo
    int countByParentId(@Param("deptId") int deptId);

}