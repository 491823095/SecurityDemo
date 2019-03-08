/**
 * 测试validator所用的实体类
 */
package com.ssy.params;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.List;

@Getter
@Setter
public class TestVo {

    /*不为空验证*/
    /*String用NotBlank*/
    @NotBlank
    private String msg;

    /*数字用NotNull*/
    @NotNull(message = "id不可以为空")
    @Min(value = 0,message = "id测试最小值为0")
    @Max(value = 10,message = "id测试最大值为10")
    private Integer id;

    /*集合用NotEmpty*/
    //@NotEmpty
    private List<String> listTest;


}
