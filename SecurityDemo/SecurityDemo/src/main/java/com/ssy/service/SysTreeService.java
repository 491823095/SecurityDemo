package com.ssy.service;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.ssy.dao.SysAclMapper;
import com.ssy.dao.SysAclModuleMapper;
import com.ssy.dao.SysDeptMapper;
import com.ssy.dto.AclDto;
import com.ssy.dto.AclModuleLevelDto;
import com.ssy.dto.DeptLevelDto;
import com.ssy.pojo.SysAcl;
import com.ssy.pojo.SysAclModule;
import com.ssy.pojo.SysDept;
import com.ssy.util.LevelUtil;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@Service
public class SysTreeService {

    @Resource
    private SysDeptMapper sysDeptMapper;

    @Resource
    private SysAclModuleMapper sysAclModuleMapper;

    @Resource
    private SysCoreService sysCoreService;

    @Resource
    private SysAclMapper sysAclMapper;

    public List<AclModuleLevelDto> userAclTree(int userId){
        List<SysAcl> userAclList=sysCoreService.getUserAclList(userId);
        List<AclDto> aclDtolist= Lists.newArrayList();
        for (SysAcl acl:userAclList){
            AclDto dto=AclDto.adapt(acl);
            dto.setHasAcl(true);
            dto.setChecked(true);
            aclDtolist.add(dto);
        }
        return aclListToTree(aclDtolist);
    }

    //角色tree
    public List<AclModuleLevelDto> roleTree(int roleId){
        //当前用户已有的权限点
        List<SysAcl> userAclList=sysCoreService.getCurrentUserAclList();
        //当前角色分配的权限点
        List<SysAcl> roleAclList=sysCoreService.getRoleAclList(roleId);
        //当前系统所有的权限点
        List<AclDto> aclDtoList =Lists.newArrayList();


        //?这种是8版本才能支持的新写法
        Set<Integer> userAclIdList=userAclList.stream().map(sysAcl -> sysAcl.getId()).collect(Collectors.toSet());
        Set<Integer> roleAclIdSet=roleAclList.stream().map(sysAcl ->sysAcl.getId()).collect(Collectors.toSet());



        List<SysAcl> allAclList=sysAclMapper.getAll();
        for (SysAcl acl:allAclList){
            AclDto dto=AclDto.adapt(acl);
            //判断用户是否包含此ID(是否有权限)
            if (userAclIdList.contains(acl.getId())){
                dto.setHasAcl(true);
            }
            //判断角色是否包含此ID
            if (roleAclIdSet.contains(acl.getId())){
                dto.setChecked(true);
            }
            aclDtoList.add(dto);
        }
        return aclListToTree(aclDtoList);
    }

    public List<AclModuleLevelDto> aclListToTree(List<AclDto> aclDtoList){
        if (CollectionUtils.isEmpty(aclDtoList)){
            return Lists.newArrayList();
        }
        //获得权限树
        List<AclModuleLevelDto> aclModuleLevelList=aclModuleTree();
        //key是权限模块ID value是AclDto 即每个权限点对应的权限列表
        Multimap<Integer,AclDto> moduleIdAclMap=ArrayListMultimap.create();
        for (AclDto acl:aclDtoList){
            //判断当前状态
            if (acl.getStatus()==1){
                moduleIdAclMap.put(acl.getAclModuleId(),acl);
            }
        }
        bindAclsWithOrder(aclModuleLevelList,moduleIdAclMap);
        return aclModuleLevelList;
    }

    //权限点绑定在权限模块上
    public void bindAclsWithOrder(List<AclModuleLevelDto> aclModuleLevelList,Multimap<Integer,AclDto> moduleIdAclMap){
        if (CollectionUtils.isEmpty(aclModuleLevelList)){
            return ;
        }
        for (AclModuleLevelDto dto:aclModuleLevelList){
            //根据模块ID取出所有权限点
            List<AclDto> aclDtoList=(List<AclDto>)moduleIdAclMap.get(dto.getId());
            if (CollectionUtils.isNotEmpty(aclDtoList)){
                Collections.sort(aclDtoList,aclSeqComparator);
                dto.setAclList(aclDtoList);
            }
            bindAclsWithOrder(dto.getAclModuleList(),moduleIdAclMap);
        }
    }

    //权限模块的tree
    public List<AclModuleLevelDto> aclModuleTree(){
        //获得所有权限模块的数据
        List<SysAclModule> aclModuleList=sysAclModuleMapper.getAllAclModule();
        //定义一个空的树形结构
        List<AclModuleLevelDto> dtoList=Lists.newArrayList();
        //遍历权限模块所有数据并存放进属性结构中
        for (SysAclModule aclModule:aclModuleList){
            dtoList.add(AclModuleLevelDto.adapt(aclModule));
        }
        return aclModuleListToTree(dtoList);
    }

    //转换成权限树方法
    public List<AclModuleLevelDto> aclModuleListToTree(List<AclModuleLevelDto> dtos){
        //为空判断
        if (CollectionUtils.isEmpty(dtos)){
            return Lists.newArrayList();
        }
        //key是dto的level等级,即按照等级分类 如:level -> [dept1,dept2]
        Multimap<String,AclModuleLevelDto> levelAclModuleMap=ArrayListMultimap.create();

        List<AclModuleLevelDto> rootList=Lists.newArrayList();
        //把传过来的deptLevelList取值放入hashMap内
        for (AclModuleLevelDto dto:dtos){
            //把level做key的好处是能获取到所有相同level的部门
            levelAclModuleMap.put(dto.getLevel(),dto);
            //判断等级是否相同
            if (LevelUtil.ROOT.equals(dto.getLevel())){
                //获取到所有level下的全部部门信息
                rootList.add(dto);
            }
        }
        //要把获取到的信息进行从小到大的排序
        Collections.sort(rootList,aclModuleSeqComparator);
        // 递归生成树
        transformAclModuleTree(rootList,LevelUtil.ROOT,levelAclModuleMap);
        return rootList;
    }

    //参数说明: List结构所有权限的信息--等级--Map结构的权限信息(以等级为key存储,value为对象)
    public void transformAclModuleTree(List<AclModuleLevelDto> dtoList,String level,Multimap<String,AclModuleLevelDto> levelAclModuleMap){
        for (int i=0;i<dtoList.size();i++){
            //获得每层dto对象
            AclModuleLevelDto dto=dtoList.get(i);
            //获得下一级的所有权限对象  如获得等级1下所有的权限对象
            String nextLevel=LevelUtil.calculateLevel(level,dto.getId());
            List<AclModuleLevelDto> tempList=(List<AclModuleLevelDto>)levelAclModuleMap.get(nextLevel);
            //判断是否存在下一层级
            if (CollectionUtils.isNotEmpty(tempList)){
                //注意按等级排序(小到大)
                Collections.sort(tempList,aclModuleSeqComparator);
                dto.setAclModuleList(tempList);
                //获得完当前等级后再调用自身,把下层需要的数据再传进去重复遍历
                transformAclModuleTree(tempList,nextLevel,levelAclModuleMap);
            }
        }
    }

    //用于返回部门树
    public List<DeptLevelDto> deptTree(){
        //获得所有部门的信息
        List<SysDept> deptList=sysDeptMapper.getAllDept();
        //把获取到的部门信息转换成DeptLevelDto对象(方便转成树形结构)
        List<DeptLevelDto> dtoList=Lists.newArrayList();
        for (SysDept dept:deptList){
            DeptLevelDto levelDto=DeptLevelDto.adapt(dept);
            dtoList.add(levelDto);
        }
        return deptListToTree(dtoList);
    }

    //
    public List<DeptLevelDto> deptListToTree(List<DeptLevelDto> deptLevelList){
        //先判断是否为空
        if (CollectionUtils.isEmpty(deptLevelList)){
            //为空则返回空集合
            return Lists.newArrayList();
        }
        //key是dto的level等级,即按照等级分类 如:level -> [dept1,dept2]
        Multimap<String,DeptLevelDto> levelDeptMap=ArrayListMultimap.create();

        List<DeptLevelDto> rootList=Lists.newArrayList();
        //把传过来的deptLevelList取值放入hashMap内
        for (DeptLevelDto dto:deptLevelList){
            //把level做key的好处是能获取到所有相同level的部门
            levelDeptMap.put(dto.getLevel(),dto);
            //判断等级是否相同
            if (LevelUtil.ROOT.equals(dto.getLevel())){
                //获取到所有level下的全部部门信息
                rootList.add(dto);
            }
        }
        //要把获取到的信息进行从小到大的排序
        Collections.sort(rootList,deptSeqComparator);
        // 递归生成树
        transformDeptTree(rootList,LevelUtil.ROOT,levelDeptMap);
        return rootList;
    }

    /*递归排序方法*/
    //参数:当前结构 当前level 当前map数据
    public void transformDeptTree(List<DeptLevelDto> deptLevelList,String level,Multimap<String,DeptLevelDto> levelDeptMap){
        for (int i=0;i<deptLevelList.size();i++){
            //遍历该层的每个元素
            DeptLevelDto deptLevelDto=deptLevelList.get(i);
            //处理当前层级(level工具类拼接)的数据
            String nextLevel=LevelUtil.calculateLevel(level,deptLevelDto.getId());
            //处理下一层(对照上面用level当key 强转成 list集合)
            List<DeptLevelDto> tempDeptList=(List<DeptLevelDto>) levelDeptMap.get(nextLevel);
            //判断非空
            if (CollectionUtils.isNotEmpty(tempDeptList)){
                //排序
                Collections.sort(tempDeptList,deptSeqComparator);
                //设置下一层部门
                deptLevelDto.setDeptList(tempDeptList);
                //进入到下一层处理
                transformDeptTree(tempDeptList,nextLevel,levelDeptMap);
            }
        }
    }

    //部门排序方法提取(从小到大)
    public Comparator<DeptLevelDto> deptSeqComparator=new Comparator<DeptLevelDto>() {
        @Override
        public int compare(DeptLevelDto o1, DeptLevelDto o2) {
            return o1.getSeq()-o2.getSeq();
        }
    };

    //权限排序方法
    public Comparator<AclModuleLevelDto> aclModuleSeqComparator=new Comparator<AclModuleLevelDto>() {
        @Override
        public int compare(AclModuleLevelDto o1, AclModuleLevelDto o2) {
            return o1.getSeq()-o2.getSeq();
        }
    };

    //权限排序方法
    public Comparator<AclDto> aclSeqComparator=new Comparator<AclDto>() {
        @Override
        public int compare(AclDto o1, AclDto o2) {
            return o1.getSeq()-o2.getSeq();
        }
    };
}
