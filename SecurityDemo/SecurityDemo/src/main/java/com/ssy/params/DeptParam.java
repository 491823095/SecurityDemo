package com.ssy.params;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;

@Setter
@Getter
@ToString
public class DeptParam {

    private Integer id;

    @NotBlank(message = "部门名称不能为空")
    @Length(min = 2,max = 15,message = "部门名称长度需要在2~15哥字符串")
    private String name;

    private Integer parentId = 0    ;

    @NotNull(message = "展示顺序不可以为空")
    private Integer seq;

    @Length(max = 150,message = "备注的长度在150个字符串内")
    private String remark;

}
