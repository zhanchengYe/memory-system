package com.memorymain.controller;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.memorymain.entity.Tag;
import com.memorymain.mapper.TagMapper;
import com.memorymain.util.LoginHelper;
import com.memorymain.util.R;
import com.memorymain.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/tag")
public class TagController {
    @Resource
    private TagMapper tagMapper;

    // 根据新闻编号获取详细信息
    @GetMapping(value = {"/", "/{tagId}"})
    public R<?> getInfo(@PathVariable(value = "tagId", required = false) Long tagId) {
        return R.ok(tagMapper.selectById(tagId));
    }

    // 获取分页列表
    @GetMapping("/list")
    public R<?> list(Tag tag, int pageNum, int pageSize) {
        pageNum = ObjectUtil.defaultIfNull(pageNum, 1);
        pageSize = ObjectUtil.defaultIfNull(pageSize, 10);
        PageHelper.startPage(pageNum,pageSize);
        LambdaQueryWrapper<Tag> wrapper = new LambdaQueryWrapper<Tag>()
                .like(StringUtils.isNotBlank(tag.getName()), Tag::getName, tag.getName());
        List<Tag> tagList = tagMapper.selectList(wrapper);
        HashMap<String, Object> rspData = new HashMap<>();
        rspData.put("rows",tagList);
        rspData.put("total",new PageInfo(tagList).getTotal());
        return R.ok(rspData);
    }

    // 添加信息
    @PostMapping
    public R<?> save(@Validated @RequestBody Tag tag) {
        tag.setCreateBy(LoginHelper.getUserId());
        tagMapper.insert(tag);
        return R.ok();
    }

    // 更新信息
    @PutMapping
    public R<?> update(@Validated @RequestBody Tag tag) {
        tagMapper.updateById(tag);
        return R.ok();
    }

    // 删除信息
    @DeleteMapping("/{tagIds}")
    public R<?> remove(@PathVariable Long[] tagIds) {
        tagMapper.delete(new QueryWrapper<Tag>().lambda()
                .in(Tag::getTagId,tagIds));
        return R.ok();
    }
}
