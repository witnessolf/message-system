package com.web.handler;

import com.web.domain.TaskInfo;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;

/**
 * @Author 17131
 * @Date 2024/3/2
 * @Description:
 */
public abstract class AbstractHandler implements Handler {
    @Autowired
    private HandlerHolder handlerHolder;

    /**
     * 标识渠道的Code
     * 子类初始化的时候指定
     */
    protected Integer channelCode;

    @PostConstruct
    public void init() {
        handlerHolder.putHandler(channelCode, this);
    }

    @Override
    public void doHandler(TaskInfo taskInfo) {
        if (handler(taskInfo)) {
            return;
        }
    }

    /**
     * 统一处理的handler接口
     *
     * @param taskInfo
     * @return
     */
    public abstract boolean handler(TaskInfo taskInfo);

}
