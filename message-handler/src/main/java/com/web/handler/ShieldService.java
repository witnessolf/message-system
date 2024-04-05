package com.web.handler;

import com.web.domain.TaskInfo;

/**
 * @Author 17131
 * @Date 2024/4/5
 * @Description:屏蔽服务
 */
public interface ShieldService {

    /**
     * 屏蔽消息
     *
     * @param taskInfo
     */
    void shield(TaskInfo taskInfo);
}
