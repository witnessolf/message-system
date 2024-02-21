package com.web.utils;

import com.dtp.core.DtpRegistry;
import com.dtp.core.thread.DtpExecutor;
import org.springframework.stereotype.Component;

/**
 * @Author 17131
 * @Date 2024/2/21
 * @Description:线程池工具类
 */
@Component
public class ThreadPoolUtil {

    private static final String SOURCE_NAME = "austin";

    /**
     * 1. 将当前线程池 加入到 动态线程池内
     * 2. 注册 线程池 被Spring管理，优雅关闭
     */
    public void register(DtpExecutor executor) {
        DtpRegistry.register(executor, SOURCE_NAME);

    }
}
