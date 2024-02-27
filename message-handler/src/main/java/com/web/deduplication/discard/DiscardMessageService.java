package com.web.deduplication.discard;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.web.constant.CommonConstant;
import com.web.domain.TaskInfo;
import com.web.service.ConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Author 17131
 * @Date 2024/2/27
 * @Description:
 */
@Service
public class DiscardMessageService {
    private static final String DISCARD_MESSAGE_KEY = "discardMsgIds";

    @Autowired
    private ConfigService config;

    public boolean isDiscard(TaskInfo taskInfo) {
        JSONArray array = JSON.parseArray(config.getProperty(DISCARD_MESSAGE_KEY, CommonConstant.EMPTY_VALUE_JSON_ARRAY));
        if (array.contains(String.valueOf(taskInfo.getMessageTemplateId()))) {
            return true;
        }
        return false;
    }
}
