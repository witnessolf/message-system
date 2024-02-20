package com.web.mq;

/**
 * @Author 17131
 * @Date 2024/1/24
 * @Description:
 */
public interface SendMqService {
    /**
     * 发送消息
     *
     * @param topic
     * @param jsonValue
     * @param tagId
     */
    void send(String topic, String jsonValue, String tagId);


    /**
     * 发送消息
     *
     * @param topic
     * @param jsonValue
     */
    void send(String topic, String jsonValue);

}
