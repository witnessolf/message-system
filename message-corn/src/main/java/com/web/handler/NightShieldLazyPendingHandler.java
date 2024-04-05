package com.web.handler;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.google.common.base.Throwables;
import com.web.config.SupportThreadPoolConfig;
import com.web.domain.TaskInfo;
import com.web.utils.RedisUtils;
import com.xxl.job.core.handler.annotation.XxlJob;
import io.netty.util.internal.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Arrays;

/**
 * @Author 17131
 * @Date 2024/4/5
 * @Description:夜间屏蔽的延迟处理类
 * example:当消息下发至austin平台时，已经是凌晨1点，业务希望此类消息在次日的早上9点推送
 */
@Service
@Slf4j
public class NightShieldLazyPendingHandler {
    private static final String NIGHT_SHIELD_BUT_NEXT_DAY_SEND_KEY = "night_shield_send";

    @Autowired
    private KafkaTemplate kafkaTemplate;
    @Value("${austin.business.topic.name}")
    private String topicName;
    @Autowired
    private RedisUtils redisUtils;

    @XxlJob("nightShieldLazyJob")
    public void execute() {
        log.info("NightShieldLazyPendingHandler#execute!");
        SupportThreadPoolConfig.getPendingSingleThreadPool().execute(() -> {
            while (redisUtils.lLen(NIGHT_SHIELD_BUT_NEXT_DAY_SEND_KEY) > 0) {
                String taskInfo = redisUtils.lPop(NIGHT_SHIELD_BUT_NEXT_DAY_SEND_KEY);
                if (StrUtil.isNotBlank(taskInfo)) {
                    try {
                        kafkaTemplate.send(topicName, JSON.toJSONString(Arrays.asList(JSON.parseObject(taskInfo, TaskInfo.class))
                                , new SerializerFeature[]{SerializerFeature.WriteClassName}));
                    } catch (Exception e) {
                        log.error("nightShieldLazyJob send kafka fail! e:{},params:{}", Throwables.getStackTraceAsString(e), taskInfo);
                    }
                }
            }
        });
    }
}
