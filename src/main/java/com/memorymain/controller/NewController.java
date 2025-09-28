package com.memorymain.controller;

import cn.dev33.satoken.annotation.SaIgnore;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.memorymain.entity.New;
import com.memorymain.mapper.NewMapper;
import com.memorymain.util.BeanMapper;
import com.memorymain.util.ExcelUtil;
import com.memorymain.util.R;
import com.memorymain.util.StringUtils;
import com.memorymain.vo.NewExportVo;
import com.memorymain.vo.SysUserExportVo;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/new")
public class NewController {
    @Resource
    private NewMapper newMapper;

    // 根据新闻编号获取详细信息
    @GetMapping(value = {"/", "/{newId}"})
    public R<?> getInfo(@PathVariable(value = "newId", required = false) Long newId) {
        return R.ok(newMapper.selectById(newId));
    }

    // 轮播图
    @GetMapping("/news")
    public R<?> news() {
        return R.ok(newMapper.selectList(new QueryWrapper<New>().lambda()
                .orderByDesc(New::getCreateTime)
                .last("limit 10")));
    }

    /**
     * 导出用户数量
     */
    @SaIgnore
    @GetMapping("/export")
    public void export(New news, HttpServletResponse response){
        LambdaQueryWrapper<New> wrapper = new LambdaQueryWrapper<New>()
                .like(StringUtils.isNotBlank(news.getTitle()), New::getTitle, news.getTitle())
                .like(StringUtils.isNotBlank(news.getContent()), New::getContent, news.getContent());
        List<New> newList = newMapper.selectList(wrapper);
        List<NewExportVo> listVo = BeanMapper.mapList(newList, NewExportVo.class);
        ExcelUtil.exportExcel(listVo, "公告数据", NewExportVo.class, response);
    }

    // 获取分页列表
    @GetMapping("/list")
    public R<?> list(New news, int pageNum, int pageSize) {
        pageNum = ObjectUtil.defaultIfNull(pageNum, 1);
        pageSize = ObjectUtil.defaultIfNull(pageSize, 10);
        PageHelper.startPage(pageNum,pageSize);
        LambdaQueryWrapper<New> wrapper = new LambdaQueryWrapper<New>()
                .like(StringUtils.isNotBlank(news.getTitle()), New::getTitle, news.getTitle())
                .like(StringUtils.isNotBlank(news.getContent()), New::getContent, news.getContent());
        List<New> newList = newMapper.selectList(wrapper);
        HashMap<String, Object> rspData = new HashMap<>();
        rspData.put("rows",newList);
        rspData.put("total",new PageInfo(newList).getTotal());
        return R.ok(rspData);
    }

    // 添加信息
    @PostMapping
    public R<?> save(@Validated @RequestBody New news) {
        news.setCreateTime(new Date());
        newMapper.insert(news);
        return R.ok();
    }

    // 更新信息
    @PutMapping
    public R<?> update(@RequestBody New news) {
        newMapper.updateById(news);
        return R.ok();
    }

    // 删除信息
    @DeleteMapping("/{newIds}")
    public R<?> remove(@PathVariable Long[] newIds) {
        newMapper.delete(new QueryWrapper<New>().lambda()
                .in(New::getNewId,newIds));
        return R.ok();
    }
}
