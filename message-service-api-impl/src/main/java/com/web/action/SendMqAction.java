package com.web.action;

import com.web.domain.SendTaskModel;
import com.web.pipline.BusinessProcess;
import com.web.pipline.ProcessContext;

/**
 * @Author 17131
 * @Date 2024/1/22
 * @Description:
 */
public class SendMqAction implements BusinessProcess<SendTaskModel> {
    @Override
    public void process(ProcessContext<SendTaskModel> context) {

    }
}
