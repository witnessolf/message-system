package com.web.handler;

import com.web.domain.AnchorInfo;
import com.web.domain.TaskInfo;
import com.web.enums.AnchorState;
import com.web.utils.LogUtil;
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

    @Autowired
    private LogUtil logUtil;

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
        // 处理消息的时候只需要记录消息处理情况的日志（点位信息）
        if (handler(taskInfo)) {
            logUtil.print(AnchorInfo.builder().state(AnchorState.SEND_SUCCESS.getCode()).businessId(taskInfo.getBusinessId()).ids(taskInfo.getReceiver()).build());
            return;
        }
        logUtil.print(AnchorInfo.builder().state(AnchorState.SEND_FAIL.getCode()).businessId(taskInfo.getBusinessId()).ids(taskInfo.getReceiver()).build());

    }

    /**
     * 统一处理的handler接口
     *
     * @param taskInfo
     * @return
     */
    public abstract boolean handler(TaskInfo taskInfo);

}
