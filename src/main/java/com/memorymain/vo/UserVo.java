package com.memorymain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.util.Date;
import java.util.List;

@Data
public class UserVo {
    private Long userId;
    private String username;
    private String password;
    private String sex;
    private String email;
    private String phone;
    private String city;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date birthday;
    private Integer capacity;
    private String avatar;
    private String code;
    private String uuid;
    /**
     * 角色对象
     */
    private List<String> roles;
    public String getLoginId() {
        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        return "pc" + ":" + userId;
    }
}
