package com.ssy.controller;

import com.ssy.common.JsonData;
import com.ssy.dto.DeptLevelDto;
import com.ssy.params.DeptParam;
import com.ssy.service.SysDeptService;
import com.ssy.service.SysTreeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import java.util.List;

@Controller
@RequestMapping("/sys/dept")
@Slf4j
public class SysDeptController {

    @Resource
    private SysDeptService sysDeptService;

    @Resource
    private SysTreeService sysTreeService;


    /*首页*/
    @RequestMapping("/dept.page")
    public ModelAndView page(){
        return new ModelAndView("dept");
    }

    /*首页*/
    @RequestMapping("/dept1.page")
    public ModelAndView page1(){
        return new ModelAndView("dept1");
    }

    /*增*/
    @RequestMapping("/save.json")
    @ResponseBody
    public JsonData saveDept(DeptParam param){
        sysDeptService.save(param);
        return JsonData.success();
    }

    @RequestMapping("/tree.json")
    @ResponseBody
    public JsonData tree(){
        List<DeptLevelDto> dtoList=sysTreeService.deptTree();
        return JsonData.success(dtoList);
    }

    /*改*/
    @RequestMapping("/update.json")
    @ResponseBody
    public JsonData updateDept(DeptParam param){
        sysDeptService.update(param);
        return JsonData.success();
    }

    @RequestMapping("/delete.json")
    @ResponseBody
    public JsonData delete(@RequestParam("id")int id){
        sysDeptService.delete(id);
        return JsonData.success();
    }

}
