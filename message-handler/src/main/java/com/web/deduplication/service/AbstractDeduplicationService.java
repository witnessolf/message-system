package com.web.deduplication.service;

import com.web.deduplication.DeduplicationHolder;
import com.web.deduplication.DeduplicationParam;
import com.web.deduplication.limit.LimitService;
import com.web.domain.AnchorInfo;
import com.web.domain.TaskInfo;
import com.web.utils.LogUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import java.util.Set;

/**
 * @Author 17131
 * @Date 2024/2/25
 * @Description:去重服务抽象类（模板方法设计模式）
 */
public abstract class AbstractDeduplicationService implements DeduplicationService{

    protected Integer deduplicationType;

    protected LimitService limitService;

    @Autowired
    private DeduplicationHolder deduplicationHolder;

    @Autowired
    private LogUtil logUtil;

    @PostConstruct
    public void init() {
        deduplicationHolder.putService(deduplicationType, this);
    }

    /**
     * 根据消息内容和接收者构建用于去重的key
     * @param taskInfo
     * @param receiver
     * @return
     */
    public abstract String deduplicationSingleKey(TaskInfo taskInfo, String receiver);

    /**
     * 调用具体去重逻辑类，剔除符合去重条件的用户
     * @param param
     */
    @Override
    public void deduplication(DeduplicationParam param) {
        TaskInfo taskInfo = param.getTaskInfo();
        Set<String> limitSet = limitService.limitFilter(this, taskInfo, param);
        if (!CollectionUtils.isEmpty(limitSet)) {
            taskInfo.getReceiver().removeAll(limitSet);
            logUtil.print(AnchorInfo.builder().businessId(taskInfo.getBusinessId()).ids(limitSet).state(param.getAnchorState().getCode()).build());
        }
    }
}
