package com.web.pipline;

/**
 * @Author 17131
 * @Date 2024/1/11
 * @Description: 责任链业务执行器
 */
public interface BusinessProcess<T extends ProcessModel>  {
    /**
     * 真正处理逻辑
     * @param context
     */
    void process(ProcessContext<T> context);
}
