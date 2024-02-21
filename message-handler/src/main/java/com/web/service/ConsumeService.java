package com.web.service;

import com.web.domain.MessageTemplate;
import com.web.domain.TaskInfo;

import java.util.List;

/**
 * @Author 17131
 * @Date 2024/2/21
 * @Description:
 */
public interface ConsumeService {
    /**
     * 从MQ拉到消息进行消费，发送消息
     *
     * @param taskInfoLists
     */
    void consume2Send(List<TaskInfo> taskInfoLists);


    /**
     * 从MQ拉到消息进行消费，撤回消息
     *
     * @param messageTemplate
     */
    void consume2recall(MessageTemplate messageTemplate);
}
