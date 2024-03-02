package com.web.handler;

import com.web.domain.MessageTemplate;
import com.web.domain.TaskInfo;

/**
 * @Author 17131
 * @Date 2024/3/2
 * @Description:消息处理器
 */
public interface Handler {
    /**
     * 发送消息处理器
     *
     * @param taskInfo
     */
    void doHandler(TaskInfo taskInfo);

    /**
     * 撤回消息
     *
     * @param messageTemplate
     * @return
     */
    void recall(MessageTemplate messageTemplate);
}
