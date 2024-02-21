package com.web.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @Author 17131
 * @Date 2024/2/21
 * @Description:优雅关闭线程池
 */
@Component
@Slf4j
public class ThreadPoolExecutorShutdownDefinition implements ApplicationListener<ContextClosedEvent> {

    private final List<ExecutorService> POOLS = Collections.synchronizedList(new ArrayList<>(12));

    /**
     * 线程中的任务在接收到应用关闭信号量后最多等待多久就强制终止，其实就是给剩余任务预留的时间， 到时间后线程池必须销毁
     */
    private final long AWAIT_TERMINATION = 20;

    /**
     * awaitTermination的单位
     */
    private final TimeUnit TIME_UNIT = TimeUnit.SECONDS;

    /**
     * 将各个groupID的线程池加入到POOLS中管理
     * @param executor
     */
    public void registryExecutor(ExecutorService executor) {
        POOLS.add(executor);
    }

    /**
     * 先调用shutdown()关闭线程池，然后awaitTermination()同步等待指定时间，若返回false证明关闭线程池超时
     * @param event
     */
    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
        log.info("容器关闭前处理线程池优雅关闭开始, 当前要处理的线程池数量为: {} >>>>>>>>>>>>>>>>", POOLS.size());
        if (CollectionUtils.isEmpty(POOLS)) {
            return;
        }
        for (ExecutorService pool : POOLS) {
            pool.shutdown();
            try {
                if (!pool.awaitTermination(AWAIT_TERMINATION, TIME_UNIT)) {
                    if (log.isWarnEnabled()) {
                        log.warn("Timed out while waiting for executor [{}] to terminate", pool);
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                if (log.isWarnEnabled()) {
                    log.warn("Timed out while waiting for executor [{}] to terminate", pool);
                }
                Thread.currentThread().interrupt();
            }
        }

    }
}
