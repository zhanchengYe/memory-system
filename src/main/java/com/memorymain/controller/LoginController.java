package com.memorymain.controller;

import cn.dev33.satoken.annotation.SaIgnore;
import cn.dev33.satoken.context.SaHolder;
import cn.dev33.satoken.context.model.SaStorage;
import cn.dev33.satoken.stp.SaLoginModel;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.memorymain.config.CustomException;
import com.memorymain.entity.SysRole;
import com.memorymain.entity.SysUserRole;
import com.memorymain.entity.User;
import com.memorymain.mapper.SysRoleMapper;
import com.memorymain.mapper.SysUserRoleMapper;
import com.memorymain.mapper.UserMapper;
import com.memorymain.util.*;
import com.memorymain.vo.LoginBody;
import com.memorymain.vo.UserVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RestController
public class LoginController {
    @Resource
    private UserMapper userMapper;
    @Resource
    private RedisCache redisCache;
    @Resource
    private SysUserRoleMapper userRoleMapper;
    @Resource
    private SysRoleMapper roleMapper;

    // 登录
    @SaIgnore
    @PostMapping("/login")
    public R<?> login(@Validated @RequestBody LoginBody loginBody) {

        Map<String, Object> ajax = new HashMap<>();
        validateCaptcha(loginBody.getCode(), loginBody.getUuid());
        User user1 = userMapper.selectOne(new LambdaQueryWrapper<User>()
                        .eq(User::getUsername, loginBody.getUsername())
                        .eq(User::getStatus,Constants.NORMAL));
        if (ObjectUtil.isNull(user1)) {
            throw new CustomException("登录账号错误:" + loginBody.getUsername());
        }
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, loginBody.getUsername())
                .eq(User::getPassword,MD5Utils.code(loginBody.getPassword()))
                .eq(User::getStatus,Constants.NORMAL));
        if (ObjectUtil.isNull(user)) {
            throw new CustomException("登录密码错误:" + loginBody.getUsername());
        }
        UserVo loginUser = buildUserVo(user);
        // 生成token
        SaStorage storage = SaHolder.getStorage();
        storage.set(Constants.LOGIN_USER_KEY, loginUser);
        storage.set(Constants.USER_KEY, loginUser.getUserId());
        SaLoginModel model = new SaLoginModel();
        model.setDevice("pc");
        StpUtil.login(loginUser.getLoginId(), model.setExtra(Constants.USER_KEY, loginUser.getUserId()));
        StpUtil.getTokenSession().set(Constants.LOGIN_USER_KEY, loginUser);
        ajax.put(Constants.TOKEN, StpUtil.getTokenValue());
        return R.ok(ajax);
    }

    /**
     * 退出登录
     */
    @PostMapping("/logout")
    public R<?> logout() {
        StpUtil.logout();
        return R.ok("退出成功");
    }

    @GetMapping("/test")
    public R<?> test() {
        UserVo loginUser = LoginHelper.getLoginUser();
        System.out.println(loginUser);
        Long userId = LoginHelper.getUserId();
        System.out.println(userId);
        return R.ok();
    }

    public void validateCaptcha(String code, String uuid)
    {
        String verifyKey = Constants.CAPTCHA_CODE_KEY + StringUtils.nvl(uuid, "");
        String captcha = redisCache.getCacheObject(verifyKey);
        redisCache.deleteObject(verifyKey);
        if (captcha == null)
        {
            throw new RuntimeException("验证码错误");
        }
        if (!code.equalsIgnoreCase(captcha))
        {
            throw new RuntimeException("验证码错误");
        }
    }

    /**
     * 构建登录用户
     */
    private UserVo buildUserVo(User user) {
        UserVo loginUser = new UserVo();
        BeanMapper.copy(user,loginUser);
        List<SysUserRole> roles = userRoleMapper.selectList(new QueryWrapper<SysUserRole>().lambda()
                .eq(SysUserRole::getUserId, user.getUserId()));
        Set<Long> set = roles.stream().filter(StringUtils::isNotNull).map(SysUserRole::getRoleId).collect(Collectors.toSet());
        List<SysRole> sysRoles = roleMapper.selectList(new QueryWrapper<SysRole>().lambda().in(SysRole::getRoleId, set));
        loginUser.setRoles(sysRoles.stream().map(SysRole::getRoleKey).collect(Collectors.toList()));
        return loginUser;
    }

}
