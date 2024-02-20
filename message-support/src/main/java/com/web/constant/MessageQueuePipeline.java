package com.web.constant;

/**
 * @Author 17131
 * @Date 2024/1/24
 * @Description:消息队列常量
 */
public interface MessageQueuePipeline {
    String EVENT_BUS = "eventBus";
    String KAFKA = "kafka";
    String ROCKET_MQ = "rocketMq";
    String RABBIT_MQ = "rabbitMq";

}