package com.web.advice;

import com.web.annotation.AustinResult;
import com.web.vo.BasicResultVO;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.util.Objects;

/**
 * @Author 17131
 * @Date 2024/3/5
 * @Description:统一返回结构
 */
//注意仅对返回值为 ResponseEntity 或者是有@ResponseBody 注解的控制器方法进行拦截
@ControllerAdvice(basePackages = "com.web.controller")
public class AustinResponseBodyAdvice implements ResponseBodyAdvice<Object> {

    private static final String RETURN_CLASS = "BasicResultVO";

    /**
     * 该方法可以用于制定准入规则，只有当该方法返回True时，才会进入beforeBodyWrite方法
     * @param methodParameter
     * @param converterType
     * @return
     */
    @Override
    public boolean supports(MethodParameter methodParameter, Class converterType) {
        // 检查methodParameter对象所属的类是否有注解，以及methodParameter对象方法上是否有注解
        return methodParameter.getContainingClass().isAnnotationPresent(AustinResult.class)
                || methodParameter.hasMethodAnnotation(AustinResult.class);
    }

    /**
     * 针对返回的数据data进行统一处理
     * @param data
     * @param methodParameter
     * @param mediaType
     * @param aClass
     * @param serverHttpRequest
     * @param serverHttpResponse
     * @return
     */
    @Override
    public Object beforeBodyWrite(Object data, MethodParameter methodParameter, MediaType mediaType, Class aClass,
                                  ServerHttpRequest serverHttpRequest, ServerHttpResponse serverHttpResponse) {
        //如果返回的数据是BasicResultVO类型则直接返回，否则用BasicResultVO进行包装
        if (Objects.nonNull(data) && Objects.nonNull(data.getClass())) {
            String simpleName = data.getClass().getSimpleName();
            if (RETURN_CLASS.equalsIgnoreCase(simpleName)) {
                return data;
            }
        }
        return BasicResultVO.success(data);
    }
}
