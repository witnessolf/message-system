package com.web.handler;

import com.dtp.core.thread.DtpExecutor;
import com.web.config.CronAsyncThreadPoolConfig;
import com.web.service.TaskService;
import com.web.utils.ThreadPoolUtil;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Author 17131
 * @Date 2024/3/17
 * @Description:后台提交的定时任务处理类
 */
@Service
@Slf4j
public class CronTaskHandler {

    @Autowired
    private TaskService taskService;

    @Autowired
    private ThreadPoolUtil threadPoolUtil;

    // 「读取文件以及远程调用发送接口」是一件比较耗时的工作，直接用线程池做了层异步，及时返回xxl-job，避免定时任务超时
    private DtpExecutor dtpExecutor = CronAsyncThreadPoolConfig.getXxlCronExecutor();

    @XxlJob("austinJob")
    public void execute() {
        log.info("CronTaskHandler#execute messageTemplateId:{} cron exec!", XxlJobHelper.getJobParam());
        threadPoolUtil.register(dtpExecutor);

        Long messageTemplateId = Long.valueOf(XxlJobHelper.getJobParam());
        dtpExecutor.execute(() -> taskService.handle(messageTemplateId));
    }
}
