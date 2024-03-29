package com.web.pending;

import cn.hutool.core.collection.CollUtil;
import com.web.deduplication.DeduplicationRuleService;
import com.web.deduplication.discard.DiscardMessageService;
import com.web.domain.TaskInfo;
import com.web.handler.Handler;
import com.web.handler.HandlerHolder;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

/**
 * @Author 17131
 * @Date 2024/2/21
 * @Description:
 * Task 执行器
 * 0.丢弃消息
 * 2.屏蔽消息
 * 2.通用去重功能
 * 3.发送消息
 */
@Data
@Accessors(chain = true)
@Slf4j
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class Task implements Runnable{

    private TaskInfo taskInfo;

    @Autowired
    private HandlerHolder handlerHolder;
    @Autowired
    private DeduplicationRuleService deduplicationRuleService;
    @Autowired
    private DiscardMessageService discardMessageService;

    @Override
    public void run() {
        log.info("task:" + Thread.currentThread().getName());

        // 0. 丢弃消息
        if (discardMessageService.isDiscard(taskInfo)) {
            return;
        }

        // 2. 平台去重
        if (CollUtil.isNotEmpty(taskInfo.getReceiver())) {
            deduplicationRuleService.duplication(taskInfo);
        }

        // 3. 真正发送消息
        if (CollUtil.isNotEmpty(taskInfo.getReceiver())) {
            handlerHolder.route(taskInfo.getSendChannel()).doHandler(taskInfo);
        }
    }
}
