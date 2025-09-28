package com.memorymain.vo;

import com.memorymain.util.Constants;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import javax.validation.constraints.NotBlank;

@Data
public class LoginBody {

    /**
     * 用户名
     */
    @NotBlank(message = "用户名不能为空")
    @Length(min = Constants.USERNAME_MIN_LENGTH, max = Constants.USERNAME_MAX_LENGTH, message = "用户名限制2-20个字符")
    private String username;

    /**
     * 用户密码
     */
    @NotBlank(message = "用户密码不能为空")
    @Length(min = Constants.PASSWORD_MIN_LENGTH, max = Constants.PASSWORD_MAX_LENGTH, message = "用户密码限制5-20个字符")
    private String password;

    /**
     * 验证码
     */
    private String code;

    /**
     * 唯一标识
     */
    private String uuid;

}
