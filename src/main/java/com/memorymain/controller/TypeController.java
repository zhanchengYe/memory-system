package com.memorymain.controller;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.memorymain.entity.Type;
import com.memorymain.mapper.TypeMapper;
import com.memorymain.util.LoginHelper;
import com.memorymain.util.R;
import com.memorymain.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/type")
public class TypeController {
    @Resource
    private TypeMapper typeMapper;

    // 根据新闻编号获取详细信息
    @GetMapping(value = {"/", "/{tyId}"})
    public R<?> getInfo(@PathVariable(value = "tyId", required = false) Long tyId) {
        return R.ok(typeMapper.selectById(tyId));
    }

    // 获取分页列表
    @GetMapping("/list")
    public R<?> list(Type type, int pageNum, int pageSize) {
        pageNum = ObjectUtil.defaultIfNull(pageNum, 1);
        pageSize = ObjectUtil.defaultIfNull(pageSize, 10);
        PageHelper.startPage(pageNum,pageSize);
        LambdaQueryWrapper<Type> wrapper = new LambdaQueryWrapper<Type>()
                .like(StringUtils.isNotBlank(type.getName()), Type::getName, type.getName())
                .eq(Type::getCreateBy,LoginHelper.getUserId());
        List<Type> typeList = typeMapper.selectList(wrapper);
        HashMap<String, Object> rspData = new HashMap<>();
        rspData.put("rows",typeList);
        rspData.put("total",new PageInfo(typeList).getTotal());
        return R.ok(rspData);
    }

    // 添加信息
    @PostMapping
    public R<?> save(@Validated @RequestBody Type type) {
        type.setCreateBy(LoginHelper.getUserId());
        typeMapper.insert(type);
        return R.ok();
    }

    // 更新信息
    @PutMapping
    public R<?> update(@Validated @RequestBody Type type) {
        typeMapper.updateById(type);
        return R.ok();
    }

    // 删除信息
    @DeleteMapping("/{typeIds}")
    public R<?> remove(@PathVariable Long[] typeIds) {
        typeMapper.delete(new QueryWrapper<Type>().lambda()
                .in(Type::getTypeId,typeIds));
        return R.ok();
    }
}
