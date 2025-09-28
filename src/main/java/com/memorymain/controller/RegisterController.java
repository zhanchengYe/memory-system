
package com.memorymain.controller;

import cn.dev33.satoken.annotation.SaIgnore;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.memorymain.entity.SysRole;
import com.memorymain.entity.SysUserRole;
import com.memorymain.entity.User;
import com.memorymain.mapper.SysRoleMapper;
import com.memorymain.mapper.SysUserRoleMapper;
import com.memorymain.mapper.UserMapper;
import com.memorymain.util.Constants;
import com.memorymain.util.MD5Utils;
import com.memorymain.util.R;
import com.memorymain.util.StringUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import javax.annotation.Resource;


@RestController
public class RegisterController {
    @Resource
    private UserMapper userMapper;
    @Resource
    private SysRoleMapper sysRoleMapper;
    @Resource
    private SysUserRoleMapper sysUserRoleMapper;

    /**
     * 注册
     */
    @SaIgnore
    @PostMapping("/register")
    @Transactional
    public R<?> register(@RequestBody User user)
    {
        String username = user.getUsername(), password = user.getPassword();
        if (StringUtils.isEmpty(username))
        {
            return R.fail("用户名不能为空");
        }
        else if (StringUtils.isEmpty(password))
        {
            return R.fail("用户密码不能为空");
        }
        else if (username.length() < Constants.USERNAME_MIN_LENGTH
                || username.length() > Constants.USERNAME_MAX_LENGTH)
        {
            return R.fail("账户长度必须在2到20个字符之间");
        }
        else if (password.length() < Constants.PASSWORD_MIN_LENGTH
                || password.length() > Constants.PASSWORD_MAX_LENGTH)
        {
            return R.fail("密码长度必须在5到20个字符之间");
        }
        else if (userMapper.selectCount(new QueryWrapper<User>().lambda().eq(User::getUsername, username))>0)
        {
            return R.fail("注册账号已存在");
        }
        else
        {
            user.setPassword(MD5Utils.code(password));
            userMapper.insert(user);
            SysRole sysRole = sysRoleMapper.selectOne(new LambdaQueryWrapper<SysRole>().eq(SysRole::getRoleKey, Constants.COMMON));
            if(StringUtils.isNotNull(sysRole)){
                SysUserRole sysUserRole = new SysUserRole();
                sysUserRole.setRoleId(sysRole.getRoleId());
                sysUserRole.setUserId(user.getUserId());
                sysUserRoleMapper.insert(sysUserRole);
            }
        }
        return R.ok();
    }
}
