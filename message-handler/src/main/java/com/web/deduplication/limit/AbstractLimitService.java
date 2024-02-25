package com.web.deduplication.limit;

import com.web.deduplication.service.AbstractDeduplicationService;
import com.web.domain.TaskInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author 17131
 * @Date 2024/2/25
 * @Description:去重逻辑实现抽象类
 */
public abstract class AbstractLimitService implements LimitService{

    /**
     * 根据消息模板和接收者获取对应的去重key
     * @param service
     * @param taskInfo
     * @param receiver
     * @return
     */
    protected String deduplicationSingleKey(AbstractDeduplicationService service, TaskInfo taskInfo, String receiver) {
        return service.deduplicationSingleKey(taskInfo, receiver);
    }

    /**
     * 获取得到当前消息模板所有的去重Key
     * @param service
     * @param taskInfo
     * @return
     */
    protected List<String> deduplicationAllKey(AbstractDeduplicationService service, TaskInfo taskInfo) {
        List<String> list = new ArrayList<>(taskInfo.getReceiver().size());
        for (String receiver : taskInfo.getReceiver()) {
            String key = service.deduplicationSingleKey(taskInfo, receiver);
            list.add(key);
        }
        return list;
    }

}
