package com.web.service;

import com.web.domain.SendRequest;
import com.web.domain.SendResponse;

/**
 * @Author 17131
 * @Date 2024/1/10
 * @Description:
 */
public interface RecallService {
    /**
     * 根据模板ID撤回消息
     *
     * @param sendRequest
     * @return
     */
    SendResponse recall(SendRequest sendRequest);

}
