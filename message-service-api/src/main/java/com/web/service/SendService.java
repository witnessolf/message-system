package com.web.service;

import com.web.domain.BatchSendRequest;
import com.web.domain.SendRequest;
import com.web.domain.SendResponse;

/**
 * @Author 17131
 * @Date 2024/1/10
 * @Description:
 */
public interface SendService {
    /**
     * 单文案发送接口
     *
     * @param sendRequest
     * @return
     */
    SendResponse send(SendRequest sendRequest);


    /**
     * 多文案发送接口
     *
     * @param batchSendRequest
     * @return
     */
    SendResponse batchSend(BatchSendRequest batchSendRequest);

}
