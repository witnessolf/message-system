package com.web.exception;

import com.web.enums.RespStatusEnum;
import lombok.Getter;


/**
 * @Author 17131
 * @Date 2024/3/6
 * @Description:
 */
@Getter
public class CommonException extends RuntimeException{
    private String code = RespStatusEnum.ERROR_400.getCode();
    private RespStatusEnum respStatusEnum = null;

    public CommonException(String message) {
        super(message);
    }

    public CommonException(RespStatusEnum respStatusEnum) {
        super(respStatusEnum.getMsg());
        this.code = respStatusEnum.getCode();
        this.respStatusEnum = respStatusEnum;
    }

    public CommonException(String code, String message) {
        super(message);
        this.code = code;
    }

    public CommonException(String code, String message, RespStatusEnum respStatusEnum) {
        super(message);
        this.code = code;
        this.respStatusEnum = respStatusEnum;
    }

    public CommonException(String message, Exception e) {
        super(message, e);
    }

    public CommonException(String code, String message, Exception e) {
        super(message, e);
        this.code = code;
    }
}
