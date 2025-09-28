package com.memorymain.config;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
import cn.dev33.satoken.util.SaResult;
import com.memorymain.util.R;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLIntegrityConstraintViolationException;

@RestControllerAdvice(annotations = {RestController.class, Controller.class}) //拦截加了@RestController或@Controller的类
public class GlobalExceptionHandler {

    // 拦截: 项目中的NotLoginException异常
    @ExceptionHandler(NotLoginException.class)
    public R<?> handlerNotLoginException(NotLoginException nle)
            throws Exception {
        // 打印堆栈，以供调试
        nle.printStackTrace();
        // 判断场景值，定制化异常信息
        String message = "";
        if(nle.getType().equals(NotLoginException.NOT_TOKEN)) {
            message = "未提供token";
        }
        else if(nle.getType().equals(NotLoginException.INVALID_TOKEN)) {
            message = "token无效";
        }
        else if(nle.getType().equals(NotLoginException.TOKEN_TIMEOUT)) {
            message = "token已过期";
        }
        else if(nle.getType().equals(NotLoginException.BE_REPLACED)) {
            message = "token已被顶下线";
        }
        else if(nle.getType().equals(NotLoginException.KICK_OUT)) {
            message = "token已被踢下线";
        }
        else {
            message = "当前会话未登录";
        }
        // 返回给前端
        return R.fail(401,message);
    }

    // 拦截：缺少权限异常
    @ExceptionHandler(NotPermissionException.class)
    public R<?> handlerException(NotPermissionException e) {
        e.printStackTrace();
        return R.fail(403,"缺少权限");
    }

    // 拦截：缺少角色异常
    @ExceptionHandler(NotRoleException.class)
    public R<?> handlerException(NotRoleException e) {
        e.printStackTrace();
        return R.fail(403,"缺少角色");
    }


    /**
     * 异常处理
     * 插入数据字段不可重复限制
     * @param ex
     * @return
     */
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public SaResult exceptionHandler(SQLIntegrityConstraintViolationException ex){
//        log.error(ex.getMessage());
        ex.printStackTrace();
        //Duplicate entry 'lisi' for key 'idx_username'
        if (ex.getMessage().contains("Duplicate entry")){
            String[] split = ex.getMessage().split(" ");
            String msg = split[2] + "已存在";
            return SaResult.error(msg);
        }
        return SaResult.error("未知错误");
    }


    /**
     * 处理自定义异常
     * 与CustomException配合
     * @param ex
     * @return
     */
    @ExceptionHandler(CustomException.class)
    public SaResult exceptionHandler(CustomException ex){
        ex.printStackTrace();
        return SaResult.error(ex.getMessage());
    }


    // 拦截：其它所有异常
    @ExceptionHandler(Exception.class)
    public SaResult handlerException(Exception e) {
        e.printStackTrace();
        return SaResult.error(e.getMessage());
    }

}
