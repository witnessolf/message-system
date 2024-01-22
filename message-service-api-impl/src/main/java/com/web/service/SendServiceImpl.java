package com.web.service;

import com.web.domain.BatchSendRequest;
import com.web.domain.SendRequest;
import com.web.domain.SendResponse;
import org.springframework.stereotype.Service;

/**
 * @Author 17131
 * @Date 2024/1/14
 * @Description:
 */
@Service
public class SendServiceImpl implements SendService{
    @Override
    public SendResponse send(SendRequest sendRequest) {
        return null;
    }

    @Override
    public SendResponse batchSend(BatchSendRequest batchSendRequest) {
        return null;
    }
}
