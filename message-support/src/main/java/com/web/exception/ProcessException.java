package com.web.exception;

import com.web.enums.RespStatusEnum;
import com.web.pipline.ProcessContext;

/**
 * @Author 17131
 * @Date 2024/1/14
 * @Description:
 */
public class ProcessException extends RuntimeException{

    private ProcessContext processContext;

    public ProcessException(ProcessContext processContext) {
        super();
        this.processContext = processContext;
    }

    public ProcessException(ProcessContext processContext, Throwable cause) {
        super(cause);
        this.processContext = processContext;
    }

    @Override
    public String getMessage() {
        if (this.processContext != null) {
            return this.processContext.getResponse().getMsg();
        } else {
            return RespStatusEnum.CONTEXT_IS_NULL.getMsg();
        }
    }

    public ProcessContext getProcessContext() {
        return processContext;
    }
}
