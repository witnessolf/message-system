package com.web.service.Impl;

import cn.hutool.core.collection.CollUtil;
import com.web.domain.AnchorInfo;
import com.web.domain.LogParam;
import com.web.domain.MessageTemplate;
import com.web.domain.TaskInfo;
import com.web.enums.AnchorState;
import com.web.pending.Task;
import com.web.pending.TaskPendingHolder;
import com.web.service.ConsumeService;
import com.web.utils.GroupIdMappingUtils;
import com.web.utils.LogUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author 17131
 * @Date 2024/2/21
 * @Description:
 */
@Service
public class ConsumeServiceImpl implements ConsumeService {

    private static final String LOG_BIZ_TYPE = "Receiver#consumer";
    private static final String LOG_BIZ_RECALL_TYPE = "Receiver#recall";
    @Autowired
    private ApplicationContext context;
    @Autowired
    private LogUtil logUtil;
    @Autowired
    private TaskPendingHolder taskPendingHolder;

    @Override
    public void consume2Send(List<TaskInfo> taskInfoLists) {
        // 1. 从mq中获取发送信息list，提取groupId
        String topicGroupId = GroupIdMappingUtils.getGroupIdByTaskInfo(CollUtil.getFirst(taskInfoLists.iterator()));
        for (TaskInfo taskInfo : taskInfoLists) {
            // 2. 从上下文中找到Task Bean将其传入到对应的线程池中执行
            Task task = context.getBean(Task.class).setTaskInfo(taskInfo);
            // 接受到请求的时候需要打印入参以及点位消息
            logUtil.print(LogParam.builder().bizType(LOG_BIZ_TYPE).object(taskInfo).build(),
                    AnchorInfo.builder().ids(taskInfo.getReceiver()).businessId(taskInfo.getBusinessId()).state(AnchorState.RECEIVE.getCode()).build());

            taskPendingHolder.route(topicGroupId).execute(task);
        }
    }

    @Override
    public void consume2recall(MessageTemplate messageTemplate) {
        logUtil.print(LogParam.builder().bizType(LOG_BIZ_RECALL_TYPE).object(messageTemplate).build());
    }
}
