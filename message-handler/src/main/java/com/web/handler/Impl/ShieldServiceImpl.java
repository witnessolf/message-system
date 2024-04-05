package com.web.handler.Impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.web.domain.AnchorInfo;
import com.web.domain.TaskInfo;
import com.web.enums.AnchorState;
import com.web.enums.ShieldType;
import com.web.handler.ShieldService;
import com.web.utils.LogUtil;
import com.web.utils.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * @Author 17131
 * @Date 2024/4/5
 * @Description:
 */
@Service
@Slf4j
public class ShieldServiceImpl implements ShieldService {

    private static final String NIGHT_SHIELD_BUT_NEXT_DAY_SEND_KEY = "night_shield_send";

    private static final long SECONDS_OF_A_DAY = 86400L;

    @Autowired
    private RedisUtils redisUtils;
    @Autowired
    private LogUtil logUtil;

    @Override
    public void shield(TaskInfo taskInfo) {
        if (ShieldType.NIGHT_NO_SHIELD.getCode().equals(taskInfo.getShieldType())) {
            return;
        }

        /**
         * example:当消息下发至austin平台时，已经是凌晨1点，业务希望此类消息在次日的早上9点推送
         * (配合 分布式任务定时任务框架搞掂)
         */
        if (isNight()) {
            if (ShieldType.NIGHT_SHIELD.getCode().equals(taskInfo.getShieldType())) {
                logUtil.print(AnchorInfo.builder().state(AnchorState.NIGHT_SHIELD.getCode())
                        .bizId(taskInfo.getBizId()).messageId(taskInfo.getMessageId())
                        .businessId(taskInfo.getBusinessId()).ids(taskInfo.getReceiver()).build());
            }
            if (ShieldType.NIGHT_SHIELD_BUT_NEXT_DAY_SEND.getCode().equals(taskInfo.getShieldType())) {
                redisUtils.lPush(NIGHT_SHIELD_BUT_NEXT_DAY_SEND_KEY,
                        JSON.toJSONString(taskInfo, SerializerFeature.WriteClassName), SECONDS_OF_A_DAY);
                logUtil.print(AnchorInfo.builder().state(AnchorState.NIGHT_SHIELD_NEXT_SEND.getCode())
                        .bizId(taskInfo.getBizId()).messageId(taskInfo.getMessageId())
                        .businessId(taskInfo.getBusinessId()).ids(taskInfo.getReceiver()).build());

            }
        }

    }

    /**
     * 小时 < 8 默认就认为是凌晨(夜晚)
     * @return
     */
    private boolean isNight() {
        return LocalDateTime.now().getHour() < 8;
    }
}
