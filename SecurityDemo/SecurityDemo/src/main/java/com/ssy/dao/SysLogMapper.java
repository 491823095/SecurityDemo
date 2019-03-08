package com.ssy.dao;

import com.ssy.beans.PageQuery;
import com.ssy.dto.SearchLogDto;
import com.ssy.pojo.SysLog;
import com.ssy.pojo.SysLogWithBLOBs;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SysLogMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(SysLogWithBLOBs record);

    int insertSelective(SysLogWithBLOBs record);

    SysLogWithBLOBs selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(SysLogWithBLOBs record);

    int updateByPrimaryKeyWithBLOBs(SysLogWithBLOBs record);

    int updateByPrimaryKey(SysLog record);

    int countBySearchDto(@Param("dto")SearchLogDto dto);

    List<SysLogWithBLOBs> getPageListBySearchDto(@Param("dto") SearchLogDto dto,@Param("page") PageQuery query);
}