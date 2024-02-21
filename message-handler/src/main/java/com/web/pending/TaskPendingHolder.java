package com.web.pending;

import com.dtp.core.thread.DtpExecutor;
import com.web.config.HandlerThreadPoolConfig;
import com.web.utils.GroupIdMappingUtils;
import com.web.utils.ThreadPoolUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

/**
 * @Author 17131
 * @Date 2024/2/21
 * @Description:存储每个groupId和线程池的对应关系
 */
@Component
public class TaskPendingHolder {
    @Autowired
    private ThreadPoolUtil threadPoolUtils;

    private Map<String, ExecutorService> taskPendingHolder = new HashMap<>(32);

    private static List<String> groupIds = GroupIdMappingUtils.getAllGroupIds();

    /**
     * 给所有不同渠道不同类型的消息都初始化一个线程池
     */
    @PostConstruct
    public void init() {
        for (String groupId : groupIds) {
            DtpExecutor executor = HandlerThreadPoolConfig.getExecutor(groupId);
            taskPendingHolder.put(groupId, executor);

            threadPoolUtils.register(executor);
        }
    }

    /**
     * 得到对应的线程池
     *
     * @param groupId
     * @return
     */
    public ExecutorService route(String groupId) {
        return taskPendingHolder.get(groupId);
    }
}
