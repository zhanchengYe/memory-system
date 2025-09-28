package com.memorymain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.Date;
@Data
@TableName("user")
public class User {
    @TableId(type = IdType.AUTO)
    private Long userId;
    @NotBlank(message = "用户账号不能为空")
    @Size(min = 2, max = 20, message = "用户账号长度不能超过{max}个字符")
    private String username;
    private String password;
    private String sex;
//    @Email(message = "邮箱格式不正确")
//    @Size(min = 0, max = 50, message = "邮箱长度不能超过{max}个字符")
    private String email;
    private String phone;
    private String city;
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date birthday;
    private String avatar;
    private String status;

}
