package com.web.aspect;

import cn.hutool.core.util.IdUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.web.vo.RequestLogDTO;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;

/**
 * @Author 17131
 * @Date 2024/3/5
 * @Description:
 */
@Slf4j
@Aspect
@Component
public class AustinAspect {
    @Autowired
    private HttpServletRequest request;

    /**
     * 同一个请求的KEY
     */
    private final String REQUEST_ID_KEY = "request_unique_id";


    /**
     * @within针对类上的注解  @annotation针对方法上的注解
     */
    @Pointcut("@within(com.web.annotation.AustinAspect) || @annotation(com.web.annotation.AustinAspect)")
    public void executeService() {

    }

    /**
     * 前置通知
     * @param joinPoint
     */
    @Before("executeService()")
    public void doBeforeAdvice(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        this.printRequestLog(signature, joinPoint.getArgs());
    }

    /**
     * 异常通知
     *
     * @param ex
     */
    @AfterThrowing(value = "executeService()", throwing = "ex")
    public void doAfterThrowingAdvice(Throwable ex) {
        printExceptionLog(ex);
    }

    /**
     * 打印异常日志
     *
     * @param ex
     */
    private void printExceptionLog(Throwable ex) {
        JSONObject logVo = new JSONObject();
        logVo.put("id", request.getAttribute(REQUEST_ID_KEY));
        log.error(JSON.toJSONString(logVo), ex);
    }

    /**
     * 打印请求日志
     *
     * @param signature
     * @param argObs
     */
    private void printRequestLog(MethodSignature signature, Object[] argObs) {
        RequestLogDTO logVo = new RequestLogDTO();
        logVo.setId(IdUtil.simpleUUID());
        request.setAttribute(REQUEST_ID_KEY, logVo.getId());
        logVo.setUri(request.getRequestURI());
        logVo.setMethod(request.getMethod());

        // 将不能够转成json字符串的参数过滤掉
        List<Object> args = Lists.newArrayList();
        Arrays.stream(argObs).forEach(e -> {
            if (e instanceof MultipartFile || e instanceof HttpServletRequest || e instanceof HttpServletResponse
                    || e instanceof BindingResult) {
                return;
            }
            args.add(e);
        });

        logVo.setArgs(args.toArray());
        logVo.setProduct("austin");
        logVo.setPath(signature.getDeclaringTypeName() + "." + signature.getMethod().getName());
        logVo.setReferer(request.getHeader("referer"));
        logVo.setRemoteAddr(request.getRemoteAddr());
        logVo.setUserAgent(request.getHeader("user-agent"));
        log.info(JSON.toJSONString(logVo));
    }
}
