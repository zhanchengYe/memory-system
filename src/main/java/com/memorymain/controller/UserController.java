package com.memorymain.controller;

import cn.dev33.satoken.annotation.SaIgnore;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.memorymain.config.CustomException;
import com.memorymain.entity.*;
import com.memorymain.mapper.*;
import com.memorymain.util.*;
import com.memorymain.vo.SysUserExportVo;
import com.memorymain.vo.UserVo;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/user")
public class UserController {
    @Resource
    private UserMapper userMapper;
    @Resource
    private SysUserRoleMapper userRoleMapper;
    @Resource
    private SysRoleMapper sysRoleMapper;
    @Resource
    private AlbumMapper albumMapper;
    @Resource
    private ImageMapper imageMapper;
    @Resource
    private TypeMapper typeMapper;
    @Resource
    private TagMapper tagMapper;
    @Resource
    private EventMapper eventMapper;

    /**
     * 导出用户数量
     */
    @SaIgnore
    @GetMapping("/export")
    public void export(User user, HttpServletResponse response) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<User>()
                .like(StringUtils.isNotBlank(user.getUsername()), User::getUsername, user.getUsername())
                .like(StringUtils.isNotBlank(user.getEmail()), User::getEmail, user.getEmail())
                .like(StringUtils.isNotBlank(user.getPhone()), User::getPhone, user.getPhone())
                .like(StringUtils.isNotBlank(user.getCity()), User::getCity, user.getCity())
                .eq(StringUtils.isNotBlank(user.getSex()), User::getSex, user.getSex())
                .eq(StringUtils.isNotBlank(user.getStatus()), User::getStatus, user.getStatus());
        List<User> list = userMapper.selectList(wrapper);
        List<SysUserExportVo> listVo = BeanMapper.mapList(list, SysUserExportVo.class);
        ExcelUtil.exportExcel(listVo, "用户数据", SysUserExportVo.class, response);
    }

    /**
     * 根据用户编号获取详细信息
     *
     * @param userId 用户ID
     */
    @GetMapping(value = {"/", "/{userId}"})
    public R<Map<String, Object>> getInfo(@PathVariable(value = "userId", required = false) Long userId) {
        Map<String, Object> ajax = new HashMap<>();
        List<String> list = new ArrayList<String>();
        List<SysUserRole> roles = userRoleMapper.selectList(new QueryWrapper<SysUserRole>().lambda()
                .eq(SysUserRole::getUserId, userId));
        Set<SysUserRole> roleSet = roles.stream().filter(StringUtils::isNotNull).collect(Collectors.toSet());
        roleSet.forEach(role -> list.add(sysRoleMapper.selectById(role.getRoleId()).getRoleKey()));
        ajax.put("roles", list);
        if (StringUtils.isNotNull(userId)) {
            User sysUser = userMapper.selectById(userId);
            ajax.put("user", sysUser);
        }
        return R.ok(ajax);
    }

    /**
     * 新增用户
     */
    @PostMapping
    @Transactional
    public R<?> add(@Validated @RequestBody User user) {
        if (userMapper.selectCount(new QueryWrapper<User>().lambda().eq(User::getUsername, user.getUsername()))>0)
        {
            return R.fail("新增用户'" + user.getUsername() + "'失败，登录账号已存在");
        }
        else if (StringUtils.isNotEmpty(user.getPhone()) && userMapper.selectCount(new QueryWrapper<User>().lambda().eq(User::getPhone, user.getPhone()))>0) {
            return R.fail("新增用户'" + user.getUsername() + "'失败，手机号码已存在");
        } else if (StringUtils.isNotEmpty(user.getEmail()) && userMapper.selectCount(new QueryWrapper<User>().lambda().eq(User::getEmail, user.getEmail()))>0) {
            return R.fail("新增用户'" + user.getUsername() + "'失败，邮箱账号已存在");
        }
        user.setPassword(MD5Utils.code(user.getPassword()));
        userMapper.insert(user);
        SysRole sysRole = sysRoleMapper.selectOne(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getRoleKey, Constants.COMMON).last("limit 1"));
        if(StringUtils.isNotNull(sysRole)){
            SysUserRole sysUserRole = new SysUserRole();
            sysUserRole.setUserId(user.getUserId());
            sysUserRole.setRoleId(sysRole.getRoleId());
            userRoleMapper.insert(sysUserRole);
        }
        return R.ok();
    }

    /**
     * 修改用户
     */
    @PutMapping
    public R<?> edit( @RequestBody User user) {
        if (userMapper.selectCount(new QueryWrapper<User>().lambda().eq(User::getUsername, user.getUsername()).ne(User::getUserId,user.getUserId()))>0)
        {
            return R.fail("修改用户'" + user.getUsername() + "'失败，登录账号已存在");
        }
        else if (StringUtils.isNotEmpty(user.getPhone()) && userMapper.selectCount(new QueryWrapper<User>().lambda().eq(User::getPhone, user.getPhone()).ne(User::getUserId,user.getUserId()))>0) {
            return R.fail("修改用户'" + user.getUsername() + "'失败，手机号码已存在");
        } else if (StringUtils.isNotEmpty(user.getEmail()) && userMapper.selectCount(new QueryWrapper<User>().lambda().eq(User::getEmail, user.getEmail()).ne(User::getUserId,user.getUserId()))>0) {
            return R.fail("修改用户'" + user.getUsername() + "'失败，邮箱账号已存在");
        }
        user.setPassword(null);
        userMapper.update(user,new UpdateWrapper<User>().lambda()
                .eq(User::getUserId,user.getUserId())
                .set(StringUtils.isEmpty(user.getEmail()),User::getEmail,null)
                .set(StringUtils.isEmpty(user.getPhone()),User::getPhone,null)
                .set(StringUtils.isEmpty(user.getCity()),User::getCity,null)
                .set(StringUtils.isNull(user.getBirthday()),User::getBirthday,null)
        );
        return R.ok();
    }

    /**
     * 删除用户
     *
     * @param userIds 角色ID串
     */
    @DeleteMapping("/{userIds}")
    @Transactional(rollbackFor = Exception.class)
    public R<Void> remove(@PathVariable Long[] userIds) {
        if (ArrayUtil.contains(userIds, LoginHelper.getUserId())) {
            return R.fail("当前用户不能删除");
        }
        // 删除用户与角色关联
        userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().in(SysUserRole::getUserId, userIds));
        userMapper.delete(new LambdaQueryWrapper<User>().in(User::getUserId,userIds));
        return R.ok();
    }

    /**
     * 重置密码
     */
    @PutMapping("/resetPwd")
    public R<?> resetPwd(@RequestBody User user) {
        if (StringUtils.isNotNull(user.getUserId()) && Constants.ADMIN_ID.equals(user.getUserId())) {
            throw new CustomException("不允许操作超级管理员用户");
        }
        user.setPassword(MD5Utils.code(user.getPassword()));
        return R.ok(userMapper.updateById(user));
    }

    /**
     * 状态修改
     */
    @PutMapping("/changeStatus")
    public R<?> changeStatus(@RequestBody User user) {
        if (StringUtils.isNotNull(user.getUserId()) && Constants.ADMIN_ID.equals(user.getUserId())) {
            throw new CustomException("不允许操作超级管理员用户");
        }
        StpUtil.logout(user.getUserId());
        return R.ok(userMapper.updateById(user));
    }

    /**
     * 获取用户列表
     */
    @GetMapping("/list")
    public R<?> list(User user, int pageNum, int pageSize) {
        pageNum = ObjectUtil.defaultIfNull(pageNum, 1);
        pageSize = ObjectUtil.defaultIfNull(pageSize, 10);
        PageHelper.startPage(pageNum,pageSize);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<User>()
                .like(StringUtils.isNotBlank(user.getUsername()), User::getUsername, user.getUsername())
                .like(StringUtils.isNotBlank(user.getEmail()), User::getEmail, user.getEmail())
                .like(StringUtils.isNotBlank(user.getPhone()), User::getPhone, user.getPhone())
                .like(StringUtils.isNotBlank(user.getCity()), User::getCity, user.getCity())
                .eq(StringUtils.isNotBlank(user.getSex()), User::getSex, user.getSex())
                .eq(StringUtils.isNotBlank(user.getStatus()), User::getStatus, user.getStatus());
        List<User> userList = userMapper.selectList(wrapper);
        HashMap<String, Object> rspData = new HashMap<>();
        rspData.put("rows",userList);
        rspData.put("total",new PageInfo(userList).getTotal());
        return R.ok(rspData);
    }

    /**
     * 获取个人信息
     */
    @GetMapping("/getUserInfo")
    public R<?> list() {
        Long userId = LoginHelper.getUserId();
        User user = userMapper.selectById(userId);
        UserVo loginUser = new UserVo();
        BeanMapper.copy(user,loginUser);
        List<SysUserRole> roles = userRoleMapper.selectList(new QueryWrapper<SysUserRole>().lambda()
                .eq(SysUserRole::getUserId, user.getUserId()));
        Set<Long> set = roles.stream().filter(StringUtils::isNotNull).map(SysUserRole::getRoleId).collect(Collectors.toSet());
        List<SysRole> sysRoles = sysRoleMapper.selectList(new QueryWrapper<SysRole>().lambda().in(SysRole::getRoleId, set));
        loginUser.setRoles(sysRoles.stream().map(SysRole::getRoleKey).collect(Collectors.toList()));
        return R.ok(loginUser);
    }

    /**
     * 获取相册信息数量
     */
    @GetMapping("/getAllCount")
    public R<?> getAllCount() {
        HashMap<String, Integer> map = new HashMap<>();
        map.put("albumCount",albumMapper.selectCount(new LambdaQueryWrapper<Album>().eq(Album::getCreateBy, LoginHelper.getUserId())));
        map.put("imageCount",imageMapper.selectCount(new LambdaQueryWrapper<Image>().eq(Image::getCreateBy,LoginHelper.getUserId())));
        map.put("eventCount",eventMapper.selectCount(new LambdaQueryWrapper<Event>().eq(Event::getCreateBy,LoginHelper.getUserId())));
        map.put("typeCount",typeMapper.selectCount(new LambdaQueryWrapper<Type>().eq(Type::getCreateBy,LoginHelper.getUserId())));
        map.put("tagCount",tagMapper.selectCount(new LambdaQueryWrapper<Tag>().eq(Tag::getCreateBy,LoginHelper.getUserId())));
        return R.ok(map);
    }

}
